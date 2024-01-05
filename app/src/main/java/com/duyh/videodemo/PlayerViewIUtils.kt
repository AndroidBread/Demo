package com.duyh.videodemo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliyun.player.IPlayer
import com.aliyun.player.alivcplayerexpand.constants.GlobalPlayerConfig
import com.aliyun.player.alivcplayerexpand.theme.Theme
import com.aliyun.player.alivcplayerexpand.view.gesturedialog.BrightnessDialog
import com.aliyun.player.alivcplayerexpand.widget.AliyunVodPlayerView
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode
import com.aliyun.player.aliyunplayerbase.util.ScreenUtils
import com.aliyun.player.aliyunplayerbase.view.tipsview.TipsView
import com.aliyun.player.bean.ErrorCode
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.nativeclass.CacheConfig
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.UrlSource
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.ToastUtils

/**
 * create by duyh at 2023/07/18
 * @email: mr_duyh@163.com
 * @Description：播放器工具类，非单例，关闭对应依赖activity的时候记得把该对象置空
 * 使用时，先创建对象，然后调用init函数，之后调用initPlayer函数，在Activity的对应生命周期调用工具类中对应的函数即可，
 * 另外，宿主activity需要复写onWindowFocusChanged函数，以解决某些手机上锁屏之后会出现标题栏的问题。
 * @ex：
 * override fun onWindowFocusChanged(hasFocus: Boolean) {
 *  super.onWindowFocusChanged(hasFocus)
 *  mPlayerViewIUtils.updatePlayerViewMode()
 *}
 * 还需要复写onSaveInstanceState函数，用来恢复以保存的页面状态
 * */
class PlayerViewIUtils {

    companion object{
        const val TAG: String = "PlayerViewIUtils"
    }

    //播放器依赖的宿主Activity
    private lateinit var mActivity: AppCompatActivity
    //当前屏幕的亮度值
    private var mCurrentBrightValue = 0
    //播放地址
    private var mLocalVideoPath: String? = null
    //是否只展示全屏
    private var mNeedOnlyFullScreen = false
    //播放器对象
    private var mPlayerView : AliyunVodPlayerView? = null
    //屏幕后台或屏幕方向变化时的存储状态
    private var savedInstanceState: Bundle? = null
    /**
     * 用于恢复原本的播放方式，如果跳转到下载界面，播放本地视频，会切换到url播放方式
     */
    private var mCurrentPlayType = GlobalPlayerConfig.mCurrentPlayType

    private var onPlayStateBtnClickListener: AliyunVodPlayerView.OnPlayStateBtnClickListener? = null

    fun setOnPlayStateBtnClickListener(onPlayStateBtnClickListener: AliyunVodPlayerView.OnPlayStateBtnClickListener){
        this.onPlayStateBtnClickListener = onPlayStateBtnClickListener
    }


    fun init(activity: AppCompatActivity , path: String = "", state: Bundle?,onlyFullScreen: Boolean = false): PlayerViewIUtils{
        mActivity = activity
        mLocalVideoPath = path
        savedInstanceState = state
        mNeedOnlyFullScreen = onlyFullScreen
        //如果有已保存状态，则先恢复状态
        savedInstanceState?.let {
            restoreSaveInstance(it)
        }
        //设置屏幕亮度模式
        setManualBright()
        return this
    }

    //跟随Activity生命周期
    fun onResume() {
        updatePlayerViewMode()
        if (!GlobalPlayerConfig.PlayConfig.mEnablePlayBackground) {
            if (mPlayerView != null) {
                mPlayerView?.setAutoPlay(true)
                mPlayerView?.onResume()
            }
            GlobalPlayerConfig.mCurrentPlayType = mCurrentPlayType
        }
    }

    //跟随Activity生命周期
    fun onStop() {
        if (!GlobalPlayerConfig.PlayConfig.mEnablePlayBackground) {
            if (mPlayerView != null) {
                mPlayerView?.setAutoPlay(false)
                mPlayerView?.onStop()
            }
            mCurrentPlayType = GlobalPlayerConfig.mCurrentPlayType
        }
    }

