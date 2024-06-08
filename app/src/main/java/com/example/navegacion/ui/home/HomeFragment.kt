package com.example.navegacion.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.navegacion.R
import com.example.navegacion.ui.users.LoginActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val ivUser: ImageView = root.findViewById(R.id.iv_user)
        ivUser.setOnClickListener {
            showUserOptions()
        }

        updateUser(root)
        setupPieChart(root)

        val viewPager: ViewPager2 = root.findViewById(R.id.viewPager)
        val adapter = MyFragmentStateAdapter(this)
        val tabLayout: TabLayout = root.findViewById(R.id.tabLayout)
        val tabTitles = listOf("Egresos", "Ingresos", "Ahorros")
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        return root
    }

    private fun updateUser(view: View) {
        val textView: TextView = view.findViewById(R.id.textView)
        val user = auth.currentUser

        if (user != null) {
            val uid = user.uid
            val userRef = database.getReference("users").child(uid).child("datos")

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    val formattedText = "$name \n $email"
                    textView.text = formattedText
                }

                override fun onCancelled(error: DatabaseError) {
                    textView.text = "Error loading user data"
                }
            })
        } else {
            textView.text = "No identificado"
        }
    }

    private fun setupPieChart(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val entries = ArrayList<PieEntry>()

            val egresosRef = database.getReference("users").child(uid).child("egresos")
            egresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalEgresos = 0.0
                    for (egresosSnapshot in snapshot.children) {
                        val valor = egresosSnapshot.child("valor").getValue(String::class.java)
                        valor?.let {
                            val valorSinComas = it.replace(",", "")
                            totalEgresos += valorSinComas.toDouble()
                        }
                    }
                    entries.add(PieEntry(totalEgresos.toFloat(), "Egresos"))
                    updatePieChart(entries)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error
                }
            })

            val ingresosRef = database.getReference("users").child(uid).child("ingresos")
            ingresosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalIngresos = 0.0
                    for (ingresosSnapshot in snapshot.children) {
                        val valor = ingresosSnapshot.child("valor").getValue(String::class.java)
                        valor?.let {
                            val valorSinComas = it.replace(",", "")
                            totalIngresos += valorSinComas.toDouble()
                        }
                    }
                    entries.add(PieEntry(totalIngresos.toFloat(), "Ingresos"))
                    updatePieChart(entries)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error
                }
            })

            val ahorrosRef = database.getReference("users").child(uid).child("ahorros")
            ahorrosRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalAhorros = 0.0
                    for (ahorrosSnapshot in snapshot.children) {
                        val valor = ahorrosSnapshot.child("valor").getValue(String::class.java)
                        valor?.let {
                            val valorSinComas = it.replace(",", "")
                            totalAhorros += valorSinComas.toDouble()
                        }
                    }
                    entries.add(PieEntry(totalAhorros.toFloat(), "Ahorros"))
                    updatePieChart(entries)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error
                }
            })
        }
    }

    private fun updatePieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "")
        val data = PieData(dataSet)
        val colors = listOf(
            ColorTemplate.rgb("#FF6384"),  // Rojo
            ColorTemplate.rgb("#36A2EB"),  // Azul
            ColorTemplate.rgb("#FFCE56")   // Amarillo
        )
        dataSet.colors = colors
        pieChart.data = data
        pieChart.invalidate() // refresh
    }

    private fun showUserOptions() {
        val user = auth.currentUser
        if (user != null) {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null)
            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnLogout = dialogView.findViewById<Button>(R.id.btnLogout)

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            btnLogout.setOnClickListener {
                auth.signOut()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
                alertDialog.dismiss()
            }

            alertDialog.show()
        } else {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }
    }
}