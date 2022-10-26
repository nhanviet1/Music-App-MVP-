package com.example.musicapp.data.source.local

import com.example.musicapp.data.model.Song

interface OnResultCallback {
    fun onDataLoaded(data: List<Song>)
    fun onFailed()
}
