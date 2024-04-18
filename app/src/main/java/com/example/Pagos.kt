package com.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.example.app_finanzas.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class Pagos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        val btnsingOut = findViewById<Button>(R.id.btnsignout)

        setup_categories()

        btnsingOut.setOnClickListener {
            signOut()
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
                textCategories_Selected.text = parent.getItemAtPosition(position).toString()
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
}