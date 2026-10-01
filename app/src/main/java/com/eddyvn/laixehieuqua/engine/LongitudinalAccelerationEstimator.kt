package com.eddyvn.laixehieuqua.engine

import kotlin.math.abs
import kotlin.math.sign

/**
 * Converts noisy device linear-acceleration magnitude into a stable signed
 * longitudinal value for the dashboard. Sign follows the fused speed trend,
 * while sensor magnitude contributes only a small amount to responsiveness.
 */
class LongitudinalAccelerationEstimator(
    private val timeConstantSeconds:Double=.75,
    private val deadZoneMs2:Double=.08,
    private val maxAbsMs2:Double=6.0,
){
    private var lastSpeedKmh:Double?=null
    private var lastTimeMs:Long?=null
    private var smoothedMs2=0.0

    fun update(
        timeMs:Long,
        speedKmh:Double,
        sensorAccelerationMs2:Double,
    ):Double{
        val previousSpeed=lastSpeedKmh
        val previousTime=lastTimeMs
        lastSpeedKmh=speedKmh
        lastTimeMs=timeMs

        if(previousSpeed==null||previousTime==null){
            smoothedMs2=0.0
            return 0.0
        }

        val dt=((timeMs-previousTime).coerceIn(100L,2_000L))/1000.0
        val speedAcceleration=(
            ((speedKmh-previousSpeed)/3.6)/dt
        ).coerceIn(-maxAbsMs2,maxAbsMs2)

        val trendSign=when{
            speedAcceleration>.10 -> 1.0
            speedAcceleration<-.10 -> -1.0
            else -> 0.0
        }

        val sensorMagnitude=abs(sensorAccelerationMs2).coerceAtMost(maxAbsMs2)
        val signedSensor=if(trendSign==0.0)0.0 else sensorMagnitude*trendSign

        // Fused speed derivative is the stable backbone. The physical sensor
        // only nudges the target so throttle/brake changes still feel responsive.
        val target=(
            speedAcceleration*.78+
                signedSensor*.22
        ).coerceIn(-maxAbsMs2,maxAbsMs2)

        val alpha=(dt/(timeConstantSeconds+dt)).coerceIn(.08,.65)
        smoothedMs2+=alpha*(target-smoothedMs2)

        if(speedKmh<1.5&&abs(speedAcceleration)<.20){
            smoothedMs2*=.45
        }
        if(abs(smoothedMs2)<deadZoneMs2){
            smoothedMs2=0.0
        }

        return smoothedMs2.coerceIn(-maxAbsMs2,maxAbsMs2)
    }

    fun reset(){
        lastSpeedKmh=null
        lastTimeMs=null
        smoothedMs2=0.0
    }
}
