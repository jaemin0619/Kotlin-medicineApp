package com.example.yakbangapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yakbangapp.network.RetrofitInstance
import com.example.yakbangapp.ui.data.YakData
import com.example.yakbangapp.ui.data.toYakData
import com.example.yakbangapp.ui.detail.DetailHostActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RecognitionResultActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogniton_result)

        imgView = findViewById(R.id.result_image)
        tv = findViewById(R.id.tv_json)

        // 사진 미리보기
        intent.getStringExtra("image_path")?.let { path ->
            if (path.isNotBlank()) {
                imgView.setImageBitmap(BitmapFactory.decodeFile(path))
            }
        }

        // 1순위: 서버에서 넘어온 item_name (plain text)
        val itemName = intent.getStringExtra("item_name")?.trim().orEmpty()

        // 2순위: OCR JSON에서 inferText 합쳐서 추출
        val ocrJson = intent.getStringExtra("result_json") ?: ""
        val extracted = if (itemName.isNotEmpty()) itemName
        else runCatching { extractInferText(ocrJson) }.getOrElse { "" }

        tv.text = extracted.ifBlank { ocrJson.ifBlank { "인식 결과가 없습니다." } }

        if (extracted.isNotBlank()) {
            searchAndOpenDetail(extracted)
        } else {
            Toast.makeText(this, "검색어가 비어 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /** OCR 결과(JSON)에서 inferText만 공백으로 이어 붙여 추출 */
    private fun extractInferText(json: String): String {
        if (json.isBlank()) return ""
        val root = JSONObject(json)
        val images = root.optJSONArray("images") ?: return ""
        if (images.length() == 0) return ""
        val fields = images.getJSONObject(0).optJSONArray("fields") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until fields.length()) {
            val word = fields.getJSONObject(i).optString("inferText")
            if (word.isNotBlank()) sb.append(word).append(' ')
        }
        return sb.toString().trim()
    }

    /** e약은요에서 제품명으로 검색 → 첫 결과로 상세 화면 이동 */
    private fun searchAndOpenDetail(keyword: String) {
        lifecycleScope.launch {
            // 괄호/특수문자 제거 + 공백 정리
            val normalized = keyword
                .replace(Regex("\\([^)]*\\)"), " ")
                .replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z0-9 ]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            val model = withContext(Dispatchers.IO) {
                runCatching {
                    RetrofitInstance.service.getYakInfo(
                        productName = normalized,
                        numOfRows = 10
                    )
                }.getOrNull()
            }

            val items = model?.body?.items
            if (items.isNullOrEmpty()) {
                Toast.makeText(
                    this@RecognitionResultActivity,
                    "약 정보를 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val yakData: YakData = items.first().toYakData()

            val intent = Intent(this@RecognitionResultActivity, DetailHostActivity::class.java)
                .putExtra("yak", yakData)
            // 방어적으로 클래스 로더 지정 (보통 없어도 됨)
            intent.setExtrasClassLoader(YakData::class.java.classLoader)

            startActivity(intent)
            finish() // 뒤로가기 시 인식 결과 화면으로 안 돌아오게
        }
    }
}
