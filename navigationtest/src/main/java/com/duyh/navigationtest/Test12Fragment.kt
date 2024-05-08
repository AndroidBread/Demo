package com.duyh.navigationtest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duyh.navigationtest.databinding.FragmentTest12Binding

class Test12Fragment: Fragment() {

    companion object{
        val TAG: String = Test12Fragment::class.java.simpleName
    }

    private val args: Test12FragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTest12Binding.inflate(layoutInflater)
        binding.tvText.text = args.content
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