package com.eddyvn.laixehieuqua.data

import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.engine.FuelEconomyEngine
import kotlinx.coroutines.flow.*
import kotlin.math.max

class FuelRepository(
    private val dao:AppDao,
    private val store:EconomyReferenceStore,
    private val tankStore:FuelTankStore,
){
    private val engine=FuelEconomyEngine()

    val entries:Flow<List<FuelEntryEntity>> = dao.fuelEntriesFlow()

    val summary:Flow<FuelSummary> = combine(
        dao.fuelEntriesFlow(),
        dao.totalTrackedDistanceFlow(),
        tankStore.state,
    ){fuelEntries,totalTrackedM,tankState->
        val sorted=fuelEntries.sortedBy{it.timestampMs}
        val cycles=engine.buildCycles(sorted)
        val best=engine.bestCycle(cycles)
        val validCycles=cycles.filter{it.confidence!=FuelConfidence.LOW}
        val averageKmPerLiter=validCycles
            .takeIf{it.isNotEmpty()}
            ?.let{items->
                val liters=items.sumOf{it.liters}
                if(liters>0.0)items.sumOf{it.distanceKm}/liters else null
            }

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

        val remaining=if(
            latestFull!=null&&capacity!=null&&averageKmPerLiter!=null&&averageKmPerLiter>0.0
        ){
            (capacity+partialLiters-currentKm/averageKmPerLiter)
                .coerceIn(0.0,capacity)
        }else null

        FuelSummary(
            currentCycleKm=currentKm,
            bestCycle=best,
            latestFullTimestampMs=latestFull?.timestampMs,
            cycles=cycles,
            learnedCycleCount=validCycles.size,
            averageKmPerLiter=averageKmPerLiter,
            averageLitersPer100Km=averageKmPerLiter?.takeIf{it>0.0}?.let{100.0/it},
            tankCapacityLiters=capacity,
            estimatedRemainingLiters=remaining,
            estimatedRangeKm=if(remaining!=null&&averageKmPerLiter!=null)
                remaining*averageKmPerLiter else null,
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
