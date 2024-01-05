package com.duyh.navigationtest

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.duyh.navigationtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this , R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val findNavController = navHostFragment.findNavController()



        binding.btnAf1.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToTest2Fragment()
            navHostFragment.findNavController().navigate(R.id.action_global_mainFragment2)
            findNavController.navigate(action)
        }

        binding.btnAf2.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToTest3Fragment()
            navHostFragment.findNavController().navigate(R.id.action_global_mainFragment2)
            navHostFragment.findNavController().navigate(action)
        }

        binding.btnInstall.setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }


    }



}