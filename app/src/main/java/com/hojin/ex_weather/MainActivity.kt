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
                val token = it.split("\t")
                //Log.d(TAG, "Print : $it")
                val input = NationalWeatherTable(
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
            }
        }else{
            Log.d(TAG, "db 는 이미 존재함")
        }

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onCreate: 권한가져와 응애")
            return
        }

        var isFinished = true


            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this@MainActivity)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Log.d(TAG, "addOnSuccessListener????????? : $location")

                    if (location != null) {
                        mLocation = location
                    }
                    Log.d(TAG, "mLocation Update : $location")
                    isFinished = false

                    val geocoder = Geocoder(this, Locale.getDefault())

                    while (isFinished) {
                        Log.d(TAG, "waiting fusedLocationClient")
                    }

                    val address = geocoder.getFromLocation(
                        mLocation.latitude,
                        mLocation.longitude,
                        10
                    )
                    if (address == null || address.isEmpty()) {
                        Toast.makeText(applicationContext, "주소 미발견", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, "Address: $address[0]")

                    val items = address[0].toString().split(" ")


                    var searchresult:List<NationalWeatherTable> = emptyList()
                    val r = Runnable {
                        searchresult = NationalWeatherDB.nationalWeatherInterface()
                            .search(items[1], items[2])
                        Log.d(TAG, "Setting searchresult   $searchresult / ${items[1]} / ${items[2]} /")

                        val mNow = System.currentTimeMillis()
                        val mDate = Date(mNow)
                        val mformat1 = SimpleDateFormat("yyyyMMdd")
                        val mformat2 = SimpleDateFormat("HHmm")
                        val date = mformat1.format(mDate).toInt()
                        var time = mformat2.format(mDate).toInt()

                        Log.d(TAG, "onCreate: $date")
                        Log.d(TAG, "onCreate: $time")
                        Log.d(TAG, "onCreate: ${searchresult[0].Nx.toString()}")
                        Log.d(TAG, "onCreate: ${searchresult[0].Ny.toString()}")
                        val call = ApiObject.retrofitService.GetWeather(
                            "JSON",
                            50,
                            1,
                            date,
                            time,
                            searchresult[0].Nx.toString(),
                            searchresult[0].Ny.toString()
                        )
                        call.enqueue(object : retrofit2.Callback<WEATHER> {
                            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                                if (response.isSuccessful) {
                                    Log.d("api", response.body().toString())
                                    Log.d("api", response.body()!!.response.body.items.item.toString())//다 잘들어옴
                                    var t1h = false
                                    var sky = false
                                    for(i in response.body()!!.response.body.items.item){
                                        if(i.category=="T1H" && !t1h){
                                            t1h = true
                                            findViewById<TextView>(R.id.text_weather).text = "현재온도 ${i.fcstValue}℃"
                                        }
                                        if(i.category=="SKY" && !sky){
                                            sky = true
                                            when(i.fcstValue.toInt()){
                                                1->findViewById<ImageView>(R.id.image_weather).setImageResource(R.drawable.nb01)
                                                3->findViewById<ImageView>(R.id.image_weather).setImageResource(R.drawable.nb03)
                                                4->findViewById<ImageView>(R.id.image_weather).setImageResource(R.drawable.nb04)
                                                else->findViewById<ImageView>(R.id.image_weather).setImageResource(R.drawable.ic_baseline_error_24)
                                            }
                                        }
                                        if(t1h && sky) {
                                            findViewById<TextView>(R.id.text_date).text = "${i.baseDate} ${i.baseTime}"
                                            break
                                        }
                                    }
                                    Log.d(TAG, "onResponse: finish work")
                                }
                            }

                            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                                Log.d("api fail : ", t.message.toString())
                                findViewById<TextView>(R.id.text_date).text = "${t.message.toString()}"
                            }
                        })
                    }

                    val thread = Thread(r)
                    thread.start()

                }
    }
}