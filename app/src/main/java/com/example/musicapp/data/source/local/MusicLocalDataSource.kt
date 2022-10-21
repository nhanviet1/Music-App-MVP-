package com.example.musicapp.data.source.local

import android.content.Context
import com.example.musicapp.data.source.MusicDataSource


class MusicLocalDataSource : MusicDataSource {

    override fun getData(context: Context, callback: OnResultCallback) {
        val songList = MusicPlayerHelper().fetchSongFromStorage(context)
        if (songList.size > 0) {
            callback.onDataLoaded(songList)
        } else {
            callback.onFailed()
        }
    }

    companion object {
        private var instance: MusicLocalDataSource? = null

        fun getInstance() = synchronized(this) {
            instance ?: MusicLocalDataSource().also { instance = it }
        }
    }

}
