package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.domain.FuelSummary
import java.util.Locale

@Composable
fun OdoEditDialog(
    currentOdoKm:Double?,
    onDismiss:()->Unit,
    onSave:(Double)->Unit,
){
    var text by remember(currentOdoKm){
        mutableStateOf(currentOdoKm?.let{"%.1f".format(Locale.US,it)}?:"")
    }
    val value=text.toDoubleOrNull()

    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Sửa ODO")},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                Text("Nhập đúng số ODO đang hiển thị trên xe. Từ mốc này app sẽ cộng quãng đường GPS hợp lệ.")
                OutlinedTextField(
                    value=text,
                    onValueChange={text=it.filter{ch->ch.isDigit()||ch=='.'}},
                    label={Text("ODO hiện tại (km)")},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                    singleLine=true,
                )
            }
        },
        confirmButton={
            TextButton(
                onClick={value?.let{onSave(it);onDismiss()}},
                enabled=value!=null&&value>=0.0,
            ){Text("LƯU")}
        },
        dismissButton={TextButton(onClick=onDismiss){Text("HỦY")}},
    )
}

@Composable
fun FuelQuickAddDialog(
    currentOdoKm:Double?,
    marketPricePerLiter:Double?,
    summary:FuelSummary,
    onRefreshPrice:()->Unit,
    onDismiss:()->Unit,
    onSave:(odometerKm:Double,unitPrice:Double,amount:Double,isFull:Boolean,tankCapacity:Double?)->Unit,
){
    var odoText by remember(currentOdoKm){
        mutableStateOf(currentOdoKm?.let{"%.1f".format(Locale.US,it)}?:"")
    }
    var priceText by remember(marketPricePerLiter){
        mutableStateOf(marketPricePerLiter?.let{"%.0f".format(Locale.US,it)}?:"")
    }
    var amountText by remember{mutableStateOf("")}
    var full by remember{mutableStateOf(true)}
    var capacityText by remember(summary.tankCapacityLiters){
        mutableStateOf(summary.tankCapacityLiters?.let{"%.1f".format(Locale.US,it)}?:"")
    }

    val odo=odoText.toDoubleOrNull()
    val unitPrice=priceText.toDoubleOrNull()
    val amount=amountText.toDoubleOrNull()
    val liters=if(unitPrice!=null&&amount!=null&&unitPrice>0.0&&amount>0.0)amount/unitPrice else null
    val capacity=capacityText.toDoubleOrNull()
    val currentRemaining=summary.estimatedRemainingLiters
    val afterFill=when{
        liters==null->null
        full&&capacity!=null->capacity
        currentRemaining!=null&&capacity!=null->(currentRemaining+liters).coerceAtMost(capacity)
        currentRemaining!=null->currentRemaining+liters
        else->null
    }

    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Đổ xăng nhanh")},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                OutlinedTextField(
                    value=odoText,
                    onValueChange={odoText=it.filter{ch->ch.isDigit()||ch=='.'}},
                    label={Text("ODO hiện tại (km)")},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                    singleLine=true,
                )
                OutlinedTextField(
                    value=priceText,
                    onValueChange={priceText=it.filter{ch->ch.isDigit()||ch=='.'}},
                    label={Text("Đơn giá xăng (₫/lít)")},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                    singleLine=true,
                    trailingIcon={
                        TextButton(onClick=onRefreshPrice){Text("Giá mới")}
                    },
                )
                OutlinedTextField(
                    value=amountText,
                    onValueChange={amountText=it.filter(Char::isDigit)},
                    label={Text("Số tiền đã đổ (₫)")},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                    singleLine=true,
                )

                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                    Text("Lượng xăng",fontWeight=FontWeight.Medium)
                    Text(liters?.let{"%.2f L".format(Locale.US,it)}?:"—",fontWeight=FontWeight.Bold)
                }

                Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
                    Checkbox(checked=full,onCheckedChange={full=it})
                    Text("Đổ đầy bình")
                }

                OutlinedTextField(
                    value=capacityText,
                    onValueChange={capacityText=it.filter{ch->ch.isDigit()||ch=='.'}},
                    label={Text("Dung tích bình (L) · chỉ cần nhập 1 lần")},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                    singleLine=true,
                )

                afterFill?.let{
                    Text("Sau khi đổ: khoảng %.2f L trong bình".format(Locale.US,it))
                }
                if(summary.averageKmPerLiter==null){
                    Text(
                        "App sẽ học mức tiêu thụ sau các chu kỳ đổ đầy → đổ đầy. Khi đủ dữ liệu sẽ tự tính xăng còn lại và km còn lại.",
                        style=MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton={
            val enabled=odo!=null&&odo>=0.0&&unitPrice!=null&&unitPrice>0.0&&amount!=null&&amount>0.0&&liters!=null
            TextButton(
                onClick={
                    if(enabled){
                        onSave(odo!!,unitPrice!!,amount!!,full,capacity)
                        onDismiss()
                    }
                },
                enabled=enabled,
            ){Text("GHI NHẬN")}
        },
        dismissButton={TextButton(onClick=onDismiss){Text("HỦY")}},
    )
}

@Composable
fun FuelEstimateStrip(
    summary:FuelSummary,
    modifier:Modifier=Modifier,
){
    val avg=summary.averageKmPerLiter
    val remaining=summary.estimatedRemainingLiters
    val range=summary.estimatedRangeKm

    Card(modifier){
        Row(
            Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),
            horizontalArrangement=Arrangement.SpaceEvenly,
        ){
            FuelMiniMetric(
                value=remaining?.let{"%.1f L".format(Locale.US,it)}?:"—",
                label="XĂNG CÒN",
            )
            FuelMiniMetric(
                value=range?.let{"%.0f km".format(Locale.US,it)}?:"—",
                label="KM CÒN",
            )
            FuelMiniMetric(
                value=summary.averageLitersPer100Km?.let{"%.2f".format(Locale.US,it)}?:"—",
                label="L/100 KM",
            )
            FuelMiniMetric(
                value=avg?.let{"%.1f".format(Locale.US,it)}?:"—",
                label="KM/L",
            )
        }
        if(avg==null){
            Text(
                "Đang học · cần ít nhất 1 chu kỳ đổ đầy hoàn chỉnh",
                style=MaterialTheme.typography.labelSmall,
                modifier=Modifier.padding(start=12.dp,end=12.dp,bottom=7.dp),
            )
        }
    }
}

@Composable
private fun FuelMiniMetric(value:String,label:String){
    Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally){
        Text(value,fontWeight=FontWeight.Bold)
        Text(label,style=MaterialTheme.typography.labelSmall)
    }
}
