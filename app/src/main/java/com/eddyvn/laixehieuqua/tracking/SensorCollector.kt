package com.eddyvn.laixehieuqua.tracking

import android.hardware.*
import kotlin.math.sqrt

class SensorCollector(private val manager:SensorManager):SensorEventListener{
    @Volatile var accelerationMs2=0.0;private set
    @Volatile var leanDeg=0.0;private set
    fun start(){
        manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let{manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)}
        manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let{manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)}
    }
    fun stop()=manager.unregisterListener(this)
    override fun onSensorChanged(e:SensorEvent){
        when(e.sensor.type){
            Sensor.TYPE_LINEAR_ACCELERATION->{val x=e.values[0].toDouble();val y=e.values[1].toDouble();val z=e.values[2].toDouble();accelerationMs2=sqrt(x*x+y*y+z*z)}
            Sensor.TYPE_ROTATION_VECTOR->{val m=FloatArray(9);val o=FloatArray(3);SensorManager.getRotationMatrixFromVector(m,e.values);SensorManager.getOrientation(m,o);leanDeg=Math.toDegrees(o[2].toDouble())}
        }
    }
    override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int)=Unit
}
