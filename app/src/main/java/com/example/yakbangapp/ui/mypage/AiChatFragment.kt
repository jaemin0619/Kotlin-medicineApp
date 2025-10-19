// app/src/main/java/com/example/yakbangapp/ui/mypage/AiChatFragment.kt
package com.example.yakbangapp.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yakbangapp.databinding.FragmentAiChatBinding
import com.example.yakbangapp.ui.aichat.AiChatAdapter
import com.example.yakbangapp.ui.aichat.AiChatViewModel

class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private val vm by viewModels<AiChatViewModel>()
    private lateinit var adapter: AiChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AiChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        vm.messages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                binding.rvChat.scrollToPosition(list.lastIndex.coerceAtLeast(0))
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text?.toString().orEmpty()
            vm.send(text)
            binding.etMessage.setText("")
        }
        binding.etMessage.doOnTextChanged { text, _, _, _ ->
            binding.btnSend.isEnabled = !text.isNullOrBlank()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
