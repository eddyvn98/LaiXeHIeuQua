package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.FuelConfidence
import com.eddyvn.laixehieuqua.domain.FuelCycle
import org.junit.Assert.*
import org.junit.Test

class FuelEstimateEngineTest {
    @Test
    fun weightedAverageDrivesRemainingFuelAndRange(){
        val cycles=listOf(
            FuelCycle(1,2,200.0,4.0,50.0,FuelConfidence.HIGH),
            FuelCycle(2,3,150.0,3.0,50.0,FuelConfidence.MEDIUM),
        )

        val result=FuelEstimateEngine().estimate(
            cycles=cycles,
            currentCycleKm=100.0,
            tankCapacityLiters=8.0,
            partialLitersSinceFull=1.0,
            hasFullReference=true,
        )

        assertEquals(2,result.learnedCycleCount)
        assertEquals(50.0,result.averageKmPerLiter!!,0.001)
        assertEquals(2.0,result.averageLitersPer100Km!!,0.001)
        assertEquals(7.0,result.remainingLiters!!,0.001)
        assertEquals(350.0,result.rangeKm!!,0.001)
    }

    @Test
    fun noCompletedCycleMeansNoRemainingEstimate(){
        val result=FuelEstimateEngine().estimate(
            cycles=emptyList(),
            currentCycleKm=20.0,
            tankCapacityLiters=8.0,
            partialLitersSinceFull=0.0,
            hasFullReference=true,
        )

        assertNull(result.averageKmPerLiter)
        assertNull(result.remainingLiters)
        assertNull(result.rangeKm)
    }
}
