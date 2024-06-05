package com.example.navegacion.ui.ingresos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.navegacion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class IngresosFragment : Fragment() {

    private lateinit var sCategories: Spinner
    private lateinit var lvIngresos: ListView
    private lateinit var tvTotalPayments: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ingresos, container, false)

        sCategories = view.findViewById(R.id.sCategories)
        lvIngresos = view.findViewById(R.id.lvIngresos)
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)

        val categorias = listOf("Todos",
            "Salario", "Venta de productos", "Ingresos por servicios",
            "Bonificaciones", "Ingresos por alquiler", "Ingresos por inversiones", "Otros"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sCategories.adapter = adapter

        fetchIngresos()

        return view
    }

    private fun fetchIngresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val ingresosRef = database.getReference("users").child(uid).child("ingresos")

            val colors = mapOf(
                "Todos" to R.color.categoria20,
                "Salario" to R.color.categoria1,
                "Venta de productos" to R.color.categoria2,
                "Ingresos por servicios" to R.color.categoria3,
                "Bonificaciones" to R.color.categoria4,
                "Ingresos por alquiler" to R.color.categoria5,
                "Ingresos por inversiones" to R.color.categoria6,
                "Otros" to R.color.categoria7
                // Agrega más categorías aquí con sus respectivos colores
            )

            ingresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ingresosList = mutableListOf<String>()
                    var totalIngresos = 0.0

                    for (ingresoSnapshot in snapshot.children) {
                        val ingreso = ingresoSnapshot.getValue(Ingreso::class.java)
                        if (ingreso != null) {
                            val ingresoString = """
                            Nombre: ${ingreso.nombre}
                            Categoría: ${ingreso.categoria}
                            Valor: ${ingreso.valor}
                            Descripción: ${ingreso.descripcion}
                            Fecha: ${ingreso.fecha}
                        """.trimIndent()
                            ingresosList.add(ingresoString)

                            // Eliminar comas antes de convertir a double
                            val valorSinComas = ingreso.valor.replace(",", "")
                            totalIngresos += valorSinComas.toDouble()
                        } else {
                            Log.e("IngresosFragment", "Ingreso is null for snapshot: $ingresoSnapshot")
                        }
                    }
                    // Dentro del método fetchIngresos() en tu fragmento IngresosFragment
                    val adapter = CustomListAdapter(requireContext(), R.layout.list_item_layout, ingresosList, colors)
                    lvIngresos.adapter = adapter

                    // Formatear el total de ingresos
                    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                    tvTotalPayments.text = numberFormat.format(totalIngresos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("IngresosFragment", "Database error: ${error.message}")
                }
            })

        }

    }

    data class Ingreso(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )
}