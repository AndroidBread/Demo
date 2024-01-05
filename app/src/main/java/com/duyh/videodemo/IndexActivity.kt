package com.duyh.videodemo

import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.aliyun.ui.activity.AliyunLiveShiftActivity
import com.duyh.videodemo.MediaControllerService.Companion.MY_TAG
import com.duyh.videodemo.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityIndexBinding
    private lateinit var mViewModel: IndexViewModel


    fun startService() {
        Log.i(MY_TAG, "MyApplication startService");
        //8.0以上系统启动为前台服务, 否则在后台, 测试中发现过几分钟后MediaController监听不到音乐信息
//        startForegroundService(Intent(this, MediaControllerService.class))
        startForegroundService(Intent(this , MediaControllerService::class.java))
    }

    private fun getServiceList(): List<String> {

        val systemService = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = systemService.getRunningServices(200)
        runningServices.ifEmpty {
            LogUtils.e(MY_TAG , "Services is empty!!!!!")
        }
        return runningServices.map {
            LogUtils.e(MY_TAG , "${it.service.className}/${it.service.packageName}")
            "${it.service.className}/${it.service.packageName}"
        }
    }

    private fun getLauncherPackageName(): String{
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveActivity = packageManager.resolveActivity(intent, 0)
        resolveActivity?.let {
            return "${it.resolvePackageName}"
        }
        return  ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Utils.isNotificationListenerEnabled(this)) {//是否开启通知使用权
            Utils.openNotificationListenSettings(this);
        }
        startService()
        LogUtils.e(MY_TAG , getLauncherPackageName())
        with(window){
            navigationBarColor = Color.BLACK
        }
        StatusBarUtil.setTranslucentForImageView(this, 0, null)
        StatusBarUtil.setLightMode(this)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_index)
        mBinding.button.setOnClickListener {
            jumpToPlayer()
        }

        mBinding.button2.setOnClickListener {
            jumpToLive()
        }

        mBinding.button3.setOnClickListener {
            jumpCustom()
        }

        mViewModel = ViewModelProvider(this)[IndexViewModel::class.java]
        mViewModel.coroutineLightweight()
    }

    fun jumpCustom(){
        val intent = Intent(this, CustomVideoActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, mBinding.button, "shareNames").toBundle());
    }

    fun jumpToPlayer(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
    fun jumpToLive(){
        startActivity(Intent(this , AliyunLiveShiftActivity::class.java))
    }
}