package com.example.navegacion.ui.egresos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EgresosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Egresos Fragment"
    }
    val text: LiveData<String> = _text
}