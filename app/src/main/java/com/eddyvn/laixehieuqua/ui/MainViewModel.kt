package com.eddyvn.laixehieuqua.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eddyvn.laixehieuqua.LaiXeApp
import com.eddyvn.laixehieuqua.camera.CalibrationSampleCollector
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.engine.EconomyProjectionEngine
import com.eddyvn.laixehieuqua.tracking.TrackingService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application:Application):AndroidViewModel(application){
    private val graph=(application as LaiXeApp).graph
    private val collector=CalibrationSampleCollector()
    private val projectionEngine=EconomyProjectionEngine()

    val drive=graph.driveStateStore.state
    val reference=graph.economyReferenceStore.state
    val calibration=graph.calibrationStore.state
    val templates=graph.templateRepository.templates.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val fuelEntries=graph.fuelRepository.entries.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val fuelSummary=graph.fuelRepository.summary.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),FuelSummary())
    val projection=combine(drive,fuelSummary){snapshot,summary->
        projectionEngine.project(snapshot,summary)
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),null)

    private val _lastOcr=MutableStateFlow<Int?>(null)
    val lastOcr:StateFlow<Int?> = _lastOcr
    private val _calibrationStatus=MutableStateFlow("Point the camera at the speedometer")
    val calibrationStatus:StateFlow<String> = _calibrationStatus

    fun startTracking()=TrackingService.start(getApplication())
    fun stopTracking()=TrackingService.stop(getApplication())

    fun addFuel(liters:Double,totalPrice:Double?,full:Boolean,odometer:Double?){
        viewModelScope.launch{graph.fuelRepository.addEntry(liters,totalPrice,full,odometer)}
    }

    fun selectTemplate(id:String){viewModelScope.launch{graph.templateRepository.select(id)}}
    fun favoriteTemplate(item:DashboardTemplateEntity){viewModelScope.launch{graph.templateRepository.favorite(item)}}
    fun duplicateTemplate(item:DashboardTemplateEntity){viewModelScope.launch{graph.templateRepository.duplicate(item)}}

    fun onOcrSpeed(vehicleSpeed:Int){
        _lastOcr.value=vehicleSpeed
        val point=collector.offer(System.currentTimeMillis(),drive.value.trueSpeedKmh,vehicleSpeed.toDouble())
        if(point==null){
            _calibrationStatus.value="Hold a steady speed for a few seconds"
            return
        }
        viewModelScope.launch{
            val ok=graph.calibrationRepository.addPoint(point.trueSpeedKmh,point.vehicleSpeedKmh)
            _calibrationStatus.value=if(ok)
                "Saved: GPS %.1f ↔ vehicle %.1f".format(point.trueSpeedKmh,point.vehicleSpeedKmh)
            else "Skipped non-monotonic sample"
        }
    }
}
