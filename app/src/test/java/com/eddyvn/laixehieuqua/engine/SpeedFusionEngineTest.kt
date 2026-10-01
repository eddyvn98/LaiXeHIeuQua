package com.eddyvn.laixehieuqua.engine

import org.junit.Assert.*
import org.junit.Test

class SpeedFusionEngineTest{
    @Test fun decelerationDoesNotPredictAcceleration(){
        val e=SpeedFusionEngine()
        e.update(1_000,50.0,0.0,5f)
        val result=e.update(2_000,40.0,2.0,5f)
        assertTrue(result<50.0)
    }

    @Test fun repeatedStoppedGpsClampsPhantomSpeedToZero(){
        val e=SpeedFusionEngine()
        e.update(1_000,8.0,0.0,5f)
        e.update(1_500,0.7,2.5,5f)
        val stopped=e.update(2_000,0.4,3.0,5f)
        assertEquals(0.0,stopped,0.001)
    }
}
