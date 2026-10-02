package com.eddyvn.laixehieuqua.domain

data class DriveInputSample(
    val timestampMs:Long,
    val rawGpsSpeedKmh:Double,
    val gpsAccuracyM:Float,
    val accelerationMs2:Double,
    val longitudinalAccelerationMs2:Double=accelerationMs2,
    val leanDeg:Double,
    val deltaDistanceM:Double,
)
