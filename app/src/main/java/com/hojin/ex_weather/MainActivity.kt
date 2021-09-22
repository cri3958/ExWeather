package com.hojin.ex_weather

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()



class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mLocation: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonFunction = JSONFunction()
        jsonFunction.getdata(this)

        Log.d(TAG, "onCreate: I'm back!")


        when (jsonFunction.result_sky) {
            1 -> findViewById<ImageView>(R.id.image_weather).setImageResource(
                R.drawable.nb01
            )
            3 -> findViewById<ImageView>(R.id.image_weather).setImageResource(
                R.drawable.nb03
            )
            4 -> findViewById<ImageView>(R.id.image_weather).setImageResource(
                R.drawable.nb04
            )
            else -> findViewById<ImageView>(R.id.image_weather).setImageResource(
                R.drawable.ic_baseline_error_24
            )
        }

        findViewById<TextView>(R.id.text_weather).text = jsonFunction.result_temparature

        findViewById<TextView>(R.id.text_date).text = jsonFunction.result_resulttime

    }
}