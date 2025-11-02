// app/src/main/java/com/example/yakbangapp/ui/mypage/MyPageActivity.kt
package com.example.yakbangapp.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.example.yakbangapp.R
import com.example.yakbangapp.auth.UserSession
import com.example.yakbangapp.databinding.ActivityMyPageBinding
import com.example.yakbangapp.ui.auth.LoginActivity
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

    // 현재 로그인 상태 캐시 (메뉴 표시용)
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = null
        binding.toolbar.subtitle = null
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 세션/VM
        session = UserSession(this)
        vm.bindSession(this)

        // 탭 구성
        val fragments = listOf(
            UserInfoFragment(),
            AiChatFragment(),
            com.example.yakbangapp.MyMedsFragment()
        )
        val titles = listOf("사용자 정보", "AI채팅", "복약 관리")
        val adapter = MyPagePagerAdapter(this, fragments)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.pager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()

        // 프로필 관찰 → 로그인 상태 갱신 후 메뉴 다시 그리기
        vm.profile.observe(this) { p ->
            isLoggedIn = p.id.isNotEmpty()
            invalidateOptionsMenu() // 메뉴 갱신 트리거
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.my_page, menu)
        // ❌ 초기 강제 숨김 금지: 로그인 상태 반영은 onPrepareOptionsMenu 에서 처리
        // menu.findItem(R.id.action_edit_name)?.isVisible = false
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // 현재 상태에 맞춰 메뉴 동기화
        val itemLogin = menu.findItem(R.id.action_login_logout)
        val itemEditName = menu.findItem(R.id.action_edit_name)

        itemLogin?.title = if (isLoggedIn) "연결 해제" else "로그인"
        itemEditName?.isVisible = isLoggedIn
        // 공간이 좁아도 항상 보이게 하려면:
        itemEditName?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onPrepareOptionsMenu(menu)
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
                        // "로그인" 선택 → 로그인 화면으로
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        // "연결 해제(로그아웃)"
                        Toast.makeText(this, "연결이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(this, LoginActivity::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            }
                        )
                        finish()
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
            setText(vm.profile.value?.name.orEmpty())
        }

        // 🔥 LinearLayout.LayoutParams 로 변경
        til.addView(
            et,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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
                } else {
                    Toast.makeText(this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
