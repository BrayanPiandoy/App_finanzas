package com.example.navegacion.ui.novedades

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.navegacion.R
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Obtener la fecha actual
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Crear una nueva instancia del selector de fecha y devolverla
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Obtener la fecha seleccionada y establecerla en el EditText
        val selectedDate = "$year-${month + 1}-$dayOfMonth"
        val editText = activity?.findViewById<EditText>(R.id.etFecha)
        editText?.setText(selectedDate)
    }
}
