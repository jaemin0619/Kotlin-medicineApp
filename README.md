# MEDIFIT

카메라/검색으로 의약품 정보를 쉽게 찾고, 내가 복용하는 약을 관리할 수 있는 Android 앱입니다.  
사진 촬영 → OCR 인식 → 공공 API 조회 → 효능/주의/상호작용 등을 한 화면에서 확인할 수 있어요. 
<p align="center">
  <!-- 스크린샷 경로 예시 -->
  <!-- <img src="docs/screen_home.png" width="260">  -->
  <!-- <img src="docs/screen_detail.png" width="260"> -->
</p>

## ✨ 주요 기능
- 📸 **카메라 인식**: 촬영한 라벨/포장을 OCR로 인식
- 🔎 **다중 검색**: 제품명/회사명/효능 등 칩 기반 빠른 검색
- 📚 **의약품 상세**: 효능, 주의사항, 경고, 상호작용, 부작용, 보관법 등
- ⭐ **내 약 관리**: 즐겨찾기/복용 리스트(로컬 RoomDB)
- 🤖 **AI 채팅(옵션)**: 약 정보 Q&A 챗봇
- 🔐 **카카오 로그인(옵션)**

## 🏗️ 기술 스택
- **Language**: Kotlin
- **UI**: AndroidX, Material Components
- **Network**: Retrofit2 + OkHttp3 (Moshi Converter)
- **DB**: Room DB 
- **Log**: Timber (선택)
- **OCR**: Clova OCR 연동
- **API**: 공공데이터포탈 api / kakao login api / backend api



## 🔧 빌드 & 실행
### 1) 요구 사항
- Android Studio Hedgehog+  
- JDK 17
- Android Gradle Plugin(AGP) 프로젝트 설정에 맞춤

### 2) 로컬 설정 파일
루트에 아래 파일들을 준비하세요(버전에 따라 이미 존재할 수 있음).

**`local.properties`** (예시)
```
sdk.dir=/YOUR/ANDROID/SDK
```

**`keystore.properties`** (릴리즈 서명용 · 선택)
```
storeFile=/absolute/path/my-release-key.jks
storePassword=****
keyAlias=yakbang
keyPassword=****
```

### 3) API 키/엔드포인트 설정
민감정보는 코드에 하드코딩하지 말고 `gradle.properties` 또는 환경변수로 주입하세요.

**`gradle.properties`** (예시)
```
# API keys (do NOT commit real values)
DRB_EASY_DRUG_API_KEY=your_drb_api_key
CLOVA_OCR_API_KEY=your_clova_key
KAKAO_NATIVE_APP_KEY=your_kakao_key

# OCR/이미지 업로드 서버 (예: ngrok/dev 서버)
PIC_API_BASE_URL=https://example.ngrok-free.dev/
PIC_API_UPLOAD_PART_NAME=file
```

앱 코드에서 BuildConfig를 통해 접근하도록 합니다.
```kotlin
val baseUrl = BuildConfig.PIC_API_BASE_URL
```

### 4) 빌드
```
./gradlew clean assembleDebug
```

## 🔌 네트워크 레이어 샘플
```kotlin
interface PicApi {
    @Multipart
    @POST("api/getPic")
    suspend fun uploadPic(@Part file: MultipartBody.Part): Response<ResponseBody>
}
```
> 서버가 특정 파트 이름을 요구하면 `MultipartBody.Part.createFormData("file", ...)`처럼 파트명을 맞춰주세요.

## 🗃️ 데이터 모델 (일부)
```kotlin
@Parcelize
data class YakData(
    val precautions: String?,
    val warning: String?,
    val businessNumber: String?,
    val storageMethod: String?,
    val efficacy: String?,
    val companyName: String?,
    val interactions: String?,
    val imageUrl: String?,
    val productName: String?,
    val productCode: String?,
    val registrationDate: String?,
    val sideEffects: String?,
    val updateDate: String?,
    val usage: String?
) : Parcelable
```

## 🔐 보안/비밀정보 가이드
- API 키/토큰/keystore는 **절대 Git에 커밋 금지**
- `.gitignore`로 `local.properties`, `keystore.properties`, `*.jks`, `*.keystore`, 캐시/빌드 산출물 제외
- 실서버 엔드포인트/키는 **GitHub Actions Secrets** 또는 **CI/CD 변수**로 관리

