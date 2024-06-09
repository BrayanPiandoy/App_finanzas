package com.example.navegacion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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
import com.example.navegacion.ui.novedades.NovedadesDialogFragment
import com.example.navegacion.ui.users.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastTouchDownX: Float = 0f
    private var lastTouchDownY: Float = 0f
    private val CLICK_DRAG_TOLERANCE = 10f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_egresos, R.id.navigation_ingresos, R.id.navigation_ahorros
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()
        val fondo: ConstraintLayout = binding.container
        fondo.setBackgroundColor(ContextCompat.getColor(this, R.color.background_app))

        val btFlotante: com.google.android.material.floatingactionbutton.FloatingActionButton = binding.fab

        btFlotante.setOnTouchListener(object : View.OnTouchListener {
            private var xCoOrdinate: Float = 0f
            private var yCoOrdinate: Float = 0f

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouchDownX = event.rawX
                        lastTouchDownY = event.rawY
                        xCoOrdinate = view.x - event.rawX
                        yCoOrdinate = view.y - event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> view.animate()
                        .x(event.rawX + xCoOrdinate)
                        .y(event.rawY + yCoOrdinate)
                        .setDuration(0)
                        .start()
                    MotionEvent.ACTION_UP -> {
                        if (abs(event.rawX - lastTouchDownX) < CLICK_DRAG_TOLERANCE &&
                            abs(event.rawY - lastTouchDownY) < CLICK_DRAG_TOLERANCE) {
                            // Consider as a click
                            btFlotante.performClick()
                        }
                    }
                    else -> return false
                }
                return true
            }
        })

        btFlotante.setOnClickListener {
            val intent = Intent(this, NovedadesActivity::class.java)
            startActivity(intent)

        }
    }
}
