package com.example.navegacion.ui.home


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class AhorrosViewHolder(val view:View):RecyclerView.ViewHolder(view) {


    val nombre = view.findViewById<TextView>(R.id.tvLvNombre)
    val valor = view.findViewById<TextView>(R.id.tvLvValor)
    val descripcion = view.findViewById<TextView>(R.id.tvLvDescripcion)
    val fecha = view.findViewById<TextView>(R.id.tvLvFecha)
    val categoria = view.findViewById<TextView>(R.id.tvLvCategoria)

    fun render(ahorros:AhorrosHomeFragment.Ahorro) {
        nombre.text = ahorros.nombre
        valor.text = ahorros.valor
        descripcion.text = ahorros.descripcion
        fecha.text = ahorros.fecha
        categoria.text = ahorros.categoria
    }
}
