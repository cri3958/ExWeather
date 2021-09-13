package com.hojin.ex_weather

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object ApiObject {
    val retrofitService: WeatherInterface by lazy {
        retrofit.create(WeatherInterface::class.java)
    }
}

private lateinit var fusedLocationClient: FusedLocationProviderClient

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mLocation: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val assetManager: AssetManager = resources.assets
        val inputStream: InputStream = assetManager.open("NationalWeatherDB.txt")
        val NationalWeatherDB = Room.databaseBuilder(this, AppDatabase::class.java, "db").build()
        val file = getDatabasePath("db")
        if(!file.exists()) {
            Log.d(TAG, "db 는 존재하지않았음")

            inputStream.bufferedReader().readLines().forEach {
                var token = it.split("\t")
                //Log.d(TAG, "Print : $it")
                var input = NationalWeatherTable(
                    token[0].toLong(),
                    token[1],
                    token[2],
                    token[3],
                    token[4].toInt(),
                    token[5].toInt()
                )
                val r = Runnable {
                    // 데이터에 읽고 쓸때는 쓰레드 사용
                    NationalWeatherDB.nationalWeatherInterface().insert(input)
                }

                val thread = Thread(r)
                thread.start()
                /*CoroutineScope(Dispatchers.Main).launch {
                NationalWeatherDB.nationalWeatherInterface().insert(input)
            }*/
            }
        }else{
            Log.d(TAG, "db 는 이미 존재함")
        }

        CoroutineScope(Dispatchers.Main).launch {

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: 권한가져와 응애")
                return@launch
            }


            fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)//or this@MainActivity로 하다가 오류나는거 같기도함??
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { it: Location ->
                    // Logic to handle location object
                    mLocation = it
                    Log.d(TAG, "mLocation Update")
                } ?: kotlin.run {
                    // Handle Null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
                    Log.d(TAG, "mLocation Update for getLastLocation")
                    //mLocation = LocationServices.FusedLocationApi.getLastLocation()  //이거 어캐씀 코루틴 빠져나가야되나 코루틴이 필요한가???????
                }
            }



            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())

            var address = geocoder.getFromLocation(
                mLocation.latitude,
                mLocation.longitude,
                10
            )
            if(address == null || address.isEmpty()){
                Toast.makeText(applicationContext, "주소 미발견", Toast.LENGTH_SHORT).show()
            }
            Log.d(TAG, "Address: $address[0]")

            val items = address[0].toString().split(" ")


            val DBresult = NationalWeatherDB.nationalWeatherInterface().search(items[1],items[2],items[3])

            val mNow = System.currentTimeMillis()
            val mDate = Date(mNow)
            val mformat1 = SimpleDateFormat("yyyyMMdd")
            val mformat2 = SimpleDateFormat("H")
            val date = mformat1.format(mDate).toInt()
            var time = mformat2.format(mDate).toInt()
            if(time%2==0) {
                time--
            }

            val call = ApiObject.retrofitService.GetWeather(
                "JSON",
                100,
                1,
                date,
                time,
                DBresult[0].Nx.toString(),
                DBresult[0].Ny.toString()
            )
            call.enqueue(object : retrofit2.Callback<WEATHER> {
                override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                    if (response.isSuccessful) {
                        Log.d("api", response.body().toString())
                        Log.d("api", response.body()!!.response.body.items.item.toString())
                        Log.d("api", response.body()!!.response.body.items.item[0].category)
                    }
                }

                override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                    Log.d("api fail : ", t.message.toString())
                }
            })
        }



    }

}