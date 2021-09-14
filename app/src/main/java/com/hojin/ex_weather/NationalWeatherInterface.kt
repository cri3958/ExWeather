package com.hojin.ex_weather

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

const val table = "NationalWeatherTable"
const val string1 = "string1"
const val string2 = "string2"
const val string3 = "string3"

const val addresstable = "db_address"

@Dao
interface NationalWeatherInterface {
    @Query("SELECT * FROM $table")
    fun getAll():List<NationalWeatherTable>

    @Insert
    fun insert(nationalWeatherTable: NationalWeatherTable)

    @Query("SELECT * FROM $table WHERE $string1 = :s1 AND $string2 = :s2")
    fun search(s1:String, s2:String):List<NationalWeatherTable>

}