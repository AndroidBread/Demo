package com.duyh.exoplayerdemo

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.duyh.exoplayerdemo.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>


    private val songList: MutableList<SongEntity> = mutableListOf()
    private val items: MutableList<MediaItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initSongList()
        initController()
        initListener()
    }

    private fun initListener() {
        binding.btnNext.setOnClickListener {
            mediaController?.seekToNextMediaItem()
            mediaController?.play()
        }

        binding.btnPre.setOnClickListener {
            mediaController?.seekToPreviousMediaItem()
            mediaController?.play()
        }

        binding.btnPlay.setOnClickListener {
            if(mediaController?.isPlaying == false){
                mediaController?.play()

                timeCountDown?.start()
            } else {
                mediaController?.pause()
                timeCountDown?.cancel()
            }

        }
    }

    private fun initSongList() {
        val string = AssetsFileUtils.getTermsString(baseContext, "song.json")
        val value = object : TypeToken<MutableList<SongEntity>>() {}
        val entities = Gson().fromJson<MutableList<SongEntity>>(string, value.type)
        songList.addAll(entities)
    }

    private fun initController() {
        val token = SessionToken(baseContext, ComponentName(baseContext, PlayerService::class.java))
        controllerFuture = MediaController.Builder(baseContext, token).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            if(songList.isNotEmpty()){
                val items1 = songList.map {
                    val metadata = MediaMetadata.Builder()
                        .setAlbumTitle(it.album)
                        .setArtist(it.artist)
                        .setGenre(it.genre)
                        .setTitle(it.title)
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




                mediaController?.addListener(object : Player.Listener{
                    override fun onEvents(player: Player, events: Player.Events) {
                        super.onEvents(player, events)
                        Log.e("player", "onEvents: $events")
                        for (i in 0 until  events.size()){
                            Log.e("player", "onEvents: ${events[i]}")
                        }
                    }

                    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                        super.onTimelineChanged(timeline, reason)
                        Log.e("player", "onTimelineChanged  timeline: $timeline \t reason: $reason")
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                        Log.e("player", "onMediaItemTransition timeline: $mediaItem \t reason: $reason")
                        binding.tvTitle.text = mediaItem?.mediaMetadata?.title

                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        super.onTracksChanged(tracks)
                        Log.e("player", "onTracksChanged tracks: ${tracks}, length: ${tracks.groups.size}")
                    }


                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                        val currentPosition = mediaController?.currentPosition
                        val currentMediaItemIndex = mediaController?.currentMediaItemIndex
                        Log.e("player", "currentPosition: $currentPosition \t currentMediaItemIndex: $currentMediaItemIndex")

                    }

                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        super.onMediaMetadataChanged(mediaMetadata)
                        val duration = mediaController?.duration?:0
                        Log.e("player", "onMediaMetadataChanged: $duration")
                        Log.e("player", "onMediaMetadataChanged: ${mediaMetadata.title}")
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
                                    showProgress()
                                }

                                override fun onFinish() {
                                    timeCountDown?.cancel()
                                }

                            })
                        timeCountDown?.start()
                    }

                    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
                        super.onPlaylistMetadataChanged(mediaMetadata)
                        Log.e("player", "onPlaylistMetadataChanged: ${ mediaMetadata.toString() }")
                        binding.tvTitle.text = mediaMetadata.title
                    }

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        super.onIsLoadingChanged(isLoading)
                        Log.e("player", isLoading.toString())
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        Log.e("player", "onIsPlayingChanged isPlaying: $isPlaying")
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Log.e("player", "onPlayerError: $error")
                    }

                    override fun onPlayerErrorChanged(error: PlaybackException?) {
                        super.onPlayerErrorChanged(error)
                        Log.e("player", "onPlayerErrorChanged: $error")
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        super.onPlayWhenReadyChanged(playWhenReady, reason)
                        Log.e("player","onPlayWhenReadyChanged: playWhenReady: $playWhenReady \t reason: $reason")
                    }
                })
            }
        }, MoreExecutors.directExecutor())
    }

    fun showProgress(){
        val currentPosition = mediaController?.currentPosition?:0L
        val currentMediaItemIndex = mediaController?.currentMediaItemIndex?:0
        Log.e("player", "currentPosition: $currentPosition \t currentMediaItemIndex: $currentMediaItemIndex")
        val p = (currentPosition / 1000L) / (songList[currentMediaItemIndex].duration * 1.0)
        binding.seek.progress = (p * 100).toInt()
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaController?.release()
        MediaController.releaseFuture(controllerFuture)
    }

    private var timeCountDown: TimeCountDown? = null

}