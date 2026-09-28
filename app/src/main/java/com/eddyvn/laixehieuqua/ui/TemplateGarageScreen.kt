package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity

@Composable
fun TemplateGarageScreen(templates:List<DashboardTemplateEntity>,onSelect:(String)->Unit,onFavorite:(DashboardTemplateEntity)->Unit,onDuplicate:(DashboardTemplateEntity)->Unit){
    Column(Modifier.fillMaxSize().padding(18.dp)){
        Text("Theme Garage",style=MaterialTheme.typography.headlineSmall)
        Text("All dashboard styles remain available. Clone a built-in style before customizing.")
        LazyColumn(Modifier.padding(top=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            items(templates,key={it.id}){item->
                val sub=item.category+" · v"+item.version+(if(item.selected)" · ACTIVE" else "")
                ListItem(
                    headlineContent={Text(item.name)},supportingContent={Text(sub)},
                    trailingContent={Row{
                        TextButton({onFavorite(item)}){Text(if(item.favorite)"★" else "☆")}
                        TextButton({onDuplicate(item)}){Text("COPY")}
                        Button({onSelect(item.id)},enabled=!item.selected){Text("USE")}
                    }}
                );HorizontalDivider()
            }
        }
    }
}
