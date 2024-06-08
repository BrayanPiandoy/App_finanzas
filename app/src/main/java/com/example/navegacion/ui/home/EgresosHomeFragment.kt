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
import com.google.firebase.database.*

class EgresosHomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var egresoAdapter: EgresoAdapter
    private val egresosList = mutableListOf<Egreso>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_egresos_home, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewEgresos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        egresoAdapter = EgresoAdapter(egresosList)
        recyclerView.adapter = egresoAdapter

        loadEgresos()

        return view
    }

    private fun loadEgresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val egresosRef = database.getReference("users").child(uid).child("egresos")

            egresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    egresosList.clear()
                    for (egresoSnapshot in snapshot.children) {
                        val egreso = egresoSnapshot.getValue(Egreso::class.java)
                        if (egreso != null) {
                            egresosList.add(egreso)
                        }
                    }
                    egresoAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
    data class Egreso(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )
}