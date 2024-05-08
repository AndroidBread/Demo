package com.duyh.navigationtest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.duyh.navigationtest.databinding.FragmentTest1Binding

class Test1Fragment: Fragment() {

    companion object{
        val TAG: String = Test1Fragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTest1Binding.inflate(layoutInflater)
        binding.back.setOnClickListener {
            back()
        }

        binding.btnFragment1.setOnClickListener {
            val action = Test1FragmentDirections.actionTest1FragmentToTest11Fragment()
            findNavController().navigate(action)
        }



        binding.btnFragment3.setOnClickListener {
            val action = Test1FragmentDirections.actionTest1FragmentToTest13Fragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    fun back(){
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: $TAG")
    }

}