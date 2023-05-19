package com.silverytitan.bnv.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fa: FragmentActivity, private val fl: MutableList<Fragment>) : FragmentStateAdapter(
        fa) {

    override fun getItemCount() = fl.size

    override fun createFragment(position: Int) = fl[position]
}