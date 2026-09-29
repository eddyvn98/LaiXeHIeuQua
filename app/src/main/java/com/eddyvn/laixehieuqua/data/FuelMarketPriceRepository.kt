package com.eddyvn.laixehieuqua.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class FuelMarketPrice(
    val product:String,
    val pricePerLiter:Int,
    val effectiveDate:String?,
    val fetchedAtMs:Long,
)

data class FuelMarketPriceState(
    val price:FuelMarketPrice?=null,
    val loading:Boolean=false,
    val error:String?=null,
)

class FuelMarketPriceRepository(context:Context){
    private val preferences=context.applicationContext.getSharedPreferences("fuel_market_price",Context.MODE_PRIVATE)

    fun cached():FuelMarketPrice?{
        val price=preferences.getInt(KEY_PRICE,0)
        if(price<=0)return null
        return FuelMarketPrice(
            product="RON 95-V · TP.HCM",
            pricePerLiter=price,
            effectiveDate=preferences.getString(KEY_EFFECTIVE_DATE,null),
            fetchedAtMs=preferences.getLong(KEY_FETCHED_AT,0L),
        )
    }

    suspend fun refresh():FuelMarketPrice=withContext(Dispatchers.IO){
        val connection=URL(PRICE_URL).openConnection() as HttpURLConnection
        try{
            connection.requestMethod="GET"
            connection.connectTimeout=8_000
            connection.readTimeout=8_000
            connection.setRequestProperty("Accept","application/json")
            val status=connection.responseCode
            check(status in 200..299){"API giá xăng phản hồi HTTP $status"}
            val body=connection.inputStream.bufferedReader().use{it.readText()}
            val response=JSONObject(body)
            val products=response.optJSONArray("data")?:response.optJSONArray("items")
                ?:error("API không có danh sách giá")
            val ron95v=(0 until products.length())
                .map{products.getJSONObject(it)}
                .firstOrNull{item->
                    val name=item.optString("name",item.optString("product"))
                        .uppercase().replace(" ","")
                    name.contains("95-V")&&!name.contains("95-III")
                }?:error("API chưa có giá RON 95-V tại TP.HCM")
            val price=ron95v.optDouble("region1",ron95v.optDouble("price",0.0)).toInt()
            check(price>0){"Giá RON 95-V từ API không hợp lệ"}
            val date=response.optJSONObject("meta")?.optString("priceDate")
                ?.takeUnless{it.isNullOrBlank()||it=="null"}
                ?:response.optString("effective_date").takeUnless{it.isNullOrBlank()||it=="null"}
            FuelMarketPrice("RON 95-V · TP.HCM",price,date,System.currentTimeMillis())
                .also(::save)
        }finally{
            connection.disconnect()
        }
    }

    private fun save(price:FuelMarketPrice){
        preferences.edit()
            .putInt(KEY_PRICE,price.pricePerLiter)
            .putString(KEY_EFFECTIVE_DATE,price.effectiveDate)
            .putLong(KEY_FETCHED_AT,price.fetchedAtMs)
            .apply()
    }

    private companion object{
        const val KEY_PRICE="price_per_liter"
        const val KEY_EFFECTIVE_DATE="effective_date"
        const val KEY_FETCHED_AT="fetched_at"
        const val PRICE_URL="https://vietfuel-api.tranqui.workers.dev/api/fuel-prices/saigon_petrolimex"
    }
}
