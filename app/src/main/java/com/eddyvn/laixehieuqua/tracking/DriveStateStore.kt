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
    private var accelerationMagnitudeSum=0.0
    private var accelerationSampleCount=0L
    val state:StateFlow<DriveSnapshot> = mutable
    private val mutableTrackingStatus=MutableStateFlow(TrackingStatus.IDLE)
    val trackingStatus:StateFlow<TrackingStatus> = mutableTrackingStatus

    fun update(value:DriveSnapshot){
        val averageAcceleration=mutable.value.averageAccelerationMs2
        mutable.value=value.copy(averageAccelerationMs2=averageAcceleration)
    }

    fun updateRealtimeMotion(displaySpeedKmh:Double,accelerationMs2:Double){
        val safeSpeed=displaySpeedKmh.coerceIn(0.0,180.0)
        val safeAcceleration=accelerationMs2.coerceIn(-8.0,8.0)

        mutable.update{current->
            if(!current.tracking)current
            else{
                if(safeSpeed>1.5||kotlin.math.abs(safeAcceleration)>.10){
                    accelerationMagnitudeSum+=kotlin.math.abs(safeAcceleration)
                    accelerationSampleCount++
                }
                val averageAcceleration=if(accelerationSampleCount>0L){
                    accelerationMagnitudeSum/accelerationSampleCount.toDouble()
                }else 0.0

                current.copy(
                    displaySpeedKmh=safeSpeed,
                    accelerationMs2=safeAcceleration,
                    averageAccelerationMs2=averageAcceleration.coerceIn(0.0,8.0),
                )
            }
        }
    }

    fun setTrackingStatus(value:TrackingStatus){mutableTrackingStatus.value=value}

    fun beginSession(startMs:Long){
        accelerationMagnitudeSum=0.0
        accelerationSampleCount=0L
        mutable.value=mutable.value.copy(
            tracking=true,
            sessionStartMs=startMs,
            sessionElapsedMs=0L,
            averageAccelerationMs2=0.0,
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
