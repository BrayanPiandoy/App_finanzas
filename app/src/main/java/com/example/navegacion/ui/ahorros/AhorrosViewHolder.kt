package com.example.navegacion.ui.ahorros


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class AhorrosViewHolder(val view:View):RecyclerView.ViewHolder(view) {


    val nombre = view.findViewById<TextView>(R.id.tvLvNombre)
    val valor = view.findViewById<TextView>(R.id.tvLvValor)
    val descripcion = view.findViewById<TextView>(R.id.tvLvDescripcion)
    val categoria = view.findViewById<TextView>(R.id.tvLvCategoria)
    val fecha = view.findViewById<TextView>(R.id.tvLvFecha)

    fun render(ahorrosModel:AhorrosFragment.Ahorros) {
        nombre.text = ahorrosModel.nombre
        valor.text = ahorrosModel.valor
        descripcion.text = ahorrosModel.descripcion
        fecha.text = ahorrosModel.fecha
        categoria.text= ahorrosModel.categoria
    }
}
