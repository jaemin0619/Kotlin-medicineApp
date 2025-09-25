package com.example.yakbangapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class RecognitionResultActivity : AppCompatActivity() {

    private lateinit var resultImageView: ImageView
    private lateinit var resultTextView: TextView

    private val serverUrl = "http://YOUR_SERVER_IP:PORT/upload" // Spring 서버 URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogniton_result)

        // resultImageView = findViewById(R.id.result_image)
        // resultTextView = findViewById(R.id.result_text)

        // TODO: CameraSearchActivity에서 이미지 전달 시 bitmap 사용
        // 예제용 더미 이미지
        //val bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_pill)
        //resultImageView.setImageBitmap(bitmap)

        // 서버로 전송
        //uploadImage(bitmap)
    }
}
/*
    private fun uploadImage(bitmap: Bitmap) {
        val client = OkHttpClient()

        // Bitmap → JPEG ByteArray
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "pill.jpg",
                RequestBody.create(MediaType.parse("image/jpeg"), byteArray)
            )
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RecognitionResultActivity, "서버 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body()?.string() ?: "결과 없음"
                runOnUiThread {
                    resultTextView.text = "인식 결과: $result"
                }
            }
        })
    }
}
*/