package com.example.navegacion.ui.novedades



import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.navegacion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class MovimientoFinancieroFragment : Fragment() {
    protected open lateinit var categoryType: String
    private val categorias: MutableList<String> = mutableListOf()

    protected lateinit var etValor: EditText
    protected lateinit var sCategorias: Spinner
    protected lateinit var etNombre: EditText
    protected lateinit var etDescripcion: EditText
    protected lateinit var btNuevo: Button
    protected lateinit var database: FirebaseDatabase
    protected lateinit var auth: FirebaseAuth
    protected lateinit var etFecha: EditText
    protected lateinit var ibAddCategoria: ImageButton
    protected lateinit var ibRemoveCategoria: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchCategories(categoryType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nov_todos, container, false)


        etValor = view.findViewById(R.id.etValor)
        sCategorias = view.findViewById(R.id.sCategorias)
        etNombre = view.findViewById(R.id.etNombre)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        btNuevo = view.findViewById(R.id.btNuevo)
        etFecha = view.findViewById(R.id.etFecha)
        ibAddCategoria = view.findViewById(R.id.ib_addCategoria)
        ibRemoveCategoria = view.findViewById(R.id.ib_removeCategoria)


        val tvCuenta = view.findViewById<TextView>(R.id.tv_Cuenta)
        tvCuenta.text = "Registra un nuevo\n$categoryType"


        ibAddCategoria.setOnClickListener {
            mostrarDialogoAgregarCategoria()
            fetchCategories(categoryType)
        }

        ibRemoveCategoria.setOnClickListener {
            // Aquí implementa la lógica para quitar la categoría seleccionada
            // Por ejemplo:
            mostrarDialogoQuitarCategoria()
            fetchCategories(categoryType)
        }
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

        btNuevo.setOnClickListener {
            saveMovimiento()
        }

        // Asignar OnClickListener al EditText etFecha
        etFecha.setOnClickListener {
            showDatePickerDialog()
        }

        // Llama a fetchCategories para obtener las categorías desde Firebase
        fetchCategories(categoryType) // Se llama a fetchCategories con el argumento "ahorros"

        return view
    }

    protected abstract fun saveMovimiento()

    protected fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    protected fun showDatePickerDialog() {
        val newFragment = DatePickerFragment()
        newFragment.show(childFragmentManager, "datePicker")
    }

    protected fun clearFields() {
        etValor.text.clear()
        etNombre.text.clear()
        etDescripcion.text.clear()
        sCategorias.setSelection(0)
    }

    private fun fetchCategories(categoryType: String) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val categoriasRef = database.getReference("users").child(uid).child("categorias").child(categoryType)

            categoriasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    categorias.clear()

                    for (categoriaSnapshot in dataSnapshot.children) {
                        val categoria = categoriaSnapshot.getValue(String::class.java)
                        categoria?.let {
                            categorias.add(it)
                        }
                    }

                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    sCategorias.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Fragment", "Error fetching categories: ${databaseError.message}")
                }
            })
        }
    }

    private fun agregarCategoriaAFirebase(nuevaCategoria: String) {
        val user = auth.currentUser
        val uid = user?.uid
        val categoriasRef = uid?.let { database.getReference("users").child(it).child("categorias").child(categoryType) }

        // Recuperar la lista actual de categorías desde Firebase
        categoriasRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categorias = mutableListOf<String>()

                // Recorrer los elementos existentes y agregarlos a la lista
                for (categoriaSnapshot in dataSnapshot.children) {
                    val categoria = categoriaSnapshot.getValue(String::class.java)
                    categoria?.let {
                        categorias.add(it)
                    }
                }

                // Agregar la nueva categoría a la lista
                categorias.add(nuevaCategoria)

                // Guardar la lista actualizada en Firebase
                categoriasRef.setValue(categorias)
                    .addOnSuccessListener {
                        Log.d("AgregarCategoria", "Nueva categoría añadida correctamente a Firebase")
                        // Actualizar el Spinner u otras vistas si es necesario
                        fetchCategories(categoryType)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AgregarCategoria", "Error al añadir nueva categoría a Firebase: $e")
                        // Aquí puedes manejar el error, si es necesario
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AgregarCategoria", "Error fetching categories: ${databaseError.message}")
            }
        })
    }

    private fun mostrarDialogoAgregarCategoria() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialogo_agregar_categoria, null)
        val editTextCategoria = dialogView.findViewById<EditText>(R.id.et_nueva_categoria)

        builder.setView(dialogView)
            .setTitle("Agregar Categoría")
            .setPositiveButton("Agregar") { dialog, _ ->
                val nuevaCategoria = editTextCategoria.text.toString().trim()
                if (nuevaCategoria.isNotEmpty()) {
                    // Agrega la nueva categoría a Firebase y actualiza el Spinner
                    agregarCategoriaAFirebase(nuevaCategoria)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun eliminarCategoriaDeFirebase(indiceCategoria: Int) {
        // Recuperar la lista actual de categorías desde Firebase
        val user = auth.currentUser
        val uid = user?.uid
        val categoriasRef = uid?.let { database.getReference("users").child(it).child("categorias").child(categoryType) }

        categoriasRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categorias = mutableListOf<String>()

                // Recorrer los elementos existentes y agregarlos a la lista
                for (categoriaSnapshot in dataSnapshot.children) {
                    val categoria = categoriaSnapshot.getValue(String::class.java)
                    categoria?.let {
                        categorias.add(it)
                    }
                }

                // Eliminar la categoría seleccionada de la lista
                if (indiceCategoria >= 0 && indiceCategoria < categorias.size) {
                    categorias.removeAt(indiceCategoria)
                }

                // Guardar la lista actualizada en Firebase
                categoriasRef.setValue(categorias)
                    .addOnSuccessListener {
                        Log.d("EliminarCategoria", "Categoría eliminada correctamente de Firebase")
                        // Actualizar el Spinner u otras vistas si es necesario
                        fetchCategories(categoryType)
                    }
                    .addOnFailureListener { e ->
                        Log.e("EliminarCategoria", "Error al eliminar categoría de Firebase: $e")
                        // Aquí puedes manejar el error, si es necesario
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EliminarCategoria", "Error fetching categories: ${databaseError.message}")
            }
        })
    }
    private fun mostrarDialogoQuitarCategoria() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Quitar Categoría")

        // Crear una lista de las categorías disponibles
        val categoriasArray = categorias.toTypedArray()

        // Configurar el diálogo con un ListView que muestre las categorías
        builder.setItems(categoriasArray) { dialog, which ->
            // which representa el índice de la categoría seleccionada en la lista

            // Mostrar un diálogo de confirmación antes de eliminar la categoría
            val confirmDialogBuilder = AlertDialog.Builder(requireContext())
            confirmDialogBuilder.setTitle("Confirmar")
                .setMessage("¿Estás seguro de que deseas quitar esta categoría?")
                .setPositiveButton("Sí") { _, _ ->
                    eliminarCategoriaDeFirebase(which)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Mostrar el diálogo con las opciones de categorías
        builder.create().show()
    }
}