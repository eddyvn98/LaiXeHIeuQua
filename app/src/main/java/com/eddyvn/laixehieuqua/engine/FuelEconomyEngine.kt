package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.data.FuelEntryEntity
import com.eddyvn.laixehieuqua.domain.*
import kotlin.math.*

class FuelEconomyEngine{
    fun buildCycles(entries:List<FuelEntryEntity>):List<FuelCycle>{
        val sorted=entries.sortedBy{it.timestampMs}
        val full=sorted.indices.filter{sorted[it].isFull}
        if(full.size<2)return emptyList()
        return full.zipWithNext().mapNotNull{(s,e)->
            val start=sorted[s];val end=sorted[e]
            val liters=sorted.subList(s+1,e+1).sumOf{it.liters}
            if(liters<=0.0)return@mapNotNull null
            val app=end.appOdometerKm-start.appOdometerKm
            val vehicle=if(start.vehicleOdometerKm!=null&&end.vehicleOdometerKm!=null)end.vehicleOdometerKm-start.vehicleOdometerKm else null
            val distance=vehicle?.takeIf{it>0.0}?:app
            if(distance<=0.0)return@mapNotNull null
            FuelCycle(start.timestampMs,end.timestampMs,distance,liters,distance/liters,confidence(vehicle,app))
        }
    }
    fun bestCycle(cycles:List<FuelCycle>)=cycles.filter{it.confidence!=FuelConfidence.LOW}.maxByOrNull{it.kmPerLiter}
    private fun confidence(vehicle:Double?,app:Double):FuelConfidence{
        if(vehicle==null)return if(app>0)FuelConfidence.MEDIUM else FuelConfidence.LOW
        return if(abs(vehicle-app)<=max(2.0,vehicle*0.03))FuelConfidence.HIGH else FuelConfidence.MEDIUM
    }
}
