package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CalibrationScreen(vm:MainViewModel){
    val instrument by vm.vehicleInstrument.collectAsStateWithLifecycle()
    val totalTracked by vm.totalTrackedDistanceKm.collectAsStateWithLifecycle()
    val liveOdo by vm.vehicleOdometerKm.collectAsStateWithLifecycle()
    var odometerText by remember(instrument.odometerBaseKm){
        mutableStateOf(instrument.odometerBaseKm?.let{"%.1f".format(it)}?:"")
    }

    Column(Modifier.fillMaxSize().padding(20.dp)){
        Text("ODO setup",style=MaterialTheme.typography.headlineSmall)
        Text(
            "ODO được đặt thủ công, độc lập với camera và đồng hồ xe. Sau khi đặt, app chỉ cộng quãng đường GPS hợp lệ do chính app ghi nhận.",
            modifier=Modifier.padding(top=10.dp),
        )

        Text(
            "ODO hiện tại: "+(liveOdo?.let{"%.1f km".format(it)}?:"chưa đặt"),
            style=MaterialTheme.typography.titleMedium,
            modifier=Modifier.padding(top=18.dp),
        )
        Text(
            "Tổng quãng đường app đã ghi: %.1f km".format(totalTracked),
            modifier=Modifier.padding(top=6.dp),
        )

        OutlinedTextField(
            value=odometerText,
            onValueChange={odometerText=it.filter{ch->ch.isDigit()||ch=='.'}},
            label={Text("Nhập ODO thực tế trên xe (km)")},
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
            modifier=Modifier.fillMaxWidth().padding(top=18.dp),
        )

        val odo=odometerText.toDoubleOrNull()
        Button(
            onClick={odo?.let(vm::setVehicleOdometer)},
            enabled=odo!=null&&odo>=0.0,
            modifier=Modifier.fillMaxWidth().padding(top=12.dp),
        ){
            Text(if(instrument.setupComplete)"CẬP NHẬT ODO" else "ĐẶT ODO")
        }

        if(instrument.setupComplete){
            OutlinedButton(
                onClick=vm::clearVehicleOdometer,
                modifier=Modifier.fillMaxWidth().padding(top=8.dp),
            ){Text("XÓA MỐC ODO")}
        }

        Text(
            "Không còn cần camera sync để hiệu chỉnh vận tốc. Vận tốc Drive dùng GPS + sensor fusion; khi đứng yên sẽ khóa về 0 để tránh vận tốc và quãng đường ảo.",
            modifier=Modifier.padding(top=18.dp),
        )
    }
}
