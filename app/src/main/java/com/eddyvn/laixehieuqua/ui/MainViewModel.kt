package com.eddyvn.laixehieuqua.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eddyvn.laixehieuqua.LaiXeApp
import com.eddyvn.laixehieuqua.camera.CalibrationSampleCollector
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.data.FuelMarketPriceState
import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.engine.EconomyProjectionEngine
import com.eddyvn.laixehieuqua.simulation.*
import com.eddyvn.laixehieuqua.tracking.TrackingService
import com.eddyvn.laixehieuqua.tracking.TrackingStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application:Application):AndroidViewModel(application){
    private val graph=(application as LaiXeApp).graph
    private val collector=CalibrationSampleCollector()
    private val projectionEngine=EconomyProjectionEngine()

    val drive=graph.driveStateStore.state
    val trackingStatus=graph.driveStateStore.trackingStatus
    val reference=graph.economyReferenceStore.state
    val calibration=graph.calibrationStore.state
    val vehicleInstrument=graph.vehicleInstrumentStore.state
    val tripMeterState=graph.tripMeterStore.state

    val totalTrackedDistanceKm=graph.database.dao().totalTrackedDistanceFlow()
        .map{it/1000.0}
        .stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),0.0)

    val vehicleOdometerKm=combine(vehicleInstrument,totalTrackedDistanceKm){instrument,total->
        instrument.odometerBaseKm?.plus(
            (total-instrument.trackedDistanceBaseKm).coerceAtLeast(0.0)
        )
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),null)

    val tripMeterKm=combine(tripMeterState,totalTrackedDistanceKm){trip,total->
        (total-trip.resetTrackedDistanceKm).coerceAtLeast(0.0)
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),0.0)

    val simulation=graph.simulationController.state
    val templates=graph.templateRepository.templates.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val fuelEntries=graph.fuelRepository.entries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val fuelSummary=graph.fuelRepository.summary.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FuelSummary(),
    )

    private val _fuelMarketPrice=MutableStateFlow(
        FuelMarketPriceState(price=graph.fuelMarketPriceRepository.cached())
    )
    val fuelMarketPrice:StateFlow<FuelMarketPriceState> = _fuelMarketPrice
    private var lastFuelPriceRefreshMs=0L

    val projection=combine(drive,fuelSummary){snapshot,summary->
        projectionEngine.project(snapshot,summary)
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),null)

    private val _lastOcr=MutableStateFlow<Int?>(null)
    val lastOcr:StateFlow<Int?> = _lastOcr
    private val _calibrationStatus=MutableStateFlow("Camera calibration is optional")
    val calibrationStatus:StateFlow<String> = _calibrationStatus

    fun startTracking(){
        graph.simulationController.stop()
        TrackingService.start(getApplication())
    }

    fun requestLocationPermission(){
        graph.driveStateStore.setTrackingStatus(TrackingStatus.REQUESTING_PERMISSION)
    }

    fun onLocationPermissionResult(granted:Boolean){
        if(granted)startTracking()
        else graph.driveStateStore.setTrackingStatus(TrackingStatus.PERMISSION_REQUIRED)
    }

    fun stopTracking(){
        if(simulation.value.active)graph.simulationController.stop()
        else {
            graph.driveStateStore.setTrackingStatus(TrackingStatus.STOPPING)
            TrackingService.stop(getApplication())
        }
    }

    fun resetTripMeter(){
        graph.tripMeterStore.reset(totalTrackedDistanceKm.value)
    }

    fun setVehicleOdometer(odometerKm:Double){
        if(odometerKm<0.0)return
        graph.vehicleInstrumentStore.completeSetup(
            odometerKm=odometerKm,
            trackedDistanceKm=totalTrackedDistanceKm.value,
        )
    }

    fun clearVehicleOdometer(){
        graph.vehicleInstrumentStore.reset()
    }

    fun startSimulation(scenario:SimulationScenario,speedMultiplier:Int){
        viewModelScope.launch{
            if(trackingStatus.value!=TrackingStatus.IDLE&&!simulation.value.active){
                TrackingService.stop(getApplication())
                delay(600)
            }
            graph.simulationController.startScenario(scenario,speedMultiplier)
        }
    }

    fun toggleSimulationPause()=graph.simulationController.togglePause()
    fun stopSimulation()=graph.simulationController.stop()
    fun setSimulationSpeed(speed:Int)=graph.simulationController.setSpeedMultiplier(speed)

    fun replayLastRide(speedMultiplier:Int){
        viewModelScope.launch{
            if(trackingStatus.value!=TrackingStatus.IDLE&&!simulation.value.active){
                TrackingService.stop(getApplication())
                delay(600)
            }
            val dao=graph.database.dao()
            val session=dao.latestCompletedSession()
            if(session==null){
                graph.simulationController.replay(emptyList(),speedMultiplier)
                return@launch
            }

            val points=dao.trackPointsForSession(session.id)
            val firstTime=points.firstOrNull()?.timestampMs?:0L
            val frames=points.map{point->
                SimulationFrame(
                    relativeTimeMs=(point.timestampMs-firstTime).coerceAtLeast(0L),
                    rawGpsSpeedKmh=point.rawGpsSpeedKmh,
                    gpsAccuracyM=point.accuracyM,
                    accelerationMs2=point.accelerationMs2,
                    leanDeg=point.leanDeg,
                    deltaDistanceM=point.deltaDistanceM,
                )
            }
            graph.simulationController.replay(frames,speedMultiplier)
        }
    }

    fun addFuel(liters:Double,totalPrice:Double?,full:Boolean,odometer:Double?){
        viewModelScope.launch{
            graph.fuelRepository.addEntry(liters,totalPrice,full,odometer)
        }
    }

    fun recordFuelFromDrive(
        odometerKm:Double,
        unitPrice:Double,
        amount:Double,
        full:Boolean,
        tankCapacityLiters:Double?,
    ){
        if(odometerKm<0.0||unitPrice<=0.0||amount<=0.0)return
        val liters=amount/unitPrice
        if(liters<=0.0)return

        setVehicleOdometer(odometerKm)
        graph.fuelRepository.setTankCapacity(tankCapacityLiters)

        viewModelScope.launch{
            graph.fuelRepository.addEntry(
                liters=liters,
                totalPrice=amount,
                isFull=full,
                vehicleOdometerKm=odometerKm,
            )
        }
    }

    fun refreshFuelMarketPrice(force:Boolean=false){
        val now=System.currentTimeMillis()
        if(_fuelMarketPrice.value.loading||(!force&&now-lastFuelPriceRefreshMs<15*60*1000))return
        lastFuelPriceRefreshMs=now
        viewModelScope.launch{
            _fuelMarketPrice.update{it.copy(loading=true,error=null)}
            try{
                val price=graph.fuelMarketPriceRepository.refresh()
                _fuelMarketPrice.value=FuelMarketPriceState(price=price)
            }catch(error:CancellationException){
                throw error
            }catch(error:Exception){
                _fuelMarketPrice.update{
                    it.copy(loading=false,error="Không tải được giá thị trường. Có thể nhập giá tại cây xăng.")
                }
            }
        }
    }

    fun selectTemplate(id:String){
        viewModelScope.launch{graph.templateRepository.select(id)}
    }

    fun favoriteTemplate(item:DashboardTemplateEntity){
        viewModelScope.launch{graph.templateRepository.favorite(item)}
    }

    fun duplicateTemplate(item:DashboardTemplateEntity){
        viewModelScope.launch{graph.templateRepository.duplicate(item)}
    }

    // Retained for compatibility with the existing optional calibration screen.
    // Live speed no longer depends on these samples.
    fun onOcrSpeed(vehicleSpeed:Int){
        _lastOcr.value=vehicleSpeed
        val point=collector.offer(
            System.currentTimeMillis(),
            drive.value.trueSpeedKmh,
            vehicleSpeed.toDouble(),
        )?:return
        viewModelScope.launch{
            graph.calibrationRepository.addPoint(point.trueSpeedKmh,point.vehicleSpeedKmh)
        }
    }

    fun completeVehicleSetup(odometerKm:Double)=setVehicleOdometer(odometerKm)

    fun resetVehicleSetup(){
        clearVehicleOdometer()
        _lastOcr.value=null
    }

    fun clearCalibrationSamples(){
        viewModelScope.launch{
            graph.calibrationRepository.reset()
            _lastOcr.value=null
        }
    }
}
