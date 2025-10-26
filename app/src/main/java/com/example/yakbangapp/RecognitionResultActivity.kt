package com.example.yakbangapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yakbangapp.network.YakService
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
            .baseUrl("https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/")
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
        if (!imgPath.isNullOrEmpty()) {
            imgView.setImageBitmap(BitmapFactory.decodeFile(imgPath))
        }

        // ✅ 1순위: 서버에서 받은 제품명(plain text)
        val itemName = intent.getStringExtra("item_name")?.trim().orEmpty()

        // ✅ 2순위: OCR JSON에서 inferText 추출 (다른 경로에서 왔을 때)
        val ocrJson = intent.getStringExtra("result_json") ?: ""
        val extracted = if (itemName.isNotEmpty()) itemName else {
            runCatching { extractInferText(ocrJson) }.getOrElse { "" }
        }

        tv.text = extracted.ifBlank { ocrJson.ifBlank { "인식 결과가 없습니다." } }

        if (extracted.isNotBlank()) {
            searchAndOpenDetail(extracted)
        } else {
            Toast.makeText(this, "검색어가 비어 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /** OCR 결과에서 inferText 문자열만 추출 */
    private fun extractInferText(json: String): String {
        if (json.isBlank()) return ""
        val root = JSONObject(json)
        val images = root.optJSONArray("images") ?: return ""
        if (images.length() == 0) return ""
        val fields = images.getJSONObject(0).optJSONArray("fields") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until fields.length()) {
            val word = fields.getJSONObject(i).optString("inferText")
            if (word.isNotBlank()) sb.append(word).append(" ")
        }
        return sb.toString().trim()
    }

    /** 결과 텍스트로 e약은요 검색 → 첫 번째 결과 상세 페이지로 이동 */
    private fun searchAndOpenDetail(keyword: String) {
        lifecycleScope.launch {
            val normalized = keyword.replace("[^ㄱ-ㅎ가-힣a-zA-Z0-9 ]".toRegex(), "").trim()

            val result = withContext(Dispatchers.IO) {
                runCatching { yakService.getYakInfo(productName = normalized) }.getOrNull()
            }

            val items = result?.body?.items.orEmpty()
            if (items.isEmpty()) {
                Toast.makeText(this@RecognitionResultActivity, "약 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val yakData = items.first().toYakData()

            // ✅ 상세 화면으로 이동 + 현재 액티비티는 종료
            val intent = Intent(this@RecognitionResultActivity, DetailHostActivity::class.java)
                .putExtra("yak", yakData)
            startActivity(intent)
            finish() // ★★★ 이 줄이 핵심! 뒤로 가기 시 첫 화면으로 안 튑니다.
            // (선호하면) 전환 애니메이션도 적용 가능
            // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

}
