package com.example.skycast

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CityAdapter(var cities: MutableList<City>, private val onCityClickListener: (City) -> Unit) : RecyclerView.Adapter<CityAdapter.CityViewHolder>(){
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.city_item, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {


        val city = cities[position]
        holder.city_name.text = city.name
        holder.tvWeather.text = city.weather
        holder.tvTemp.text = city.temperature.toString()

        Glide.with(holder.weatherIcon.context)
            .load("https://openweathermap.org/img/wn/${city.weatherIcon}@2x.png")
            .into(holder.weatherIcon)

        holder.itemView.setOnClickListener {
            onCityClickListener(city)
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateCities(newCities: List<City>) {
        cities = newCities.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = cities.size

    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val city_name = itemView.findViewById<TextView>(R.id.city_name)
        val tvWeather = itemView.findViewById<TextView>(R.id.tvWeather)
        val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp)
        val weatherIcon = itemView.findViewById<ImageView>(R.id.weatherIcon)

    }

    fun removeItem(position: Int) {
        cities.removeAt(position)
        notifyItemRemoved(position)
    }

    // Interface for the click listener
    interface OnClickListener {
        fun onClick(position: Int, city: City)
    }
}