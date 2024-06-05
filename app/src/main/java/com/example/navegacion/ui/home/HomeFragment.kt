package com.example.navegacion.ui.home

import android.annotation.SuppressLint
import android.content.DialogInterface
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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager2.widget.ViewPager2
import com.example.navegacion.R
import com.example.navegacion.ui.novedades.MyFragmentStateAdapter
import com.example.navegacion.ui.users.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    @SuppressLint("MissingInflatedId")
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

        val viewPager: ViewPager2 = root.findViewById(R.id.viewPager)
        val adapter = com.example.navegacion.ui.home.MyFragmentStateAdapter(this)
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