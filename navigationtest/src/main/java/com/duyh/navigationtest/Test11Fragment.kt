package com.duyh.navigationtest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.duyh.navigationtest.databinding.FragmentTest11Binding

class Test11Fragment: Fragment() {

    companion object{
        val TAG: String = Test11Fragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTest11Binding.inflate(layoutInflater)

        binding.btnFragment2.setOnClickListener {
            val action =
                Test11FragmentDirections.actionTest11FragmentToTest12Fragment(content = "这个是传入的字符串")
            findNavController().navigate(action)
        }
        return binding.root
    }

    fun back(view: View){
        findNavController().popBackStack()
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: $TAG")
    }

}