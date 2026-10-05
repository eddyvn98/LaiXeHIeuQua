package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.data.FuelEntryEntity
import com.eddyvn.laixehieuqua.data.FuelMarketPriceState
import com.eddyvn.laixehieuqua.domain.FuelSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val vietnamLocale=Locale.forLanguageTag("vi-VN")

@Composable
fun FuelScreen(
    entries:List<FuelEntryEntity>,
    summary:FuelSummary,
    marketPrice:FuelMarketPriceState,
    currentOdoKm:Double?,
    onRefreshPrice:(Boolean)->Unit,
    onRecord:(Double,Double,Double,Boolean,Double?)->Unit,
    onStartCycle:(Double)->Unit,
    onEditOdo:(Long,Double)->Unit,
    onDelete:(Long)->Unit,
    onResetAll:()->Unit,
){
    var odoText by remember(currentOdoKm){mutableStateOf(currentOdoKm?.let{"%.1f".format(Locale.US,it)}?:"")}
    var amountText by remember{mutableStateOf("")}
    var priceText by remember{mutableStateOf("")}
    var priceEdited by remember{mutableStateOf(false)}
    var full by remember{mutableStateOf(true)}
    var capacityText by remember(summary.tankCapacityLiters){
        mutableStateOf(summary.tankCapacityLiters?.let{"%.1f".format(Locale.US,it)}?:"")
    }
    var showStartDialog by remember{mutableStateOf(false)}
    var editEntry by remember{mutableStateOf<FuelEntryEntity?>(null)}
    var deleteEntry by remember{mutableStateOf<FuelEntryEntity?>(null)}
    var showResetConfirm by remember{mutableStateOf(false)}
    val marketPricePerLiter=marketPrice.price?.pricePerLiter

    LaunchedEffect(Unit){onRefreshPrice(false)}
    LaunchedEffect(marketPricePerLiter){
        if(!priceEdited&&marketPricePerLiter!=null)priceText=marketPricePerLiter.toString()
    }

    val odo=odoText.toDoubleOrNull()
    val amount=amountText.toDoubleOrNull()
    val price=priceText.toDoubleOrNull()
    val liters=if(amount!=null&&price!=null&&price>0)amount/price else null
    val capacity=capacityText.toDoubleOrNull()
    val latestCycle=summary.cycles.lastOrNull()

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal=16.dp),
        verticalArrangement=Arrangement.spacedBy(10.dp),
        contentPadding=PaddingValues(top=12.dp,bottom=24.dp),
    ){
        item{
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text("Đổ xăng",style=MaterialTheme.typography.headlineSmall)
                TextButton(onClick={showResetConfirm=true},enabled=entries.isNotEmpty()){Text("RESET")}
            }
        }
        item{
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    Text("Chu kỳ nhiên liệu",style=MaterialTheme.typography.titleMedium)
                    Text("%.1f km từ mốc đổ đầy gần nhất".format(summary.currentCycleKm))
                    if(entries.isEmpty()){
                        Text("Chưa có mốc bắt đầu. Nhập ODO ban đầu để bắt đầu chu kỳ trước lần đổ xăng tiếp theo.",style=MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick={showStartDialog=true},modifier=Modifier.fillMaxWidth()){Text("TẠO MỐC BẮT ĐẦU")}
                    }else{
                        Text("Có thể sửa ODO hoặc xóa từng bản ghi ở phần lịch sử nếu nhập sai.",style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item{
            OutlinedTextField(
                value=odoText,
                onValueChange={odoText=decimalFilter(it)},
                label={Text("ODO tại thời điểm đổ xăng (km)")},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                singleLine=true,
                modifier=Modifier.fillMaxWidth(),
            )
        }
        item{
            OutlinedTextField(
                value=amountText,
                onValueChange={amountText=it.filter(Char::isDigit)},
                label={Text("Số tiền đã đổ (₫)")},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                singleLine=true,
                modifier=Modifier.fillMaxWidth(),
            )
        }
        item{
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                        Column{
                            Text("Giá RON 95-V",style=MaterialTheme.typography.titleSmall)
                            Text(marketPrice.price?.effectiveDate?.let{"Giá kỳ $it"}?:"Giá tham khảo",style=MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick={priceEdited=false;onRefreshPrice(true)},enabled=!marketPrice.loading){
                            Text(if(marketPrice.loading)"Đang tải…" else "Cập nhật")
                        }
                    }
                    OutlinedTextField(
                        value=priceText,
                        onValueChange={priceText=decimalFilter(it);priceEdited=true},
                        label={Text("Giá mỗi lít (₫)")},
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                        singleLine=true,
                        modifier=Modifier.fillMaxWidth(),
                    )
                    marketPrice.error?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)}
                }
            }
        }
        item{
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text("Lượng xăng ước tính")
                Text(liters?.takeIf{it>0}?.let{"%.2f L".format(it)}?:"—",style=MaterialTheme.typography.titleLarge)
            }
        }
        item{
            Row{
                Checkbox(checked=full,onCheckedChange={full=it})
                Column{
                    Text("Đổ đầy bình")
                    Text("Bật để đóng/mở chu kỳ tính km/L.",style=MaterialTheme.typography.bodySmall)
                }
            }
        }
        item{
            OutlinedTextField(
                value=capacityText,
                onValueChange={capacityText=decimalFilter(it)},
                label={Text("Dung tích bình (L) · tùy chọn")},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                singleLine=true,
                modifier=Modifier.fillMaxWidth(),
            )
        }
        item{
            val enabled=odo!=null&&odo>=0&&amount!=null&&amount>0&&price!=null&&price>0&&liters!=null
            Button(
                onClick={
                    if(enabled){
                        onRecord(odo!!,price!!,amount!!,full,capacity)
                        amountText=""
                    }
                },
                enabled=enabled,
                modifier=Modifier.fillMaxWidth().height(52.dp),
            ){Text("GHI NHẬN ĐỔ XĂNG")}
        }
        item{
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text("Mức tiêu thụ",style=MaterialTheme.typography.titleMedium)
                    if(latestCycle!=null){
                        Text("%.1f km/L".format(latestCycle.kmPerLiter),style=MaterialTheme.typography.headlineSmall)
                        Text("%.1f km ÷ %.2f L".format(latestCycle.distanceKm,latestCycle.liters))
                    }else{
                        Text("Cần hai mốc đổ đầy (hoặc mốc bắt đầu + lần đổ đầy) để tính chu kỳ.")
                    }
                    summary.averageLitersPer100Km?.let{Text("Trung bình %.2f L/100 km".format(it),style=MaterialTheme.typography.bodySmall)}
                }
            }
        }
        item{Text("Lịch sử",style=MaterialTheme.typography.titleMedium)}
        items(entries,key={it.id}){entry->
            val isStart=entry.isFull&&entry.liters==0.0&&entry.totalPrice==null
            val date=SimpleDateFormat("dd/MM/yyyy HH:mm",vietnamLocale).format(Date(entry.timestampMs))
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text(
                        if(isStart)"Mốc bắt đầu chu kỳ"
                        else "%.2f L%s".format(entry.liters,entry.totalPrice?.let{" · %,.0f ₫".format(vietnamLocale,it)}?:""),
                        style=MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "$date · ODO ${entry.vehicleOdometerKm?.let{"%.1f km".format(it)}?:"—"}" +
                            if(isStart)"" else " · ${if(entry.isFull)"Đổ đầy" else "Đổ thêm"}",
                        style=MaterialTheme.typography.bodySmall,
                    )
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){
                        TextButton(onClick={editEntry=entry}){Text("SỬA ODO")}
                        TextButton(onClick={deleteEntry=entry}){Text("XÓA")}
                    }
                }
            }
        }
    }

    if(showStartDialog){
        OdoValueDialog(
            title="Mốc bắt đầu chu kỳ",
            initial=currentOdoKm,
            confirmLabel="TẠO MỐC",
            onDismiss={showStartDialog=false},
            onConfirm={onStartCycle(it);showStartDialog=false},
        )
    }
    editEntry?.let{entry->
        OdoValueDialog(
            title="Sửa ODO bản ghi",
            initial=entry.vehicleOdometerKm,
            confirmLabel="LƯU",
            onDismiss={editEntry=null},
            onConfirm={onEditOdo(entry.id,it);editEntry=null},
        )
    }
    deleteEntry?.let{entry->
        AlertDialog(
            onDismissRequest={deleteEntry=null},
            title={Text("Xóa bản ghi?")},
            text={Text("Chu kỳ và mức tiêu thụ sẽ được tính lại sau khi xóa.")},
            confirmButton={TextButton(onClick={onDelete(entry.id);deleteEntry=null}){Text("XÓA")}},
            dismissButton={TextButton(onClick={deleteEntry=null}){Text("HỦY")}},
        )
    }
    if(showResetConfirm){
        AlertDialog(
            onDismissRequest={showResetConfirm=false},
            title={Text("Reset dữ liệu đổ xăng?")},
            text={Text("Xóa toàn bộ lịch sử nhiên liệu để nhập lại từ đầu. Dữ liệu hành trình GPS không bị xóa.")},
            confirmButton={TextButton(onClick={onResetAll();showResetConfirm=false}){Text("RESET")}},
            dismissButton={TextButton(onClick={showResetConfirm=false}){Text("HỦY")}},
        )
    }
}

@Composable
private fun OdoValueDialog(
    title:String,
    initial:Double?,
    confirmLabel:String,
    onDismiss:()->Unit,
    onConfirm:(Double)->Unit,
){
    var text by remember(initial){mutableStateOf(initial?.let{"%.1f".format(Locale.US,it)}?:"")}
    val value=text.toDoubleOrNull()
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text(title)},
        text={
            OutlinedTextField(
                value=text,
                onValueChange={text=decimalFilter(it)},
                label={Text("ODO (km)")},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                singleLine=true,
            )
        },
        confirmButton={
            TextButton(onClick={value?.let{onConfirm(it)}},enabled=value!=null&&value>=0){Text(confirmLabel)}
        },
        dismissButton={TextButton(onClick=onDismiss){Text("HỦY")}},
    )
}

private fun decimalFilter(value:String)=value.filter{it.isDigit()||it=='.'}
