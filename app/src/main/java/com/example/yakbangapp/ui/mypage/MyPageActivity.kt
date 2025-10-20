package com.example.yakbangapp.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.example.yakbangapp.MyMedsFragment
import com.example.yakbangapp.R
import com.example.yakbangapp.auth.UserSession
import com.example.yakbangapp.databinding.ActivityMyPageBinding
import com.example.yakbangapp.ui.auth.LoginActivity
import com.example.yakbangapp.ui.mypage.MyPagePagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPageBinding
    private val vm: MyPageViewModel by viewModels()
    private lateinit var session: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = null  // ✅ subtitle 제거
        binding.toolbar.subtitle = null
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 세션 및 ViewModel 초기화
        session = UserSession(this)
        vm.bindSession(this)

        // 탭 구성
        val fragments = listOf(
            UserInfoFragment(),   // 사용자 정보
            AiChatFragment(),     // AI 채팅
            MyMedsFragment()      // 복약 관리
        )
        val titles = listOf("사용자 정보", "AI채팅", "복약 관리")
        val adapter = MyPagePagerAdapter(this, fragments)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.pager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()

        // 프로필 관찰 — subtitle 제거 후 메뉴만 업데이트
        vm.profile.observe(this) { p ->
            val loggedIn = p.id.isNotEmpty()
            val menu = binding.toolbar.menu
            val itemLogin = menu.findItem(R.id.action_login_logout)
            val itemEditName = menu.findItem(R.id.action_edit_name)

            itemLogin?.title = if (loggedIn) "연결 해제" else "로그인"
            itemEditName?.isVisible = loggedIn
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.my_page, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_name -> {
                showEditNameDialog()
                true
            }
            R.id.action_login_logout -> {
                vm.onLoginOrLogout(this) { goLogin ->
                    if (goLogin) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        Toast.makeText(this, "연결이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showEditNameDialog() {
        val til = TextInputLayout(this).apply {
            isHintEnabled = true
            hint = "새 닉네임"
            setPadding(32, 16, 32, 0)
        }

        val et = TextInputEditText(til.context).apply {
            setPadding(0, 24, 0, 16)
        }

        til.addView(
            et,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("닉네임 설정")
            .setView(til)
            .setPositiveButton("저장") { d, _ ->
                val name = et.text?.toString()?.trim().orEmpty()
                if (name.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        session.setName(name)
                    }
                    Toast.makeText(this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
