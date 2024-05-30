package com.example

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.app_finanzas.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.lang.ref.Reference
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

class Ingreso_pagos : AppCompatActivity() {

    private val categories = ArrayList<String>()
    private lateinit var dbReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingreso_pagos)

        val btn_addpago = findViewById<Button>(R.id.btn_registropago)
        val etdate = findViewById<EditText>(R.id.etDate)
        val etMonto = findViewById<EditText>(R.id.etMonto)
        val etName = findViewById<EditText>(R.id.etName)
        val autoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.autoCompleteTextView)

        dbReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        consultarCategorias()


        // Configuramos el adaptador para el MaterialAutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.apply {
            isFocusable = false  // Deshabilita el foco
            isClickable = true   // Hace que el TextView sea clickeable
            isLongClickable = false // Deshabilita el clic largo
            setTextIsSelectable(false) // Deshabilita la selección de texto
        }

        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }

        val addCategoryButton = findViewById<Button>(R.id.add_category)
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }

        val removeCategoryButton = findViewById<Button>(R.id.remove_category)
        removeCategoryButton.setOnClickListener {
            showAddCategoryDialog_remove()
        }

        val etDate = findViewById<EditText>(R.id.etDate)
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Crear y mostrar el DatePickerDialog
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->

                // Formatear la fecha seleccionada
                val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)

                // Establecer el texto formateado en el EditText
                etDate.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }


        btn_addpago.setOnClickListener {
            val category = autoCompleteTextView.text.toString().trim()
            val date = etdate.text.toString().trim()
            val monto = etMonto.text.toString().trim()
            val nombre = etName.text.toString().trim()

            // Validar el monto
            if (monto.isEmpty() || monto.toInt() == 0 || monto.toInt() > 1000000000) {
                Toast.makeText(baseContext, "Ingrese un monto válido (mayor a 0 y menor o igual a 1000000000)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar el nombre
            if (nombre.isEmpty() || nombre.length > 30) {
                Toast.makeText(baseContext, "Ingrese un nombre válido (no vacío y máximo 30 caracteres)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar la categoría
            if (category.isEmpty()) {
                Toast.makeText(baseContext, "Debe seleccionar una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar la fecha (puedes agregar la validación según tu formato)
            if (date.isEmpty()) {
                Toast.makeText(baseContext, "Debe ingresar una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si pasa todas las validaciones, registrar el pago
            registrarPago(category, date, monto, nombre)
        }


    }

    private fun registrarPago(category: String, date: String, monto: String, nombre: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val montoInt = monto.toInt()

            val userDB = dbReference.child("USUARIO").child(userId)

            // Generar un nuevo identificador único para los pagos
            val pagoId = userDB.child("PAGOS").push().key

            // Guardar la información del pago en el nodo del usuario
            val nuevoPago = hashMapOf(
                "categoria" to category,
                "fecha" to date,
                "monto" to monto,
                "nombre" to nombre
            )
            userDB.child("PAGOS").child(pagoId!!).setValue(nuevoPago).addOnCompleteListener {
                if (it.isSuccessful) {
                    // Actualizar el total de pagos después de registrar el nuevo pago
                    actualizarTotalPagos(userId, montoInt)

                    Toast.makeText(baseContext, "Pago registrado exitosamente", Toast.LENGTH_SHORT).show()

                    val etdate = findViewById<EditText>(R.id.etDate)
                    val etMonto = findViewById<EditText>(R.id.etMonto)
                    val etName = findViewById<EditText>(R.id.etName)
                    val autoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.autoCompleteTextView)

                    // Limpiar los campos después de registrar el pago
                    autoCompleteTextView.setText("")
                    etdate.setText("")
                    etMonto.setText("")
                    etName.setText("")
                } else {
                    Toast.makeText(baseContext, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addCategory(categoryName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("CATEGORIAS")
            val newCategoryRef = dbReference.push()
            newCategoryRef.setValue(categoryName).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Categoría añadida correctamente", Toast.LENGTH_SHORT).show()
                    consultarCategorias() // Recargar categorías
                } else {
                    Toast.makeText(this, "Error al añadir la categoría", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun consultarCategorias() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("CATEGORIAS")

            dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categories.clear()
                    if (snapshot.exists()) {
                        for (categorySnapshot in snapshot.children) {
                            val category = categorySnapshot.getValue(String::class.java)
                            category?.let { categories.add(it) }
                        }
                        updateCategoryAdapter()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al cargar categorías: ${error.message}")
                }
            })
        }
    }

    private fun eliminarCategoria(categoryName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val dbReference = FirebaseDatabase.getInstance().getReference("USUARIO").child(uid).child("CATEGORIAS")

            dbReference.orderByValue().equalTo(categoryName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) { // Check if any category exists with the given name
                        for (categorySnapshot in snapshot.children) {
                            categorySnapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@Ingreso_pagos, "Categoría eliminada correctamente", Toast.LENGTH_SHORT).show()
                                    categories.remove(categoryName)
                                    updateCategoryAdapter()
                                } else {
                                    Toast.makeText(this@Ingreso_pagos, "Error al eliminar la categoría", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@Ingreso_pagos, "Categoría no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al eliminar categoría: ${error.message}")
                }
            })
        }
    }
    private fun updateCategoryAdapter() {
        val autoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.autoCompleteTextView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva Categoría")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Agregar") { dialog, _ ->
            val categoryName = input.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                addCategory(categoryName)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "El nombre de la categoría a eliminar no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }


    private fun showAddCategoryDialog_remove() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar Categoría")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Eliminar") { dialog, _ ->
            val categoryName = input.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                eliminarCategoria(categoryName)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "El nombre de la categoría no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun actualizarTotalPagos(userId: String, monto: Int) {
        val userDB = dbReference.child("USUARIO").child(userId)
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
