package com.example.mvpmusicapp.ui

import android.content.Context
import com.example.musicapp.data.model.Song

interface SongInterface {
    interface Presenter {
        fun getLocalSongs(context: Context)
        fun playMusic(status: Boolean)
    }

    interface View {
        fun getAllSongList(songList: List<Song>?)
        fun showErrorMessage()
        fun setPlayButton()
        fun setPauseButton()
    }
}
