package com.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.app_finanzas.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.ref.Reference

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

        categories.add("Categoría 1")
        categories.add("Categoría 2")
        categories.add("Categoría 3")

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

        btn_addpago.setOnClickListener {
            val category = autoCompleteTextView.text.toString().trim()
            val date = etdate.text.toString().trim()
            val monto = etMonto.text.toString().trim()
            val nombre = etName.text.toString().trim()

            if(category.isEmpty()){
                Toast.makeText(baseContext, "Debe seleccionar una categoría", Toast.LENGTH_SHORT).show()
            } else if(date.isEmpty()){
                Toast.makeText(baseContext, "Debe ingresar una fecha", Toast.LENGTH_SHORT).show()
            } else if(monto.isEmpty()){
                Toast.makeText(baseContext, "Debe ingresar un monto", Toast.LENGTH_SHORT).show()
            } else if(nombre.isEmpty()){
                Toast.makeText(baseContext, "Debe ingresar un nombre", Toast.LENGTH_SHORT).show()
            } else {
                registrarPago(category, date, monto, nombre)
            }
        }

    }

    private fun registrarPago(category: String, date: String, monto: String, nombre: String) {
        val userId = auth.currentUser?.uid
        if(userId != null){
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
            userDB.child("PAGOS").child(pagoId!!).setValue(nuevoPago)

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
        }

    }


    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva Categoría")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Agregar") { dialog, _ ->
            val categoryName = input.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                categories.add(categoryName)
                // Aquí puedes actualizar tu UI con las nuevas categorías
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }


}
