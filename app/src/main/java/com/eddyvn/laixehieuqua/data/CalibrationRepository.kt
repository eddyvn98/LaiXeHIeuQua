package com.eddyvn.laixehieuqua.data

import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.engine.SpeedCalibrationEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CalibrationRepository(private val dao:AppDao,private val store:CalibrationStore){
    private val engine=SpeedCalibrationEngine()
    val profiles:Flow<List<CalibrationProfileEntity>> = dao.calibrationProfilesFlow()
    suspend fun ensureProfile(vehicleName:String="My motorcycle"):String{
        dao.activeCalibrationProfile()?.let{return it.id}
        val id=UUID.randomUUID().toString()
        dao.upsertCalibrationProfile(CalibrationProfileEntity(id,"Camera calibration",vehicleName,true,System.currentTimeMillis()))
        refreshActiveProfile();return id
    }
    suspend fun addPoint(trueSpeedKmh:Double,vehicleSpeedKmh:Double):Boolean{
        val id=ensureProfile()
        val current=dao.calibrationPoints(id).map{CalibrationPoint(it.trueSpeedKmh,it.vehicleSpeedKmh)}
        if(current.any{kotlin.math.abs(it.trueSpeedKmh-trueSpeedKmh)<3.0})return false
        if(!engine.isMonotonic(current+CalibrationPoint(trueSpeedKmh,vehicleSpeedKmh)))return false
        dao.insertCalibrationPoint(CalibrationPointEntity(profileId=id,trueSpeedKmh=trueSpeedKmh,vehicleSpeedKmh=vehicleSpeedKmh,timestampMs=System.currentTimeMillis()))
        refreshActiveProfile();return true
    }
    suspend fun reset(){
        dao.clearCalibrationPoints()
        dao.clearCalibrationProfiles()
        store.set(null)
    }
    suspend fun refreshActiveProfile(){
        val p=dao.activeCalibrationProfile()
        if(p==null){store.set(null);return}
        store.set(ActiveCalibration(p.id,dao.calibrationPoints(p.id).map{CalibrationPoint(it.trueSpeedKmh,it.vehicleSpeedKmh)}))
    }
}
