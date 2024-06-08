package com.example.navegacion.ui.ahorros


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.ingresos.IngresosAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class AhorrosFragment : Fragment() {

    private lateinit var sCategories: Spinner
    private lateinit var rvEgresos: RecyclerView
    private lateinit var tvTotalPayments: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AhorrosAdapter
    private var egresosList= mutableListOf<Ahorros>()

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
        rvEgresos = view.findViewById(R.id.rvAhorros)
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)

        fetchCategories()
        fetchEgresos()

        return view
    }

    private fun fetchCategories() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val categoriasRef = database.getReference("users").child(uid).child("categorias").child("ahorros")

            categoriasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val categoriasList = mutableListOf<String>()

                    for (categoriaSnapshot in dataSnapshot.children) {
                        val categoria = categoriaSnapshot.getValue(String::class.java)
                        categoria?.let {
                            categoriasList.add(it)
                        }
                    }

                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoriasList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    sCategories.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("AhorrosFragment", "Error fetching categories: ${databaseError.message}")
                }
            })
        }
    }

    private fun fetchEgresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val egresosRef = database.getReference("users").child(uid).child("ahorros")

            egresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    egresosList
                    var totalEgresos = 0.0

                    for (egresosSnapshot in snapshot.children) {
                        val egresos = egresosSnapshot.getValue(Ahorros::class.java)
                        if (egresos != null) {

                            egresosList.add(egresos)

                            // Eliminar comas antes de convertir a double
                            val valorSinComas = egresos.valor.replace(",", "")
                            totalEgresos += valorSinComas.toDouble()
                        }
                    }

                    setupRecyclerView()

                    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                    tvTotalPayments.text = numberFormat.format(totalEgresos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AhorrosFragment", "Database error: ${error.message}")
                }
            })

        }
    }

    private fun setupRecyclerView() {
        adapter = AhorrosAdapter(egresosList)
        rvEgresos.layoutManager = LinearLayoutManager(requireContext())
        rvEgresos.adapter = adapter
    }
    data class Ahorros(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )
}