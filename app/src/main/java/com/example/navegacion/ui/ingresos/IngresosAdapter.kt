package com.example.navegacion.ui.ingresos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class IngresosAdapter(val ingresosList: List<IngresosFragment.Ingresos>) : RecyclerView.Adapter<IngresosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngresosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
        return IngresosViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngresosViewHolder, position: Int) {
        val iten =  ingresosList[position]
        holder.render(iten)
    }

    override fun getItemCount(): Int =  ingresosList.size

}

