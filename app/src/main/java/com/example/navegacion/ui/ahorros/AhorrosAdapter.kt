package com.example.navegacion.ui.ahorros

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class AhorrosAdapter(val ahorrosList: List<AhorrosFragment.Ahorros>) : RecyclerView.Adapter<AhorrosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AhorrosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
        return AhorrosViewHolder(view)
    }

    override fun onBindViewHolder(holder: AhorrosViewHolder, position: Int) {
        val iten =  ahorrosList[position]
        holder.render(iten)
    }

    override fun getItemCount(): Int =  ahorrosList.size

}

