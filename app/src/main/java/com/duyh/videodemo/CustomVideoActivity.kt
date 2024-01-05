package com.duyh.videodemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.aliyun.player.IPlayer
import com.aliyun.player.IPlayer.OnTrackChangedListener
import com.aliyun.player.alivcplayerexpand.widget.AliyunRenderView
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.nativeclass.MediaInfo
import com.aliyun.player.nativeclass.PlayerConfig
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.UrlSource
import com.duyh.videodemo.databinding.ActivityCustomVideoBinding

class CustomVideoActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCustomVideoBinding
    private var mLocalSource: UrlSource? = null
    private var mLocalVideoPath: String = "https://media.w3.org/2010/05/sintel/trailer.mp4"
    private val hasLoadEnd: HashMap<MediaInfo, Boolean> = HashMap()
    private var mPlayerState = IPlayer.idle

    //媒体信息
    private var mAliyunMediaInfo: MediaInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_custom_video)
        initCustomPlayer()
        val urlSource = UrlSource()
        urlSource.uri = mLocalVideoPath
        setLocalSource(urlSource)
    }

    /**
     * 设置播放源
     *
     */
    private fun setLocalSource(source: UrlSource?) {
        mLocalSource = null
        reset()
        //播放本地资源,需要清空VIDEO_FUNCTION
        mLocalSource = source
        if (source != null && source.uri.startsWith("artc")) {
            Log.e("player", "artc setPlayerConfig")
            val playerConfig: PlayerConfig = mBinding.arvCustom.playerConfig
            playerConfig.mMaxDelayTime = 1000
            playerConfig.mHighBufferDuration = 10
            playerConfig.mStartBufferDuration = 10
            mBinding.arvCustom.playerConfig = playerConfig
        }
        mBinding.arvCustom.setAutoPlay(true)
        mBinding.arvCustom.setDataSource(source)
        mBinding.arvCustom.prepare()
    }

    private fun reset(){

    }

    /**
     * 停止播放
     */
    private fun stop() {
        var hasLoadedEnd: Boolean? = null
        var mediaInfo: MediaInfo? = null
        mediaInfo = mBinding.arvCustom.mediaInfo
        hasLoadedEnd = hasLoadEnd[mediaInfo]
        if (hasLoadedEnd != null) {
            mPlayerState = IPlayer.stopped
            mBinding.arvCustom.stop()
        }
        hasLoadEnd.remove(mediaInfo)
    }

    private fun initCustomPlayer() {
        val arvCustom = mBinding.arvCustom
        arvCustom.setSurfaceType(AliyunRenderView.SurfaceType.SURFACE_VIEW)
        //设置准备回调
        arvCustom.setOnPreparedListener {
            mAliyunMediaInfo = mBinding.arvCustom.mediaInfo
        }
        //播放器出错监听
        arvCustom.setOnErrorListener {

        }
        //播放器加载回调
        arvCustom.setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {
            override fun onLoadingBegin() {
            }

            override fun onLoadingProgress(percent: Int, v: Float) {
            }

            override fun onLoadingEnd() {

            }

        })
        arvCustom.setOnTrackReadyListenenr {

        }
        //播放器状态
        arvCustom.setOnStateChangedListener {

        }
        //播放结束
        arvCustom.setOnCompletionListener {

        }
        //播放信息监听
        arvCustom.setOnInfoListener {

        }
        //第一帧显示
        arvCustom.setOnRenderingStartListener {

        }

        //trackChange监听
        arvCustom.setOnTrackChangedListener(object : OnTrackChangedListener {
            override fun onChangedSuccess(info: TrackInfo?) {
            }

            override fun onChangedFail(info: TrackInfo?, error: ErrorInfo?) {
            }

        })

        //seek结束事件
        arvCustom.setOnSeekCompleteListener {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        mBinding.arvCustom.release()
        mAliyunMediaInfo = null
        hasLoadEnd.clear()
    }

}