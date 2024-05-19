package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_finanzas.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Collections

class Pagos : AppCompatActivity() {

    private lateinit var dbReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val pagosList = mutableListOf<Pago>()

    object CategoriaSingleton {
        val categories = ArrayList<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        auth = FirebaseAuth.getInstance()
        dbReference = FirebaseDatabase.getInstance().reference

        obtenerDatosUsuario()
        obtenerDatosPago()
        setup_categories()
        mostrarTotalPagos()

        val btnAddPago = findViewById<Button>(R.id.btn_addpago)
        btnAddPago.setOnClickListener {
            val intent = Intent(this, Ingreso_pagos::class.java)
            startActivity(intent)
        }

        val btnSignOut = findViewById<Button>(R.id.btnsignout)
        btnSignOut.setOnClickListener {
            signOut()
        }
    }

    data class Pago(val id: String, val categoria: String, val fecha: String, val monto: String, val nombre: String)

    interface ItemTouchHelperAdapter {
        fun onItemMove(fromPosition: Int, toPosition: Int)
        fun onItemDismiss(position: Int)
    }

    class PagosAdapter(private val pagos: List<Pago>) :
        RecyclerView.Adapter<PagosAdapter.ViewHolder>(), ItemTouchHelperAdapter {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.spinner_dropdown_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pago = pagos[position]
            holder.textView.text = "${pago.categoria} - ${pago.fecha} - ${pago.monto} - ${pago.nombre}"
        }

        override fun getItemCount(): Int {
            return pagos.size
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            Collections.swap(pagos, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemDismiss(position: Int) {
            val pago = pagos[position]
            eliminarPagoEnFirebase(pago.id)
            notifyItemRemoved(position)
        }

        private fun eliminarPagoEnFirebase(pagoId: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.uid?.let { uid ->
                val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("PAGOS")
                dbReference.child(pagoId).removeValue()
            }
        }
    }

    private fun obtenerDatosPago() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("PAGOS")

            dbReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        pagosList.clear()

                        for (pagoSnapshot in snapshot.children) {
                            val categoria = pagoSnapshot.child("categoria").getValue(String::class.java)
                            val fecha = pagoSnapshot.child("fecha").getValue(String::class.java)
                            val monto = pagoSnapshot.child("monto").getValue(String::class.java)
                            val nombre = pagoSnapshot.child("nombre").getValue(String::class.java)

                            if (categoria != null && fecha != null && monto != null && nombre != null) {
                                pagosList.add(Pago(pagoSnapshot.key!!, categoria, fecha, monto, nombre))
                            }
                        }

                        actualizarAdaptadores(pagosList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LecturaBD", "Error al leer datos de pagos: ${error.message}")
                }
            })
        }
    }

    private fun actualizarAdaptadores(pagosList: List<Pago>) {
        runOnUiThread {
            val rcPagos = findViewById<RecyclerView>(R.id.recycle_pagos)
            rcPagos.layoutManager = LinearLayoutManager(this@Pagos)

            val pagoAdapter = PagosAdapter(pagosList.map { Pago(it.id, it.categoria, it.fecha, it.monto, it.nombre) })

            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    pagoAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    pagoAdapter.onItemDismiss(viewHolder.adapterPosition)
                }
            })

            itemTouchHelper.attachToRecyclerView(rcPagos)
            rcPagos.adapter = pagoAdapter
            pagoAdapter.notifyDataSetChanged()
        }
    }

    private fun setup_categories() {
        val spinnerCategories: Spinner = findViewById(R.id.spinner_categories)
        val textCategoriesSelected: TextView = findViewById(R.id.tv_categorias)

        spinnerCategories.prompt = "Categorías"

        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategories.adapter = adapter
        }

        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                textCategoriesSelected.text = parent.getItemAtPosition(position)?.toString() ?: ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No es necesario implementar esto si no es necesario
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
        finish()
    }

    private fun obtenerDatosUsuario() {
        val user = FirebaseAuth.getInstance().currentUser
        val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO")
        user?.uid?.let { uid ->
            val userDB = dbReference.child(uid)

            userDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("name").getValue(String::class.java)
                        onUserDataUpdated(username ?: "")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LecturaBD", "Error al leer datos: ${error.message}")
                }
            })
        }
    }

    fun onUserDataUpdated(username: String) {
        runOnUiThread {
            val txtName = findViewById<TextView>(R.id.tvnameuser)
            txtName?.text = username
        }
    }

    // este metodo consulta total de pagos en la BD y los muestra en un texview
    private fun mostrarTotalPagos() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userDB = dbReference.child("USUARIO").child(userId)
            val totalPagosRef = userDB.child("TOTAL_PAGOS")

            totalPagosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalPagos = snapshot.getValue(Int::class.java) ?: 0
                    val tvTotalPayments = findViewById<TextView>(R.id.tv_total_payments)
                    tvTotalPayments.text = "Total Pagos: $totalPagos"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("Firebase", "Error al leer el total de pagos", error.toException())
                }
            })
        }
    }
}
