package com.example.navegacion.ui.egresos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class EgresosAdapter(val egresosList: MutableList<EgresosFragment.Egresos>) : RecyclerView.Adapter<EgresosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EgresosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
        return EgresosViewHolder(view)
    }

    override fun onBindViewHolder(holder: EgresosViewHolder, position: Int) {
        val iten =  egresosList[position]
        holder.render(iten)
    }

    override fun getItemCount(): Int =  egresosList.size

    fun removeItem(position: Int) {
        egresosList.removeAt(position)
        notifyItemRemoved(position)
    }
}

