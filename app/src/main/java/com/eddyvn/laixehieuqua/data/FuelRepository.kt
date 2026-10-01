package com.eddyvn.laixehieuqua.data

import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.engine.FuelEconomyEngine
import com.eddyvn.laixehieuqua.engine.FuelEstimateEngine
import kotlinx.coroutines.flow.*
import kotlin.math.max

class FuelRepository(
    private val dao:AppDao,
    private val store:EconomyReferenceStore,
    private val tankStore:FuelTankStore,
){
    private val engine=FuelEconomyEngine()
    private val estimateEngine=FuelEstimateEngine()

    val entries:Flow<List<FuelEntryEntity>> = dao.fuelEntriesFlow()

    val summary:Flow<FuelSummary> = combine(
        dao.fuelEntriesFlow(),
        dao.totalTrackedDistanceFlow(),
        tankStore.state,
    ){fuelEntries,totalTrackedM,tankState->
        val sorted=fuelEntries.sortedBy{it.timestampMs}
        val cycles=engine.buildCycles(sorted)
        val best=engine.bestCycle(cycles)
        val latestFull=sorted.lastOrNull{it.isFull}
        val currentKm=latestFull?.let{
            max(0.0,totalTrackedM/1000.0-it.appOdometerKm)
        }?:0.0

        val capacity=tankState.capacityLiters
        val partialLiters=latestFull?.let{full->
            sorted.asSequence()
                .filter{it.timestampMs>full.timestampMs&&!it.isFull}
                .sumOf{it.liters}
        }?:0.0

        val estimate=estimateEngine.estimate(
            cycles=cycles,
            currentCycleKm=currentKm,
            tankCapacityLiters=capacity,
            partialLitersSinceFull=partialLiters,
            hasFullReference=latestFull!=null,
        )

        FuelSummary(
            currentCycleKm=currentKm,
            bestCycle=best,
            latestFullTimestampMs=latestFull?.timestampMs,
            cycles=cycles,
            learnedCycleCount=estimate.learnedCycleCount,
            averageKmPerLiter=estimate.averageKmPerLiter,
            averageLitersPer100Km=estimate.averageLitersPer100Km,
            tankCapacityLiters=capacity,
            estimatedRemainingLiters=estimate.remainingLiters,
            estimatedRangeKm=estimate.rangeKm,
        )
    }.distinctUntilChanged()

    suspend fun addEntry(
        liters:Double,
        totalPrice:Double?,
        isFull:Boolean,
        vehicleOdometerKm:Double?,
        timestampMs:Long=System.currentTimeMillis(),
    ){
        dao.insertFuelEntry(
            FuelEntryEntity(
                timestampMs=timestampMs,
                liters=liters,
                totalPrice=totalPrice,
                isFull=isFull,
                vehicleOdometerKm=vehicleOdometerKm,
                appOdometerKm=dao.totalTrackedDistanceM()/1000.0,
            )
        )
        refreshBestReference()
    }

    fun setTankCapacity(liters:Double?){
        tankStore.setCapacity(liters)
    }

    suspend fun refreshBestReference(){
        val best=engine.bestCycle(engine.buildCycles(dao.fuelEntries()))
        if(best==null){store.set(null);return}
        val speeds=dao.speedsBetween(best.startMs,best.endMs).sorted()
        store.set(EconomyReference(best,speeds.takeIf{it.size>=5}?.let{it[it.size/2]}))
    }
}
