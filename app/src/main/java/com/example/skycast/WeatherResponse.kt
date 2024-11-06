package com.example.skycast

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)