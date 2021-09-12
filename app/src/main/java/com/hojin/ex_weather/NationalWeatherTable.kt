package com.hojin.ex_weather

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NationalWeatherTable(
    @PrimaryKey
    val areacode:Long,
    val string1:String,
    val string2:String,
    val string3:String,
    val Nx:Int,
    val Ny:Int
)
