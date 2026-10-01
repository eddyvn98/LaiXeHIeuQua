package com.eddyvn.laixehieuqua.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.composables.icons.tabler.Tabler
import com.composables.icons.tabler.outline.*
import com.eddyvn.laixehieuqua.ui.components.DriveAmbientBackground

private data class NavItem(val route:String,val label:String,val icon:ImageVector)

@Composable
fun AppRoot(vm:MainViewModel=viewModel()){
    val nav=rememberNavController()
    val context=LocalContext.current
    val drive by vm.drive.collectAsStateWithLifecycle()
    val trackingStatus by vm.trackingStatus.collectAsStateWithLifecycle()
    val reference by vm.reference.collectAsStateWithLifecycle()
    val templates by vm.templates.collectAsStateWithLifecycle()
    val fuel by vm.fuelEntries.collectAsStateWithLifecycle()
    val summary by vm.fuelSummary.collectAsStateWithLifecycle()
    val fuelMarketPrice by vm.fuelMarketPrice.collectAsStateWithLifecycle()
    val projection by vm.projection.collectAsStateWithLifecycle()
    val simulation by vm.simulation.collectAsStateWithLifecycle()
    val vehicleOdometerKm by vm.vehicleOdometerKm.collectAsStateWithLifecycle()
    val tripMeterKm by vm.tripMeterKm.collectAsStateWithLifecycle()
    val selected=templates.firstOrNull{it.selected}

    val permissionLauncher=rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){result->
        vm.onLocationPermissionResult(result[Manifest.permission.ACCESS_FINE_LOCATION]==true)
    }

    fun startWithPermission(){
        if(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )==PackageManager.PERMISSION_GRANTED
        ){
            vm.startTracking()
        }else{
            vm.requestLocationPermission()
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            )
        }
    }

    val items=listOf(
        NavItem("drive","Drive",Tabler.Outline.Gauge),
        NavItem("fuel","Fuel",Tabler.Outline.GasStation),
        NavItem("garage","Garage",Tabler.Outline.Palette),
        NavItem("calibration","ODO",Tabler.Outline.Settings),
        NavItem("simulation","Sim",Tabler.Outline.Gauge),
    )

    val current=nav.currentBackStackEntryAsState().value?.destination?.route
    var menuExpanded by remember{mutableStateOf(false)}
    Box(Modifier.fillMaxSize().background(Color(0xFF030508))){
    if(current==null||current=="drive"){
        DriveAmbientBackground(
            speedKmh=drive.displaySpeedKmh,
            accelerationMs2=drive.accelerationMs2,
            modifier=Modifier.fillMaxSize(),
        )
    }
    Box(Modifier.fillMaxSize()){
    Scaffold(containerColor=Color.Transparent,contentColor=Color.White){paddingValues->
        NavHost(
            nav,
            startDestination="drive",
            modifier=Modifier.padding(paddingValues),
        ){
            composable("drive"){
                DashboardScreen(
                    drive=drive,
                    trackingStatus=trackingStatus,
                    template=selected,
                    vehicleOdometerKm=vehicleOdometerKm,
                    tripMeterKm=tripMeterKm,
                    onResetTrip=vm::resetTripMeter,
                    onStart=::startWithPermission,
                    onStop=vm::stopTracking,
                )
            }
            composable("fuel"){
                FuelScreen(
                    entries=fuel,
                    summary=summary,
                    marketPrice=fuelMarketPrice,
                    onRefreshPrice=vm::refreshFuelMarketPrice,
                    onAdd=vm::addFuel,
                )
            }
            composable("garage"){
                TemplateGarageScreen(
                    templates,
                    vm::selectTemplate,
                    vm::favoriteTemplate,
                    vm::duplicateTemplate,
                )
            }
            composable("calibration"){CalibrationScreen(vm)}
            composable("simulation"){
                SimulationScreen(
                    state=simulation,
                    drive=drive,
                    onStart=vm::startSimulation,
                    onTogglePause=vm::toggleSimulationPause,
                    onStop=vm::stopSimulation,
                    onSetSpeed=vm::setSimulationSpeed,
                    onReplayLastRide=vm::replayLastRide,
                )
            }
        }
    }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(end=4.dp),
            contentAlignment=Alignment.TopEnd,
        ){
            Box{
                IconButton(onClick={menuExpanded=true},modifier=Modifier.size(48.dp)){
                    Icon(Tabler.Outline.Settings,"Mở menu tab",modifier=Modifier.size(22.dp),tint=Color.White)
                }
                DropdownMenu(
                    expanded=menuExpanded,
                    onDismissRequest={menuExpanded=false},
                ){
                    items.forEach{item->
                        DropdownMenuItem(
                            text={Text(item.label)},
                            leadingIcon={Icon(item.icon,null)},
                            trailingIcon=if(current==item.route)({Text("✓")})else null,
                            onClick={
                                menuExpanded=false
                                nav.navigate(item.route){
                                    launchSingleTop=true
                                    restoreState=true
                                }
                            },
                        )
                    }
                }
            }
        }
    }
    }
}
