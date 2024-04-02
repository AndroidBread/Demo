package com.duyh.exoplayerdemo

import android.os.CountDownTimer

class TimeCountDown(totalTime: Long,countDownInterval: Long, private val timeChangeListener: TimeChangeListener): CountDownTimer(totalTime, countDownInterval) {
    override fun onTick(millisUntilFinished: Long) {
        timeChangeListener.onTick(millisUntilFinished)
    }

    override fun onFinish() {
        timeChangeListener.onFinish()
    }

    interface TimeChangeListener{
        fun onTick(millisUntilFinished: Long)

        fun onFinish()
    }
}