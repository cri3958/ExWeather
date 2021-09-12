package com.hojin.ex_weather

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NationalWeatherTable::class],version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun nationalWeatherInterface():NationalWeatherInterface
}