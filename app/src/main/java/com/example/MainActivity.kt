package com.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.app_finanzas.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnpayments = findViewById<Button>(R.id.payments)
        val btnlogin = findViewById<Button>(R.id.logIn)
        val btnIngresos = findViewById<Button>(R.id.Ingresos)
        val btnGastos = findViewById<Button>(R.id.Gastos)


        btnpayments.setOnClickListener {
            val intent = Intent(this, Pagos::class.java)
            startActivity(intent)
        }

        btnlogin.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        btnIngresos.setOnClickListener {
            val intent = Intent(this, IngresosActivity::class.java)
            startActivity(intent)
        }

        btnGastos.setOnClickListener {
            val intent = Intent(this, GastosActivity2::class.java)
            startActivity(intent)
        }

    }
}