package com.example.mvpmusicapp.ui

interface ServiceInterface {
    fun updateProgress(position: Int?)
    fun onCheckButtonState()
    fun updateTitle(position: Int?)
}
