package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.simulation.SimulationScenario
import com.eddyvn.laixehieuqua.simulation.SimulationState

@Composable
fun SimulationScreen(
    state:SimulationState,
    drive:DriveSnapshot,
    onStart:(SimulationScenario,Int)->Unit,
    onTogglePause:()->Unit,
    onStop:()->Unit,
    onSetSpeed:(Int)->Unit,
    onReplayLastRide:(Int)->Unit,
){
    var selected by remember{mutableStateOf(state.scenario)}
    val speeds=listOf(1,5,20,100)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(14.dp),
    ){
        Text("Simulation Lab",style=MaterialTheme.typography.headlineSmall)
        Text("Synthetic scenarios and recorded-ride replay use the same drive engines as live tracking.")

        Card(Modifier.fillMaxWidth()){
            Column(
                Modifier.padding(14.dp),
                verticalArrangement=Arrangement.spacedBy(6.dp),
            ){
                Text(state.source,style=MaterialTheme.typography.titleMedium)
                Text(state.message)
                LinearProgressIndicator(
                    progress={state.progress.coerceIn(0f,1f)},
                    modifier=Modifier.fillMaxWidth(),
                )
                Text("Scenario time: ${state.elapsedScenarioMs/1000}s / ${state.totalScenarioMs/1000}s")
                Text(
                    "GPS ${"%.1f".format(drive.rawGpsSpeedKmh)} → fused ${"%.1f".format(drive.trueSpeedKmh)} km/h"
                )
                Text(
                    "Acceleration ${"%.2f".format(drive.accelerationMs2)} m/s² · lean ${"%.1f".format(drive.leanDeg)}°"
                )
                Text(
                    "Traffic ${if(drive.traffic)"YES" else "NO"} · Eco target ${drive.ecoTargetKmh?.let{"%.1f".format(it)}?:"—"}"
                )
                state.lastOcrSpeedKmh?.let{Text("Simulated OCR: $it km/h")}
            }
        }

        Text("Scenario",style=MaterialTheme.typography.titleMedium)
        SimulationScenario.entries.forEach{scenario->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled=!state.active){selected=scenario}
                    .padding(vertical=5.dp),
            ){
                RadioButton(
                    selected=selected==scenario,
                    onClick={if(!state.active)selected=scenario},
                    enabled=!state.active,
                )
                Column(Modifier.padding(start=8.dp)){
                    Text(scenario.title)
                    Text(scenario.description,style=MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Playback speed",style=MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(8.dp),
        ){
            speeds.forEach{speed->
                FilterChip(
                    selected=state.speedMultiplier==speed,
                    onClick={onSetSpeed(speed)},
                    label={Text("×$speed")},
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(8.dp),
        ){
            Button(
                onClick={
                    if(state.active)onTogglePause()
                    else onStart(selected,state.speedMultiplier)
                },
                modifier=Modifier.weight(1f),
            ){
                Text(
                    when{
                        state.active&&state.paused -> "Resume"
                        state.active -> "Pause"
                        else -> "Run"
                    }
                )
            }
            OutlinedButton(
                onClick=onStop,
                enabled=state.active,
                modifier=Modifier.weight(1f),
            ){Text("Stop")}
        }

        OutlinedButton(
            onClick={onReplayLastRide(state.speedMultiplier)},
            enabled=!state.active,
            modifier=Modifier.fillMaxWidth(),
        ){
            Text("Replay last recorded ride")
        }

        Text(
            "Simulation never writes synthetic track points or fuel data into the real ride database.",
            style=MaterialTheme.typography.bodySmall,
        )
    }
}
