package com.eddyvn.laixehieuqua.engine
import org.junit.Assert.*
import org.junit.Test
class TrafficDetectorTest{
 @Test fun stopGoPatternBecomesTraffic(){val d=TrafficDetector();var t=false;listOf(0.0,0.0,8.0,1.0,0.0,10.0,1.0,0.0,9.0,0.0).forEachIndexed{i,s->t=d.update(i*1000L,s)};assertTrue(t)}
 @Test fun steadyUrbanSpeedIsNotTraffic(){val d=TrafficDetector();var t=false;repeat(20){i->t=d.update(i*1000L,35.0)};assertFalse(t)}
}
