package com.eddyvn.laixehieuqua.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eddyvn.laixehieuqua.camera.SpeedOcrAnalyzer

@Composable
fun CalibrationScreen(vm:MainViewModel){
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current
    val drive by vm.drive.collectAsStateWithLifecycle()
    val ocr by vm.lastOcr.collectAsStateWithLifecycle()
    val status by vm.calibrationStatus.collectAsStateWithLifecycle()
    val calibration by vm.calibration.collectAsStateWithLifecycle()
    val instrument by vm.vehicleInstrument.collectAsStateWithLifecycle()
    var odometerText by remember{mutableStateOf("")}

    Column(Modifier.fillMaxSize().padding(16.dp)){
        Text("Vehicle setup",style=MaterialTheme.typography.headlineSmall)

        if(instrument.setupComplete){
            Text("Setup đã hoàn tất. Camera không được dùng khi chạy xe.",modifier=Modifier.padding(top=8.dp))
            Text("Drive dùng GPS + bảng hiệu chỉnh đã học để mô phỏng đồng hồ xe.",modifier=Modifier.padding(top=4.dp))
            Text("ODO gốc: "+(instrument.odometerBaseKm?.let{"%.1f km".format(it)}?:"—"),modifier=Modifier.padding(top=12.dp))
            Button(vm::resetVehicleSetup,modifier=Modifier.padding(top=12.dp)){Text("SETUP LẠI")}
            return@Column
        }

        Text(
            "Chỉ dùng camera trong bước này để học chênh lệch giữa GPS và đồng hồ thật. Khi đủ mẫu, nhập ODO hiện tại và hoàn tất.",
            modifier=Modifier.padding(top=8.dp),
        )

        var granted by remember{
            mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
        }
        val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted=it}

        if(!granted){
            Button({launcher.launch(Manifest.permission.CAMERA)},modifier=Modifier.padding(top=12.dp)){Text("CHO PHÉP CAMERA")}
        }else{
            val analyzer=remember{SpeedOcrAnalyzer(vm::onOcrSpeed)}
            var provider by remember{mutableStateOf<ProcessCameraProvider?>(null)}
            AndroidView(
                factory={ctx->
                    PreviewView(ctx).also{view->
                        val future=ProcessCameraProvider.getInstance(ctx)
                        future.addListener({
                            val cameraProvider=future.get()
                            provider=cameraProvider
                            val preview=Preview.Builder().build().also{it.surfaceProvider=view.surfaceProvider}
                            val analysis=ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx),analyzer)
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycle,CameraSelector.DEFAULT_BACK_CAMERA,preview,analysis)
                        },ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier=Modifier.fillMaxWidth().height(320.dp).padding(top=12.dp),
            )
            DisposableEffect(Unit){onDispose{provider?.unbindAll()}}
        }

        Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.SpaceBetween){
            Text("GPS: %.1f km/h".format(drive.trueSpeedKmh))
            Text("Đồng hồ xe: "+(ocr?.toString()?:"—")+" km/h")
        }
        Text("Mẫu đã học: "+(calibration?.points?.size?:0),modifier=Modifier.padding(top=6.dp))
        Text(status,modifier=Modifier.padding(top=4.dp))

        OutlinedTextField(
            value=odometerText,
            onValueChange={odometerText=it.filter{ch->ch.isDigit()||ch=='.'}},
            label={Text("ODO hiện tại của xe (km)")},
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
            modifier=Modifier.fillMaxWidth().padding(top=12.dp),
        )
        val odo=odometerText.toDoubleOrNull()
        Button(
            onClick={odo?.let(vm::completeVehicleSetup)},
            enabled=odo!=null&&(calibration?.points?.size?:0)>=2,
            modifier=Modifier.fillMaxWidth().padding(top=12.dp),
        ){Text("HOÀN TẤT SETUP · TẮT CAMERA")}
        Text("Nên thu mẫu ở ít nhất 2–3 vùng tốc độ cách nhau rõ rệt trước khi hoàn tất.",modifier=Modifier.padding(top=8.dp))
    }
}
