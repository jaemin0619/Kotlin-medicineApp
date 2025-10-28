package com.example.yakbangapp

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.example.yakbangapp.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CameraSearchActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var shootButton: Button
    private lateinit var processButton: Button

    private lateinit var photoUri: Uri
    private var savedFile: File? = null

    // 카메라 권한 요청
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera() else toast("카메라 권한이 필요합니다.")
    }

    // 카메라 촬영: 지정한 Uri에 저장
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                lifecycleScope.launch {
                    val fixed = withContext(Dispatchers.IO) {
                        fixRotateAndDownscale(photoUri, 1600, 1600, 90)
                    }
                    savedFile = fixed
                    previewImage.setImageBitmap(BitmapFactory.decodeFile(fixed.absolutePath))
                }
            } else {
                toast("촬영이 취소되었습니다.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_search)

        previewImage = findViewById(R.id.preview_image)
        shootButton = findViewById(R.id.button_shoot)
        processButton = findViewById(R.id.button_process)

        shootButton.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        processButton.setOnClickListener {
            val file = savedFile
            if (file == null) {
                toast("먼저 사진을 촬영해 주세요.")
                return@setOnClickListener
            }
            uploadToServer(file)
        }
    }

    /** 카메라 실행: FileProvider로 저장할 Uri 생성 후 촬영 */
    private fun openCamera() {
        val imageFile = File.createTempFile("capture_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
        takePictureLauncher.launch(photoUri)
    }

    /** 서버 업로드 후 itemName(plain text)을 받아 결과 화면으로 전달 */
    private fun uploadToServer(file: File) {
        lifecycleScope.launch {
            try {
                val body = file.asRequestBody("image/jpeg".toMediaType())
                // ❗ 서버가 @RequestParam("file") 이므로 파트명은 "file"
                val part = MultipartBody.Part.createFormData("file", file.name, body)

                val resp = withContext(Dispatchers.IO) {
                    ApiClient.pic.uploadPic(part)
                }
                if (!resp.isSuccessful) {
                    val err = resp.errorBody()?.string()
                    throw RuntimeException("HTTP ${resp.code()} - ${err ?: "업로드 실패"}")
                }

                val itemName = resp.body()?.string()?.trim().orEmpty() // 서버가 반환한 제품명(plain text)
                if (itemName.isEmpty()) {
                    toast("서버 응답이 비어 있습니다.")
                    return@launch
                }

                // 결과 화면으로 이동 (item_name 전달)
                startActivity(
                    Intent(this@CameraSearchActivity, RecognitionResultActivity::class.java).apply {
                        putExtra("image_path", file.absolutePath)
                        putExtra("item_name", itemName) // ✅ 핵심: 제품명으로 직접 검색
                    }
                )

            } catch (e: Exception) {
                toast("업로드 실패: ${e.message}")
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    /** EXIF 회전 보정 + 다운스케일 후 새 파일 반환 */
    private fun fixRotateAndDownscale(uri: Uri, maxW: Int, maxH: Int, quality: Int): File {
        val src = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalStateException("이미지를 열 수 없습니다.")

        val rotation = contentResolver.openInputStream(uri)?.use { ins ->
            val exif = ExifInterface(ins)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        var bmp: Bitmap = if (rotation != 0f) {
            val m = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
        } else src

        val ratio = minOf(maxW.toFloat() / bmp.width, maxH.toFloat() / bmp.height, 1f)
        if (ratio < 1f) {
            bmp = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width * ratio).toInt(),
                (bmp.height * ratio).toInt(),
                true
            )
        }

        val out = File.createTempFile("fixed_", ".jpg", cacheDir)
        FileOutputStream(out).use { fos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos)
        }
        return out
    }
}
