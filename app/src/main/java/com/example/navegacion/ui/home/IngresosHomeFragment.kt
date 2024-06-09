package com.example.navegacion.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class IngresosHomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var ingresoAdapter: IngresoAdapter
    private val ingresosList = mutableListOf<Ingreso>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ingresos_home, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewIngresos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ingresoAdapter = IngresoAdapter(ingresosList)
        recyclerView.adapter = ingresoAdapter

        loadEgresos()

        return view
    }

    private fun loadEgresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val ingresosRef = database.getReference("users").child(uid).child("ingresos")

            ingresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ingresosList.clear()
                    for (ingresoSnapshot in snapshot.children) {
                        val ingreso = ingresoSnapshot.getValue(Ingreso::class.java)
                        if (ingreso != null) {
                            ingresosList.add(ingreso)
                        }
                    }
                    ingresoAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
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