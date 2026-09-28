package com.eddyvn.laixehieuqua.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TemplateRepository(private val dao:AppDao){
    val templates:Flow<List<DashboardTemplateEntity>> = dao.templatesFlow()
    suspend fun ensureBuiltIns(){
        if(dao.templateCount()>0)return
        dao.upsertTemplates(listOf(
            DashboardTemplateEntity("tft-sport","TFT Sport Bike","Sport",1,true,true,true,"TFT_SPORT",0xFF5CE7FF,0xFF68FFB2),
            DashboardTemplateEntity("premium-segmented","Premium Segmented","Premium",1,true,false,false,"PREMIUM_SEGMENTED",0xFFDFF7FF,0xFFA8FFD9)
        ))
    }
    suspend fun select(id:String)=dao.selectTemplate(id)
    suspend fun favorite(item:DashboardTemplateEntity)=dao.setTemplateFavorite(item.id,!item.favorite)
    suspend fun duplicate(item:DashboardTemplateEntity)=dao.upsertTemplate(item.copy(id=UUID.randomUUID().toString(),name=item.name+" Copy",builtIn=false,selected=false,version=1))
}
