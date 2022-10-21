package com.example.musicapp.data.repository

import android.content.Context
import com.example.musicapp.data.source.MusicDataSource
import com.example.musicapp.data.source.local.OnResultCallback

class SongRepository(private val musicDataSource: MusicDataSource) :
    MusicDataSource {

    override fun getData(context: Context, callback: OnResultCallback) {
        musicDataSource.getData(context, callback)
    }

    companion object {
        private var instance: SongRepository? = null
        fun getInstance(musicDataSource: MusicDataSource) = synchronized(this) {
            instance ?: SongRepository(musicDataSource).also { instance = it }
        }
    }
}
