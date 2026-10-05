package com.eddyvn.laixehieuqua.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TemplateRepository(private val dao:AppDao){
    val templates:Flow<List<DashboardTemplateEntity>> = dao.templatesFlow()

    suspend fun ensureBuiltIns(){
        val builtIns=listOf(
            DashboardTemplateEntity("tft-sport","TFT Sport Bike","Sport",1,true,true,true,"TFT_SPORT",0xFF5CE7FF,0xFF68FFB2),
            DashboardTemplateEntity("premium-segmented","Premium Segmented","Premium",1,true,false,false,"PREMIUM_SEGMENTED",0xFFDFF7FF,0xFFA8FFD9),
            DashboardTemplateEntity("sport-cockpit-v1","Sport Cockpit","Sport",1,true,true,false,"SPORT_COCKPIT_V1",0xFFFF5D3A,0xFFFFC857),
        )
        builtIns.forEach{item->
            if(dao.templateById(item.id)==null)dao.upsertTemplate(item)
        }
    }

    suspend fun select(id:String)=dao.selectTemplate(id)
    suspend fun favorite(item:DashboardTemplateEntity)=dao.setTemplateFavorite(item.id,!item.favorite)
    suspend fun duplicate(item:DashboardTemplateEntity)=dao.upsertTemplate(
        item.copy(
            id=UUID.randomUUID().toString(),
            name=item.name+" Copy",
            builtIn=false,
            selected=false,
            version=1,
        )
    )
}
