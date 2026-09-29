package com.eddyvn.laixehieuqua.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.composables.icons.tabler.Tabler
import com.composables.icons.tabler.outline.*

private data class NavItem(val route:String,val label:String,val icon:ImageVector)

@Composable
fun AppRoot(vm:MainViewModel=viewModel()){
    val nav=rememberNavController()
    val context=LocalContext.current
    val drive by vm.drive.collectAsStateWithLifecycle()
    val reference by vm.reference.collectAsStateWithLifecycle()
    val templates by vm.templates.collectAsStateWithLifecycle()
    val fuel by vm.fuelEntries.collectAsStateWithLifecycle()
    val summary by vm.fuelSummary.collectAsStateWithLifecycle()
    val projection by vm.projection.collectAsStateWithLifecycle()
    val selected=templates.firstOrNull{it.selected}

    val permissionLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result->
        if(result[Manifest.permission.ACCESS_FINE_LOCATION]==true)vm.startTracking()
    }

    fun startWithPermission(){
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            vm.startTracking()
        }else{
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
        NavItem("calibration","Sync",Tabler.Outline.Camera),
    )

    Scaffold(bottomBar={
        NavigationBar{
            val current=nav.currentBackStackEntryAsState().value?.destination?.route
            items.forEach{item->
                NavigationBarItem(
                    selected=current==item.route,
                    onClick={nav.navigate(item.route){launchSingleTop=true;restoreState=true}},
                    icon={Icon(item.icon,null)},
                    label={Text(item.label)},
                )
            }
        }
    }){paddingValues->
        NavHost(nav,startDestination="drive",modifier=Modifier.padding(paddingValues)){
            composable("drive"){
                DashboardScreen(drive,reference,summary,projection,selected,::startWithPermission,vm::stopTracking)
            }
            composable("fuel"){FuelScreen(fuel,vm::addFuel)}
            composable("garage"){TemplateGarageScreen(templates,vm::selectTemplate,vm::favoriteTemplate,vm::duplicateTemplate)}
            composable("calibration"){CalibrationScreen(vm)}
        }
    }
}
