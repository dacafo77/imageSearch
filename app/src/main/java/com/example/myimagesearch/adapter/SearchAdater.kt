package com.example.myimagesearch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.R
import com.example.myimagesearch.databinding.ItemImageBinding
import com.example.myimagesearch.model.SearchModel
import java.text.SimpleDateFormat
import java.util.Locale

class SearchAdapter : ListAdapter<SearchModel, SearchAdapter.ImageItemViewHolder>(diffUtil) {

    inner class ImageItemViewHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(searchModel: SearchModel) {
            binding.titleTextView.text = searchModel.siteName

            // datetime을 String으로 변환하여 설정
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateText = searchModel.datetime?.let { dateFormat.format(it) } ?: "Unknown Date"
            binding.dateTextView.text = dateText

            // 썸네일 이미지 Glide로 로드
            Log.d("SearchAdapter", "Loading thumbnail URL: ${searchModel.thumbnailUrl}")

            Glide.with(binding.root.context)
                .load(searchModel.thumbnailUrl)
                .placeholder(R.drawable.notification_tile_bg) // 로딩 중 표시할 기본 이미지
                .error(R.drawable.notification_bg) // 에러 발생 시 표시할 기본 이미지
                .into(binding.thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<SearchModel>() {
            override fun areItemsTheSame(oldItem: SearchModel, newItem: SearchModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SearchModel, newItem: SearchModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
