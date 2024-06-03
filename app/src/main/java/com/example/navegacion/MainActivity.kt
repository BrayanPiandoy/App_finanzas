package com.example.navegacion

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.navegacion.databinding.ActivityMainBinding
import com.example.navegacion.ui.novedades.NovedadesActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_egresos, R.id.navigation_ingresos, R.id.navigation_ahorros
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Ocultar la barra de acción
        supportActionBar?.hide()
        // Establecer el color de fondo del ConstraintLayout
        val fondo: ConstraintLayout = binding.container
        fondo.setBackgroundColor(ContextCompat.getColor(this, R.color.background_app))

        val bt_flotante: com.google.android.material.floatingactionbutton.FloatingActionButton = binding.fab
        bt_flotante.setOnClickListener {
            val intent = Intent(this@MainActivity, NovedadesActivity::class.java)
            startActivity(intent)
        }

    }
}