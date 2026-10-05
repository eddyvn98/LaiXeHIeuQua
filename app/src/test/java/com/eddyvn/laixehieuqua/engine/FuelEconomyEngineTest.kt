package com.eddyvn.laixehieuqua.engine
import com.eddyvn.laixehieuqua.data.FuelEntryEntity
import com.eddyvn.laixehieuqua.domain.FuelConfidence
import org.junit.Assert.*
import org.junit.Test
class FuelEconomyEngineTest{
 @Test fun fullToFullIncludesPartialFills(){
  val entries=listOf(
   FuelEntryEntity(timestampMs=1,liters=5.0,totalPrice=null,isFull=true,vehicleOdometerKm=1000.0,appOdometerKm=0.0),
   FuelEntryEntity(timestampMs=2,liters=1.0,totalPrice=null,isFull=false,vehicleOdometerKm=null,appOdometerKm=100.0),
   FuelEntryEntity(timestampMs=3,liters=4.0,totalPrice=null,isFull=true,vehicleOdometerKm=1250.0,appOdometerKm=250.0))
  val c=FuelEconomyEngine().buildCycles(entries).single()
  assertEquals(250.0,c.distanceKm,.01);assertEquals(5.0,c.liters,.01);assertEquals(50.0,c.kmPerLiter,.01);assertEquals(FuelConfidence.HIGH,c.confidence)
 }

 @Test fun cycleCanStartFromOdoBaseline(){
  val entries=listOf(
   FuelEntryEntity(timestampMs=1,liters=0.0,totalPrice=null,isFull=true,vehicleOdometerKm=2000.0,appOdometerKm=0.0),
   FuelEntryEntity(timestampMs=2,liters=4.0,totalPrice=100000.0,isFull=true,vehicleOdometerKm=2120.0,appOdometerKm=120.0))
  val c=FuelEconomyEngine().buildCycles(entries).single()
  assertEquals(120.0,c.distanceKm,.01)
  assertEquals(4.0,c.liters,.01)
  assertEquals(30.0,c.kmPerLiter,.01)
  assertEquals(FuelConfidence.HIGH,c.confidence)
 }
}
