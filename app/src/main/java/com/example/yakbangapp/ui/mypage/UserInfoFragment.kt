package com.example.yakbangapp.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.yakbangapp.databinding.FragmentUserInfoBinding
import com.example.yakbangapp.auth.UserProfile
import com.example.yakbangapp.auth.UserSession
import com.example.yakbangapp.ui.auth.LoginActivity
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class UserInfoFragment : Fragment() {
    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    private val vm by viewModels<MyPageViewModel>({ requireActivity() }) // 액티비티와 공유

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.profile.observe(viewLifecycleOwner) { p ->
            renderProfile(p)
        }

        binding.btnConnectKakao.setOnClickListener {
            // 미로그인 → 로그인 화면
            val p = vm.profile.value ?: UserProfile()
            if (p.id.isEmpty()) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "이미 연결되어 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDisconnect.setOnClickListener {
            val p = vm.profile.value ?: UserProfile()
            if (p.provider == "kakao" && p.id.isNotEmpty()) {
                // 카카오 연결 해제 (연결 끊기)
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), "연결 해제 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        lifecycleScope.launch {
                            UserSession(requireContext()).clear()
                            Toast.makeText(requireContext(), "연결이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "연결된 계정이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderProfile(p: UserProfile) {
        val loggedIn = p.id.isNotEmpty()

        // 이름 표시: 로그인됐으면 실제 이름, 아니면 안내 문구
        binding.tvName.text = if (loggedIn) (p.name.ifEmpty { "이름 미설정" }) else "로그인이 필요합니다"
        binding.tvEmail.text = if (loggedIn) p.email else ""
        binding.tvProvider.text = if (loggedIn) "연결: ${p.provider}" else "연결된 계정 없음"

        // 프로필 이미지
        if (loggedIn && p.avatarUrl.isNotEmpty()) {
            Glide.with(this).load(p.avatarUrl).circleCrop().into(binding.ivProfile)
        } else {
            binding.ivProfile.setImageResource(com.example.yakbangapp.R.drawable.ic_person)
        }

        // 버튼 가시성 토글
        binding.btnConnectKakao.visibility   = if (loggedIn) View.GONE else View.VISIBLE
        binding.btnDisconnect.visibility     = if (loggedIn && p.provider == "kakao") View.VISIBLE else View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
