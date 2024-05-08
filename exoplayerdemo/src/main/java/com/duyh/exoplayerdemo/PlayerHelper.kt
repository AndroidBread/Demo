package com.duyh.exoplayerdemo

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

/**
 *@Author moongod
 *@Date 2024/4/2 - 09:46
 *@Email mr_duyh@163.com
 *@Description 播放器辅助工具类
 */
class PlayerHelper private constructor(){

    companion object {
        val TAG: String = PlayerHelper::class.java.simpleName

        @JvmStatic
        val instance by lazy {
            synchronized(this) {
                PlayerHelper()
            }
        }
    }

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>


    private val items: MutableList<MediaItem> = mutableListOf()
    private val listenerMap = hashMapOf<String, PlayerStatusListener>()

    private lateinit var mContext: Context

    private var timeCountDown: TimeCountDown? = null


    fun initHelper(context: Context){
        this.mContext = context
    }

    fun setPlayStatusListener(tag: String, playerStatusListener: PlayerStatusListener){
        if(listenerMap.containsKey(tag)){
            return
        }
        listenerMap[tag] = playerStatusListener
    }


    fun removeListener(tag: String){
        listenerMap.remove(tag)
    }

    fun createController(){
        if(!this::mContext.isLateinit){
            throw InstantiationException("必须先初始化mContext")
        }

        if(mediaController != null){
            listenerMap.forEach { (_, u) ->
                u.onPlayerCreated(mediaController)
            }
            return
        }

        val token = SessionToken(mContext, ComponentName(mContext, PlayerService::class.java))
        controllerFuture = MediaController.Builder(mContext, token).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()

            if(mediaController != null){
                listenerMap.forEach { (_, u) ->
                    u.onPlayerCreated(mediaController)
                }
            }


            mediaController?.addListener(object : Player.Listener{
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    listenerMap.forEach { (_, u) ->
                        u.onEvent(events)
                    }
                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    val duration = mediaController?.duration?:0
                    val position = mediaController?.currentMediaItemIndex?:-1
                    if(position >= 0){
                        listenerMap.forEach { (_, u) ->
                            u.onItemChange(position)
                        }
                        if(position == items.size - 1){
                            listenerMap.forEach { (_, u) ->
                                u.onPlayerLast()
                            }
                        }
                    }
                    if(duration < 0){
                        return
                    }
                    if(timeCountDown != null){
                        timeCountDown?.cancel()
                        timeCountDown = null
                    }
                    timeCountDown =
                        TimeCountDown(duration - (duration%1000), 500, object : TimeCountDown.TimeChangeListener {
                            override fun onTick(millisUntilFinished: Long) {
                                val currentPosition = mediaController?.currentPosition?:0L
                                val currentMediaItemIndex = mediaController?.currentMediaItemIndex?:0
                                Log.e("player", "currentPosition: $currentPosition \t currentMediaItemIndex: $currentMediaItemIndex")
                                listenerMap.forEach { (_, u) ->
                                    u.onProgressChange((currentPosition/1000), duration, currentMediaItemIndex)
                                }
                            }

                            override fun onFinish() {
                                timeCountDown?.cancel()
                            }

                        })
                    timeCountDown?.start()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    listenerMap.forEach { (_, u) ->
                        u.onIsPlayingChanged(isPlaying)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    listenerMap.forEach { (_, u) ->
                        u.onError(error)
                    }
                }

                override fun onPlayerErrorChanged(error: PlaybackException?) {
                    super.onPlayerErrorChanged(error)
                    listenerMap.forEach { (_, u) ->
                        u.onError(error)
                    }
                }
            })
        }, ContextCompat.getMainExecutor(mContext))
    }

    /**
     * 添加曲目列表,默认自动播放
     * @param songList：曲目列表
     * @param isAutoPlay：是否自动播放，默认为是
     */
    fun addPlayerItems(songList: MutableList<SongEntity>, isAutoPlay: Boolean = true){
        if(songList.isNotEmpty()){
            val items1 = songList.map {
                val metadata = MediaMetadata.Builder()
                    .setAlbumTitle(it.album)
                    .setArtist(it.artist)
                    .setGenre(it.genre)
                    .setTitle(it.title)
                    .setDiscNumber(it.duration)
                    .setArtworkUri(Uri.parse(it.image))
                    .setDisplayTitle(it.title)
                    .build()


                MediaItem.Builder()
                    .setMediaId(it.id)
                    .setUri(it.source)
                    .setMediaMetadata(metadata).build()
            }.toList()
            items.addAll(items1)
            mediaController?.addMediaItems(items)

            mediaController?.prepare()
            mediaController?.play()

        }
    }


    //*****************************播放器常规方法start*********************************
    fun play(){
        mediaController?.play()
        timeCountDown?.start()
    }

    fun pause(){
        mediaController?.pause()
        timeCountDown?.cancel()
    }

    fun seekTo(position: Long){
        mediaController?.seekTo(position * 1000)
    }

    fun playNext(){
        mediaController?.seekToNextMediaItem()
        play()
    }

    fun playPre(){
        mediaController?.seekToPreviousMediaItem()
        play()
    }
    //*****************************播放器常规方法end*********************************

    interface PlayerStatusListener{

        /**
         * 播放控制器创建成功之后的回调，设置歌曲要在创建成功后
         * @param mediaController: 播放控制器对象
         */
        fun onPlayerCreated(mediaController: MediaController?){}

        /**
         * @param isPlaying: 播放/暂停状态改变
         */
        fun onIsPlayingChanged(isPlaying: Boolean){}

        /**
         * @param current: 当前播放的时间点
         * @param total：歌曲总时长
         * @param position；当前歌曲在列表中的位置
         */
        fun onProgressChange(current: Long, total: Long?, position: Int)

        /**
         * @param error: 播放器错误回调对象
         */
        fun onError(error: PlaybackException?)

        /**
         * @param position；当前歌曲在列表中的位置
         */
        fun onItemChange(position: Int)

        //播放最后一首歌曲
        fun onPlayerLast()

        fun onEvent(events: Player.Events){}
    }

}