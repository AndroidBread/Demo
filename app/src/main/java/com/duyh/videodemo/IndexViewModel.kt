package com.duyh.videodemo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IndexViewModel: ViewModel() {

    fun coroutineTest(){
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000L)
            Log.i("viewModelScope" , "New coroutine!")
        }
    }

    fun coroutineLightweight(){
        val start = System.currentTimeMillis()
        //启动100个协程，每个协程打印一行日志
        repeat(100){
            viewModelScope.launch {
                Log.i("viewModelScope" , "coroutine num is $it")
                if (it == 99){
                    Log.i("viewModelScope" , "use time is ${System.currentTimeMillis() - start}")
                }
            }
        }
    }

}