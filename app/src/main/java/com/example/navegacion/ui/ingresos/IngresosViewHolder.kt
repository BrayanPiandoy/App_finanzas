package com.example.navegacion.ui.ingresos


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

    fun render(ingresosModel:IngresosFragment.Ingresos) {
        nombre.text = ingresosModel.nombre
        valor.text = ingresosModel.valor
        descripcion.text = ingresosModel.descripcion
        fecha.text = ingresosModel.fecha
        categoria.text = ingresosModel.categoria
    }
}
