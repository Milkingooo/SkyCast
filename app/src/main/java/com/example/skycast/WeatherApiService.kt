package com.example.skycast

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService{
    @GET("weather")

    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru"
    ) : WeatherResponse

    @GET("forecast")

    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru"
    ): ForecastResponse

}