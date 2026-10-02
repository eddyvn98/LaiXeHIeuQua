package com.eddyvn.laixehieuqua.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RealtimeMotionPredictorTest {
    @Test
    fun predictsSpeedBetweenGpsFixes(){
        val predictor=RealtimeMotionPredictor()
        predictor.onGpsFix(nowMs=1_000L,gpsSpeedKmh=20.0)

        var result:MotionPrediction?=null
        var now=1_000L
        repeat(20){
            now+=50L
            result=predictor.predict(
                nowMs=now,
                signedLongitudinalAccelerationMs2=2.0,
                sensorMagnitudeMs2=2.0,
            )
        }

        assertNotNull(result)
        assertTrue(result!!.speedKmh>23.0)
        assertTrue(result!!.accelerationMs2>1.0)
    }

    @Test
    fun gpsFixReanchorsPrediction(){
        val predictor=RealtimeMotionPredictor()
        predictor.onGpsFix(nowMs=1_000L,gpsSpeedKmh=30.0)
        predictor.predict(
            nowMs=1_500L,
            signedLongitudinalAccelerationMs2=3.0,
            sensorMagnitudeMs2=3.0,
        )

        val corrected=predictor.onGpsFix(
            nowMs=2_000L,
            gpsSpeedKmh=31.0,
        )

        assertTrue(corrected.speedKmh in 30.0..34.0)
    }

    @Test
    fun predictionStopsDriftingWhenGpsGetsStale(){
        val predictor=RealtimeMotionPredictor()
        predictor.onGpsFix(nowMs=1_000L,gpsSpeedKmh=40.0)

        val fresh=predictor.predict(
            nowMs=1_500L,
            signedLongitudinalAccelerationMs2=2.0,
            sensorMagnitudeMs2=2.0,
        )!!
        val stale=predictor.predict(
            nowMs=3_000L,
            signedLongitudinalAccelerationMs2=2.0,
            sensorMagnitudeMs2=2.0,
        )!!

        assertTrue(stale.accelerationMs2<fresh.accelerationMs2)
        assertEquals(true,stale.speedKmh>=0.0)
    }

    @Test
    fun strongMountVibrationAttenuatesLongitudinalImpulse(){
        val clean=RealtimeMotionPredictor()
        val noisy=RealtimeMotionPredictor()
        clean.onGpsFix(1_000L,35.0)
        noisy.onGpsFix(1_000L,35.0)

        var cleanResult:MotionPrediction?=null
        var noisyResult:MotionPrediction?=null
        var now=1_000L
        repeat(10){
            now+=50L
            cleanResult=clean.predict(now,1.8,1.8,vibrationScore=.15)
            noisyResult=noisy.predict(now,1.8,3.5,vibrationScore=4.0)
        }

        assertNotNull(cleanResult)
        assertNotNull(noisyResult)
        assertTrue(
            abs(noisyResult!!.accelerationMs2)<
                abs(cleanResult!!.accelerationMs2)
        )
    }

    @Test
    fun vibrationAloneDoesNotCreateSpeed(){
        val predictor=RealtimeMotionPredictor()
        predictor.onGpsFix(1_000L,0.0)

        var result:MotionPrediction?=null
        var now=1_000L
        repeat(20){
            now+=50L
            result=predictor.predict(
                nowMs=now,
                signedLongitudinalAccelerationMs2=.06,
                sensorMagnitudeMs2=3.0,
                vibrationScore=4.5,
            )
        }

        assertNotNull(result)
        assertEquals(0.0,result!!.speedKmh,.15)
        assertEquals(0.0,result!!.accelerationMs2,.15)
    }
}
