package com.example.musicapp.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.model.Song
import com.example.musicapp.databinding.SongItemBinding
import com.example.mvpmusicapp.ui.convertToDurationFormat

class SongAdapter(private val context: Context, private val onClickItem: (Int) -> Unit) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
        private val songList = mutableListOf<Song>()

    inner class ViewHolder(private val binding: SongItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(data: Song, position: Int) {
            binding.textName.text = data.name
            binding.textInfor.text = context.resources.getString(
                R.string.text_music_infor,
                convertToDurationFormat(context, data.duration.toDouble()),
                data.singer
            )
            binding.root.setOnClickListener{onClickItem(position)}
        }
    }

    fun setData(data: List<Song>) {
        songList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(songList[holder.adapterPosition], position)
    }

    override fun getItemCount(): Int {
        return songList.size
    }
}
