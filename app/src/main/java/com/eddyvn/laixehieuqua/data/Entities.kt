package com.eddyvn.laixehieuqua.data

import androidx.room.*

@Entity(tableName="sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val startMs:Long,
    val endMs:Long?=null,
    val distanceM:Double=0.0,
    val avgSpeedKmh:Double=0.0,
    val maxSpeedKmh:Double=0.0,
    val trackedComplete:Boolean=true,
)
@Entity(tableName="track_points", indices=[Index("sessionId"),Index("timestampMs")])
data class TrackPointEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val sessionId:Long,
    val timestampMs:Long,
    val latitude:Double,
    val longitude:Double,
    val accuracyM:Float,
    val rawGpsSpeedKmh:Double,
    val trueSpeedKmh:Double,
    val accelerationMs2:Double,
    val leanDeg:Double,
    val deltaDistanceM:Double,
)
@Entity(tableName="fuel_entries")
data class FuelEntryEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val timestampMs:Long,
    val liters:Double,
    val totalPrice:Double?,
    val isFull:Boolean,
    val vehicleOdometerKm:Double?,
    val appOdometerKm:Double,
)
@Entity(tableName="dashboard_templates")
data class DashboardTemplateEntity(
    @PrimaryKey val id:String,
    val name:String,
    val category:String,
    val version:Int,
    val builtIn:Boolean,
    val favorite:Boolean,
    val selected:Boolean,
    val layoutType:String,
    val accentHex:Long,
    val secondaryHex:Long,
)
@Entity(tableName="calibration_profiles")
data class CalibrationProfileEntity(
    @PrimaryKey val id:String,
    val name:String,
    val vehicleName:String,
    val active:Boolean,
    val createdAtMs:Long,
)
@Entity(tableName="calibration_points", indices=[Index("profileId")])
data class CalibrationPointEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val profileId:String,
    val trueSpeedKmh:Double,
    val vehicleSpeedKmh:Double,
    val timestampMs:Long,
)
