package com.example.navegacion.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EgresosHomeFragment()
            1 -> IngresosHomeFragment()
            2 -> AhorrosHomeFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}