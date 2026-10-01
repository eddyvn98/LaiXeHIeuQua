package com.eddyvn.laixehieuqua.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DriveSnapshot(
    val tracking:Boolean=false,
    val trueSpeedKmh:Double=0.0,
    val displaySpeedKmh:Double=0.0,
    val rawGpsSpeedKmh:Double=0.0,
    val accelerationMs2:Double=0.0,
    val leanDeg:Double=0.0,
    val distanceKm:Double=0.0,
    val totalTrackedKm:Double=0.0,
    val movingTimeMs:Long=0L,
    val averageSpeedKmh:Double=0.0,
    val traffic:Boolean=false,
    val ecoTargetKmh:Double?=null,
    val ecoTargetConfidence:Double=0.0,
    val speedHistoryKmh:List<Double> = emptyList(),
    val ecoTargetHistoryKmh:List<Double> = emptyList(),
    val efficiencyHistory:List<Double> = emptyList(),
)

enum class FuelConfidence { HIGH, MEDIUM, LOW }

data class FuelCycle(
    val startMs:Long,
    val endMs:Long,
    val distanceKm:Double,
    val liters:Double,
    val kmPerLiter:Double,
    val confidence:FuelConfidence,
)

data class EconomyReference(val cycle:FuelCycle,val ecoSpeedKmh:Double?)

data class FuelSummary(
    val currentCycleKm:Double=0.0,
    val bestCycle:FuelCycle?=null,
    val latestFullTimestampMs:Long?=null,
    val cycles:List<FuelCycle> = emptyList(),
    val learnedCycleCount:Int=0,
    val averageKmPerLiter:Double?=null,
    val averageLitersPer100Km:Double?=null,
    val tankCapacityLiters:Double?=null,
    val estimatedRemainingLiters:Double?=null,
    val estimatedRangeKm:Double?=null,
)

data class EconomyProjection(
    val projectedCycleKm:Double,
    val deltaToBestKm:Double,
    val behaviorFactor:Double,
)

class EconomyReferenceStore {
    private val mutable=MutableStateFlow<EconomyReference?>(null)
    val state:StateFlow<EconomyReference?> = mutable
    fun set(value:EconomyReference?){mutable.value=value}
}

data class CalibrationPoint(val trueSpeedKmh:Double,val vehicleSpeedKmh:Double)
data class ActiveCalibration(val profileId:String,val points:List<CalibrationPoint>)

class CalibrationStore {
    private val mutable=MutableStateFlow<ActiveCalibration?>(null)
    val state:StateFlow<ActiveCalibration?> = mutable
    fun set(value:ActiveCalibration?){mutable.value=value}
}
