package com.example.skycast

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(){
    private lateinit var weatherApi: WeatherApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var selectedCity: String

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        selectedCity = loadCity()

        fun openActivity() {
            val intent = Intent(this, ActivityCities::class.java)
            startActivity(intent)
        }

        val mb = findViewById<AppCompatImageButton>(R.id.menuButton)
        mb.setOnClickListener { it: View -> openActivity() }

        //RecyclerView
        recyclerView= findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //search button
        val searchButton: AppCompatImageButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            showCityInputDialog()
        }

        //WeatherApi
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherApi = retrofit.create(WeatherApiService::class.java)

        getWeather(selectedCity)
        getWeeksWeather(selectedCity)
        loadCity()

        val cityTView = findViewById<TextView>(R.id.cityTView)
        cityTView.setOnClickListener{
            loadCity()
            getWeather(selectedCity)
            getWeeksWeather(selectedCity)
            Toast.makeText(this, "Обновление", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCity(city: String) {
        try {
            openFileOutput("current_city.txt", Context.MODE_PRIVATE).bufferedWriter().use {
                it.write(city)
            }
        } catch (e: Exception) {
            Log.d("SaveCityError", "Error: ${e.message}")
        }
    }

    private fun loadCity(): String {
        return try {
            val file = getFileStreamPath("current_city.txt")
            if (file.exists()) {
                openFileInput("current_city.txt").bufferedReader().use {
                    it.readText().ifEmpty { "London" }
                }
            } else {
                "Чебоксары"
            }
        } catch (e: Exception) {
            Log.d("LoadCityError", "Error: ${e.message}")
            "Чебоксары"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getWeather(city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentWeather = weatherApi.getCurrentWeather(city, "2d845be74061ca6a12c39e464b21ef58")
                val forecast = weatherApi.getForecast(city, "2d845be74061ca6a12c39e464b21ef58")

                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.tempTView).text = "${currentWeather.main.temp.toInt()} °C"
                    findViewById<TextView>(R.id.weatherTView).text = currentWeather.weather[0].description
                    findViewById<TextView>(R.id.cityTView).text = city
                    Log.d("WeatherApp", "Prognoz: ${forecast}")
                }
            } catch (e: Exception) {
                Log.d("getWeather", e.toString())
            }
        }
    }

    private fun showCityInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Введите город")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val cityName = input.text.toString().trim()
            if (cityName.isNotEmpty()) {
                selectedCity = cityName
                saveCity(cityName)
            }
            dialog.dismiss()
            getWeather(selectedCity.toString())
            getWeeksWeather(selectedCity.toString())
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun getWeeksWeather(city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val forecast = weatherApi.getForecast(city, "2d845be74061ca6a12c39e464b21ef58")
                val dailyList = forecast.list
                    .filter { it.dt_txt.contains("12:00:00") }
                    .take(5)
                    .map {
                        WeatherData(
                            day = it.dt_txt.split(" ")[0].split("-")[2],
                            temperature = it.main.temp.toInt(),
                            weatherIcon = it.weather[0].icon
                        )
                    }
                withContext(Dispatchers.Main) {
                    weatherAdapter = WeatherAdapter(dailyList)
                    recyclerView.adapter = weatherAdapter
                }
            } catch (e: Exception) {
                Log.d("getWeatherWeek", e.toString())
            }
        }
    }
}