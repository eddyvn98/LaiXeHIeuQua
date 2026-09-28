package com.eddyvn.laixehieuqua.engine
import com.eddyvn.laixehieuqua.domain.CalibrationPoint
import org.junit.Assert.*
import org.junit.Test
class SpeedCalibrationEngineTest{
 private val e=SpeedCalibrationEngine()
 @Test fun interpolatesPiecewise(){val p=listOf(CalibrationPoint(20.0,22.0),CalibrationPoint(40.0,44.0),CalibrationPoint(60.0,67.0));assertEquals(33.0,e.map(30.0,p),.01);assertEquals(55.5,e.map(50.0,p),.01)}
 @Test fun rejectsNonMonotonicProfile(){assertFalse(e.isMonotonic(listOf(CalibrationPoint(20.0,24.0),CalibrationPoint(40.0,23.0))))}
}
