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

    // nullable 제거: 반드시 openCamera()에서 초기화 후 사용
    private lateinit var photoUri: Uri
    private var savedFile: File? = null

    // 권한 요청
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera() else toast("카메라 권한이 필요합니다.")
    }

    // 카메라 촬영 콜백 (지정한 Uri에 원본 저장)
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

    /** 카메라 실행: FileProvider로 저장할 Uri를 만들고 TakePicture 실행 */
    private fun openCamera() {
        val imageFile = File.createTempFile("capture_", ".jpg", cacheDir)

        // 여기서 non-null Uri 생성 후 프로퍼티 초기화
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )

        // 지정한 Uri에 원본 저장
        takePictureLauncher.launch(photoUri)
    }

    /** 서버 업로드 후 결과 화면으로 이동 */
    private fun uploadToServer(file: File) {
        lifecycleScope.launch {
            try {
                val body = file.asRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("file", file.name, body) // ← 기존
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.pic.uploadPic(part)
                }
                if (!resp.isSuccessful) {
                    val err = resp.errorBody()?.string()
                    throw RuntimeException("HTTP ${resp.code()} - $err")
                }
                val json = resp.body()?.string().orEmpty()

                startActivity(
                    Intent(this@CameraSearchActivity, RecognitionResultActivity::class.java).apply {
                        putExtra("image_path", file.absolutePath)
                        putExtra("result_json", json)
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
        // 원본 로드
        val src = contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it) }

        // EXIF 회전값
        val rotation = contentResolver.openInputStream(uri)!!.use { ins ->
            val exif = ExifInterface(ins)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }

        // 회전 적용
        var bmp: Bitmap = if (rotation != 0f) {
            val m = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
        } else src

        // 다운스케일
        val ratio = minOf(maxW.toFloat() / bmp.width, maxH.toFloat() / bmp.height, 1f)
        if (ratio < 1f) {
            bmp = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width * ratio).toInt(),
                (bmp.height * ratio).toInt(),
                true
            )
        }

        // JPEG 저장
        val out = File.createTempFile("fixed_", ".jpg", cacheDir)
        FileOutputStream(out).use { fos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos)
        }
        return out
    }
}
