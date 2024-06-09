package com.example.navegacion.ui.egresos

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class EgresosFragment : Fragment() {
    private lateinit var sCategories: Spinner
    private lateinit var rvEgresos: RecyclerView
    private lateinit var tvTotalPayments: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: EgresosAdapter
    private var egresosList = mutableListOf<Egresos>()

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
        rvEgresos = view.findViewById(R.id.lvEgresos)
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)

        fetchCategories()
        fetchEgresos()

        return view
    }

    private fun fetchCategories() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val categoriasRef = database.getReference("users").child(uid).child("categorias").child("egresos")

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
                    Log.e("EgresosFragment", "Error fetching categories: ${databaseError.message}")
                }
            })
        }
    }

    private fun fetchEgresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val egresosRef = database.getReference("users").child(uid).child("egresos")

            egresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    egresosList.clear() // Limpiar la lista antes de agregar nuevos elementos
                    var totalEgresos = 0.0

                    for (egresosSnapshot in snapshot.children) {
                        val egresos = egresosSnapshot.getValue(Egresos::class.java)
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
                    Log.e("EgresosFragment", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = EgresosAdapter(egresosList)
        rvEgresos.layoutManager = LinearLayoutManager(requireContext())
        rvEgresos.adapter = adapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToRemove = egresosList[position]
                showDeleteConfirmationDialog(position, itemToRemove)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvEgresos)
    }

    private fun showDeleteConfirmationDialog(position: Int, itemToRemove: Egresos) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar este elemento?")
            .setPositiveButton("Eliminar") { dialog, which ->
                deleteItemFromFirebase(position, itemToRemove)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteItemFromFirebase(position: Int, itemToRemove: Egresos) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val egresosRef = database.getReference("users").child(uid).child("egresos")
            egresosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (egresosSnapshot in snapshot.children) {
                        val egresos = egresosSnapshot.getValue(Egresos::class.java)
                        if (egresos != null && egresosMatches(egresos, itemToRemove)) {
                            egresosSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Elimina el elemento de la lista local
                                    adapter.removeItem(position)

                                    // Vuelve a calcular el total después de la eliminación
                                    fetchEgresos()
                                } else {
                                    // Manejar error en la eliminación de Firebase
                                    Log.e("EgresosFragment", "Error al eliminar el elemento de Firebase")
                                    adapter.notifyItemChanged(position)
                                }
                            }
                            break // Sal del bucle después de encontrar la coincidencia
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EgresosFragment", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun egresosMatches(egresos: Egresos, itemToRemove: Egresos): Boolean {
        // Verifica si los campos coinciden
        return egresos.nombre == itemToRemove.nombre &&
                egresos.descripcion == itemToRemove.descripcion &&
                egresos.valor == itemToRemove.valor
    }

    data class Egresos(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )
}
