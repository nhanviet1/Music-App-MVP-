package com.example.musicapp.data.model

import java.io.Serializable

data class Song(
    val id: Int = 0,
    val name: String = "",
    val singer: String = "",
    val path: String = "",
    val duration: Int = 0,
): Serializable
