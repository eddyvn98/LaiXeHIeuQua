package com.eddyvn.laixehieuqua.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TripMeterState(
    val resetTrackedDistanceKm:Double=0.0,
    val resetAtMs:Long?=null,
)

class TripMeterStore(context:Context){
    private val prefs=context.getSharedPreferences("trip_meter",Context.MODE_PRIVATE)
    private val mutable=MutableStateFlow(load())
    val state:StateFlow<TripMeterState> = mutable

    fun reset(totalTrackedDistanceKm:Double){
        val safeBase=totalTrackedDistanceKm.coerceAtLeast(0.0)
        val now=System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_BASE,java.lang.Double.doubleToRawLongBits(safeBase))
            .putLong(KEY_RESET_AT,now)
            .apply()
        mutable.value=TripMeterState(
            resetTrackedDistanceKm=safeBase,
            resetAtMs=now,
        )
    }

    private fun load():TripMeterState{
        val baseBits=prefs.getLong(
            KEY_BASE,
            java.lang.Double.doubleToRawLongBits(0.0),
        )
        val resetAt=prefs.getLong(KEY_RESET_AT,0L).takeIf{it>0L}
        return TripMeterState(
            resetTrackedDistanceKm=java.lang.Double.longBitsToDouble(baseBits),
            resetAtMs=resetAt,
        )
    }

    companion object{
        private const val KEY_BASE="tracked_distance_base_bits"
        private const val KEY_RESET_AT="reset_at"
    }
}
