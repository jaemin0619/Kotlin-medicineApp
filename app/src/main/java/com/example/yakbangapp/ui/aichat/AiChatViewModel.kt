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

    fun send(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatItem(nextId++, userText.trim(), isUser = true)
        _messages.value = _messages.value.orEmpty() + userMsg

        viewModelScope.launch {
            try {
                val resp = ApiClient.api.chat(ChatRequest(userText.trim()))
                val reply = if (resp.isSuccessful) {
                    resp.body()?.response?.takeIf { it.isNotBlank() } ?: "응답이 비어 있습니다."
                } else {
                    "오류: ${resp.code()} - ${resp.errorBody()?.string().orEmpty()}"
                }
                val botMsg = ChatItem(nextId++, reply, isUser = false)
                _messages.postValue(_messages.value.orEmpty() + botMsg)
            } catch (e: Exception) {
                val err = ChatItem(nextId++, "네트워크 오류: ${e.message}", isUser = false)
                _messages.postValue(_messages.value.orEmpty() + err)
            }
        }
    }
}
