package com.example.musicapp.view.extensions

import com.example.musicapp.view.ONE_HUNDRED
import com.example.musicapp.view.SIXTY

fun String.convertDurationToInt(): Int{
    return ((this.toDouble() / ONE_HUNDRED) * SIXTY).toInt()
}
