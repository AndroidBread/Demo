package com.duyh.videodemo

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class CustomViewTest: LinearLayout {
    constructor(context: Context):super(context , null , 0){
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context , attrs , 0){
        init()
    }

    @SuppressLint("MissingInflatedId")
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs , defStyle){
        init()
    }

    private fun init() {
        val inflater = context
            .applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.view_test, this)
        view.findViewById<TextView>(R.id.tv_test).text = "1111111"
    }
}