package com.example.navegacion.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class EgresoAdapter(val egresosList: MutableList<EgresosHomeFragment.Egreso>) : RecyclerView.Adapter<EgresosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EgresosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
        return EgresosViewHolder(view)
    }

    override fun onBindViewHolder(holder: EgresosViewHolder, position: Int) {
        val iten =  egresosList[position]
        holder.render(iten)
    }

    override fun getItemCount(): Int =  egresosList.size

}
