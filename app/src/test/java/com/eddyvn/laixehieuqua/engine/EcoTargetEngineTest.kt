package com.eddyvn.laixehieuqua.engine
import org.junit.Assert.*
import org.junit.Test
class EcoTargetEngineTest{
 @Test fun hidesTargetInTraffic(){assertNull(EcoTargetEngine().update(8.0,.1,true,45.0).targetKmh)}
 @Test fun usesBestReferenceWhenClear(){assertEquals(44.0,EcoTargetEngine().update(45.0,.1,false,44.0).targetKmh!!,.01)}
}
