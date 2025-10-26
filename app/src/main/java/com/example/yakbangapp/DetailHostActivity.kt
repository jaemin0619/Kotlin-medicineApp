package com.example.yakbangapp.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbangapp.R
import com.example.yakbangapp.ui.data.YakData

class DetailHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_host)

        if (savedInstanceState == null) {
            val yak = intent.getParcelableExtra<YakData>("yak") ?: run { finish(); return }
            val fragment = DetailFragment().apply {
                arguments = Bundle().apply { putParcelable("yak", yak) }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.detail_container, fragment)
                .commit()
        }
    }
}

