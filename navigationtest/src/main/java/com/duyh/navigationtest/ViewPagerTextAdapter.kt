package com.duyh.navigationtest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class ViewPagerTextAdapter(
    activity: FragmentActivity,
    private val fragmentList: MutableList<Test13Fragment>
) : FragmentStateAdapter(activity) {


    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}