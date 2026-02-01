package com.example.climahoy.utils.extensions

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.applyWindowKeyboardInsetsTo(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        // Obtener los márgenes para el teclado si está visible
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        // Calcula el padding inferior máximo (teclado o barra de navegación, el que sea mayor)
        val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
        // Aplica el padding
        v.setPadding(0, systemBars.top, 0, bottomPadding)
        insets
    }
}