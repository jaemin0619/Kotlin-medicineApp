// app/src/main/java/com/example/yakbangapp/ui/mypage/AiChatFragment.kt
package com.example.yakbangapp.ui.mypage

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbangapp.databinding.FragmentAiChatBinding
import com.example.yakbangapp.ui.aichat.AiChatAdapter
import com.example.yakbangapp.ui.aichat.AiChatViewModel

class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private val vm by viewModels<AiChatViewModel>()
    private lateinit var adapter: AiChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- RecyclerView 설정 ---
        adapter = AiChatAdapter()
        val lm = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvChat.apply {
            layoutManager = lm
            adapter = this@AiChatFragment.adapter
            setHasFixedSize(false)
        }

        // 새 메시지 들어오면 맨 아래로 스크롤
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvChat.scrollToPosition(
                    (adapter.itemCount - 1).coerceAtLeast(0)
                )
            }
        })

        // LiveData 관찰
        vm.messages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                binding.rvChat.scrollToPosition(list.lastIndex.coerceAtLeast(0))
            }
        }

        // 키보드/레이아웃 변화 시 보정 스크롤
        binding.rvChat.doOnLayout {
            if (::adapter.isInitialized && adapter.itemCount > 0) {
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
            }
        }

        // --- 입력/전송 로직 ---
        fun sendCurrentText() {
            val msg = binding.etMessage.text?.toString()?.trim().orEmpty()
            if (msg.isEmpty()) return            // 공백 전송 방지 (Span 오류 예방)
            vm.send(msg)                         // 실제 전송은 ViewModel에 위임
            binding.etMessage.setText("")        // 입력창 초기화
        }

        // 버튼 활성/비활성
        binding.etMessage.doOnTextChanged { text, _, _, _ ->
            binding.btnSend.isEnabled = !text.isNullOrBlank()
        }

        // 전송 버튼
        binding.btnSend.setOnClickListener { sendCurrentText() }

        // 키보드 "보내기" 액션
        binding.etMessage.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                sendCurrentText()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
