package com.duyh.exoplayerdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.duyh.exoplayerdemo.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMain2Binding.inflate(layoutInflater)
        inflate.tvInfo.text = "s"
    }
}