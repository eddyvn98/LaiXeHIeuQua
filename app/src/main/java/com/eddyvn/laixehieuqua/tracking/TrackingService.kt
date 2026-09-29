package com.eddyvn.laixehieuqua.tracking

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eddyvn.laixehieuqua.LaiXeApp
import com.eddyvn.laixehieuqua.R
import com.eddyvn.laixehieuqua.data.*
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.engine.DrivePipeline
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class TrackingService:Service(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO.limitedParallelism(1))
    private lateinit var fused:FusedLocationProviderClient
    private lateinit var sensors:SensorCollector
    private lateinit var app:LaiXeApp

    private val pipeline=DrivePipeline()

    private var sessionId:Long?=null
    private var previous:Location?=null
    private var stopping=false

    override fun onCreate(){
        super.onCreate()
        app=application as LaiXeApp
        fused=LocationServices.getFusedLocationProviderClient(this)
        sensors=SensorCollector(getSystemService(SENSOR_SERVICE) as SensorManager)
        val channel=NotificationChannel(
            CHANNEL,
            getString(R.string.tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
        if(intent?.action==ACTION_STOP)stopTracking() else startTracking()
        return START_STICKY
    }

    private fun startTracking(){
        if(app.graph.driveStateStore.state.value.tracking)return
        stopping=false
        previous=null

        startForeground(
            NOTIFICATION_ID,
            NotificationCompat.Builder(this,CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle(getString(R.string.tracking_notification_title))
                .setOngoing(true)
                .build()
        )
        sensors.start()

        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )!=PackageManager.PERMISSION_GRANTED
        ){
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        scope.launch{
            val dao=app.graph.database.dao()
            val active=dao.activeSession()
            val id=active?.id?:dao.insertSession(SessionEntity(startMs=System.currentTimeMillis()))
            sessionId=id

            val baseTotalDistanceM=dao.totalTrackedDistanceM()
            val sessionBaseDistanceM=dao.sessionDistanceM(id)
            pipeline.reset(
                sessionDistanceM=sessionBaseDistanceM,
                totalDistanceM=baseTotalDistanceM,
            )

            val request=LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1_000L,
            )
                .setMinUpdateIntervalMillis(500L)
                .build()

            withContext(Dispatchers.Main.immediate){
                fused.requestLocationUpdates(request,callback,mainLooper)
            }
        }
    }

    private val callback=object:LocationCallback(){
        override fun onLocationResult(result:LocationResult){
            result.locations.forEach(::consume)
        }
    }

    private fun consume(location:Location){
        val id=sessionId?:return
        val time=location.time.takeIf{it>0}?:System.currentTimeMillis()
        val raw=(location.speed*3.6).coerceAtLeast(0.0)

        val delta=previous?.distanceTo(location)?.toDouble()
            ?.takeIf{location.accuracy<=50f&&it in 0.0..250.0}?:0.0
        previous=location

        val sample=DriveInputSample(
            timestampMs=time,
            rawGpsSpeedKmh=raw,
            gpsAccuracyM=location.accuracy,
            accelerationMs2=sensors.accelerationMs2,
            leanDeg=sensors.leanDeg,
            deltaDistanceM=delta,
        )
        val snapshot=pipeline.consume(
            sample=sample,
            reference=app.graph.economyReferenceStore.state.value,
            activeCalibration=app.graph.calibrationStore.state.value,
        )
        app.graph.driveStateStore.update(snapshot)

        scope.launch{
            app.graph.database.dao().insertTrackPoint(
                TrackPointEntity(
                    sessionId=id,
                    timestampMs=time,
                    latitude=location.latitude,
                    longitude=location.longitude,
                    accuracyM=location.accuracy,
                    rawGpsSpeedKmh=raw,
                    trueSpeedKmh=snapshot.trueSpeedKmh,
                    accelerationMs2=sensors.accelerationMs2,
                    leanDeg=sensors.leanDeg,
                    deltaDistanceM=delta,
                )
            )
        }
    }

    private fun stopTracking(){
        if(stopping)return
        stopping=true
        fused.removeLocationUpdates(callback)
        sensors.stop()
        val id=sessionId

        scope.launch{
            if(id!=null){
                val dao=app.graph.database.dao()
                dao.closeSession(
                    id=id,
                    endMs=System.currentTimeMillis(),
                    distanceM=dao.sessionDistanceM(id),
                    avgSpeed=dao.sessionAvgSpeed(id),
                    maxSpeed=dao.sessionMaxSpeed(id),
                    complete=true,
                )
                app.graph.fuelRepository.refreshBestReference()
            }
            app.graph.driveStateStore.markStopped()
            withContext(Dispatchers.Main.immediate){
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onDestroy(){
        fused.removeLocationUpdates(callback)
        sensors.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent:Intent?):IBinder?=null

    companion object{
        private const val CHANNEL="drive_tracking"
        private const val NOTIFICATION_ID=1001
        private const val ACTION_STOP="com.eddyvn.laixehieuqua.STOP"

        fun start(context:Context)=ContextCompat.startForegroundService(
            context,
            Intent(context,TrackingService::class.java),
        )

        fun stop(context:Context){
            context.startService(
                Intent(context,TrackingService::class.java).setAction(ACTION_STOP)
            )
        }
    }
}
