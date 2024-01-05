package com.duyh.videodemo

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.media.RemoteController
import android.media.RemoteController.OnClientUpdateListener
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.app.NotificationCompat

class MediaControllerService : NotificationListenerService(), OnClientUpdateListener {
    override fun onCreate() {
        super.onCreate()
        Log.i(MY_TAG, "MediaControllerService onCreate")
        initNotify("MediaController", "MediaControllerService")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(MY_TAG, "MediaControllerService onStartCommand")
        if (Utils.isNotificationListenerEnabled(this)) { //开启通知使用权后再设置,否则会报权限错误
            initMediaSessionManager()
            registerRemoteController()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MY_TAG, "MediaControllerService onDestroy")
    }

    //////////////////////////////////MediaController获取音乐信息/////////////////////////////////////
    private var mActiveSessions: List<MediaController>? = null
    private var mSessionCallback: MediaController.Callback? = null
    private fun initMediaSessionManager() {
        val mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        val localComponentName = ComponentName(this, MediaControllerService::class.java)
        mediaSessionManager.addOnActiveSessionsChangedListener(object :
            OnActiveSessionsChangedListener {
            override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
                for (mediaController in controllers!!) {
                    val packageName = mediaController.packageName
                    Log.e(
                        MY_TAG,
                        "MyApplication onActiveSessionsChanged mediaController.getPackageName: $packageName"
                    )
                    synchronized(this) {
                        mActiveSessions = controllers
                        registerSessionCallbacks()
                    }
                }
            }
        }, localComponentName)
        synchronized(this) {
            mActiveSessions = mediaSessionManager.getActiveSessions(localComponentName)
            registerSessionCallbacks()
        }
    }

    private fun registerSessionCallbacks() {
        for (controller in mActiveSessions!!) {
            if (mSessionCallback == null) {
                mSessionCallback = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        if (metadata != null) {
                            val trackName = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                            val artistName =
                                metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                            val albumArtistName =
                                metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                            val albumName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
                            Log.i(MY_TAG, "---------------------------------")
                            Log.i(MY_TAG, "| trackName: $trackName")
                            Log.i(MY_TAG, "| artistName: $artistName")
                            Log.i(MY_TAG, "| albumArtistName: $albumArtistName")
                            Log.i(MY_TAG, "| albumName: $albumName")
                            Log.i(MY_TAG, "---------------------------------")
                        }
                    }

                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        if (state != null) {
                            val isPlaying = state.state == PlaybackState.STATE_PLAYING
                            Log.e(
                                MY_TAG,
                                "MediaController.Callback onPlaybackStateChanged isPlaying: $isPlaying"
                            )
                        }
                    }
                }
            }
            controller.registerCallback(mSessionCallback!!)
        }
    }

    //////////////////////////////////RemoteController获取音乐信息/////////////////////////////////////
    private var remoteController: RemoteController? = null
    private fun registerRemoteController() {
        remoteController = RemoteController(this, this)
        val registered: Boolean = try {
            (getSystemService(AUDIO_SERVICE) as AudioManager).registerRemoteController(
                remoteController
            )
        } catch (e: NullPointerException) {
            false
        }
        if (registered) {
            try {
                remoteController!!.setArtworkConfiguration(100, 100)
                remoteController!!.setSynchronizationMode(RemoteController.POSITION_SYNCHRONIZATION_CHECK)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    override fun onClientChange(clearing: Boolean) {
        Log.d(MY_TAG, "clearing == $clearing")
    }

    override fun onClientPlaybackStateUpdate(state: Int) {
        Log.d(MY_TAG, "state1 == $state")
    }

    override fun onClientPlaybackStateUpdate(
        state: Int,
        stateChangeTimeMs: Long,
        currentPosMs: Long,
        speed: Float
    ) {
        Log.i(
            MY_TAG,
            "state2 == " + state + "stateChangeTimeMs == " + stateChangeTimeMs + "currentPosMs == " + currentPosMs + "speed == " + speed
        )
    }

    override fun onClientTransportControlUpdate(transportControlFlags: Int) {
        Log.d(MY_TAG, "transportControlFlags == $transportControlFlags")
    }

    override fun onClientMetadataUpdate(metadataEditor: RemoteController.MetadataEditor) {
        val artist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "null")
        val album = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "null")
        val title = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "null")
        val duration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1)
        val defaultCover =
            BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_compass)
        val bitmap = metadataEditor.getBitmap(
            RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK,
            defaultCover
        )
        Log.e(MY_TAG, "artist:$artist, album:$album, title:$title, duration:$duration")
    }

    /**
     * 添加一个常驻通知
     * @param title
     * @param context
     */
    fun initNotify(title: String?, context: String?) {
        val CHANNEL_ONE_ID = "1000"
        val drawable = resources.getDrawable(R.mipmap.ic_launcher, null)
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        //Bitmap bitmapIcon = BitmapUtils.getBitmapFromDrawable(drawable);
        val nfIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_MUTABLE)
        @SuppressLint("WrongConstant") val builder =
            NotificationCompat.Builder(this.applicationContext, CHANNEL_ONE_ID)
                .setContentIntent(pendingIntent) // 设置PendingIntent
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                //.setLargeIcon(bitmapIcon)// 设置大图标
                .setContentTitle(title)
                .setContentText(context) // 设置上下文内容
                .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏显示全部通知
                //.setDefaults(Notification.DEFAULT_ALL)// //使用默认的声音、振动、闪光
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 通知的优先级

        //----------------  新增代码 ------------------------
        //修改安卓8.1以上系统报错
        val notificationChannel = NotificationChannel(
            CHANNEL_ONE_ID,
            "app_service_notify",
            NotificationManager.IMPORTANCE_MIN
        )
        notificationChannel.enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
        notificationChannel.setShowBadge(false) //是否显示角标
        notificationChannel.enableVibration(false) //是否震动
        notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC //锁屏显示全部通知
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
        builder.setChannelId(CHANNEL_ONE_ID)
        val notification = builder.build() // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND //设置为默认的声音
        startForeground(1, notification)
    }

    companion object {
        var MY_TAG = "MediaControllerService"
    }
}