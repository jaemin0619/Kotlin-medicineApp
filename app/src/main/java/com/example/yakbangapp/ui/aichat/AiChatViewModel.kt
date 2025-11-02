// app/src/main/java/com/example/yakbangapp/ui/aichat/AiChatViewModel.kt
package com.example.yakbangapp.ui.aichat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yakbangapp.ui.aichat.net.ApiClient
import com.example.yakbangapp.ui.aichat.net.ChatRequest
import kotlinx.coroutines.launch

class AiChatViewModel : ViewModel() {

    private var nextId = 1L
    private val _messages = MutableLiveData<List<ChatItem>>(emptyList())
    val messages: LiveData<List<ChatItem>> = _messages

    private fun append(item: ChatItem) {
        val cur = _messages.value.orEmpty()
        _messages.value = cur + item
    }

    fun send(userText: String) {
        val text = userText.trim()
        if (text.isBlank()) return

        val userMsg = ChatItem(nextId++, text, isUser = true)
        append(userMsg)

        // (선택) 타이핑 표시용 플레이스홀더
        val typingId = nextId++
        append(ChatItem(typingId, "…", isUser = false))

        viewModelScope.launch {
            try {
                val resp = ApiClient.api.chat(ChatRequest(text))
                val reply = if (resp.isSuccessful) {
                    resp.body()?.response?.takeIf { it.isNotBlank() } ?: "응답이 비어 있습니다."
                } else {
                    val errBody = resp.errorBody()?.string().orEmpty()
                    "오류: ${resp.code()} - $errBody"
                }
                // 타이핑 메시지 교체
                _messages.value = _messages.value.orEmpty().map {
                    if (it.id == typingId) it.copy(text = reply) else it
                }
            } catch (e: Exception) {
                _messages.value = _messages.value.orEmpty().map {
                    if (it.id == typingId) it.copy(text = "네트워크 오류: ${e.message}") else it
                }
            }
        }
    }
}
