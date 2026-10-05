package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.data.WeatherState
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.domain.FuelSummary
import com.eddyvn.laixehieuqua.tracking.TrackingStatus
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun SportCockpitDashboard(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    sessionElapsedMs:Long,
    fuelSummary:FuelSummary,
    weather:WeatherState,
    accent:Color,
    secondary:Color,
    onRefreshWeather:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    onResetTrip:()->Unit,
    onStart:()->Unit,
    onStop:()->Unit,
){
    val active=trackingStatus in setOf(
        TrackingStatus.STARTING,
        TrackingStatus.WAITING_FOR_GPS,
        TrackingStatus.LIVE,
        TrackingStatus.STOPPING,
    )
    val status=when(trackingStatus){
        TrackingStatus.IDLE->"READY"
        TrackingStatus.REQUESTING_PERMISSION->"PERMISSION"
        TrackingStatus.PERMISSION_REQUIRED->"GPS REQUIRED"
        TrackingStatus.STARTING->"STARTING"
        TrackingStatus.WAITING_FOR_GPS->"GPS SEARCH"
        TrackingStatus.LIVE->"● LIVE"
        TrackingStatus.STOPPING->"STOPPING"
        TrackingStatus.ERROR->"ERROR"
    }
    val action=when(trackingStatus){
        TrackingStatus.STARTING->"ĐANG BẮT ĐẦU"
        TrackingStatus.STOPPING->"ĐANG DỪNG"
        else->if(active)"CHẠM ĐỂ DỪNG" else "CHẠM ĐỂ BẮT ĐẦU"
    }
    val actionEnabled=trackingStatus !in setOf(
        TrackingStatus.REQUESTING_PERMISSION,
        TrackingStatus.STOPPING,
    )
    val actionClick=if(active)onStop else onStart
    val elapsed=formatSportDuration(sessionElapsedMs)

    BoxWithConstraints(Modifier.fillMaxSize()){
        if(maxWidth>maxHeight){
            SportCockpitLandscape(
                drive=drive,
                vehicleOdometerKm=vehicleOdometerKm,
                tripMeterKm=tripMeterKm,
                elapsed=elapsed,
                fuelSummary=fuelSummary,
                weather=weather,
                status=status,
                action=action,
                actionEnabled=actionEnabled,
                accent=accent,
                secondary=secondary,
                onRefreshWeather=onRefreshWeather,
                onOdoClick=onOdoClick,
                onFuelClick=onFuelClick,
                onResetTrip=onResetTrip,
                onGaugeClick=actionClick,
            )
        }else{
            SportCockpitPortrait(
                drive=drive,
                vehicleOdometerKm=vehicleOdometerKm,
                tripMeterKm=tripMeterKm,
                elapsed=elapsed,
                fuelSummary=fuelSummary,
                weather=weather,
                status=status,
                action=action,
                actionEnabled=actionEnabled,
                accent=accent,
                secondary=secondary,
                onRefreshWeather=onRefreshWeather,
                onOdoClick=onOdoClick,
                onFuelClick=onFuelClick,
                onResetTrip=onResetTrip,
                onGaugeClick=actionClick,
            )
        }
    }
}

