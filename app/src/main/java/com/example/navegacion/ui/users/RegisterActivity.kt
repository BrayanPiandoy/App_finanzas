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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

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
        val etPassword2 = findViewById<EditText>(R.id.etPassword2)
        val etName = findViewById<EditText>(R.id.etName)
        val btnShowPassword = findViewById<ImageButton>(R.id.btnShowPassword)
        val btnShowPassword2 = findViewById<ImageButton>(R.id.btnShowPassword2)

        val registrar: Button = findViewById(R.id.btn_registro)
        home.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Función para mostrar u ocultar las contraseñas
        btnShowPassword.setOnClickListener {
            val isPasswordVisible = etPassword.inputType == 129
            etPassword.inputType = if (isPasswordVisible) 145 else 129
        }
        btnShowPassword2.setOnClickListener {
            val isPasswordVisible = etPassword.inputType == 129
            etPassword2.inputType = if (isPasswordVisible) 145 else 129
        }


        registrar.setOnClickListener {
            val email = etMail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val password2 = etPassword2.text.toString().trim()
            val name = etName.text.toString().trim()

            // Verificar que las contraseñas coincidan
            if (password != password2) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar que el correo sea un correo electrónico válido
            if (!isValidEmail(email)) {
                Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

        supportActionBar?.hide()
    }

    // Función para verificar si un correo electrónico es válido
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        )
        return emailRegex.matcher(email).matches()
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

        // Guardar datos del usuario
        val userData = HashMap<String, Any>()
        userData["name"] = name
        userData["email"] = email

        userRef.child("datos").setValue(userData)
            .addOnSuccessListener {
                // Datos del usuario guardados correctamente
                Toast.makeText(baseContext, "User data saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al guardar los datos del usuario
                Toast.makeText(baseContext, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Guardar listas de categorías
        saveCategories(userRef, "ahorros", listOf(
            "Todos",
            "Cuenta de ahorros",
            "Fondo de emergencia",
            "CDT",
            "Ahorros educativos",
            "Inversiones a largo plazo",
            "Planes de retiro",
            "Plan de pensiones",
            "Cuenta de jubilación",
        ))
        saveCategories(userRef, "ingresos", listOf(
            "Todos",
            "Salario",
            "Pensión",
            "Bonificaciones",
            "Subsidios",
            "Dividendos de acciones",
            "Venta de productos",
            "Ingresos por servicios",
            "Bonificaciones",
            "Ingresos por alquiler",
            "Ingresos por inversiones"
        ))
        saveCategories(userRef, "egresos", listOf(
            "Todos",
            "Alquiler",
            "Servicios públicos",
            "Transporte",
            "Alimentación",
            "Seguro",
            "Ropa y calzado",
            "Entretenimiento",
            "Salud",
            "Deudas",
            "Otros gastos"
        ))
    }

    private fun saveCategories(userRef: DatabaseReference, category: String, categoryList: List<String>) {
        userRef.child("categorias").child(category).setValue(categoryList)
            .addOnSuccessListener {
                // Lista de categorías guardada correctamente
                Toast.makeText(baseContext, "User $category saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al guardar la lista de categorías
                Toast.makeText(baseContext, "Failed to save user $category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}