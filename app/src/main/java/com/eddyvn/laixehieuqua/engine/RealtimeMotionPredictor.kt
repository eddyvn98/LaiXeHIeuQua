package com.eddyvn.laixehieuqua.engine

import kotlin.math.abs
import kotlin.math.sign

data class MotionPrediction(
    val speedKmh:Double,
    val accelerationMs2:Double,
)

/**
 * High-rate dashboard predictor used between slower GNSS fixes.
 *
 * GPS remains the authoritative speed source. Between fixes we integrate
 * signed longitudinal linear acceleration at 20 Hz, then reconcile back
 * toward the next fused GPS speed instead of letting prediction drift.
 */
class RealtimeMotionPredictor(
    private val accelerationTimeConstantSeconds:Double=.12,
    private val deadZoneMs2:Double=.10,
    private val maxAbsAccelerationMs2:Double=5.5,
    private val fullTrustAgeMs:Long=1_000L,
    private val maxPredictionAgeMs:Long=1_800L,
){
    private var predictedSpeedKmh=0.0
    private var filteredAccelerationMs2=0.0
    private var lastTickMs:Long?=null
    private var lastGpsFixMs:Long?=null
    private var lastGpsSpeedKmh:Double?=null
    private var gpsTrendSign=0.0

    fun reset(){
        predictedSpeedKmh=0.0
        filteredAccelerationMs2=0.0
        lastTickMs=null
        lastGpsFixMs=null
        lastGpsSpeedKmh=null
        gpsTrendSign=0.0
    }

    fun onGpsFix(nowMs:Long,gpsSpeedKmh:Double):MotionPrediction{
        val gps=gpsSpeedKmh.coerceIn(0.0,180.0)
        val previousGps=lastGpsSpeedKmh
        val previousGpsTime=lastGpsFixMs

        if(previousGps!=null&&previousGpsTime!=null){
            val dt=((nowMs-previousGpsTime).coerceAtLeast(1L))/1000.0
            val delta=gps-previousGps
            gpsTrendSign=when{
                gps<1.2 -> 0.0
                dt<=3.0&&abs(delta)>=.45 -> sign(delta)
                else -> gpsTrendSign*.75
            }
        }

        predictedSpeedKmh=when{
            gps<1.0 -> 0.0
            lastGpsFixMs==null -> gps
            abs(gps-predictedSpeedKmh)>12.0 -> gps
            else -> gps*.75+predictedSpeedKmh*.25
        }

        if(gps<1.0){
            filteredAccelerationMs2=0.0
        }

        lastGpsSpeedKmh=gps
        lastGpsFixMs=nowMs
        lastTickMs=nowMs
        return MotionPrediction(predictedSpeedKmh,filteredAccelerationMs2)
    }

    fun predict(
        nowMs:Long,
        signedLongitudinalAccelerationMs2:Double,
        sensorMagnitudeMs2:Double,
    ):MotionPrediction?{
        val gpsFixTime=lastGpsFixMs?:return null
        val previousTick=lastTickMs?:nowMs.also{lastTickMs=it}
        val dt=((nowMs-previousTick).coerceIn(0L,200L))/1000.0
        lastTickMs=nowMs

        val gpsAge=(nowMs-gpsFixTime).coerceAtLeast(0L)
        val trust=when{
            gpsAge<=fullTrustAgeMs -> 1.0
            gpsAge>=maxPredictionAgeMs -> 0.0
            else -> 1.0-
                (gpsAge-fullTrustAgeMs).toDouble()/
                (maxPredictionAgeMs-fullTrustAgeMs).toDouble()
        }

        val projected=signedLongitudinalAccelerationMs2
            .coerceIn(-maxAbsAccelerationMs2,maxAbsAccelerationMs2)
        val fallbackMagnitude=sensorMagnitudeMs2
            .coerceIn(0.0,maxAbsAccelerationMs2)
        val fallbackSigned=fallbackMagnitude*gpsTrendSign*.65

        var target=if(abs(projected)>=deadZoneMs2){
            projected
        }else{
            fallbackSigned
        }

        if(abs(target)<deadZoneMs2){
            target=0.0
        }
        target*=trust

        if(dt>0.0){
            val alpha=(dt/(accelerationTimeConstantSeconds+dt)).coerceIn(.08,.80)
            filteredAccelerationMs2+=alpha*(target-filteredAccelerationMs2)
            predictedSpeedKmh=(
                predictedSpeedKmh+
                    filteredAccelerationMs2*3.6*dt
            ).coerceIn(0.0,180.0)
        }

        if(trust==0.0){
            filteredAccelerationMs2*=.72
        }
        if(predictedSpeedKmh<1.0&&abs(filteredAccelerationMs2)<.18){
            predictedSpeedKmh=0.0
        }

        return MotionPrediction(
            speedKmh=predictedSpeedKmh,
            accelerationMs2=filteredAccelerationMs2,
        )
    }
}
