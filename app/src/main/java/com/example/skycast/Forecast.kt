package com.example.skycast

data class Forecast(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>,
)