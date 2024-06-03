package com.example.navegacion.ui.novedades

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.navegacion.ui.home.AhorrosHomeFragment
import com.example.navegacion.ui.home.EgresosHomeFragment
import com.example.navegacion.ui.home.IngresosHomeFragment

class MyFragmentStateAdapter(activity: NovedadesActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NovEgresosFragment()
            1 -> NovIngresosFragment()
            2 -> NovAhorrosFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}