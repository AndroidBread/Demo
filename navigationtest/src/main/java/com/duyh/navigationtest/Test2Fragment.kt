package com.duyh.navigationtest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.duyh.navigationtest.databinding.FragmentTest2Binding

class Test2Fragment: Fragment() {
    
    companion object{
        val TAG: String = Test2Fragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTest2Binding.inflate(layoutInflater)

        binding.btnFragment2.setOnClickListener {
            findNavController().navigate(R.id.action_global_mainFragment2)
            val bundle = Bundle()
            val option: NavOptions = NavOptions.Builder()
                .setRestoreState(true)
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.action_global_mainFragment2, true,true)
                .build()
            findNavController().navigate(R.id.test1Fragment, bundle, option)
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