package com.example.yakbangapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SearchOptionActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_REQUEST = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_option)

        val textSearchButton = findViewById<Button>(R.id.button_text_search)
        val cameraSearchButton = findViewById<Button>(R.id.button_camera_search)
        val myMedsButton = findViewById<Button>(R.id.button_my_meds)

        // 텍스트 검색
        textSearchButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 카메라 검색
        cameraSearchButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        // 복약 관리
        myMedsButton.setOnClickListener {
            Toast.makeText(this, "복약 관리 기능은 추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            openCameraActivity()
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(this, CameraSearchActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
