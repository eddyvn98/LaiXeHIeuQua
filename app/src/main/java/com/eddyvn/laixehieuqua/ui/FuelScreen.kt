package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.data.FuelEntryEntity

@Composable
fun FuelScreen(entries:List<FuelEntryEntity>,onAdd:(Double,Double?,Boolean,Double?)->Unit){
    var liters by remember{mutableStateOf("")};var price by remember{mutableStateOf("")}
    var odo by remember{mutableStateOf("")};var full by remember{mutableStateOf(true)}
    Column(Modifier.fillMaxSize().padding(18.dp)){
        Text("Fuel cycle",style=MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedTextField(liters,{liters=it},label={Text("Liters")},modifier=Modifier.weight(1f))
            OutlinedTextField(odo,{odo=it},label={Text("Odometer km")},modifier=Modifier.weight(1f))
        }
        OutlinedTextField(price,{price=it},label={Text("Total price (optional)")},modifier=Modifier.fillMaxWidth())
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
            Checkbox(full,{full=it});Text("Filled full tank");Spacer(Modifier.weight(1f))
            Button(onClick={liters.toDoubleOrNull()?.let{onAdd(it,price.toDoubleOrNull(),full,odo.toDoubleOrNull());liters="";price="";odo=""}}){Text("SAVE")}
        }
        HorizontalDivider(Modifier.padding(vertical=10.dp))
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
            items(entries,key={it.id}){e->
                val sub=if(e.isFull)"FULL · odo "+(e.vehicleOdometerKm?.toString()?:"—") else "Partial fill"
                ListItem(headlineContent={Text("%.2f L".format(e.liters))},supportingContent={Text(sub)})
            }
        }
    }
}
