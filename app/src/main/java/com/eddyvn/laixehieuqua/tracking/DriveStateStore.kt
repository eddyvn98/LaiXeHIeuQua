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
    fun markStopped(){
        mutable.value=mutable.value.copy(tracking=false)
        mutableTrackingStatus.value=TrackingStatus.IDLE
    }
}
