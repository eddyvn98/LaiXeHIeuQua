package com.eddyvn.laixehieuqua.tracking

import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import kotlinx.coroutines.flow.*

class DriveStateStore{
    private val mutable=MutableStateFlow(DriveSnapshot())
    val state:StateFlow<DriveSnapshot> = mutable
    fun update(value:DriveSnapshot){mutable.value=value}
    fun markStopped(){mutable.value=mutable.value.copy(tracking=false)}
}
