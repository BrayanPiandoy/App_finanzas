package com.example.navegacion.ui.ingresos

import android.app.AlertDialog
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navegacion.R
import com.example.navegacion.ui.ahorros.AhorrosAdapter
import com.example.navegacion.ui.ahorros.AhorrosFragment
import com.example.navegacion.ui.egresos.EgresosAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class IngresosFragment : Fragment() {
    private lateinit var sCategories: Spinner
    private lateinit var rvIngresos: RecyclerView
    private lateinit var tvTotalPayments: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: IngresosAdapter
    private var ingresosList= mutableListOf<Ingresos>()

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
        rvIngresos = view.findViewById(R.id.rvIngresos)
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)

        fetchCategories()
        fetchIngresos()

        return view
    }

    private fun fetchCategories() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val categoriasRef = database.getReference("users").child(uid).child("categorias").child("ingresos")

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
                    Log.e("IngresosFragment", "Error fetching categories: ${databaseError.message}")
                }
            })
        }
    }

    private fun fetchIngresos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val ingresosRef = database.getReference("users").child(uid).child("ingresos")

            ingresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ingresosList.clear() // Limpiar la lista antes de agregar nuevos elementos
                    var totalIngresos = 0.0

                    for (ingresosSnapshot in snapshot.children) {
                        val ingresos = ingresosSnapshot.getValue(Ingresos::class.java)
                        if (ingresos != null) {
                            ingresosList.add(ingresos)

                            // Eliminar comas antes de convertir a double
                            val valorSinComas = ingresos.valor.replace(",", "")
                            totalIngresos += valorSinComas.toDouble()
                        }
                    }

                    setupRecyclerView()

                    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                    tvTotalPayments.text = numberFormat.format(totalIngresos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("IngresosFragment", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = IngresosAdapter(ingresosList)
        rvIngresos.layoutManager = LinearLayoutManager(requireContext())
        rvIngresos.adapter = adapter

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
                val itemToRemove = ingresosList[position]
                showDeleteConfirmationDialog(position, itemToRemove)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvIngresos)
    }

    private fun showDeleteConfirmationDialog(position: Int, itemToRemove: Ingresos) {
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

    private fun deleteItemFromFirebase(position: Int, itemToRemove: Ingresos) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val ingresosRef = database.getReference("users").child(uid).child("ingresos")
            ingresosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ingresosSnapshot in snapshot.children) {
                        val ingresos = ingresosSnapshot.getValue(Ingresos::class.java)
                        if (ingresos != null && ingresosMatches(ingresos, itemToRemove)) {
                            ingresosSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Elimina el elemento de la lista local
                                    adapter.removeItem(position)

                                    // Vuelve a calcular el total después de la eliminación
                                    fetchIngresos()
                                } else {
                                    // Manejar error en la eliminación de Firebase
                                    Log.e("IngresosFragment", "Error al eliminar el elemento de Firebase")
                                    adapter.notifyItemChanged(position)
                                }
                            }
                            break // Sal del bucle después de encontrar la coincidencia
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("IngresosFragment", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun ingresosMatches(ingresos: Ingresos, itemToRemove: Ingresos): Boolean {
        // Verifica si los campos coinciden
        return ingresos.nombre == itemToRemove.nombre &&
                ingresos.descripcion == itemToRemove.descripcion &&
                ingresos.valor == itemToRemove.valor
    }

    data class Ingresos(
        val valor: String = "",
        val categoria: String = "",
        val nombre: String = "",
        val descripcion: String = "",
        val fecha: String = ""
    )
}