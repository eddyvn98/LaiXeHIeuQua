package com.eddyvn.laixehieuqua.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities=[
        SessionEntity::class,TrackPointEntity::class,FuelEntryEntity::class,
        DashboardTemplateEntity::class,CalibrationProfileEntity::class,CalibrationPointEntity::class
    ],
    version=1,
    exportSchema=false,
)
abstract class AppDatabase:RoomDatabase(){ abstract fun dao():AppDao }
