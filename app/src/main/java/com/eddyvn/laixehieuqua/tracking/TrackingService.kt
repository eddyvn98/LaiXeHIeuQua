package com.eddyvn.laixehieuqua.tracking

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import androidx.core.app.*
import androidx.core.content.ContextCompat
import com.eddyvn.laixehieuqua.*
import com.eddyvn.laixehieuqua.data.*
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.engine.*
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlin.math.max

class TrackingService:Service(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private lateinit var fused:FusedLocationProviderClient
    private lateinit var sensors:SensorCollector
    private lateinit var app:LaiXeApp
    private val fusion=SpeedFusionEngine()
    private val traffic=TrafficDetector()
    private val eco=EcoTargetEngine()
    private val calibration=SpeedCalibrationEngine()
    private var sessionId:Long?=null
    private var previous:Location?=null
    private var distanceM=0.0
    private var speedSum=0.0
    private var speedCount=0
    private var maxSpeed=0.0
    private val history=ArrayDeque<Double>()
    private val targets=ArrayDeque<Double>()

    override fun onCreate(){
        super.onCreate();app=application as LaiXeApp
        fused=LocationServices.getFusedLocationProviderClient(this)
        sensors=SensorCollector(getSystemService(SENSOR_SERVICE) as SensorManager)
        val channel=NotificationChannel(CHANNEL,getString(R.string.tracking_channel_name),NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
        if(intent?.action==ACTION_STOP)stopTracking() else startTracking()
        return START_STICKY
    }
    private fun startTracking(){
        startForeground(NOTIFICATION_ID,NotificationCompat.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_compass).setContentTitle(getString(R.string.tracking_notification_title)).setOngoing(true).build())
        sensors.start()
        scope.launch{sessionId=app.graph.database.dao().activeSession()?.id?:app.graph.database.dao().insertSession(SessionEntity(startMs=System.currentTimeMillis()))}
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return
        val request=LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).setMinUpdateIntervalMillis(500).build()
        fused.requestLocationUpdates(request,callback,mainLooper)
    }
    private val callback=object:LocationCallback(){override fun onLocationResult(r:LocationResult){r.locations.forEach(::consume)}}
    private fun consume(location:Location){
        val time=location.time.takeIf{it>0}?:System.currentTimeMillis()
        val raw=(location.speed*3.6).coerceAtLeast(0.0)
        val trueSpeed=fusion.update(time,raw,sensors.accelerationMs2,location.accuracy)
        val isTraffic=traffic.update(time,trueSpeed)
        val target=eco.update(trueSpeed,sensors.accelerationMs2,isTraffic,app.graph.economyReferenceStore.state.value?.ecoSpeedKmh)
        val display=calibration.map(trueSpeed,app.graph.calibrationStore.state.value?.points.orEmpty())
        val delta=previous?.distanceTo(location)?.toDouble()?.takeIf{it in 0.0..250.0}?:0.0
        distanceM+=delta;previous=location;speedSum+=trueSpeed;speedCount++;maxSpeed=max(maxSpeed,trueSpeed)
        history.addLast(trueSpeed);while(history.size>40)history.removeFirst()
        target.targetKmh?.let{targets.addLast(it)};while(targets.size>40)targets.removeFirst()
        app.graph.driveStateStore.update(DriveSnapshot(true,trueSpeed,display,raw,sensors.accelerationMs2,sensors.leanDeg,distanceM/1000.0,isTraffic,target.targetKmh,target.confidence,history.toList(),targets.toList()))
        val id=sessionId?:return
        scope.launch{app.graph.database.dao().insertTrackPoint(TrackPointEntity(sessionId=id,timestampMs=time,latitude=location.latitude,longitude=location.longitude,accuracyM=location.accuracy,rawGpsSpeedKmh=raw,trueSpeedKmh=trueSpeed,accelerationMs2=sensors.accelerationMs2,leanDeg=sensors.leanDeg,deltaDistanceM=delta))}
    }
    private fun stopTracking(){
        fused.removeLocationUpdates(callback);sensors.stop()
        sessionId?.let{id->scope.launch{app.graph.database.dao().closeSession(id,System.currentTimeMillis(),distanceM,if(speedCount==0)0.0 else speedSum/speedCount,maxSpeed,true);app.graph.fuelRepository.refreshBestReference()}}
        app.graph.driveStateStore.markStopped();stopForeground(STOP_FOREGROUND_REMOVE);stopSelf()
    }
    override fun onDestroy(){fused.removeLocationUpdates(callback);sensors.stop();scope.cancel();super.onDestroy()}
    override fun onBind(intent:Intent?):IBinder?=null
    companion object{
        private const val CHANNEL="drive_tracking";private const val NOTIFICATION_ID=1001
        private const val ACTION_STOP="com.eddyvn.laixehieuqua.STOP"
        fun start(context:Context)=ContextCompat.startForegroundService(context,Intent(context,TrackingService::class.java))
        fun stop(context:Context){context.startService(Intent(context,TrackingService::class.java).setAction(ACTION_STOP))}
    }
}
