package com.example.musicapp.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.musicapp.R
import com.example.musicapp.data.model.Song
import com.example.musicapp.receiver.Receiver
import com.example.musicapp.view.*
import com.example.mvpmusicapp.ui.ServiceInterface

class BoundService : Service() {

    private var media: MediaPlayer? = null
    private val binder = MyBinder()
    private var notificationManager: NotificationManager? = null
    var isPlaying = false
    var songHolder = 0
    private var prgCallback: ServiceInterface? = null
    val songList: MutableList<Song> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }

    inner class MyBinder : Binder() {
        fun getBoundService(): BoundService = this@BoundService
    }

    override fun onBind(p0: Intent?): IBinder {
        initReceiver()
        return binder
    }

    fun setData(list: List<Song>) {
        if (songList.isNotEmpty()) {
            songList.clear()
            songList.addAll(list)
        } else {
            songList.addAll(list)
        }
    }

    private var serviceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PLAY -> {
                    actionControl(RESUME)
                }
                ACTION_PAUSE -> {
                    actionControl(PAUSE)
                }
                ACTION_NEXT -> {
                    actionControl(NEXT)
                }
                ACTION_PREVIOUS -> {
                    actionControl(PREVIOUS)
                }
            }
        }
    }

    private fun sendNotification(song: Song) {
        val remoteView = RemoteViews(packageName, R.layout.notification)
        remoteView.setTextViewText(R.id.textNotifyName, song.name)
        remoteView.setTextViewText(R.id.textNotifyInfor, song.singer)
        remoteView.setOnClickPendingIntent(
            R.id.imageRewind,
            getPendingIntent(this, PREVIOUS)
        )
        remoteView.setOnClickPendingIntent(
            R.id.imageForward,
            getPendingIntent(this, NEXT)
        )
        if (isPlaying) {
            remoteView.setOnClickPendingIntent(
                R.id.imagePlayMusic,
                getPendingIntent(this, PAUSE)
            )
            remoteView.setImageViewResource(
                R.id.imagePlayMusic,
                R.drawable.ic_baseline_pause_circle_24_black
            )
        } else {
            remoteView.setOnClickPendingIntent(
                R.id.imagePlayMusic,
                getPendingIntent(this, RESUME)
            )
            remoteView.setImageViewResource(
                R.id.imagePlayMusic,
                R.drawable.ic_baseline_play_arrow_24_black
            )
        }

        val notification = NotificationCompat.Builder(this@BoundService, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music)
            .setCustomContentView(remoteView)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            notificationManager?.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(context, Receiver::class.java)
        intent.putExtra("actionMusic", action)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(
            applicationContext,
            action,
            intent,
            pendingIntent
        )
    }

    private fun actionControl(action: Int) {
        when (action) {
            PAUSE -> pauseMusic()
            RESUME -> resumeMusic()
            NEXT -> switchSong(NEXT)
            PREVIOUS -> switchSong(PREVIOUS)
        }
        prgCallback?.onCheckButtonState()
    }

    fun playSong(position: Int) {
        if (isPlaying) {
            media?.pause()
        }
        if (songList.isNotEmpty()) {
            media = MediaPlayer.create(applicationContext, songList[position].path.toUri())
            media?.start()
            isPlaying = true
            songHolder = position
            sendNotification(songList[songHolder])
            prgCallback?.updateTitle(position)
        }
    }

    fun pauseMusic() {
        if (songList.isNotEmpty()) {
            if (isPlaying) {
                media?.pause()
                isPlaying = false
                sendNotification(songList[songHolder])
            }
        }
    }

    fun resumeMusic() {
        if (songList.isNotEmpty()) {
            if (!isPlaying) {
                media?.start()
                isPlaying = true
                sendNotification(songList[songHolder])
            }
        }
    }

    fun currentPosition() = media?.currentPosition

    fun getDuration() = media?.duration

    fun switchSong(value: Int) {
        if (songList.isNotEmpty()) {
            when (value) {
                NEXT -> {
                    if (songHolder == songList.size - 1) {
                        songHolder = 0
                        playSong(songHolder)
                    } else {
                        songHolder += 1
                        playSong(songHolder)
                    }
                }
                PREVIOUS -> {
                    if (songHolder == 0) {
                        songHolder = songList.size - 1
                        playSong(songHolder)
                    } else {
                        songHolder -= 1
                        playSong(songHolder)
                    }
                }
            }
        }
    }

    private fun initReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_PREVIOUS)
        intentFilter.addAction(ACTION_PLAY)
        intentFilter.addAction(ACTION_PAUSE)
        intentFilter.addAction(ACTION_NEXT)
        intentFilter.addAction(ACTION_STOP)
        registerReceiver(serviceReceiver, intentFilter)
    }

    fun setProgressInterface(serviceInterface: ServiceInterface) {
        prgCallback = serviceInterface
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlaying) {
            media?.release()
        }
        unregisterReceiver(serviceReceiver)
    }
}
