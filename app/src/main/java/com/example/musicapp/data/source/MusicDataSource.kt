package com.example.musicapp.data.source

import android.content.Context
import com.example.musicapp.data.source.local.OnResultCallback

interface MusicDataSource {
    fun getData(context: Context, callback: OnResultCallback)
}
