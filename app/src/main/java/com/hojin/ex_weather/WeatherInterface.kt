package com.hojin.ex_weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {
    @GET("getUltraSrtFcst?serviceKey=" +
            "PopaVGfopxS%2FhgXo2zqOaRTtWU3j4ADDJo%2BRwb9rNpPV7%2FRUPlvowNT7PFdIkauBn2fV0m1LbEJm84ZXnvAW5w%3D%3D")
    fun GetWeather(
        @Query("dataType") data_type : String,
        @Query("numOfRows") num_of_rows : Int,
        @Query("pageNo") page_no : Int,
        @Query("base_date") base_date : Int,
        @Query("base_time") base_time : Int,
        @Query("nx") nx : String,
        @Query("ny") ny : String
    ): Call<WEATHER> // WEATHER는 DATA CLASS
}