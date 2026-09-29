package com.eddyvn.laixehieuqua.simulation

enum class SimulationScenario(val title:String,val description:String){
    CITY_TRAFFIC("City traffic","Stop/go traffic with repeated starts and stops"),
    CLEAR_ROAD("Clear road","Stable 45–55 km/h economy cruising"),
    ACCEL_BRAKE("Acceleration + braking","Repeated acceleration and controlled braking"),
    GPS_STRESS("GPS noise + dropout","Accuracy degradation, speed spikes and missing updates"),
    LEANING("Lean / cornering","Stable speed with left/right lean changes"),
    CALIBRATION("Speedometer calibration","30/50/70 km/h plateaus with simulated OCR"),
    LONG_RIDE("2-hour long ride","Mixed two-hour ride for accelerated soak testing"),
}

data class SimulationFrame(
    val relativeTimeMs:Long,
    val rawGpsSpeedKmh:Double,
    val gpsAccuracyM:Float=5f,
    val accelerationMs2:Double=0.0,
    val leanDeg:Double=0.0,
    val deltaDistanceM:Double=0.0,
    val gpsAvailable:Boolean=true,
    val simulatedOcrSpeedKmh:Int?=null,
)

data class SimulationState(
    val active:Boolean=false,
    val paused:Boolean=false,
    val source:String="Idle",
    val scenario:SimulationScenario=SimulationScenario.CITY_TRAFFIC,
    val speedMultiplier:Int=20,
    val progress:Float=0f,
    val elapsedScenarioMs:Long=0,
    val totalScenarioMs:Long=0,
    val lastOcrSpeedKmh:Int?=null,
    val message:String="Ready",
)
