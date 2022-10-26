package com.example.musicapp.view

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.data.model.Song
import com.example.musicapp.data.repository.SongRepository
import com.example.musicapp.data.source.local.MusicLocalDataSource
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.service.BoundService
import com.example.musicapp.view.adapter.SongAdapter
import com.example.musicapp.view.presenter.SongPresenter
import com.example.mvpmusicapp.ui.ServiceInterface
import com.example.mvpmusicapp.ui.SongInterface
import java.util.*

class MainActivity : AppCompatActivity(), SongInterface.View, ServiceInterface {

    private val list = mutableListOf<Song>()
    private val songAdapter = SongAdapter(this@MainActivity, ::onClickItem)
    private var songPresenter: SongInterface.Presenter? = null
    private var isServiceConnected = false
    private var mService: BoundService? = null
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initService()
        setup()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as BoundService.MyBinder
            mService = binder.getBoundService()
            mService?.setProgressInterface(this@MainActivity)
            isServiceConnected = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceConnected = false
        }
    }

    private fun setup() {
        songAdapter
        binding.rvMusicList.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rvMusicList.adapter = songAdapter
        songPresenter = SongPresenter(
            SongRepository.getInstance(MusicLocalDataSource.getInstance()),
            this@MainActivity
        )
        storagePermission()
        bindingButton()
    }

    private fun onClickItem(position: Int) {
        if (isServiceConnected) {
            if (mService != null) {
                if (mService!!.songList.isEmpty()) {
                    mService!!.setData(list)
                    mService!!.playSong(position)
                } else {
                    mService!!.playSong(position)
                }
            }
        }
        binding.textSongName.text = list[position].name
        setProgressBar()
        updateProgress()
        onCheckButtonState()
    }

    override fun getAllSongList(songList: List<Song>?) {
        songAdapter.setData(songList!!)
        list.clear()
        list.addAll(songList)
    }

    override fun showErrorMessage() {
        Toast.makeText(this, R.string.text_load_song_failed, Toast.LENGTH_SHORT).show()
    }

    override fun setPlayButton() {
        val songHolder = mService?.songHolder
        if (isServiceConnected && songHolder != null) {
            if (mService?.songList!!.isEmpty()) {
                mService?.setData(list)
                mService?.playSong(mService?.songHolder!!)
            } else {
                if (mService?.currentPosition() == 0) {
                    mService?.playSong(mService?.songHolder!!)
                } else {
                    mService?.resumeMusic()
                }
            }
        }
        setProgressBar()
        updateProgress()
        binding.imagePlay.setImageResource(R.drawable.ic_baseline_pause_circle_24)
    }

    override fun setPauseButton() {
        mService?.pauseMusic()
        binding.imagePlay.setImageResource(R.drawable.ic_baseline_play_circle_24)
    }

    private fun storagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    songPresenter?.getLocalSongs(this@MainActivity)
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }


    private fun bindingButton() {
        binding.imagePlay.setOnClickListener {
            if (list.isNotEmpty()) {
                if (!mService?.isPlaying!!) {
                    songPresenter?.playMusic(true)
                } else {
                    songPresenter?.playMusic(false)
                }
            }
        }

        binding.imagePrevious.setOnClickListener {
            if (list.isNotEmpty()) {
                if (isServiceConnected) {
                    if (mService?.songList!!.isEmpty()) {
                        mService?.setData(list)
                        mService?.switchSong(PREVIOUS)
                    } else {
                        mService?.switchSong(PREVIOUS)
                    }
                }
                binding.imagePlay.setImageResource(R.drawable.ic_baseline_pause_circle_24)
            }
        }

        binding.imageNext.setOnClickListener {
            if (list.isNotEmpty()) {
                if (isServiceConnected) {
                    if (mService?.songList!!.isEmpty()) {
                        mService?.setData(list)
                        mService?.switchSong(NEXT)
                    } else {
                        mService?.switchSong(NEXT)
                    }
                }
                binding.imagePlay.setImageResource(R.drawable.ic_baseline_pause_circle_24)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceConnected) {
            unbindService(connection)
            isServiceConnected = false
        }
    }

    private fun initService() {
        val intent = Intent(this, BoundService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun updateProgress(position: Int?) {
        Thread { binding.progressMusic.progress = position ?: 0 }.start()
        if (mService != null) {
            if (mService!!.currentPosition()!! < mService!!.getDuration()!!) {
                binding.progressMusic.progress = mService!!.currentPosition()!!
            }
        }
    }

    private fun setProgressBar() {
        if (mService?.getDuration() != null) {
            binding.progressMusic.max = mService!!.getDuration()!!
            binding.progressMusic.progress = 0
        }
    }

    private fun updateProgress() {
        val musicTime = Timer()
        val currentPosition = mService?.currentPosition()
        val duration = mService?.getDuration()
        musicTime.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (currentPosition != null && duration != null) {
                        if (currentPosition < duration) {
                            binding.progressMusic.progress =
                                mService!!.currentPosition()!!
                        } else {
                            binding.progressMusic.progress = 0
                            musicTime.cancel()
                            musicTime.purge()
                        }
                    }
                }
            }

        }, TIME_DELAY, TIME_PERIOD)
    }

    override fun onCheckButtonState() {
        if (mService != null) {
            when (mService!!.isPlaying) {
                true -> binding.imagePlay.setImageResource(R.drawable.ic_baseline_pause_circle_24)
                false -> binding.imagePlay.setImageResource(R.drawable.ic_baseline_play_circle_24)
            }
        }
    }

    override fun updateTitle(position: Int?) {
        if (position != null) {
            binding.textSongName.text = list[position].name
        }
    }
}
