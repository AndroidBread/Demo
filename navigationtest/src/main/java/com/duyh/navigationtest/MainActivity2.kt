package com.duyh.navigationtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.duyh.navigationtest.databinding.ActivityMain2Binding
import com.duyh.navigationtest.databinding.ActivityMainBinding

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMain2Binding>(this , R.layout.activity_main2)



        val list = mutableListOf<Test13Fragment>()
        for (i in 0 until 3){
            list.add(Test13Fragment())
        }


        val viewPagerTextAdapter = ViewPagerTextAdapter(this, list)

        binding.vpTest.adapter = viewPagerTextAdapter

        binding.vpTest.currentItem = 0
    }
}