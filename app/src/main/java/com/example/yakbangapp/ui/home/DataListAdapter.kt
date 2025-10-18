package com.example.yakbangapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yakbangapp.R
import com.example.yakbangapp.databinding.LayoutItemBinding
import com.example.yakbangapp.ui.data.YakData

class DataListAdapter(
    private val onClickAction: (data: YakData) -> Unit
) : ListAdapter<YakData, DataListAdapter.YakItemViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<YakData>() {
        override fun areItemsTheSame(oldItem: YakData, newItem: YakData): Boolean {
            return oldItem.productCode == newItem.productCode
        }
        override fun areContentsTheSame(oldItem: YakData, newItem: YakData): Boolean {
            return oldItem == newItem
        }
    }

    inner class YakItemViewHolder(private val binding: LayoutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: YakData) = with(binding) {
            // 텍스트
            yakNameTv.text = data.productName
            yakCodeTv.text = data.productCode
            yakVendorTv.text = data.companyName

            // ⭐ 재활용 잔상/Glide null 방지: 항상 먼저 clear()
            Glide.with(yakIv).clear(yakIv)

            val url = data.imageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(yakIv)
                    .load(url)
                    .placeholder(R.drawable.ic_dia) // 필요 시 교체
                    .error(R.drawable.ic_dia)       // 필요 시 교체
                    .into(yakIv)
            } else {
                yakIv.setImageResource(R.drawable.ic_dia)
            }

            root.setOnClickListener { onClickAction(data) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YakItemViewHolder {
        val binding = LayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YakItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: YakItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
