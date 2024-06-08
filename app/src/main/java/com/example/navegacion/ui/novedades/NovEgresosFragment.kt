package com.example.navegacion.ui.novedades

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.navegacion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class NovEgresosFragment : MovimientoFinancieroFragment() {
    override var categoryType = "egresos"

    override fun saveMovimiento() {
        val valor = etValor.text.toString().trim()
        val categoria = sCategorias.selectedItem.toString().trim()
        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()

        if (valor.isEmpty() || categoria.isEmpty() || nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val egresosRef = database.getReference("users").child(uid).child("egresos").push()

            val currentDate = etFecha.text.toString()
            val egresosData = hashMapOf(
                "valor" to valor,
                "categoria" to categoria,
                "nombre" to nombre,
                "descripcion" to descripcion,
                "fecha" to currentDate
            )

            egresosRef.setValue(egresosData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Egreso guardado con éxito", Toast.LENGTH_SHORT).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar el egreso: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            activity?.finish()
        }
    }
}
