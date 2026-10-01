package com.eddyvn.laixehieuqua.tracking

import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import kotlinx.coroutines.flow.*

enum class TrackingStatus {
    IDLE,
    REQUESTING_PERMISSION,
    PERMISSION_REQUIRED,
    STARTING,
    WAITING_FOR_GPS,
    LIVE,
    STOPPING,
    ERROR,
}

class DriveStateStore{
    private val mutable=MutableStateFlow(DriveSnapshot())
    val state:StateFlow<DriveSnapshot> = mutable
    private val mutableTrackingStatus=MutableStateFlow(TrackingStatus.IDLE)
    val trackingStatus:StateFlow<TrackingStatus> = mutableTrackingStatus

    fun update(value:DriveSnapshot){mutable.value=value}
    fun setTrackingStatus(value:TrackingStatus){mutableTrackingStatus.value=value}

    fun beginSession(startMs:Long){
        mutable.value=mutable.value.copy(
            tracking=true,
            sessionStartMs=startMs,
            sessionElapsedMs=0L,
        )
    }

    fun markStopped(endMs:Long=System.currentTimeMillis()){
        val current=mutable.value
        val elapsed=current.sessionStartMs
            ?.let{(endMs-it).coerceAtLeast(current.sessionElapsedMs)}
            ?:current.sessionElapsedMs
        mutable.value=current.copy(
            tracking=false,
            sessionElapsedMs=elapsed,
        )
        mutableTrackingStatus.value=TrackingStatus.IDLE
    }
}
