package com.eddyvn.laixehieuqua

import android.content.Context
import androidx.room.Room
import com.eddyvn.laixehieuqua.data.*
import com.eddyvn.laixehieuqua.domain.CalibrationStore
import com.eddyvn.laixehieuqua.domain.EconomyReferenceStore
import com.eddyvn.laixehieuqua.simulation.SimulationController
import com.eddyvn.laixehieuqua.tracking.DriveStateStore
import kotlinx.coroutines.*

class AppGraph(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "lai_xe_hieu_qua.db").build()
    val driveStateStore = DriveStateStore()
    val economyReferenceStore = EconomyReferenceStore()
    val calibrationStore = CalibrationStore()
    val fuelRepository = FuelRepository(database.dao(), economyReferenceStore)
    val fuelMarketPriceRepository = FuelMarketPriceRepository(context)
    val templateRepository = TemplateRepository(database.dao())
    val calibrationRepository = CalibrationRepository(database.dao(), calibrationStore)
    val simulationController = SimulationController(
        driveStateStore,
        economyReferenceStore,
        calibrationStore,
    )

    fun initialize() {
        scope.launch {
            templateRepository.ensureBuiltIns()
            fuelRepository.refreshBestReference()
            calibrationRepository.refreshActiveProfile()
        }
    }
}
