package com.example.yakbangapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yakbangapp.model.YakModel
import com.example.yakbangapp.network.YakService
import com.example.yakbangapp.ui.data.YakData
import com.example.yakbangapp.ui.data.toYakData
import com.example.yakbangapp.ui.detail.DetailHostActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecognitionResultActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var tv: TextView

    private val yakService: YakService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/") // ✅ e약은요 실제 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YakService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogniton_result)

        imgView = findViewById(R.id.result_image)
        tv = findViewById(R.id.tv_json)

        val imgPath = intent.getStringExtra("image_path")
        val json = intent.getStringExtra("result_json") ?: "{}"

        if (!imgPath.isNullOrEmpty()) {
            imgView.setImageBitmap(BitmapFactory.decodeFile(imgPath))
        }

        val extracted = runCatching { extractInferText(json) }.getOrElse { "" }
        tv.text = extracted.ifBlank { json }

        // 🔹 OCR 텍스트로 약 이름 검색 실행
        if (extracted.isNotBlank()) {
            searchAndOpenDetail(extracted)
        }
    }

    /** OCR 결과에서 inferText 문자열만 추출 */
    private fun extractInferText(json: String): String {
        val root = JSONObject(json)
        val images = root.optJSONArray("images") ?: return json
        if (images.length() == 0) return json
        val fields = images.getJSONObject(0).optJSONArray("fields") ?: return json
        val sb = StringBuilder()
        for (i in 0 until fields.length()) {
            val word = fields.getJSONObject(i).optString("inferText")
            if (word.isNotBlank()) sb.append(word).append(" ")
        }
        return sb.toString().trim()
    }

    /** OCR 결과로 YakService 검색 → 첫 번째 결과 상세 페이지로 이동 */
    private fun searchAndOpenDetail(keyword: String) {
        lifecycleScope.launch {
            val normalized = keyword.replace("[^ㄱ-ㅎ가-힣a-zA-Z0-9 ]".toRegex(), "")
                .trim()

            val result = withContext(Dispatchers.IO) {
                try {
                    yakService.getYakInfo(productName = normalized)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (result == null || result.body?.items.isNullOrEmpty()) {
                Toast.makeText(
                    this@RecognitionResultActivity,
                    "약 정보를 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // ✅ 첫 번째 아이템을 YakData 로 변환
            val first = result.body!!.items.first()
            val yakData = first.toYakData()

            // ✅ Detail 화면으로 이동
            val intent = Intent(this@RecognitionResultActivity, DetailHostActivity::class.java)
            intent.putExtra("yak", yakData)
            startActivity(intent)
        }
    }
}
