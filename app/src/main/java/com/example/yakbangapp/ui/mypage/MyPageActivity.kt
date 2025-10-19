// app/src/main/java/com/example/yakbangapp/ui/mypage/MyPageActivity.kt
package com.example.yakbangapp.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbangapp.R
import com.example.yakbangapp.auth.UserSession
import com.example.yakbangapp.databinding.ActivityMyPageBinding
import com.example.yakbangapp.ui.auth.LoginActivity
import com.example.yakbangapp.MyMedsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPageBinding
    private val vm by viewModels<MyPageViewModel>()
    private lateinit var session: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // UserSession & ViewModel
        session = UserSession(this)
        vm.bindSession(this)

        // ★ Tabs: 즐겨찾기 제거, AI채팅 추가
        val fragments = listOf(
            UserInfoFragment(),   // 사용자 정보
            AiChatFragment(),     // ★ AI채팅
            MyMedsFragment()      // 복약 관리
        )
        val titles = listOf("사용자 정보", "AI채팅", "복약 관리")
        val adapter = MyPagePagerAdapter(this, fragments)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.pager) { tab, pos -> tab.text = titles[pos] }.attach()

        // 프로필 관찰: 툴바 서브타이틀/메뉴 갱신
        vm.profile.observe(this) { p ->
            val loggedIn = p.id.isNotEmpty()
            binding.toolbar.subtitle = if (loggedIn) {
                if (p.name.isNotBlank()) p.name else p.id
            } else {
                getString(R.string.login_required_short)
            }

            val menu = binding.toolbar.menu
            val itemLogin = menu.findItem(R.id.action_login_logout)
            val itemEditName = menu.findItem(R.id.action_edit_name)
            itemLogin?.title = if (loggedIn) "연결해제" else "로그인"
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
                    if (goLogin) startActivity(Intent(this, LoginActivity::class.java))
                    else Toast.makeText(this, "연결이 해제되었습니다.", Toast.LENGTH_SHORT).show()
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
