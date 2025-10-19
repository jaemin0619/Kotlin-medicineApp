// app/src/main/java/com/example/yakbangapp/ui/aichat/AiChatAdapter.kt
package com.example.yakbangapp.ui.aichat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbangapp.databinding.ItemChatBotBinding
import com.example.yakbangapp.databinding.ItemChatUserBinding

private const val TYPE_USER = 1
private const val TYPE_BOT = 2

class AiChatAdapter : ListAdapter<ChatItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(o: ChatItem, n: ChatItem) = o.id == n.id
            override fun areContentsTheSame(o: ChatItem, n: ChatItem) = o == n
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val binding = ItemChatUserBinding.inflate(inf, parent, false)
            UserVH(binding)
        } else {
            val binding = ItemChatBotBinding.inflate(inf, parent, false)
            BotVH(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is UserVH) holder.bind(item)
        else if (holder is BotVH) holder.bind(item)
    }

    class UserVH(private val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            binding.tvMsg.text = item.text
        }
    }
    class BotVH(private val binding: ItemChatBotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            binding.tvMsg.text = item.text
        }
    }
}
