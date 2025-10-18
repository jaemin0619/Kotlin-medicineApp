package com.example.yakbangapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class RecognitionResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogniton_result)

        val imgView = findViewById<ImageView>(R.id.result_image)
        val tv = findViewById<TextView>(R.id.tv_json)

        val imgPath = intent.getStringExtra("image_path")
        val json = intent.getStringExtra("result_json") ?: "{}"

        if (!imgPath.isNullOrEmpty()) {
            imgView.setImageBitmap(BitmapFactory.decodeFile(imgPath))
        }

        // 서버 응답이 클로바 형태면 inferText만 뽑아 보여주기(없으면 원문 표시)
        tv.text = runCatching { extractInferText(json) }.getOrElse { json }
    }

    private fun extractInferText(json: String): String {
        val root = JSONObject(json)
        val images = root.getJSONArray("images")
        if (images.length() == 0) return json
        val fields = images.getJSONObject(0).optJSONArray("fields") ?: return json
        val sb = StringBuilder()
        for (i in 0 until fields.length()) {
            sb.append(fields.getJSONObject(i).optString("inferText"))
        }
        return sb.toString()
    }
}
