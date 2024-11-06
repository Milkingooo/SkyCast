package com.example.skycast

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WeatherAdapter(private val weatherList: List<WeatherData>) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forecast_item, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherData = weatherList[position]
        holder.tvDay.text = weatherData.day
        holder.tvTemp.text = "${weatherData.temperature} °C"
        //holder.weatherIcon.setImageResource(weatherData.weatherIcon)

        Glide.with(holder.weatherIcon.context)
            .load("https://openweathermap.org/img/wn/${weatherData.weatherIcon}@2x.png")
            .into(holder.weatherIcon)
    }

    override fun getItemCount(): Int = weatherList.size

    class WeatherViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvDay = itemView.findViewById<TextView>(R.id.tvDay)
        val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp)
        val weatherIcon = itemView.findViewById<ImageView>(R.id.weatherIcon)
    }


}