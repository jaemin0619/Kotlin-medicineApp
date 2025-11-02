package com.example.yakbangapp.ui.aichat

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
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
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUser) TYPE_USER else TYPE_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val b = ItemChatUserBinding.inflate(inf, parent, false)
            UserVH(b)
        } else {
            val b = ItemChatBotBinding.inflate(inf, parent, false)
            BotVH(b)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is UserVH -> holder.bind(item)
            is BotVH  -> holder.bind(item)
        }
    }

    /** 사용자 말풍선 */
    inner class UserVH(val binding: ItemChatUserBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            binding.tvMsg.text = item.text
        }
    }

    /** 봇 말풍선 */
    inner class BotVH(val binding: ItemChatBotBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            bindText(binding.tvMsg, item.text)   // inner → 외부 멤버 호출 OK
        }
    }

    /** 공통 텍스트 바인딩 (링크/전화/메일 자동 감지) */
    private fun bindText(tv: TextView, text: String) {
        tv.text = text
        tv.setTextIsSelectable(true)
        LinkifyCompat.addLinks(
            tv,
            android.text.util.Linkify.WEB_URLS or
                    android.text.util.Linkify.PHONE_NUMBERS or
                    android.text.util.Linkify.EMAIL_ADDRESSES
        )
        tv.movementMethod = LinkMovementMethod.getInstance()
    }
}
