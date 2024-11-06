package com.example.skycast

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ActivityCities : AppCompatActivity() {
    private lateinit var weatherApi: WeatherApiService
    private lateinit var recyclerViewCity: RecyclerView
    private lateinit var adapter: CityAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cities)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //clearCities()

        val myCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val cityName = adapter.cities[viewHolder.adapterPosition].name
                removeFormFile(cityName)
                adapter.removeItem(viewHolder.adapterPosition)

                val savedCities = loadCity()
                getWeather(savedCities)
            }
        }

        val addCityButton = findViewById<FloatingActionButton>(R.id.addCityButton)
        addCityButton.setOnClickListener {
            showCityInputDialog()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherApi = retrofit.create(WeatherApiService::class.java)

        recyclerViewCity = findViewById(R.id.recyclerViewCities)
        recyclerViewCity.layoutManager = LinearLayoutManager(this)
        adapter = CityAdapter(arrayListOf()) { cityName ->
            saveCity(cityName.name)
            Toast.makeText(this, "Выбрана: ${cityName.name}", Toast.LENGTH_SHORT).show()
            finish()
        }
        recyclerViewCity.adapter = adapter

        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(recyclerViewCity)

        val savedCities = loadCity()
        getWeather(savedCities)
        //Toast.makeText(this, savedCities[1], Toast.LENGTH_SHORT).show()// add cities to file
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

    @SuppressLint("NotifyDataSetChanged")
    private fun getWeather(cities: MutableList<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dailyList = mutableListOf<City>()

                for (city in cities) {
                    try {
                        val forecast = weatherApi.getForecast(city, "2d845be74061ca6a12c39e464b21ef58")
                        dailyList.add(
                            City(
                                name = city,
                                weather = forecast.list[0].weather[0].description,
                                temperature = "${forecast.list[0].main.temp.toInt()} °C",
                                weatherIcon = forecast.list[0].weather[0].icon
                            )
                        )
                    } catch (e: Exception) {
                        Log.d("getWeatherCities", "Error fetching weather for $city: ${e.message}")
                        if (e.message?.contains("HTTP 404 Not Found") == true) {
                            removeFormFile(city)
                            break
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    adapter.updateCities(dailyList)
                    Log.d("WeatherAppCities", "Adapter update cities with: $dailyList")
                }
            } catch (e: Exception) {
                Log.d("getWeatherCities", "Error fetching weather ${e.message}")
            }
        }
    }
    private fun clearCities() {
        try {
            openFileOutput("saved_cities.txt", Context.MODE_PRIVATE).bufferedWriter().use {
                it.write("")
            }
        } catch (e: Exception) {
            Log.d("ClearCityError", "Error: ${e.message}")
        }
    }

    private fun isCitySaved(city: String): Boolean {
        try {
            val file = getFileStreamPath("saved_cities.txt")
            if (file.exists()) {
                openFileInput("saved_cities.txt").bufferedReader().use {
                    val cities = it.readText().split(";").filter { it.isNotBlank() }
                    return cities.contains(city)
                }
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.d("LoadCityError", "Error: ${e.message}")
            return false
            }
    }

    private fun removeFormFile(city: String) {
        val newCities: MutableList<String> = arrayListOf()
        try {
            openFileInput("saved_cities.txt").bufferedReader().use {
                val cities = it.readText().split(";").filter { it.isNotBlank() }
                for (cityy in cities) {
                    if (cityy != city) {
                        newCities.add(cityy)
                    }
                }
                openFileOutput("saved_cities.txt", Context.MODE_PRIVATE).bufferedWriter().use {
                    it.write("${newCities.joinToString(";")}")
                }
            }
        } catch (e: Exception) {
            Log.d("RemoveCityError", "Error: ${e.message}")
        }
    }

    private fun addCity(city: String) {
        try {
            if (!isCitySaved(city)) {
                openFileOutput("saved_cities.txt", Context.MODE_APPEND).bufferedWriter().use {
                    it.write("$city;")
                }
                getWeather(loadCity())
            } else {
                Toast.makeText(this, "Город уже добавлен", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.d("SaveCityError", "Error: ${e.message}")
        }
    }

    private fun loadCity(): MutableList<String>{
         try {
            val file = getFileStreamPath("saved_cities.txt")
            if (file.exists()) {
                openFileInput("saved_cities.txt").bufferedReader().use {
                    val cities = it.readText().split(";").filter { it.isNotBlank()}
                    return cities.toMutableList()
                }
            } else {
                return arrayListOf()
            }
        } catch (e: Exception) {
            Log.d("LoadCityError", "Error: ${e.message}")
            return arrayListOf()
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
                addCity(cityName)
            }
            dialog.dismiss()
            getWeather(loadCity())
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

}