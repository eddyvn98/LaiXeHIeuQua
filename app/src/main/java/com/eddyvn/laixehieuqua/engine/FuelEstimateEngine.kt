package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.FuelConfidence
import com.eddyvn.laixehieuqua.domain.FuelCycle

data class FuelEstimate(
    val learnedCycleCount:Int,
    val averageKmPerLiter:Double?,
    val averageLitersPer100Km:Double?,
    val remainingLiters:Double?,
    val rangeKm:Double?,
)

class FuelEstimateEngine {
    fun estimate(
        cycles:List<FuelCycle>,
        currentCycleKm:Double,
        tankCapacityLiters:Double?,
        partialLitersSinceFull:Double,
        hasFullReference:Boolean,
    ):FuelEstimate{
        val valid=cycles.filter{it.confidence!=FuelConfidence.LOW}
        val average=valid.takeIf{it.isNotEmpty()}?.let{items->
            val liters=items.sumOf{it.liters}
            if(liters>0.0)items.sumOf{it.distanceKm}/liters else null
        }

        val remaining=if(
            hasFullReference&&
            tankCapacityLiters!=null&&
            average!=null&&
            average>0.0
        ){
            (
                tankCapacityLiters+
                    partialLitersSinceFull.coerceAtLeast(0.0)-
                    currentCycleKm.coerceAtLeast(0.0)/average
            ).coerceIn(0.0,tankCapacityLiters)
        }else null

        return FuelEstimate(
            learnedCycleCount=valid.size,
            averageKmPerLiter=average,
            averageLitersPer100Km=average?.takeIf{it>0.0}?.let{100.0/it},
            remainingLiters=remaining,
            rangeKm=if(remaining!=null&&average!=null)remaining*average else null,
        )
    }
}
