package com.example.yakbangapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.yakbangapp.databinding.ActivitySearchOptionBinding
import com.example.yakbangapp.MyMedsActivity

class SearchOptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchOptionBinding
    private val CAMERA_PERMISSION_REQUEST = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 텍스트 검색 → MainActivity
        binding.buttonTextSearch.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 카메라 검색 → 권한 체크 후 CameraSearchActivity
        binding.buttonCameraSearch.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        // 복약 관리 → MyMedsActivity
        binding.buttonMyMeds.setOnClickListener {
            startActivity(Intent(this, MyMedsActivity::class.java))
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST
            )
        } else {
            openCameraActivity()
        }
    }

    private fun openCameraActivity() {
        startActivity(Intent(this, CameraSearchActivity::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraActivity()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
