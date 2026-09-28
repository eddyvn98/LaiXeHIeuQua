package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.*
import org.junit.Assert.*
import org.junit.Test

class EconomyProjectionEngineTest{
    private val best=FuelCycle(0,1,300.0,7.0,42.85,FuelConfidence.HIGH)

    @Test fun stableEcoTargetCanProjectAboveBest(){
        val snapshot=DriveSnapshot(
            trueSpeedKmh=45.0,
            ecoTargetKmh=45.0,
            speedHistoryKmh=List(10){45.0},
        )
        val result=EconomyProjectionEngine().project(snapshot,FuelSummary(120.0,best,0))!!
        assertTrue(result.projectedCycleKm>300.0)
    }

    @Test fun currentDistanceIsNeverProjectedBackwards(){
        val snapshot=DriveSnapshot(trueSpeedKmh=80.0,ecoTargetKmh=40.0,speedHistoryKmh=List(10){80.0})
        val result=EconomyProjectionEngine().project(snapshot,FuelSummary(305.0,best,0))!!
        assertTrue(result.projectedCycleKm>=305.0)
    }
}
