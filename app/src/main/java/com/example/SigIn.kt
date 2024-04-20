package com.example

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.app_finanzas.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SigIn : AppCompatActivity() {
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sig_in)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("User")

        val btnReturn = findViewById<Button>(R.id.btn_Return)
        val btnRegister = findViewById<Button>(R.id.btn_registro)

        btnReturn.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            register()

        }
    }

    private fun register(){
        val name = findViewById<EditText>(R.id.etName).text.toString()
        val email = findViewById<EditText>(R.id.etUser).text.toString()
        val password = findViewById<EditText>(R.id.etPassword).text.toString()

        if(name.isEmpty()){
            Toast.makeText(this, "El campo Nombre está vacío", Toast.LENGTH_SHORT).show()
        }else if(email.isEmpty()){
            Toast.makeText(this, "El campo Correo está vacío", Toast.LENGTH_SHORT).show()
        }else if(password.isEmpty()||password.length<=5){
            Toast.makeText(this, "La contraseña debe tener minimo 6 caracteres", Toast.LENGTH_SHORT).show()
        }else{
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser

                        Log.d("UID", "UID del usuario: ${user?.uid.toString()}")
                        val userDB = dbReference.child(user?.uid.toString())

                        userDB.child("name").setValue(name)
                        userDB.child("email").setValue(email)
                        userDB.child("password").setValue(password)

                        val intent = Intent(this, LogIn::class.java)
                        startActivity(intent)

                        Toast.makeText(baseContext, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
}