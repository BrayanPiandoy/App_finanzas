package com.example.navegacion.ui.home


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class IngresosViewHolder(val view:View):RecyclerView.ViewHolder(view) {


    val nombre = view.findViewById<TextView>(R.id.tvLvNombre)
    val valor = view.findViewById<TextView>(R.id.tvLvValor)
    val descripcion = view.findViewById<TextView>(R.id.tvLvDescripcion)
    val fecha = view.findViewById<TextView>(R.id.tvLvFecha)
    val categoria = view.findViewById<TextView>(R.id.tvLvCategoria)

    fun render(ingreso:IngresosHomeFragment.Ingreso) {
        nombre.text = ingreso.nombre
        valor.text = ingreso.valor
        descripcion.text = ingreso.descripcion
        fecha.text = ingreso.fecha
        categoria.text = ingreso.categoria
    }
}
