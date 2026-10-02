package com.eddyvn.laixehieuqua.tracking

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eddyvn.laixehieuqua.LaiXeApp
import com.eddyvn.laixehieuqua.R
import com.eddyvn.laixehieuqua.data.*
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.engine.DrivePipeline
import com.eddyvn.laixehieuqua.engine.RealtimeMotionPredictor
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlin.math.max

class TrackingService:Service(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO.limitedParallelism(1))
    private val realtimeScope=CoroutineScope(
        SupervisorJob()+Dispatchers.Default.limitedParallelism(1)
    )
    private lateinit var fused:FusedLocationProviderClient
    private lateinit var sensors:SensorCollector
    private lateinit var app:LaiXeApp

    private val pipeline=DrivePipeline()
    private val realtimePredictor=RealtimeMotionPredictor()

    private var sessionId:Long?=null
    private var previous:Location?=null
    @Volatile private var stopping=false
    @Volatile private var latestBearingDeg:Float?=null
    private var startJob:Job?=null
    private var predictionJob:Job?=null

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
        val store=app.graph.driveStateStore
        if(store.trackingStatus.value in setOf(
                TrackingStatus.STARTING,
                TrackingStatus.WAITING_FOR_GPS,
                TrackingStatus.LIVE,
                TrackingStatus.STOPPING,
            ))return
        store.setTrackingStatus(TrackingStatus.STARTING)
        stopping=false
        previous=null
        latestBearingDeg=null
        realtimePredictor.reset()
        predictionJob?.cancel()

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
            store.setTrackingStatus(TrackingStatus.PERMISSION_REQUIRED)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        startJob=scope.launch{
            try{
                val dao=app.graph.database.dao()
                val active=dao.activeSession()
                val sessionStartMs=active?.startMs?:System.currentTimeMillis()
                val id=active?.id?:dao.insertSession(SessionEntity(startMs=sessionStartMs))
                sessionId=id
                store.beginSession(sessionStartMs)

                val baseTotalDistanceM=dao.totalTrackedDistanceM()
                val sessionBaseDistanceM=dao.sessionDistanceM(id)
                pipeline.reset(
                    sessionDistanceM=sessionBaseDistanceM,
                    totalDistanceM=baseTotalDistanceM,
                    sessionStartMs=sessionStartMs,
                )
                startRealtimePrediction()

                // Ask for the freshest GNSS fixes and avoid batching. The previous
                // 1 s max delivery delay could make the dashboard feel several seconds
                // behind once device/GNSS scheduling latency was added on top.
                val request=LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    250L,
                )
                    .setMinUpdateIntervalMillis(100L)
                    .setMinUpdateDistanceMeters(0f)
                    .setWaitForAccurateLocation(false)
                    .build()

                if(stopping)return@launch
                store.setTrackingStatus(TrackingStatus.WAITING_FOR_GPS)
                withContext(Dispatchers.Main.immediate){
                    if(!stopping){
                        fused.requestLocationUpdates(request,callback,mainLooper)
                            .addOnFailureListener{failStart()}
                    }
                }
            }catch(cancelled:CancellationException){
                throw cancelled
            }catch(_:Exception){
                failStart()
            }
        }
    }

    private val callback=object:LocationCallback(){
        override fun onLocationResult(result:LocationResult){
            if(stopping)return
            if(result.locations.isNotEmpty()){
                app.graph.driveStateStore.setTrackingStatus(TrackingStatus.LIVE)
            }
            result.locations.forEach(::consume)
        }
    }

    private fun failStart(){
        if(stopping)return
        app.graph.driveStateStore.setTrackingStatus(TrackingStatus.ERROR)
        fused.removeLocationUpdates(callback)
        predictionJob?.cancel()
        realtimePredictor.reset()
        sensors.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun consume(location:Location){
        val id=sessionId?:return
        val time=location.time.takeIf{it>0}?:System.currentTimeMillis()
        val raw=(location.speed*3.6).coerceAtLeast(0.0)

        val old=previous
        val delta=validatedDeltaMeters(old,location,raw)
        previous=location

        if(location.hasBearing()&&raw>=3.0){
            latestBearingDeg=location.bearing
        }

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
            activeCalibration=null,
        )
        realtimePredictor.onGpsFix(
            nowMs=SystemClock.elapsedRealtime(),
            gpsSpeedKmh=snapshot.displaySpeedKmh,
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
                    accelerationMs2=snapshot.accelerationMs2,
                    leanDeg=sensors.leanDeg,
                    deltaDistanceM=delta,
                )
            )
        }
    }

    private fun startRealtimePrediction(){
        predictionJob?.cancel()
        predictionJob=realtimeScope.launch{
            while(isActive&&!stopping){
                if(app.graph.driveStateStore.trackingStatus.value==TrackingStatus.LIVE){
                    val prediction=realtimePredictor.predict(
                        nowMs=SystemClock.elapsedRealtime(),
                        signedLongitudinalAccelerationMs2=
                            sensors.longitudinalAccelerationMs2(latestBearingDeg),
                        sensorMagnitudeMs2=sensors.accelerationMs2,
                    )
                    if(prediction!=null){
                        app.graph.driveStateStore.updateRealtimeMotion(
                            displaySpeedKmh=prediction.speedKmh,
                            accelerationMs2=prediction.accelerationMs2,
                        )
                    }
                }
                delay(50L)
            }
        }
    }

    private fun validatedDeltaMeters(old:Location?,current:Location,currentSpeedKmh:Double):Double{
        if(old==null)return 0.0
        if(current.accuracy>30f || old.accuracy>30f)return 0.0

        val oldSpeedKmh=(old.speed*3.6).coerceAtLeast(0.0)
        // Both samples say stopped: ignore coordinate wander completely.
        if(currentSpeedKmh<2.0 && oldSpeedKmh<2.0)return 0.0

        val distance=old.distanceTo(current).toDouble()
        if(distance<0.7)return 0.0

        val dtSeconds=((current.time-old.time).coerceAtLeast(250L))/1000.0
        val speedLimitKmh=max(currentSpeedKmh,oldSpeedKmh)+35.0
        val plausibleMaxMeters=(speedLimitKmh/3.6)*dtSeconds+8.0
        if(distance>plausibleMaxMeters || distance>120.0)return 0.0
        return distance
    }

    private fun stopTracking(){
        if(stopping)return
        stopping=true
        predictionJob?.cancel()
        fused.removeLocationUpdates(callback)
        sensors.stop()

        scope.launch{
            startJob?.join()
            val id=sessionId
            val endMs=System.currentTimeMillis()
            if(id!=null){
                val dao=app.graph.database.dao()
                dao.closeSession(
                    id=id,
                    endMs=endMs,
                    distanceM=dao.sessionDistanceM(id),
                    avgSpeed=dao.sessionAvgSpeed(id),
                    maxSpeed=dao.sessionMaxSpeed(id),
                    complete=true,
                )
                app.graph.fuelRepository.refreshBestReference()
            }
            app.graph.driveStateStore.markStopped(endMs)
            withContext(Dispatchers.Main.immediate){
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onDestroy(){
        predictionJob?.cancel()
        fused.removeLocationUpdates(callback)
        sensors.stop()
        scope.cancel()
        realtimeScope.cancel()
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
