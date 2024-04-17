package com.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.app_finanzas.R

class LogIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val btnRegistrar = findViewById<Button>(R.id.btnSignIn)

        btnRegistrar.setOnClickListener {
            val intent = Intent(this,SigIn::class.java)
            startActivity(intent)
        }
    }
}