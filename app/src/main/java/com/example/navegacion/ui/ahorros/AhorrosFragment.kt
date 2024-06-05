package com.example.navegacion.ui.ahorros

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
import com.example.navegacion.R
import com.example.navegacion.ui.ahorros.CustomListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class AhorrosFragment : Fragment() {

    private lateinit var sCategories: Spinner
    private lateinit var lvAhorros: ListView
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
        val view = inflater.inflate(R.layout.fragment_ahorros, container, false)

        sCategories = view.findViewById(R.id.sCategories)
        lvAhorros = view.findViewById(R.id.lvAhorros)
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)

        val categorias = listOf("Todos",
            "Cuenta de ahorros", "Fondos de inversión", "Plan de pensiones", "Cuenta de jubilación", "Inversiones a largo plazo", "Inversiones a corto plazo", "Otros"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sCategories.adapter = adapter

        fetchAhorros()

        return view
    }

    private fun fetchAhorros() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val ahorrosRef = database.getReference("users").child(uid).child("ahorros")

            val colors = mapOf(
                "Todos" to R.color.categoria20,
                "Cuenta de ahorros" to R.color.categoria1,
                "Fondos de inversión" to R.color.categoria2,
                "Plan de pensiones" to R.color.categoria3,
                "Cuenta de jubilación" to R.color.categoria4,
                "Inversiones a largo plazo" to R.color.categoria5,
                "Inversiones a corto plazo" to R.color.categoria6,
                "Otros" to R.color.categoria7
                // Agrega más categorías aquí con sus respectivos colores
            )

            ahorrosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ahorrosList = mutableListOf<String>()
                    var totalAhorros = 0.0

                    for (ahorrosSnapshot in snapshot.children) {
                        val ahorros = ahorrosSnapshot.getValue(Ahorros::class.java)
                        if (ahorros != null) {
                            val ahorrosString = """
                            Nombre: ${ahorros.nombre}
                            Categoría: ${ahorros.categoria}
                            Valor: ${ahorros.valor}
                            Descripción: ${ahorros.descripcion}
                            Fecha: ${ahorros.fecha}
                        """.trimIndent()
                            ahorrosList.add(ahorrosString)

                            // Eliminar comas antes de convertir a double
                            val valorSinComas = ahorros.valor.replace(",", "")
                            totalAhorros += valorSinComas.toDouble()
                        } else {
                            Log.e("EgresosFragment", "Egreso is null for snapshot: $ahorrosSnapshot")
                        }
                    }
                    // Dentro del método fetchIngresos() en tu fragmento IngresosFragment
                    val adapter = CustomListAdapter(requireContext(), R.layout.list_item_layout, ahorrosList, colors)
                    lvAhorros.adapter = adapter

                    // Formatear el total de ingresos
                    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                    tvTotalPayments.text = numberFormat.format(totalAhorros)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EgresosFragment", "Database error: ${error.message}")
                }
            })

        }

    }

    data class Ahorros(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )


}