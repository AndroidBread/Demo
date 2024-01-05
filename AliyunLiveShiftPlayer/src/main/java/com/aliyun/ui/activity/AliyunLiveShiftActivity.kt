package com.aliyun.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.aliyun.player.aliyunplayerbase.activity.BaseActivity
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode
import com.aliyun.player.aliyunplayerbase.util.ScreenUtils
import com.aliyun.sdk.player.AliLiveShiftPlayer
import com.aliyun.sdk.player.aliyunliveshiftplayer.R
import com.aliyun.sdk.player.aliyunliveshiftplayer.databinding.ActivityAliyunLiveShiftBinding
import com.aliyun.ui.view.AliyunLiveShiftPlayerView
import com.aliyun.ui.view.control.ControlView.OnBackIconClickListener
import java.lang.ref.WeakReference

class AliyunLiveShiftActivity : BaseActivity() {

    private var mLiveShiftView: AliyunLiveShiftPlayerView? = null
    private var mLiveShiftPlayerTitleTextView: TextView? = null
    lateinit var mBinding: ActivityAliyunLiveShiftBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mBinding = DataBindingUtil.setContentView(this , R.layout.activity_aliyun_live_shift)
        initView()
        initListener()
    }

    private fun initView() {
        mLiveShiftView = mBinding.liveShiftView
        mLiveShiftPlayerTitleTextView = mBinding.liveShiftPlayerTitle
        mLiveShiftView!!.prepare()
    }

    private fun initListener() {
        mLiveShiftView!!.setOutOnBackIconClickListener(MyOnBackIconClickListener(this))
        mLiveShiftView!!.setOrientationChangeListener(MyOrientationChangeListener(this))
        mLiveShiftView!!.setmOutOnSeekLiveCompletionListener(MyOnSeekLiveCompletionListener(this))
        mLiveShiftView!!.setmOutOnTimeShiftUpdaterListener(MyOnTimeShiftUpdaterListener(this))

        mBinding.btnChange.setOnClickListener {
            if(mBinding.editInput.text.toString().isNotEmpty()){
                mLiveShiftView!!.setURL(mBinding.editInput.text.toString())
            }
        }

    }

    /**
     * 时间更新监听
     */
    private class MyOnTimeShiftUpdaterListener(aliyunLiveShiftActivity: AliyunLiveShiftActivity) :
        AliLiveShiftPlayer.OnTimeShiftUpdaterListener {
        private val weakReference: WeakReference<AliyunLiveShiftActivity>

        init {
            weakReference = WeakReference(aliyunLiveShiftActivity)
        }

        override fun onUpdater(currentTime: Long, shiftStartTime: Long, shiftEndTime: Long) {
            val aliyunLiveShiftActivity = weakReference.get()
            aliyunLiveShiftActivity?.onUpdater(currentTime, shiftStartTime, shiftEndTime)
        }
    }

    private fun onUpdater(currentTime: Long, shiftStartTime: Long, shiftEndTime: Long) {}

    /**
     * 直播时移seek完成监听
     */
    private class MyOnSeekLiveCompletionListener(aliyunLiveShiftActivity: AliyunLiveShiftActivity) :
        AliLiveShiftPlayer.OnSeekLiveCompletionListener {
        private val weakReference: WeakReference<AliyunLiveShiftActivity>

        init {
            weakReference = WeakReference(aliyunLiveShiftActivity)
        }

        override fun onSeekLiveCompletion(playTime: Long) {
            val aliyunLiveShiftActivity = weakReference.get()
            aliyunLiveShiftActivity?.onSeekLiveCompletion(playTime)
        }
    }

    private fun onSeekLiveCompletion(playTime: Long) {}

    /**
     * 屏幕方向改变监听
     */
    private class MyOrientationChangeListener(aliyunLiveShiftActivity: AliyunLiveShiftActivity) :
        AliyunLiveShiftPlayerView.OnOrientationChangeListener {
        private val weakReference: WeakReference<AliyunLiveShiftActivity>

        init {
            weakReference = WeakReference(aliyunLiveShiftActivity)
        }

        override fun orientationChange(from: Boolean, currentMode: AliyunScreenMode) {
            val aliyunLiveShiftActivity = weakReference.get()
            if (aliyunLiveShiftActivity != null) {
            }
        }
    }

    /**
     * 返回按钮点击监听事件
     */
    private class MyOnBackIconClickListener(aliyunLiveShiftActivity: AliyunLiveShiftActivity) :
        OnBackIconClickListener {
        private val weakReference: WeakReference<AliyunLiveShiftActivity>

        init {
            weakReference = WeakReference(aliyunLiveShiftActivity)
        }

        override fun onBackClickListener() {
            val aliyunLiveShiftActivity = weakReference.get()
            aliyunLiveShiftActivity?.onBackClick()
        }
    }

    private fun onBackClick() {
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (mLiveShiftView != null) {
            mLiveShiftView!!.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLiveShiftView != null) {
            mLiveShiftView!!.pause()
        }
    }

    override fun onDestroy() {
        if (mLiveShiftView != null) {
            mLiveShiftView!!.destroy()
        }
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updatePlayerViewMode()
    }

    private fun updatePlayerViewMode() {
        if (mLiveShiftView != null) {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ScreenUtils.showSystemBars(this)

                //设置view的布局，宽高之类
                val aliVcVideoViewLayoutParams = mLiveShiftView!!
                    .layoutParams as LinearLayout.LayoutParams
                aliVcVideoViewLayoutParams.height = (ScreenUtils.getWidth(this) * 9.0f / 16).toInt()
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //转到横屏了。
                //隐藏状态栏
                if (isStrangePhone){
                    ScreenUtils.hideSystemBars(this)
                }
                //设置view的布局，宽高
                val aliVcVideoViewLayoutParams = mLiveShiftView!!
                    .layoutParams as LinearLayout.LayoutParams
                aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }
}