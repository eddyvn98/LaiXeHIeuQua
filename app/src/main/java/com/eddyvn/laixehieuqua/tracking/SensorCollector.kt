package com.eddyvn.laixehieuqua.tracking

import android.hardware.*
import kotlin.math.*

class SensorCollector(private val manager:SensorManager):SensorEventListener{
    @Volatile var accelerationMs2=0.0
        private set
    @Volatile var longitudinalAccelerationMs2=0.0
        private set
    @Volatile var leanDeg=0.0
        private set

    private val baselinePitches=ArrayDeque<Double>()
    private var pitchBaselineDeg:Double?=null
    private var accelerationInitialized=false
    private var longitudinalAccelerationInitialized=false

    fun start(){
        baselinePitches.clear()
        pitchBaselineDeg=null
        leanDeg=0.0
        accelerationMs2=0.0
        longitudinalAccelerationMs2=0.0
        accelerationInitialized=false
        longitudinalAccelerationInitialized=false
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
                val raw=sqrt(x*x+y*y+z*z)
                val filtered=if(accelerationInitialized){
                    accelerationMs2*.82+raw*.18
                }else{
                    accelerationInitialized=true
                    raw
                }
                accelerationMs2=if(filtered<.05)0.0 else filtered

                // With the phone mounted facing the rider, vehicle forward/back
                // acceleration is primarily along the screen-normal Z axis.
                // Keep this signed value separate from the magnitude used by
                // the existing fusion pipeline so the live gauge can update
                // directly from the sensor without waiting for the next GPS fix.
                val longitudinalRaw=-z
                val longitudinalFiltered=if(longitudinalAccelerationInitialized){
                    longitudinalAccelerationMs2*.72+longitudinalRaw*.28
                }else{
                    longitudinalAccelerationInitialized=true
                    longitudinalRaw
                }
                longitudinalAccelerationMs2=if(abs(longitudinalFiltered)<.04){
                    0.0
                }else{
                    longitudinalFiltered.coerceIn(-8.0,8.0)
                }
            }
            Sensor.TYPE_ROTATION_VECTOR->{
                val matrix=FloatArray(9)
                val orientation=FloatArray(3)
                SensorManager.getRotationMatrixFromVector(matrix,e.values)
                SensorManager.getOrientation(matrix,orientation)
                val pitch=Math.toDegrees(orientation[1].toDouble())

                if(pitchBaselineDeg==null){
                    baselinePitches.addLast(pitch)
                    if(baselinePitches.size>=20){
                        pitchBaselineDeg=baselinePitches.average()
                        baselinePitches.clear()
                    }
                    leanDeg=0.0
                }else{
                    // Keep the persisted field name for DB compatibility.
                    // Positive means the vehicle nose is uphill, negative downhill.
                    leanDeg=-normalizeAngle(pitch-pitchBaselineDeg!!)
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
