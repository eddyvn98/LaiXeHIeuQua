package com.eddyvn.laixehieuqua.ui

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eddyvn.laixehieuqua.camera.CalibrationSampleCollector
import com.eddyvn.laixehieuqua.data.AppDatabase
import com.eddyvn.laixehieuqua.data.CalibrationRepository
import com.eddyvn.laixehieuqua.data.FuelRepository
import com.eddyvn.laixehieuqua.data.TemplateRepository
import com.eddyvn.laixehieuqua.data.SessionEntity
import com.eddyvn.laixehieuqua.data.TrackPointEntity
import com.eddyvn.laixehieuqua.domain.CalibrationStore
import com.eddyvn.laixehieuqua.domain.EconomyReferenceStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtherTabsDeviceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun fuelTabAddsEntriesAndBuildsAValidCycle() = runBlocking {
        val database = inMemoryDatabase()
        try {
            val dao = database.dao()
            val referenceStore = EconomyReferenceStore()
            val repository = FuelRepository(dao, referenceStore)
            val sessionId = dao.insertSession(SessionEntity(startMs = 1_000L))
            repository.addEntry(10.0, 200_000.0, true, 1_000.0, timestampMs = 1_000L)
            repeat(5) { index ->
                dao.insertTrackPoint(
                    TrackPointEntity(
                        sessionId = sessionId,
                        timestampMs = 1_200L + index,
                        latitude = 0.0,
                        longitude = 0.0,
                        accuracyM = 5f,
                        rawGpsSpeedKmh = 50.0,
                        trueSpeedKmh = 50.0,
                        accelerationMs2 = 0.0,
                        leanDeg = 0.0,
                        deltaDistanceM = 10_000.0,
                    ),
                )
            }
            assertEquals(50_000.0, dao.totalTrackedDistanceM(), 0.01)
            repository.addEntry(10.0, 210_000.0, true, 1_050.0, timestampMs = 2_000L)

            val entries = repository.entries.first { it.size == 2 }
            val summary = repository.summary.first { it.bestCycle != null }
            val reference = referenceStore.state.value

            assertEquals(
                50.0,
                entries.first().appOdometerKm,
                0.01,
            )
            assertEquals(50.0, summary.bestCycle?.distanceKm ?: 0.0, 0.01)
            assertEquals(5.0, summary.bestCycle?.kmPerLiter ?: 0.0, 0.01)
            assertEquals(50.0, reference?.ecoSpeedKmh ?: 0.0, 0.01)
        } finally {
            database.close()
        }
    }

    @Test
    fun garageCanSeedSelectFavoriteAndDuplicateTemplates() = runBlocking {
        val database = inMemoryDatabase()
        try {
            val repository = TemplateRepository(database.dao())
            repository.ensureBuiltIns()
            val builtIns = repository.templates.first { it.size == 2 }
            assertTrue(builtIns.any { it.selected && it.id == "tft-sport" })

            val premium = builtIns.first { it.id == "premium-segmented" }
            repository.select(premium.id)
            val selectedRows = repository.templates.first { rows ->
                rows.any { it.id == premium.id && it.selected } && rows.none { it.id == "tft-sport" && it.selected }
            }
            assertEquals(premium.id, selectedRows.single { it.selected }.id)

            repository.favorite(premium)
            assertTrue(repository.templates.first { rows -> rows.any { it.id == premium.id && it.favorite } }
                .first { it.id == premium.id }.favorite)
            repository.duplicate(premium)
            val copied = repository.templates.first { it.size == 3 }.first { it.name == "Premium Segmented Copy" }
            assertFalse(copied.builtIn)
            assertFalse(copied.selected)
        } finally {
            database.close()
        }
    }

    @Test
    fun syncTabCollectsStableOcrAndRejectsNonMonotonicCalibration() = runBlocking {
        val database = inMemoryDatabase()
        try {
            val store = CalibrationStore()
            val repository = CalibrationRepository(database.dao(), store)
            val collector = CalibrationSampleCollector()
            var accepted: com.eddyvn.laixehieuqua.domain.CalibrationPoint? = null

            (1..4).forEach { second ->
                accepted = collector.offer(second * 1_000L, 50.0, 53.0) ?: accepted
            }
            assertNotNull(accepted)
            assertTrue(repository.addPoint(accepted!!.trueSpeedKmh, accepted!!.vehicleSpeedKmh))

            val profile = repository.profiles.first { it.isNotEmpty() }.single()
            val points = database.dao().calibrationPoints(profile.id)
            assertEquals(1, points.size)
            assertEquals(50.0, store.state.value?.points?.single()?.trueSpeedKmh ?: 0.0, 0.01)
            assertFalse(repository.addPoint(60.0, 52.0))
            assertEquals(1, database.dao().calibrationPoints(profile.id).size)
        } finally {
            database.close()
        }
    }

    private fun inMemoryDatabase() = Room.inMemoryDatabaseBuilder(
        context,
        AppDatabase::class.java,
    ).allowMainThreadQueries().build()
}
