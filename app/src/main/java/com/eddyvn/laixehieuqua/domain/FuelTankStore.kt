package com.eddyvn.laixehieuqua.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FuelTankState(
    val capacityLiters:Double?=null,
)

class FuelTankStore(context:Context){
    private val prefs=context.getSharedPreferences("fuel_tank",Context.MODE_PRIVATE)
    private val mutable=MutableStateFlow(load())
    val state:StateFlow<FuelTankState> = mutable

    fun setCapacity(liters:Double?){
        val safe=liters?.takeIf{it in 1.0..60.0}
        if(safe==null){
            prefs.edit().remove(KEY_CAPACITY).apply()
        }else{
            prefs.edit()
                .putLong(KEY_CAPACITY,java.lang.Double.doubleToRawLongBits(safe))
                .apply()
        }
        mutable.value=FuelTankState(safe)
    }

    private fun load():FuelTankState{
        if(!prefs.contains(KEY_CAPACITY))return FuelTankState()
        val bits=prefs.getLong(KEY_CAPACITY,0L)
        return FuelTankState(java.lang.Double.longBitsToDouble(bits))
    }

    companion object{
        private const val KEY_CAPACITY="capacity_liters_bits"
    }
}
