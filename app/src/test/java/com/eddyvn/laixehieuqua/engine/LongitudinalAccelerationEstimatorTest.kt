package com.eddyvn.laixehieuqua.engine

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class LongitudinalAccelerationEstimatorTest {
    @Test
    fun accelerationFollowsSpeedTrendWithoutSensorSpike(){
        val estimator=LongitudinalAccelerationEstimator()
        estimator.update(1_000,20.0,0.2)

        val first=estimator.update(1_500,22.0,5.5)
        val second=estimator.update(2_000,24.0,0.4)

        assertTrue(first>0.0)
        assertTrue(second>0.0)
        assertTrue(abs(first)<6.0)
        assertTrue(abs(second)<6.0)
    }

    @Test
    fun brakingProducesNegativeStableValue(){
        val estimator=LongitudinalAccelerationEstimator()
        estimator.update(1_000,50.0,0.0)

        val braking=estimator.update(1_500,45.0,2.0)
        val brakingAgain=estimator.update(2_000,40.0,2.5)

        assertTrue(braking<0.0)
        assertTrue(brakingAgain<0.0)
    }

    @Test
    fun stoppedVehicleSettlesTowardZero(){
        val estimator=LongitudinalAccelerationEstimator()
        estimator.update(1_000,10.0,0.0)
        estimator.update(1_500,5.0,2.0)
        estimator.update(2_000,0.5,1.5)

        var value=1.0
        var time=2_000L
        repeat(8){
            time+=500
            value=estimator.update(time,0.3,1.8)
        }

        assertTrue(abs(value)<0.20)
    }
}
