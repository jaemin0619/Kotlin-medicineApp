package com.example.yakbangapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CameraSearchActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private lateinit var previewImage: ImageView
    private lateinit var processButton: Button // 인식 처리 페이지 이동 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_search)

        previewImage = findViewById(R.id.preview_image)
        processButton = findViewById(R.id.button_process)

        // 카메라 실행
        openCamera()

        // 인식 처리 페이지 이동
        processButton.setOnClickListener {
            if (previewImage.drawable != null) {
                val intent = Intent(this, RecognitionResultActivity::class.java)
                // 촬영된 이미지 전달 (Bitmap 직렬화 불가 → 추후 파일 저장 후 Uri 전달 권장)
                // intent.putExtra("photo", bitmap)
                startActivity(intent)
            } else {
                Toast.makeText(this, "촬영된 사진이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                previewImage.setImageBitmap(photo)
            }
        }
    }
}
