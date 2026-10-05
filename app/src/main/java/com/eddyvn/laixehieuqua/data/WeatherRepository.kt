package com.eddyvn.laixehieuqua.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class WeatherState(
    val loading:Boolean=false,
    val currentTempC:Double?=null,
    val weatherCode:Int?=null,
    val todayMinC:Double?=null,
    val todayMaxC:Double?=null,
    val todayRainChance:Int?=null,
    val tomorrowMinC:Double?=null,
    val tomorrowMaxC:Double?=null,
    val tomorrowRainChance:Int?=null,
    val updatedAtMs:Long?=null,
    val error:String?=null,
)

class WeatherRepository {
    suspend fun fetch(latitude:Double,longitude:Double):WeatherState=withContext(Dispatchers.IO){
        val url=URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&forecast_days=2&timezone=auto"
        )
        val connection=(url.openConnection() as HttpURLConnection).apply{
            connectTimeout=8_000
            readTimeout=8_000
            requestMethod="GET"
            setRequestProperty("Accept","application/json")
        }
        try{
            val status=connection.responseCode
            if(status !in 200..299)error("Weather HTTP $status")
            val body=connection.inputStream.bufferedReader().use{it.readText()}
            parse(body)
        }finally{
            connection.disconnect()
        }
    }

    private fun parse(body:String):WeatherState{
        val root=JSONObject(body)
        val current=root.getJSONObject("current")
        val daily=root.getJSONObject("daily")
        val max=daily.getJSONArray("temperature_2m_max")
        val min=daily.getJSONArray("temperature_2m_min")
        val rain=daily.getJSONArray("precipitation_probability_max")
        return WeatherState(
            currentTempC=current.optDouble("temperature_2m").takeUnless{it.isNaN()},
            weatherCode=current.optInt("weather_code"),
            todayMinC=min.optDouble(0).takeUnless{it.isNaN()},
            todayMaxC=max.optDouble(0).takeUnless{it.isNaN()},
            todayRainChance=rain.optInt(0),
            tomorrowMinC=min.optDouble(1).takeUnless{it.isNaN()},
            tomorrowMaxC=max.optDouble(1).takeUnless{it.isNaN()},
            tomorrowRainChance=rain.optInt(1),
            updatedAtMs=System.currentTimeMillis(),
        )
    }
}
