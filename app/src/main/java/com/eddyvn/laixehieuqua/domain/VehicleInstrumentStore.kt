package com.eddyvn.laixehieuqua.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class VehicleInstrumentState(
    val setupComplete:Boolean=false,
    val odometerBaseKm:Double?=null,
    val trackedDistanceBaseKm:Double=0.0,
    val setupAtMs:Long?=null,
)

class VehicleInstrumentStore(context:Context){
    private val prefs=context.getSharedPreferences("vehicle_instrument",Context.MODE_PRIVATE)
    private val mutable=MutableStateFlow(load())
    val state:StateFlow<VehicleInstrumentState> = mutable

    fun completeSetup(odometerKm:Double,trackedDistanceKm:Double){
        val now=System.currentTimeMillis()
        prefs.edit()
            .putBoolean(KEY_COMPLETE,true)
            .putLong(KEY_ODO,java.lang.Double.doubleToRawLongBits(odometerKm))
            .putLong(KEY_TRACKED,java.lang.Double.doubleToRawLongBits(trackedDistanceKm))
            .putLong(KEY_SETUP_AT,now)
            .apply()
        mutable.value=VehicleInstrumentState(
            setupComplete=true,
            odometerBaseKm=odometerKm,
            trackedDistanceBaseKm=trackedDistanceKm,
            setupAtMs=now,
        )
    }

    fun reset(){
        prefs.edit().clear().apply()
        mutable.value=VehicleInstrumentState()
    }

    private fun load():VehicleInstrumentState{
        val odoBits=prefs.getLong(KEY_ODO,Long.MIN_VALUE)
        val trackedBits=prefs.getLong(KEY_TRACKED,java.lang.Double.doubleToRawLongBits(0.0))
        val setupAt=prefs.getLong(KEY_SETUP_AT,0L).takeIf{it>0L}
        return VehicleInstrumentState(
            setupComplete=prefs.getBoolean(KEY_COMPLETE,false),
            odometerBaseKm=if(odoBits==Long.MIN_VALUE)null else java.lang.Double.longBitsToDouble(odoBits),
            trackedDistanceBaseKm=java.lang.Double.longBitsToDouble(trackedBits),
            setupAtMs=setupAt,
        )
    }

    companion object{
        private const val KEY_COMPLETE="setup_complete"
        private const val KEY_ODO="odometer_bits"
        private const val KEY_TRACKED="tracked_distance_bits"
        private const val KEY_SETUP_AT="setup_at"
    }
}
