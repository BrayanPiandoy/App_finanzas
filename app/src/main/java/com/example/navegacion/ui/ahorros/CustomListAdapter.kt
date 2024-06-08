package com.example.navegacion.ui.ahorros

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.example.navegacion.R

class CustomListAdapter(context: Context, objects: List<String>) :
    ArrayAdapter<String>(context, R.layout.list_item_layout, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        if (rowView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.list_item_layout, parent, false)
        }

        // Obtener referencia a los elementos del diseño personalizado
        val tvNombre = rowView?.findViewById<TextView>(R.id.tvLvNombre)
        val tvValor = rowView?.findViewById<TextView>(R.id.tvLvValor)
        val tvDescripcion = rowView?.findViewById<TextView>(R.id.tvLvDescripcion)
        val tvFecha = rowView?.findViewById<TextView>(R.id.tvLvFecha)

        // Obtener el elemento de datos correspondiente a esta posición
        val item = getItem(position)

        // Separar el texto del elemento de datos
        val parts = item?.split("\n")

        // Establecer los textos para cada TextView
        tvNombre?.text = parts?.get(0)
        tvValor?.text = parts?.get(2)
        tvDescripcion?.text = parts?.get(3)
        tvFecha?.text = parts?.get(4)

        // Obtener la categoría del elemento
        val categoria = parts?.get(1)

        return rowView!!
    }
}

