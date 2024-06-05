package com.example.navegacion.ui.egresos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentEgresosBinding
import com.example.navegacion.ui.egresos.CustomListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class EgresosFragment : Fragment() {
    private lateinit var sCategories: Spinner
    private lateinit var lvEgresos: ListView
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
        val view = inflater.inflate(R.layout.fragment_egresos, container, false)

        sCategories = view.findViewById(R.id.sCategories)
        lvEgresos = view.findViewById(R.id.lvEgresos)
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)

        val categorias = listOf("Todos",
            "Alimentación", "Transporte", "Educación", "Entretenimiento", "Salud", "Compras", "Otros"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sCategories.adapter = adapter

        fetchEgresos()

        return view
    }

    private fun fetchEgresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val egresosRef = database.getReference("users").child(uid).child("egresos")

            val colors = mapOf(
                "Todos" to R.color.categoria20,
                "Alimentación" to R.color.categoria1,
                "Transporte" to R.color.categoria2,
                "Educación" to R.color.categoria3,
                "Entretenimiento" to R.color.categoria4,
                "Salud" to R.color.categoria5,
                "Compras" to R.color.categoria6,
                "Otros" to R.color.categoria7
                // Agrega más categorías aquí con sus respectivos colores
            )

            egresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val egresosList = mutableListOf<String>()
                    var totalEgresos = 0.0

                    for (egresosSnapshot in snapshot.children) {
                        val egresos = egresosSnapshot.getValue(Egresos::class.java)
                        if (egresos != null) {
                            val egresosString = """
                            Nombre: ${egresos.nombre}
                            Categoría: ${egresos.categoria}
                            Valor: ${egresos.valor}
                            Descripción: ${egresos.descripcion}
                            Fecha: ${egresos.fecha}
                        """.trimIndent()
                            egresosList.add(egresosString)

                            // Eliminar comas antes de convertir a double
                            val valorSinComas = egresos.valor.replace(",", "")
                            totalEgresos += valorSinComas.toDouble()
                        } else {
                            Log.e("EgresosFragment", "Egreso is null for snapshot: $egresosSnapshot")
                        }
                    }
                    // Dentro del método fetchIngresos() en tu fragmento IngresosFragment
                    val adapter = CustomListAdapter(requireContext(), R.layout.list_item_layout, egresosList, colors)
                    lvEgresos.adapter = adapter

                    // Formatear el total de ingresos
                    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                    tvTotalPayments.text = numberFormat.format(totalEgresos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EgresosFragment", "Database error: ${error.message}")
                }
            })

        }

    }

    data class Egresos(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )


}