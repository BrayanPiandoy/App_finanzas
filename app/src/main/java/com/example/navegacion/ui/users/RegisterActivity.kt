package com.example.navegacion.ui.users

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.navegacion.MainActivity
import com.example.navegacion.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        val home = findViewById<ImageButton>(R.id.btn_Return)
        val etMail = findViewById<EditText>(R.id.etMail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etName = findViewById<EditText>(R.id.etName)
        val registrar: Button = findViewById(R.id.btn_registro)
        home.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        registrar.setOnClickListener {
            val email = etMail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()
            if (email.isEmpty()) {
                etMail.error = "Email cannot be empty"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                createAccount(email, password, name)
            }
        }
    }

    private fun createAccount(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserData(user.uid, name, email)
                        Toast.makeText(baseContext, "Registration successful.",
                            Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        // Ocultar la barra de acción
        supportActionBar?.hide()
    }

    private fun saveUserData(uid: String, name: String, email: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(uid)

        val userData = HashMap<String, Any>()
        userData["name"] = name
        userData["email"] = email

        userRef.child("datos").setValue(userData)
            .addOnSuccessListener {
                // Datos guardados correctamente
                Toast.makeText(baseContext, "User data saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al guardar los datos
                Toast.makeText(baseContext, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
