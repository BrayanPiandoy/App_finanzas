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

class NovEgresosFragment : Fragment() {

    private val categorias = listOf("Alimentación", "Transporte", "Educación", "Entretenimiento", "Salud", "Compras", "Otros")

    private lateinit var etValor: EditText
    private lateinit var sCategorias: Spinner
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btNuevo: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var listener: OnSavedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nov_egresos, container, false)

        etValor = view.findViewById(R.id.etValor)
        sCategorias = view.findViewById(R.id.sCategorias)
        etNombre = view.findViewById(R.id.etNombre)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        btNuevo = view.findViewById(R.id.btNuevo)

        // Formatear el número con puntos como separadores de miles
        etValor.addTextChangedListener(object : TextWatcher {
            private val decimalFormat = DecimalFormat("#,###.##")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Eliminar el listener para evitar ciclos infinitos al actualizar el texto
                etValor.removeTextChangedListener(this)

                // Formatear el número con puntos para los miles
                val parsed = s.toString().replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
                val formatted = decimalFormat.format(parsed)
                etValor.setText(formatted)
                etValor.setSelection(formatted.length)

                // Restaurar el listener
                etValor.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Crea un adaptador para el Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sCategorias.adapter = adapter

        // Verifica si el parentFragment implementa OnEgresoSavedListener
        if (parentFragment is OnSavedListener) {
            listener = parentFragment as OnSavedListener
        }

        btNuevo.setOnClickListener {
            saveEgreso()
        }

        return view
    }

    private fun saveEgreso() {
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
            val egresoRef = database.getReference("users").child(uid).child("egresos").push()

            val currentDate = getCurrentDate()
            val egresoData = hashMapOf(
                "valor" to valor,
                "categoria" to categoria,
                "nombre" to nombre,
                "descripcion" to descripcion,
                "fecha" to currentDate
            )

            egresoRef.setValue(egresoData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Egreso guardado con éxito", Toast.LENGTH_SHORT).show()
                    clearFields()
                    listener?.onSaved() // Notifica al listener
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar el egreso: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun clearFields() {
        etValor.text.clear()
        etNombre.text.clear()
        etDescripcion.text.clear()
        sCategorias.setSelection(0)
    }
}
