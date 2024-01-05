package com.duyh.navigationtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.duyh.navigationtest.databinding.FragmentMainBinding

class MainFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(layoutInflater , container , false)
        binding.btnFragment1.setOnClickListener {
            gotoFragment(1)
        }
        binding.btnFragment2.setOnClickListener {
            gotoFragment(2)
        }
        binding.btnFragment3.setOnClickListener {
            gotoFragment(3)
        }
        return binding.root
    }

    private fun gotoFragment(type: Int){
        when(type){
            1 -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTest1Fragment()
                findNavController().navigate(action)
            }
            2 -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTest2Fragment()
                findNavController().navigate(action)
            }
            3 -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTest3Fragment()
                findNavController().navigate(action)
            }
            else -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTest1Fragment()
                findNavController().navigate(action)
            }
        }
    }

}