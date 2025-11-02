plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") // ⬅ 버전은 settings.gradle.kts에서만 지정
}

android {
    namespace = "com.example.yakbangapp"
    compileSdk = 34

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    defaultConfig {
        applicationId = "com.example.yakbangapp"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // buildConfigField("Boolean", "LOG_NETWORK", "true")
        }
    }

    // ✅ AGP 8.x 권장 JDK 17 정렬
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    // 선택: 명시 안 해도 됨 (AGP가 자동 선택)
    buildToolsVersion = "34.0.0"
}

/* ⛔ repositories는 모듈이 아니라 settings.gradle.kts에만 둡니다. */

dependencies {
    // AndroidX / UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")

    // Room + KSP
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // 네트워크
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Kakao SDK
    implementation("com.kakao.sdk:v2-user:2.20.6")
    implementation(libs.identity.android.legacy)

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
