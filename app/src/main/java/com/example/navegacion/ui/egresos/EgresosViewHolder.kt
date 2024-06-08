package com.example.navegacion.ui.egresos


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R

class EgresosViewHolder(val view:View):RecyclerView.ViewHolder(view) {


    val nombre = view.findViewById<TextView>(R.id.tvLvNombre)
    val valor = view.findViewById<TextView>(R.id.tvLvValor)
    val descripcion = view.findViewById<TextView>(R.id.tvLvDescripcion)
    val fecha = view.findViewById<TextView>(R.id.tvLvFecha)
    val categoria = view.findViewById<TextView>(R.id.tvLvCategoria)

    fun render(egresosModel: EgresosFragment.Egresos) {
        nombre.text = egresosModel.nombre
        valor.text = egresosModel.valor
        descripcion.text = egresosModel.descripcion
        fecha.text = egresosModel.fecha
        categoria.text = egresosModel.categoria
    }
}