    //跟随Activity生命周期
    fun onDestroy() {
        if (mPlayerView != null) {
            mPlayerView?.onDestroy()
            mPlayerView = null
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("mCurrentPlayType", GlobalPlayerConfig.PLAYTYPE.URL.ordinal)
        outState.putString("mUrlPath", GlobalPlayerConfig.mUrlPath)
        //PlayerConfig
        outState.putInt("mStartBufferDuration", GlobalPlayerConfig.PlayConfig.mStartBufferDuration)
        outState.putInt("mHighBufferDuration", GlobalPlayerConfig.PlayConfig.mHighBufferDuration)
        outState.putInt("mMaxBufferDuration", GlobalPlayerConfig.PlayConfig.mMaxBufferDuration)
        outState.putInt("mMaxDelayTime", GlobalPlayerConfig.PlayConfig.mMaxDelayTime)
        outState.putInt("mMaxProbeSize", GlobalPlayerConfig.PlayConfig.mMaxProbeSize)
        outState.putString("mReferrer", GlobalPlayerConfig.PlayConfig.mReferrer)
        outState.putString("mHttpProxy", GlobalPlayerConfig.PlayConfig.mHttpProxy)
        outState.putInt("mNetworkTimeout", GlobalPlayerConfig.PlayConfig.mNetworkTimeout)
        outState.putInt("mNetworkRetryCount", GlobalPlayerConfig.PlayConfig.mNetworkRetryCount)
        outState.putBoolean("mEnableSei", GlobalPlayerConfig.PlayConfig.mEnableSei)
        outState.putBoolean(
            "mEnableClearWhenStop",
            GlobalPlayerConfig.PlayConfig.mEnableClearWhenStop
        )
        outState.putBoolean("mAutoSwitchOpen", GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen)
        outState.putBoolean(
            "mEnableAccurateSeekModule",
            GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule
        )
        outState.putBoolean(
            "mEnablePlayBackground",
            GlobalPlayerConfig.PlayConfig.mEnablePlayBackground
        )
        outState.putBoolean("mEnableHardDecodeType", GlobalPlayerConfig.mEnableHardDecodeType)
        //CacheConfig
        outState.putBoolean("mEnableCache", GlobalPlayerConfig.PlayCacheConfig.mEnableCache)
        outState.putString("mDir", GlobalPlayerConfig.PlayCacheConfig.mDir)
        outState.putInt("mMaxDurationS", GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS)
        outState.putInt("mMaxSizeMB", GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB)
    }

    private fun setManualBright() {
        try {
            Settings.System.putInt(
                mActivity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } catch (localException: Exception) {
            localException.printStackTrace()
        }
    }

    /***
     * 更新错误日志提示
     * msg：错误信息文本
     * needRetry：是否需要展示重试图标
     */
    fun updateErrorMsg(msg: String, needRetry: Boolean){
        mPlayerView?.showErrorEmptyView(needRetry, msg)
    }
    private var mLayoutParams: ViewGroup.LayoutParams? = null
    //更新屏幕控件模式
    fun updatePlayerViewMode() {
        if (mPlayerView != null) {
//            val orientation = mActivity.resources?.configuration?.orientation
            if (mLayoutParams == null) {
                //如果未设置，默认使用播放器的布局文件
                mLayoutParams = mPlayerView?.layoutParams
            }
            if ((mLayoutParams is ConstraintLayout.LayoutParams || mLayoutParams is FrameLayout.LayoutParams)) {
                val aliVcVideoViewLayoutParams =
                    if (mLayoutParams is ConstraintLayout.LayoutParams) {
                        mLayoutParams as ConstraintLayout.LayoutParams
                    } else {
                        mLayoutParams as FrameLayout.LayoutParams
                    }
                if (mPlayerView?.screenMode == AliyunScreenMode.Small) {
                    showPortraitScreen(aliVcVideoViewLayoutParams)
                } else if (mPlayerView?.screenMode == AliyunScreenMode.Full) {
                    //隐藏状态栏
                    ScreenUtils.hideSystemBars(mActivity)
                    //设置view的布局，宽高
                    aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        }
    }

    private fun showPortraitScreen(aliVcVideoViewLayoutParams: ViewGroup.MarginLayoutParams) {
        ScreenUtils.showSystemBars(mActivity)
        aliVcVideoViewLayoutParams.height = (ScreenUtils.getWidth(mActivity) * 9.0f / 16).toInt()
        aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
    }

    //初始化播放器
    fun initPlayer(playerView: AliyunVodPlayerView) {

        mPlayerView = playerView
        mCurrentBrightValue = getCurrentBrightValue()
        //保持屏幕敞亮
        playerView.keepScreenOn = true
        playerView.setTheme(Theme.Blue)
        playerView.setAutoPlay(false)
        playerView.needOnlyFullScreenPlay(mNeedOnlyFullScreen)
        playerView.screenBrightness = BrightnessDialog.getActivityBrightness(mActivity)

        playerView.setOnPreparedListener{
            val mediaInfo = playerView.mediaInfo
            Log.i("AliyunPlayer" , "mediaInfo--> ${mediaInfo.videoId}")
        }
        playerView.setOnTrackReadyListener { mediaInfo ->
            if (mediaInfo != null) {
                var bitrate: Long = mediaInfo.totalBitrate
                if (bitrate == 0L) {
                    val trackInfoList: List<TrackInfo> = mediaInfo.trackInfos
                    if (trackInfoList.isNotEmpty()) {
                        bitrate = trackInfoList[0].videoBitrate.toLong()
                        Log.i(TAG , "bitrate: $bitrate")
                    }
                }
            }
        }
        playerView.setNetConnectedListener(object : AliyunVodPlayerView.NetConnectedListener {
            override fun onReNetConnected(isReconnect: Boolean) {
                //网络重连
            }

            override fun onNetUnConnected() {
                //网络断开
            }
        })
        playerView.setOnCompletionListener{
            LogUtils.i(TAG, "播放完成!")
//            playerView.showReplay()
            playerView.pause()
            changeVideoUrl(mLocalVideoPath!!)
            playerView.setAutoPlay(false)

        }


        playerView.setOnStoppedListener {
            LogUtils.i(TAG, "视频暂停！")
        }
        playerView.setOrientationChangeListener { from, currentMode ->
            //点击全屏/小屏按钮，强制更新屏幕方向
            if(currentMode == AliyunScreenMode.Small){
                mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            //屏幕方向切换回调
            LogUtils.i(TAG, "from: $from , currentMode: $currentMode")
        }

        playerView.setOnPlayStateBtnClickListener{
            onPlayStateBtnClickListener?.onPlayBtnClick(it)
            when (it) {
                IPlayer.started -> {
                    //开始播放
                    LogUtils.i(TAG, "暂停播放")
                }
                IPlayer.paused -> {
                    //暂停播放
                    LogUtils.i(TAG, "开始播放")
                }
            }
        }
        playerView.setOnSeekCompleteListener{
            //跳转完成
            LogUtils.i(TAG, "跳转完成")
        }
        playerView.setOnSeekStartListener {
            //跳转开始
            LogUtils.i(TAG, "跳转开始")
        }
        playerView.setOnFinishListener {
            //播放结束
            LogUtils.i(TAG, "播放结束")
        }

        playerView.setOnScreenBrightness {
            setWindowBrightness(it)
            playerView.screenBrightness = it
        }

        playerView.setOnErrorListener {
            ToastUtils.show(mActivity, it.msg)
            Log.e("AliyunPlayer" , "error: ${it.msg}")
        }


        playerView.setOnInfoListener { infoBean ->
            when (infoBean.code) {
                InfoCode.CacheSuccess -> {
                    LogUtils.i(TAG, "缓存成功")
                }
                InfoCode.CacheError -> {
                    ToastUtils.show(mActivity, infoBean.extraMsg)
                }
                InfoCode.SwitchToSoftwareVideoDecoder -> {
                    LogUtils.i(TAG,"播放器切换到软件视频解码器")
                }
                else -> {
//                    LogUtils.e(TAG,"未知错误！")
                }
            }
        }

        playerView.setOnTipClickListener(object : TipsView.OnTipClickListener {
            override fun onContinuePlay() {
            }

            override fun onStopPlay() {
            }

            override fun onRetryPlay(errorCode: Int) {
                if (errorCode == ErrorCode.ERROR_LOADING_TIMEOUT.value) {
                    playerView.reTry()
                } else {
                    refresh()
                }
            }

            override fun onReplay() {
            }

            override fun onRefreshSts() {
            }

            override fun onWait() {
            }

            override fun onExit() {
                mActivity.finish()
            }

        })
        playerView.setOnTipsViewBackClickListener {
            mActivity.finish()
        }

        playerView.enableNativeLog(true)
        playerView.screenBrightness = mCurrentBrightValue
        playerView.startNetWatch()
        initPlayerConfig(playerView)
        if(!mLocalVideoPath.isNullOrEmpty()){
            initDataSource()
        }
    }

    //初始化播放器配置信息
    private fun initPlayerConfig(playerView: AliyunVodPlayerView) {
        //界面设置
        playerView.setEnableHardwareDecoder(GlobalPlayerConfig.mEnableHardDecodeType)
        playerView.setRenderMirrorMode(GlobalPlayerConfig.mMirrorMode)
        playerView.setRenderRotate(GlobalPlayerConfig.mRotateMode)
        playerView.setDefaultBandWidth(GlobalPlayerConfig.mCurrentMutiRate.value)
        //播放配置设置
        val playerConfig = playerView.playerConfig
        playerConfig.mStartBufferDuration = GlobalPlayerConfig.PlayConfig.mStartBufferDuration
        playerConfig.mHighBufferDuration = GlobalPlayerConfig.PlayConfig.mHighBufferDuration
        playerConfig.mMaxBufferDuration = GlobalPlayerConfig.PlayConfig.mMaxBufferDuration
        playerConfig.mMaxDelayTime = GlobalPlayerConfig.PlayConfig.mMaxDelayTime
        playerConfig.mNetworkTimeout = GlobalPlayerConfig.PlayConfig.mNetworkTimeout
        playerConfig.mMaxProbeSize = GlobalPlayerConfig.PlayConfig.mMaxProbeSize
        playerConfig.mReferrer = GlobalPlayerConfig.PlayConfig.mReferrer
        playerConfig.mHttpProxy = GlobalPlayerConfig.PlayConfig.mHttpProxy
        playerConfig.mNetworkRetryCount = GlobalPlayerConfig.PlayConfig.mNetworkRetryCount
        playerConfig.mEnableSEI = GlobalPlayerConfig.PlayConfig.mEnableSei
        playerConfig.mClearFrameWhenStop = GlobalPlayerConfig.PlayConfig.mEnableClearWhenStop
        playerView.playerConfig = playerConfig
        //缓存设置
        initCacheConfig()
        Log.e("AliyunPlayer", "cache dir : " + GlobalPlayerConfig.PlayCacheConfig.mDir
                + " startBufferDuration = " + GlobalPlayerConfig.PlayConfig.mStartBufferDuration
                + " highBufferDuration = " + GlobalPlayerConfig.PlayConfig.mHighBufferDuration
                + " maxBufferDuration = " + GlobalPlayerConfig.PlayConfig.mMaxBufferDuration
                + " maxDelayTime = " + GlobalPlayerConfig.PlayConfig.mMaxDelayTime
                + " enableCache = " + GlobalPlayerConfig.PlayCacheConfig.mEnableCache
                + " --- mMaxDurationS = " + GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS
                + " --- mMaxSizeMB = " + GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB
        )
    }

    //初始化缓存配置
    private fun initCacheConfig() {
        val cacheConfig = CacheConfig()
        GlobalPlayerConfig.PlayCacheConfig.mDir =
            FileUtils.getDir(mActivity) + GlobalPlayerConfig.CACHE_DIR_PATH
        cacheConfig.mEnable = GlobalPlayerConfig.PlayCacheConfig.mEnableCache
        cacheConfig.mDir = GlobalPlayerConfig.PlayCacheConfig.mDir
        cacheConfig.mMaxDurationS = GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS.toLong()
        cacheConfig.mMaxSizeMB = GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB
        mPlayerView?.setCacheConfig(cacheConfig)
    }


    /**
     * 重试
     */
    private fun refresh() {
        mPlayerView?.reTry()
    }

    //恢复之前保存的状态
    private fun restoreSaveInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            GlobalPlayerConfig.mUrlPath = savedInstanceState.getString("mUrlPath")
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.URL
            //PlayerConfig
            GlobalPlayerConfig.PlayConfig.mStartBufferDuration =
                savedInstanceState.getInt("mStartBufferDuration")
            GlobalPlayerConfig.PlayConfig.mHighBufferDuration =
                savedInstanceState.getInt("mHighBufferDuration")
            GlobalPlayerConfig.PlayConfig.mMaxBufferDuration =
                savedInstanceState.getInt("mMaxBufferDuration")
            GlobalPlayerConfig.PlayConfig.mMaxDelayTime = savedInstanceState.getInt("mMaxDelayTime")
            GlobalPlayerConfig.PlayConfig.mMaxProbeSize = savedInstanceState.getInt("mMaxProbeSize")
            GlobalPlayerConfig.PlayConfig.mReferrer = savedInstanceState.getString("mReferrer")
            GlobalPlayerConfig.PlayConfig.mHttpProxy = savedInstanceState.getString("mHttpProxy")
            GlobalPlayerConfig.PlayConfig.mNetworkTimeout =
                savedInstanceState.getInt("mNetworkTimeout")
            GlobalPlayerConfig.PlayConfig.mNetworkRetryCount =
                savedInstanceState.getInt("mNetworkRetryCount")
            GlobalPlayerConfig.PlayConfig.mEnableSei = savedInstanceState.getBoolean("mEnableSei")
            GlobalPlayerConfig.PlayConfig.mEnableClearWhenStop =
                savedInstanceState.getBoolean("mEnableClearWhenStop")
            GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen =
                savedInstanceState.getBoolean("mAutoSwitchOpen")
            GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule =
                savedInstanceState.getBoolean("mEnableAccurateSeekModule")
            GlobalPlayerConfig.PlayConfig.mEnablePlayBackground =
                savedInstanceState.getBoolean("mEnablePlayBackground")

            //CacheConfig
            GlobalPlayerConfig.PlayCacheConfig.mEnableCache =
                savedInstanceState.getBoolean("mEnableCache")
            GlobalPlayerConfig.PlayCacheConfig.mDir = savedInstanceState.getString("mDir")
            GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS =
                savedInstanceState.getInt("mMaxDurationS")
            GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB = savedInstanceState.getInt("mMaxSizeMB")
            GlobalPlayerConfig.mEnableHardDecodeType =
                savedInstanceState.getBoolean("mEnableHardDecodeType")
        }
    }

    //设置播放源地址
    private fun initDataSource() {
        //如果链接是空的，就直接返回
        val urlSource = UrlSource()
        urlSource.uri = mLocalVideoPath
        if (mPlayerView != null) mPlayerView?.setLocalSource(urlSource) else throw NullPointerException("Expression 'mPlayerView' must not be null")
    }

    //更换视频源
    fun changeVideoUrl(newPath: String){
        mLocalVideoPath = newPath
        initDataSource()
    }

    //获取当前亮度
    private fun getCurrentBrightValue(): Int {
        var nowBrightnessValue = 0
        val resolver = mActivity.contentResolver
        try {
            nowBrightnessValue = Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS, 255
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return nowBrightnessValue
    }

    private fun setWindowBrightness(brightness: Int) {
        val window = mActivity.window
        val lp = window.attributes
        lp.screenBrightness = brightness / 100.00f
        window.attributes = lp
    }
}

class LogUtils{
    companion object{
        fun i(tag: String, msg: String){
            Log.i(tag , msg)
        }
        fun e(tag: String, msg: String){
            Log.e(tag , msg)
        }
    }
}