@Composable
private fun SportCockpitPortrait(
    drive:DriveSnapshot,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    elapsed:String,
    fuelSummary:FuelSummary,
    weather:WeatherState,
    status:String,
    action:String,
    actionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    onRefreshWeather:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    onResetTrip:()->Unit,
    onGaugeClick:()->Unit,
){
    Column(
        Modifier.fillMaxSize().padding(horizontal=12.dp,vertical=8.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(7.dp),
    ){
        SportStatusBar(weather,status,accent,onRefreshWeather)
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)){
            val size=minOf(maxWidth,maxHeight)
            SportSpeedCluster(
                speedKmh=drive.displaySpeedKmh,
                averageSpeedKmh=drive.averageSpeedKmh,
                ecoTargetKmh=drive.ecoTargetKmh,
                accent=accent,
                secondary=secondary,
                actionLabel=action,
                enabled=actionEnabled,
                modifier=Modifier.size(size).align(Alignment.Center),
                onClick=onGaugeClick,
            )
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            SportTelemetryPod(
                value=String.format(Locale.US,"%+.1f",drive.accelerationMs2),
                label="ACCEL m/s²",
                accent=accent,
                modifier=Modifier.weight(1f),
            )
            SportTelemetryPod(
                value=String.format(Locale.US,"%+.1f°",drive.leanDeg),
                label=if(abs(drive.leanDeg)<.7)"LEVEL" else "SLOPE",
                accent=secondary,
                modifier=Modifier.weight(1f),
            )
            SportTelemetryPod(
                value=drive.efficiencyHistory.lastOrNull()?.toInt()?.toString()?:"—",
                label="ECO SCORE",
                accent=accent,
                modifier=Modifier.weight(1f),
            )
        }
        SportFuelStrip(fuelSummary,accent,Modifier.fillMaxWidth().clickable(onClick=onFuelClick))
        SportDataStrip(
            odo=vehicleOdometerKm,
            trip=tripMeterKm,
            elapsed=elapsed,
            accent=accent,
            modifier=Modifier.fillMaxWidth(),
            onOdoClick=onOdoClick,
            onResetTrip=onResetTrip,
        )
    }
}

@Composable
private fun SportCockpitLandscape(
    drive:DriveSnapshot,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    elapsed:String,
    fuelSummary:FuelSummary,
    weather:WeatherState,
    status:String,
    action:String,
    actionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    onRefreshWeather:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    onResetTrip:()->Unit,
    onGaugeClick:()->Unit,
){
    Column(
        Modifier.fillMaxSize().padding(horizontal=12.dp,vertical=7.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp),
    ){
        SportStatusBar(weather,status,accent,onRefreshWeather)
        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement=Arrangement.spacedBy(12.dp),
            verticalAlignment=Alignment.CenterVertically,
        ){
            BoxWithConstraints(Modifier.weight(1.05f).fillMaxHeight()){
                val size=minOf(maxWidth,maxHeight)
                SportSpeedCluster(
                    speedKmh=drive.displaySpeedKmh,
                    averageSpeedKmh=drive.averageSpeedKmh,
                    ecoTargetKmh=drive.ecoTargetKmh,
                    accent=accent,
                    secondary=secondary,
                    actionLabel=action,
                    enabled=actionEnabled,
                    modifier=Modifier.size(size).align(Alignment.Center),
                    onClick=onGaugeClick,
                )
            }
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement=Arrangement.SpaceEvenly,
            ){
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                    SportTelemetryPod(
                        value=String.format(Locale.US,"%+.1f",drive.accelerationMs2),
                        label="ACCEL",
                        accent=accent,
                        modifier=Modifier.weight(1f),
                    )
                    SportTelemetryPod(
                        value=String.format(Locale.US,"%+.1f°",drive.leanDeg),
                        label="SLOPE",
                        accent=secondary,
                        modifier=Modifier.weight(1f),
                    )
                    SportTelemetryPod(
                        value=drive.efficiencyHistory.lastOrNull()?.toInt()?.toString()?:"—",
                        label="ECO",
                        accent=accent,
                        modifier=Modifier.weight(1f),
                    )
                }
                SportFuelStrip(fuelSummary,accent,Modifier.fillMaxWidth().clickable(onClick=onFuelClick))
                SportDataStrip(
                    odo=vehicleOdometerKm,
                    trip=tripMeterKm,
                    elapsed=elapsed,
                    accent=accent,
                    modifier=Modifier.fillMaxWidth(),
                    onOdoClick=onOdoClick,
                    onResetTrip=onResetTrip,
                )
            }
        }
    }
}

private fun formatSportDuration(ms:Long):String{
    val total=ms.coerceAtLeast(0L)/1000L
    val h=total/3600L
    val m=(total%3600L)/60L
    return "%02d:%02d".format(h,m)
}
