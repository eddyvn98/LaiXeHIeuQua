package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    onRefreshPrice:(Boolean)->Unit,
    onAdd:(Double,Double?,Boolean,Double?)->Unit,
){
    var amountText by remember{mutableStateOf("")}
    var priceText by remember{mutableStateOf("")}
    var priceEdited by remember{mutableStateOf(false)}
    var full by remember{mutableStateOf(true)}
    val marketPricePerLiter=marketPrice.price?.pricePerLiter

    LaunchedEffect(Unit){onRefreshPrice(false)}
    LaunchedEffect(marketPricePerLiter){
        if(!priceEdited&&marketPricePerLiter!=null)priceText=marketPricePerLiter.toString()
    }

    val amount=amountText.toLongOrNull()?.toDouble()
    val price=priceText.toDoubleOrNull()
    val liters=if(amount!=null&&price!=null&&price>0)amount/price else null
    val latestCycle=summary.cycles.lastOrNull()

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal=16.dp),
        verticalArrangement=Arrangement.spacedBy(10.dp),
        contentPadding=PaddingValues(top=12.dp,bottom=20.dp),
    ){
        item{Text("Đổ xăng",style=MaterialTheme.typography.headlineSmall)}
        item{
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text("Quãng đường đã ghi nhận",style=MaterialTheme.typography.titleSmall)
                    Text("%.1f km từ lần đổ đầy gần nhất".format(summary.currentCycleKm))
                    Text("Quãng đường tính từ các chuyến GPS đã lưu.",style=MaterialTheme.typography.bodySmall)
                }
            }
        }
        item{
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text("Ước tính nhiên liệu",style=MaterialTheme.typography.titleMedium)
                    if(summary.averageKmPerLiter!=null){
                        Text(
                            "Còn khoảng %.1f L · ~%.0f km".format(
                                summary.estimatedRemainingLiters?:0.0,
                                summary.estimatedRangeKm?:0.0,
                            ),
                            style=MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            "Trung bình %.2f L/100 km · %.1f km/L · %d chu kỳ".format(
                                summary.averageLitersPer100Km?:0.0,
                                summary.averageKmPerLiter,
                                summary.learnedCycleCount,
                            ),
                            style=MaterialTheme.typography.bodySmall,
                        )
                    }else{
                        Text("Đang học. Cần ít nhất một chu kỳ đổ đầy hoàn chỉnh để ước tính xăng còn lại và quãng đường còn chạy.",style=MaterialTheme.typography.bodyMedium)
                    }
                    if(summary.tankCapacityLiters==null){
                        Text("Nhập dung tích bình ở lần ghi nhận đổ xăng tiếp theo.",style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
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
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){
                            Text("Giá RON 95-V · TP.HCM",style=MaterialTheme.typography.titleSmall)
                            val effective=marketPrice.price?.effectiveDate
                            Text(
                                if(effective!=null)"Giá kỳ $effective · VietFuel / Petrolimex Sài Gòn"
                                else "Giá tham khảo · VietFuel / Petrolimex Sài Gòn",
                                style=MaterialTheme.typography.bodySmall,
                            )
                        }
                        TextButton(
                            onClick={priceEdited=false;onRefreshPrice(true)},
                            enabled=!marketPrice.loading,
                        ){
                            Text(if(marketPrice.loading)"Đang tải…" else "Cập nhật")
                        }
                    }
                    OutlinedTextField(
                        value=priceText,
                        onValueChange={priceText=it.filter{ch->ch.isDigit()||ch=='.'};priceEdited=true},
                        label={Text("Giá mỗi lít (₫) · có thể chỉnh")},
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                        singleLine=true,
                        modifier=Modifier.fillMaxWidth(),
                    )
                    marketPrice.error?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)}
                }
            }
        }
        item{
            Card(Modifier.fillMaxWidth()){
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically,
                ){
                    Text("Lượng xăng ước tính",style=MaterialTheme.typography.titleSmall)
                    Text(liters?.takeIf{it>0}?.let{"%.2f lít".format(it)}?:"—",style=MaterialTheme.typography.titleLarge)
                }
            }
        }
        item{
            Row(verticalAlignment=Alignment.CenterVertically){
                Checkbox(checked=full,onCheckedChange={full=it})
                Column{
                    Text("Đổ đầy bình")
                    Text("Bật để tính km/L giữa các lần đổ đầy.",style=MaterialTheme.typography.bodySmall)
                }
            }
        }
        item{
            Button(
                onClick={
                    val paid=amount
                    val volume=liters
                    if(paid!=null&&volume!=null&&paid>0&&volume>0){
                        onAdd(volume,paid,full,null)
                        amountText=""
                    }
                },
                enabled=amount!=null&&amount>0&&liters!=null&&liters>0,
                modifier=Modifier.fillMaxWidth().height(52.dp),
            ){Text("GHI NHẬN ĐỔ XĂNG")}
        }
        item{
            Card(Modifier.fillMaxWidth()){
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text("Mức tiêu thụ",style=MaterialTheme.typography.titleMedium)
                    if(latestCycle!=null){
                        Text("%.1f km/L".format(latestCycle.kmPerLiter),style=MaterialTheme.typography.headlineSmall)
                        Text(
                            "%.1f km ÷ %.2f lít · giữa hai lần đổ đầy".format(latestCycle.distanceKm,latestCycle.liters),
                            style=MaterialTheme.typography.bodySmall,
                        )
                    }else if(entries.any{it.isFull}){
                        Text("Đã có mốc đầu. Km/L sẽ được tính sau lần đổ đầy tiếp theo.",style=MaterialTheme.typography.bodyMedium)
                    }else{
                        Text("Km/L bắt đầu được tính từ lần đổ đầy thứ hai.",style=MaterialTheme.typography.bodyMedium)
                    }
                    summary.bestCycle?.let{Text("Tốt nhất: %.1f km/L".format(it.kmPerLiter),style=MaterialTheme.typography.bodySmall)}
                }
            }
        }
        item{Text("Lịch sử",style=MaterialTheme.typography.titleMedium)}
        items(entries,key={it.id}){entry->
            val date=SimpleDateFormat("dd/MM/yyyy HH:mm",vietnamLocale).format(Date(entry.timestampMs))
            val paid=entry.totalPrice?.let{" · %,.0f ₫".format(vietnamLocale,it)}?:""
            val unit=entry.totalPrice?.takeIf{entry.liters>0}?.div(entry.liters)
                ?.let{" · %,.0f ₫/lít".format(vietnamLocale,it)}?:""
            ListItem(
                headlineContent={Text("%.2f lít$paid".format(entry.liters))},
                supportingContent={Text("$date · ${if(entry.isFull)"Đổ đầy" else "Đổ thêm"}$unit")},
            )
        }
    }
}
