package com.eddyvn.laixehieuqua.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert suspend fun insertSession(session:SessionEntity):Long

    @Query("SELECT * FROM sessions WHERE endMs IS NULL ORDER BY startMs DESC LIMIT 1")
    suspend fun activeSession():SessionEntity?

    @Query("SELECT * FROM sessions WHERE endMs IS NOT NULL ORDER BY startMs DESC LIMIT 1")
    suspend fun latestCompletedSession():SessionEntity?

    @Query("UPDATE sessions SET endMs=:endMs,distanceM=:distanceM,avgSpeedKmh=:avgSpeed,maxSpeedKmh=:maxSpeed,trackedComplete=:complete WHERE id=:id")
    suspend fun closeSession(id:Long,endMs:Long,distanceM:Double,avgSpeed:Double,maxSpeed:Double,complete:Boolean)

    @Insert suspend fun insertTrackPoint(point:TrackPointEntity)

    @Query("SELECT * FROM track_points WHERE sessionId=:sessionId ORDER BY timestampMs ASC")
    suspend fun trackPointsForSession(sessionId:Long):List<TrackPointEntity>

    @Query("SELECT COALESCE(SUM(deltaDistanceM),0) FROM track_points")
    suspend fun totalTrackedDistanceM():Double

    @Query("SELECT COALESCE(SUM(deltaDistanceM),0) FROM track_points")
    fun totalTrackedDistanceFlow():Flow<Double>

    @Query("SELECT COALESCE(SUM(deltaDistanceM),0) FROM track_points WHERE sessionId=:sessionId")
    suspend fun sessionDistanceM(sessionId:Long):Double

    @Query("SELECT COALESCE(AVG(trueSpeedKmh),0) FROM track_points WHERE sessionId=:sessionId")
    suspend fun sessionAvgSpeed(sessionId:Long):Double

    @Query("SELECT COALESCE(MAX(trueSpeedKmh),0) FROM track_points WHERE sessionId=:sessionId")
    suspend fun sessionMaxSpeed(sessionId:Long):Double

    @Query("SELECT trueSpeedKmh FROM track_points WHERE timestampMs BETWEEN :startMs AND :endMs AND trueSpeedKmh BETWEEN 15 AND 100")
    suspend fun speedsBetween(startMs:Long,endMs:Long):List<Double>

    @Insert suspend fun insertFuelEntry(entry:FuelEntryEntity)

    @Query("SELECT * FROM fuel_entries ORDER BY timestampMs ASC")
    suspend fun fuelEntries():List<FuelEntryEntity>

    @Query("SELECT * FROM fuel_entries ORDER BY timestampMs DESC")
    fun fuelEntriesFlow():Flow<List<FuelEntryEntity>>

    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertTemplates(items:List<DashboardTemplateEntity>)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertTemplate(item:DashboardTemplateEntity)
    @Query("SELECT COUNT(*) FROM dashboard_templates") suspend fun templateCount():Int
    @Query("SELECT * FROM dashboard_templates ORDER BY builtIn DESC,name ASC")
    fun templatesFlow():Flow<List<DashboardTemplateEntity>>
    @Query("UPDATE dashboard_templates SET selected=CASE WHEN id=:id THEN 1 ELSE 0 END")
    suspend fun selectTemplate(id:String)
    @Query("UPDATE dashboard_templates SET favorite=:favorite WHERE id=:id")
    suspend fun setTemplateFavorite(id:String,favorite:Boolean)

    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertCalibrationProfile(profile:CalibrationProfileEntity)
    @Query("SELECT * FROM calibration_profiles WHERE active=1 LIMIT 1")
    suspend fun activeCalibrationProfile():CalibrationProfileEntity?
    @Query("SELECT * FROM calibration_profiles ORDER BY createdAtMs DESC")
    fun calibrationProfilesFlow():Flow<List<CalibrationProfileEntity>>
    @Insert suspend fun insertCalibrationPoint(point:CalibrationPointEntity)
    @Query("SELECT * FROM calibration_points WHERE profileId=:profileId ORDER BY trueSpeedKmh ASC")
    suspend fun calibrationPoints(profileId:String):List<CalibrationPointEntity>
    @Query("DELETE FROM calibration_points") suspend fun clearCalibrationPoints()
    @Query("DELETE FROM calibration_profiles") suspend fun clearCalibrationProfiles()
}
