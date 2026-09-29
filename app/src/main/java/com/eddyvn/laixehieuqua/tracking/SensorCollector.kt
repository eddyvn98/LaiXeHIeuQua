package com.eddyvn.laixehieuqua.tracking

import android.hardware.*
import kotlin.math.*

class SensorCollector(private val manager:SensorManager):SensorEventListener{
    @Volatile var accelerationMs2=0.0
        private set
    @Volatile var leanDeg=0.0
        private set

    private val baselineRolls=ArrayDeque<Double>()
    private var rollBaselineDeg:Double?=null

    fun start(){
        baselineRolls.clear()
        rollBaselineDeg=null
        leanDeg=0.0
        manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let{
            manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)
        }
        manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let{
            manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop()=manager.unregisterListener(this)

    override fun onSensorChanged(e:SensorEvent){
        when(e.sensor.type){
            Sensor.TYPE_LINEAR_ACCELERATION->{
                val x=e.values[0].toDouble()
                val y=e.values[1].toDouble()
                val z=e.values[2].toDouble()
                accelerationMs2=sqrt(x*x+y*y+z*z)
            }
            Sensor.TYPE_ROTATION_VECTOR->{
                val matrix=FloatArray(9)
                val orientation=FloatArray(3)
                SensorManager.getRotationMatrixFromVector(matrix,e.values)
                SensorManager.getOrientation(matrix,orientation)
                val roll=Math.toDegrees(orientation[2].toDouble())

                if(rollBaselineDeg==null){
                    baselineRolls.addLast(roll)
                    if(baselineRolls.size>=20){
                        rollBaselineDeg=baselineRolls.average()
                        baselineRolls.clear()
                    }
                    leanDeg=0.0
                }else{
                    leanDeg=normalizeAngle(roll-rollBaselineDeg!!)
                }
            }
        }
    }

    private fun normalizeAngle(value:Double):Double{
        var angle=value
        while(angle>180)angle-=360
        while(angle<-180)angle+=360
        return angle
    }

    override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int)=Unit
}
