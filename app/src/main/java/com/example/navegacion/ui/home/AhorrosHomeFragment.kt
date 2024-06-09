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


class AhorrosHomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var ahorroAdapter: AhorrosAdapter
    private val ahorrosList = mutableListOf<Ahorro>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ahorros_home, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewAhorros)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ahorroAdapter = AhorrosAdapter(ahorrosList)
        recyclerView.adapter = ahorroAdapter

        loadAhorros()

        return view
    }

    private fun loadAhorros() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val ahorroRef = database.getReference("users").child(uid).child("ahorros")

            ahorroRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ahorrosList.clear()
                    for (ahorroSnapshot in snapshot.children) {
                        val ahorro = ahorroSnapshot.getValue(Ahorro::class.java)
                        if (ahorro != null) {
                            ahorrosList.add(ahorro)
                        }
                    }
                    ahorroAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
    data class Ahorro(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )
}