package com.example

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_finanzas.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections
import kotlin.math.log

class Pagos : AppCompatActivity() {

    private val pagosList = mutableListOf<Pago>()
    object CategoriaSingleton {
        val categories = ArrayList<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        obtenerDatosUsuario()
        obtenerDatosPago()
        setup_categories()

        val btn_addpago = findViewById<Button>(R.id.btn_addpago)
        btn_addpago.setOnClickListener {
            val intent = Intent(this, Ingreso_pagos::class.java)
            startActivity(intent)
        }

        val btnsingOut = findViewById<Button>(R.id.btnsignout)
        btnsingOut.setOnClickListener {
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
            // Puedes implementar lógica para cambiar el orden de los elementos si es necesario
            Collections.swap(pagos, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemDismiss(position: Int) {
            val pago = pagos[position]

            // Elimina la mascota de Firebase usando la clave única
            eliminarPagoEnFirebase(pago.id)

            // Elimina la mascota de la lista local y notifica al adaptador
            notifyItemRemoved(position)
        }

        fun eliminarPagoEnFirebase(pagoId: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.uid?.let { uid ->
                val dbReference =
                    FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("PAGOS")

                // Elimina la mascota de Firebase utilizando su clave única
                dbReference.child(pagoId).removeValue()
            }
        }
    }

    private fun obtenerDatosPago() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("PAGOS")

            // Escuchar cambios en los datos de mascotas
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

                        // Actualizar los adaptadores con la lista de mascotas
                        actualizarAdaptadores(pagosList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores de lectura de la base de datos
                    Log.e("LecturaBD", "Error al leer datos de mascotas: ${error.message}")
                }
            })
        }
    }

    private fun actualizarAdaptadores(pagosList: List<Pago>) {
        Log.e("DEBUG", "Número de mascotas: ${pagosList.size}")
        // Separar los procesos en sus respectivos hilos
        runOnUiThread {
            // Obtener referencias a los RecyclerView
            val rc_pagos = findViewById<RecyclerView>(R.id.recycle_pagos)
            rc_pagos.layoutManager = LinearLayoutManager(this@Pagos)


            // Crear adaptadores con la lista de mascotas
            val pago_adapter = PagosAdapter(pagosList.map { Pago(it.id, it.categoria, it.fecha, it.monto, it.nombre) })

            // Adjuntar ItemTouchHelper al RecyclerView
            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // Llama a la función onItemMove del adaptador al mover un elemento
                    pago_adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Llama a la función onItemDismiss del adaptador al deslizar un elemento
                    pago_adapter.onItemDismiss(viewHolder.adapterPosition)
                }
            })

            itemTouchHelper.attachToRecyclerView(rc_pagos)

            // Notificar a los adaptadores que los datos han cambiado
            pago_adapter.notifyDataSetChanged()

            // Configurar los adaptadores en los RecyclerView correspondientes
            rc_pagos.adapter = pago_adapter

            // Notificar a los adaptadores que los datos han cambiado
            pago_adapter.notifyDataSetChanged()
        }
    }

    private fun setup_categories(){

        val spinner_Categories: Spinner = findViewById(R.id.spinner_categories)
        val textCategories_Selected: TextView = findViewById(R.id.tv_categorias)

        spinner_Categories.prompt = "Categorías"

        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner_Categories.adapter = adapter
        }


        spinner_Categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                // Actualizar el TextView con la categoría seleccionada
                textCategories_Selected.text = parent.getItemAtPosition(position)?.toString() ?: ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

    }

    private fun signOut() {
        Firebase.auth.signOut()
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
                        // Obtiene los datos del usuario desde la base de datos
                        val username = snapshot.child("name").getValue(String::class.java)


                        // Llama al método en la interfaz para actualizar la interfaz de usuario
                        onUserDataUpdated(username ?: "") // Si username es nulo, usa una cadena vacía
                    } else {
                        // El usuario no tiene datos en la base de datos
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Maneja el error de lectura de la base de datos
                    Log.e("LecturaBD", "Error al leer datos: ${error.message}")
                }
            })
        }
    }

    fun onUserDataUpdated(username: String) {
        runOnUiThread{
            // Actualiza el TextView con los datos obtenidos
            val txtnamee = findViewById<TextView>(R.id.tvnameuser)
            txtnamee?.text = username
        }
    }
}
