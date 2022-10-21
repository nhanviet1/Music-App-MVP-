package com.example.musicapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicapp.view.*


class Receiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, intent: Intent?) {
        val actionMusic = intent?.getIntExtra(ACTION_MUSIC, 0)
        when (actionMusic) {
            PAUSE -> {
                Intent(ACTION_PAUSE).run {
                    putExtra(ACTION_MUSIC, "$actionMusic")
                    p0?.sendBroadcast(this)
                }
            }
            RESUME -> {
                Intent(ACTION_PLAY).run {
                    putExtra(ACTION_MUSIC, "$actionMusic")
                    p0?.sendBroadcast(this)
                }
            }
            NEXT -> {
                Intent(ACTION_NEXT).run {
                    putExtra(ACTION_MUSIC, "$actionMusic")
                    p0?.sendBroadcast(this)
                }
            }
            PREVIOUS -> {
                Intent(ACTION_PREVIOUS).run {
                    putExtra("actionMusic", "$actionMusic")
                    p0?.sendBroadcast(this)
                }
            }
        }
    }
}
