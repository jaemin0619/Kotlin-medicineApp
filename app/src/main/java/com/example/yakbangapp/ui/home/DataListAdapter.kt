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

class DataListAdapter(private val onClickAction: (data: YakData) -> Unit) :
    ListAdapter<YakData, DataListAdapter.YakItemViewHolder>(object :
        DiffUtil.ItemCallback<YakData>() {
        override fun areItemsTheSame(oldItem: YakData, newItem: YakData): Boolean {
            return oldItem.productCode == newItem.productCode
        }

        override fun areContentsTheSame(oldItem: YakData, newItem: YakData): Boolean {
            return oldItem == newItem
        }

    }) {

    inner class YakItemViewHolder(binding: LayoutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val thumbnailView = binding.yakIv
        private val nameView = binding.yakNameTv
        private val codeView = binding.yakCodeTv
        private val vendorView = binding.yakVendorTv

        fun bind(data: YakData) {
            with(data) {
                Glide.with(itemView).load(imageUrl).placeholder(R.drawable.ic_dia)
                    .into(thumbnailView)
                nameView.text = productName
                codeView.text = productCode
                vendorView.text = companyName
                itemView.setOnClickListener { onClickAction(data) }
            }
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