package com.duyh.videodemo

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.view.KeyEvent
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.aliyun.player.IPlayer
import com.aliyun.player.aliyunplayerbase.activity.BaseActivity
import com.aliyun.svideo.common.utils.ToastUtils
import com.duyh.videodemo.databinding.ActivityMainBinding
import com.google.android.material.transition.platform.MaterialFade

open class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding
//    private var mLocalVideoPath: String = "https://media.w3.org/2010/05/sintel/trailer.mp4"
    private var mLocalVideoPath: String = "https://sf1-hscdn-tos.pstatp.com/obj/media-fe/xgplayer_doc_video/flv/xgplayer-demo-360p.flv"
    private var mNeedOnlyFullScreen = false
    private val mPlayerViewIUtils: PlayerViewIUtils by lazy {
        PlayerViewIUtils()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            if(Build.VERSION_CODES.TIRAMISU >= Build.VERSION.SDK_INT){
                enterTransition = AutoTransition()
                exitTransition = AutoTransition()
            } else {
                enterTransition = MaterialFade()
                exitTransition = MaterialFade()
            }

            statusBarColor = Color.BLACK
            navigationBarColor = Color.BLACK
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mPlayerViewIUtils.init(this , mLocalVideoPath , savedInstanceState , mNeedOnlyFullScreen).initPlayer(mBinding.avpTest)
        setListener()
    }

    private fun setListener() {
        mBinding.button3.setOnClickListener {
            ToastUtils.show(this, "change!!")
            mPlayerViewIUtils.changeVideoUrl("")
        }
        mBinding.clVideoRoot.setOnClickListener {
            finishAfterTransition()
        }

        mPlayerViewIUtils.setOnPlayStateBtnClickListener{
            when (it) {
                IPlayer.started -> {
                    //暂停播放
                    ToastUtils.show(this , "$it 播放暂停了！！")
                }
                IPlayer.paused -> {
                    //开始播放
                    ToastUtils.show(this , "$it 播放开始了！！")
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mPlayerViewIUtils.onResume()
    }

    override fun onStop() {
        super.onStop()
        mPlayerViewIUtils.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mPlayerViewIUtils.updatePlayerViewMode()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val handler = mBinding.avpTest.onKeyDown(keyCode, event)
        if (!handler) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //解决某些手机上锁屏之后会出现标题栏的问题。
        mPlayerViewIUtils.updatePlayerViewMode()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mPlayerViewIUtils.onSaveInstanceState(outState)
    }

    //    private fun hideSystemBars() {
    //        val insetsControllerCompat = WindowInsetsControllerCompat(window, window.decorView)
    //        insetsControllerCompat.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    //        insetsControllerCompat.hide(systemBars())
    //    }
    //
    //    private fun showSystemBars() {
    //        val insetsControllerCompat = WindowInsetsControllerCompat(window, window.decorView)
    //        insetsControllerCompat.show(systemBars())
    //    }



    override fun onDestroy() {
        mPlayerViewIUtils.onDestroy()
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        super.onDestroy()
    }



}
