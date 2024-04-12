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


        btnpayments.setOnClickListener {
            val intent = Intent(this, Pagos::class.java)
            startActivity(intent)
        }

    }
}