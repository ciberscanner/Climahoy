package com.example.climahoy.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.climahoy.R
import com.example.climahoy.data.model.CityResponse

class CitySearchAdapter(private var list: List<CityResponse>, private val onClickListener: (CityResponse) -> Unit) : RecyclerView.Adapter<CitySearchAdapter.CityViewHolder>() {
    class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val city: TextView = view.findViewById(R.id.tvCityName)
        val region: TextView = view.findViewById(R.id.tvCityName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city_search, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = list[position]
        holder.city.text = city.name
        holder.region.text = city.region
        holder.itemView.setOnClickListener { onClickListener(city) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<CityResponse>) {
        list = newList
        notifyDataSetChanged()
    }
}