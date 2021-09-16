package com.hojin.ex_weather

data class WEATHER(
    val response:RESPONSE
)

data class RESPONSE(
    val header:HEADER,
    val body:BODY
)

data class HEADER(
    val resultCode:Int,
    val resultMsg:String
)

data class BODY(
    val dataType:String,
    val items : ITEMS
)

data class ITEMS(
    val item: List<ITEM>
)

data class ITEM(
    val baseDate:Int,
    val baseTime:Int,
    val category:String,
    val fcstDate:String,
    val fcstTime:String,
    val fcstValue:String,
    val nx:Int,
    val ny:Int
)
