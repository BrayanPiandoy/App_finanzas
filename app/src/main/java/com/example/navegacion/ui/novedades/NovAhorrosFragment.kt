package com.example.navegacion.ui.novedades

import android.widget.Toast

class NovAhorrosFragment : MovimientoFinancieroFragment() {
    override var categoryType = "ahorros"

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
            val ahorrosRef = database.getReference("users").child(uid).child("ahorros").push()

            val currentDate = etFecha.text.toString()
            val ahorrosData = hashMapOf(
                "valor" to valor,
                "categoria" to categoria,
                "nombre" to nombre,
                "descripcion" to descripcion,
                "fecha" to currentDate
            )

            ahorrosRef.setValue(ahorrosData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Ahorro guardado con éxito", Toast.LENGTH_SHORT).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar el ahorro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            activity?.finish()

        }
    }
}
