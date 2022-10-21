package com.example.musicapp.data.source.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.musicapp.data.model.Song
import com.example.musicapp.view.TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.*


class MusicPlayerHelper() {

    fun fetchSongFromStorage(context: Context): MutableList<Song> {
        val songList = mutableListOf<Song>()
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val audioCursor = context.contentResolver.query(contentUri, proj, null, null, null)

        if (audioCursor?.moveToFirst() == true) {
            do {
                audioCursor.run {
                    val audioId = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val audioTitle =getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val audioArtist =
                        getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val audioDuration =
                        getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val audioData = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    if (audioDuration != null) {
                        val song = Song(audioId, audioTitle, audioArtist, audioData, audioDuration)
                        songList.add(song)
                    }
                }

            } while (audioCursor.moveToNext())
        }

        return songList
    }

    fun parseLongToTime(timeLong: Int): String {
        var time = ""
        time = SimpleDateFormat(TIME_FORMAT).format(Date(timeLong.toLong()))
        return time
    }

    fun getBitmapSong(path: String): Bitmap? {
        val mmr = MediaMetadataRetriever()
        val uri = Uri.parse(path).toString()
        try {
            mmr.setDataSource(uri, hashMapOf())
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }
        val byteImage = mmr.embeddedPicture
        if (byteImage != null) {
            return BitmapFactory.decodeByteArray(byteImage, 0, byteImage.size)
        }
        return null
    }
}
