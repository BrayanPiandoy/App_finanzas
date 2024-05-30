package com.example

import android.content.Context
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
        loadCategoriesAndFilterPayments()
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

    class PagosAdapter(private val context: Context, private val pagos: List<Pago>) :
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
            eliminarPagoEnFirebase(pago.id, pago.monto.toInt())
            notifyItemRemoved(position)
        }

        private fun eliminarPagoEnFirebase(pagoId: String, monto: Int) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.uid?.let { uid ->
                val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("PAGOS")
                dbReference.child(pagoId).removeValue().addOnCompleteListener {
                        task ->
                    if (task.isSuccessful) {
                        actualizarTotalPagos(uid, -monto)
                        Toast.makeText(context, "Pago eliminado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al eliminar el pago", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun actualizarTotalPagos(userId: String, monto: Int) {
            val userDB = FirebaseDatabase.getInstance().getReference("USUARIO").child(userId)
            val totalPagosRef = userDB.child("TOTAL_PAGOS")

            totalPagosRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    var currentTotal = mutableData.getValue(Int::class.java) ?: 0
                    mutableData.value = currentTotal + monto
                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (databaseError != null) {
                        Log.w("Firebase", "TotalPagos:onComplete", databaseError.toException())
                    }
                }
            })
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

            val pagoAdapter = PagosAdapter(this@Pagos, pagosList.map { Pago(it.id, it.categoria, it.fecha, it.monto, it.nombre) })

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


    private fun loadCategoriesAndFilterPayments() {
        val spinnerCategories: Spinner = findViewById(R.id.spinner_categories)
        val textCategoriesSelected: TextView = findViewById(R.id.tv_categorias)

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("CATEGORIAS")

            dbReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val categories = mutableListOf<String>()

                        // Agregar la opción por defecto "General"
                        categories.add("General")

                        // Recorrer todas las categorías y agregarlas a la lista
                        for (categorySnapshot in snapshot.children) {
                            val category = categorySnapshot.getValue(String::class.java)
                            category?.let { categories.add(it) }
                        }

                        // Crear un adaptador para el Spinner con las categorías obtenidas
                        val adapter = ArrayAdapter(
                            this@Pagos,
                            android.R.layout.simple_spinner_item,
                            categories
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerCategories.adapter = adapter

                        // Establecer un listener para cuando se seleccione una categoría
                        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val selectedCategory = parent?.getItemAtPosition(position).toString()

                                // Actualizar el TextView con la categoría seleccionada
                                textCategoriesSelected.text = selectedCategory

                                // Filtrar los pagos según la categoría seleccionada
                                if (selectedCategory == "General") {
                                    // Mostrar todos los pagos si se selecciona "General"
                                    actualizarAdaptadores(pagosList)
                                } else {
                                    // Filtrar los pagos por la categoría seleccionada
                                    val filteredPayments = pagosList.filter { it.categoria == selectedCategory }
                                    actualizarAdaptadores(filteredPayments)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // No se realiza ninguna acción si no se selecciona nada
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LecturaBD", "Error al leer datos de categorías: ${error.message}")
                }
            })
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
