package com.eddyvn.laixehieuqua.tracking

import android.hardware.*
import kotlin.math.*

data class VehicleMotionSample(
    val longitudinalMs2:Double,
    val lateralMs2:Double,
    val verticalMs2:Double,
    val vibrationScore:Double,
)

class SensorCollector(private val manager:SensorManager):SensorEventListener{
    @Volatile var accelerationMs2=0.0
        private set
    @Volatile var leanDeg=0.0
        private set

    @Volatile private var worldEastAccelerationMs2=0.0
    @Volatile private var worldNorthAccelerationMs2=0.0
    @Volatile private var worldUpAccelerationMs2=0.0
    @Volatile private var gyroMagnitudeRadS=0.0

    private val baselinePitches=ArrayDeque<Double>()
    private var pitchBaselineDeg:Double?=null
    private var accelerationInitialized=false

    private val rotationMatrix=FloatArray(9)
    private var rotationReady=false
    private var deviceAccelerationX=0.0
    private var deviceAccelerationY=0.0
    private var deviceAccelerationZ=0.0

    fun start(){
        baselinePitches.clear()
        pitchBaselineDeg=null
        leanDeg=0.0
        accelerationMs2=0.0
        worldEastAccelerationMs2=0.0
        worldNorthAccelerationMs2=0.0
        worldUpAccelerationMs2=0.0
        gyroMagnitudeRadS=0.0
        accelerationInitialized=false
        rotationReady=false
        deviceAccelerationX=0.0
        deviceAccelerationY=0.0
        deviceAccelerationZ=0.0

        manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let{
            manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)
        }
        manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let{
            manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)
        }
        manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let{
            manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop()=manager.unregisterListener(this)

    /**
     * Projects gravity-free acceleration into vehicle coordinates using the
     * current GPS bearing. Android world axes are east/north/up.
     *
     * longitudinal: positive forward, negative braking.
     * lateral: positive approximately to the vehicle's right.
     */
    fun vehicleMotionSample(bearingDeg:Float?):VehicleMotionSample{
        if(bearingDeg==null||!rotationReady){
            return VehicleMotionSample(
                longitudinalMs2=0.0,
                lateralMs2=0.0,
                verticalMs2=worldUpAccelerationMs2,
                vibrationScore=(
                    abs(worldUpAccelerationMs2)*.30+
                        gyroMagnitudeRadS*.85
                ).coerceIn(0.0,6.0),
            )
        }

        val radians=Math.toRadians(bearingDeg.toDouble())
        val longitudinal=
            worldEastAccelerationMs2*sin(radians)+
                worldNorthAccelerationMs2*cos(radians)
        val lateral=
            worldEastAccelerationMs2*cos(radians)-
                worldNorthAccelerationMs2*sin(radians)

        // Mount/engine vibration usually appears strongly outside the vehicle
        // longitudinal axis. Gyro magnitude catches rapid phone rotation that
        // an accelerometer-only filter cannot distinguish from real motion.
        val vibrationScore=(
            abs(lateral)*.55+
                abs(worldUpAccelerationMs2)*.25+
                gyroMagnitudeRadS*.85
        ).coerceIn(0.0,6.0)

        return VehicleMotionSample(
            longitudinalMs2=if(abs(longitudinal)<.04)0.0 else longitudinal.coerceIn(-8.0,8.0),
            lateralMs2=lateral.coerceIn(-8.0,8.0),
            verticalMs2=worldUpAccelerationMs2.coerceIn(-8.0,8.0),
            vibrationScore=vibrationScore,
        )
    }

    fun longitudinalAccelerationMs2(bearingDeg:Float?):Double=
        vehicleMotionSample(bearingDeg).longitudinalMs2

    override fun onSensorChanged(e:SensorEvent){
        when(e.sensor.type){
            Sensor.TYPE_LINEAR_ACCELERATION->{
                deviceAccelerationX=e.values[0].toDouble()
                deviceAccelerationY=e.values[1].toDouble()
                deviceAccelerationZ=e.values[2].toDouble()
                updateWorldAcceleration()

                val raw=sqrt(
                    deviceAccelerationX*deviceAccelerationX+
                        deviceAccelerationY*deviceAccelerationY+
                        deviceAccelerationZ*deviceAccelerationZ
                )
                val filtered=if(accelerationInitialized){
                    accelerationMs2*.82+raw*.18
                }else{
                    accelerationInitialized=true
                    raw
                }
                accelerationMs2=if(filtered<.05)0.0 else filtered
            }

            Sensor.TYPE_ROTATION_VECTOR->{
                SensorManager.getRotationMatrixFromVector(rotationMatrix,e.values)
                rotationReady=true
                updateWorldAcceleration()

                val orientation=FloatArray(3)
                SensorManager.getOrientation(rotationMatrix,orientation)
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

            Sensor.TYPE_GYROSCOPE->{
                val gx=e.values[0].toDouble()
                val gy=e.values[1].toDouble()
                val gz=e.values[2].toDouble()
                val raw=sqrt(gx*gx+gy*gy+gz*gz)
                // Fast enough to react to mount shake, but suppress single-sample spikes.
                gyroMagnitudeRadS=gyroMagnitudeRadS*.72+raw*.28
            }
        }
    }

    private fun updateWorldAcceleration(){
        if(!rotationReady)return
        val x=deviceAccelerationX
        val y=deviceAccelerationY
        val z=deviceAccelerationZ

        worldEastAccelerationMs2=
            rotationMatrix[0]*x+
                rotationMatrix[1]*y+
                rotationMatrix[2]*z
        worldNorthAccelerationMs2=
            rotationMatrix[3]*x+
                rotationMatrix[4]*y+
                rotationMatrix[5]*z
        worldUpAccelerationMs2=
            rotationMatrix[6]*x+
                rotationMatrix[7]*y+
                rotationMatrix[8]*z
    }

    private fun normalizeAngle(value:Double):Double{
        var angle=value
        while(angle>180)angle-=360
        while(angle<-180)angle+=360
        return angle
    }

    override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int)=Unit
}
