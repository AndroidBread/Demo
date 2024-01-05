package com.duyh.sudoku

import android.app.Application
import com.duyh.sudoku.utils.Assets

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initAssets()
    }

    private fun initAssets() {
        Assets.extractAssets(baseContext)
    }

}