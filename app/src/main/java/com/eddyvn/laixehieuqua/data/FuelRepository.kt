package com.eddyvn.laixehieuqua.data

import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.engine.FuelEconomyEngine
import kotlinx.coroutines.flow.*
import kotlin.math.max

class FuelRepository(private val dao:AppDao,private val store:EconomyReferenceStore){
    private val engine=FuelEconomyEngine()

    val entries:Flow<List<FuelEntryEntity>> = dao.fuelEntriesFlow()

    val summary:Flow<FuelSummary> = combine(
        dao.fuelEntriesFlow(),
        dao.totalTrackedDistanceFlow(),
    ){fuelEntries,totalTrackedM->
        val sorted=fuelEntries.sortedBy{it.timestampMs}
        val cycles=engine.buildCycles(sorted)
        val best=engine.bestCycle(cycles)
        val latestFull=sorted.lastOrNull{it.isFull}
        val currentKm=latestFull?.let{
            max(0.0,totalTrackedM/1000.0-it.appOdometerKm)
        }?:0.0
        FuelSummary(
            currentCycleKm=currentKm,
            bestCycle=best,
            latestFullTimestampMs=latestFull?.timestampMs,
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

    suspend fun refreshBestReference(){
        val best=engine.bestCycle(engine.buildCycles(dao.fuelEntries()))
        if(best==null){store.set(null);return}
        val speeds=dao.speedsBetween(best.startMs,best.endMs).sorted()
        store.set(EconomyReference(best,speeds.takeIf{it.size>=5}?.let{it[it.size/2]}))
    }
}
