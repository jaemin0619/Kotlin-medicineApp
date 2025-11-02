pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // (선택) 카카오 플러그인이 필요할 때만
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
    // ✅ 플러그인 버전은 여기서 '한 번만' 지정 (모듈엔 버전 쓰지 않음)
    plugins {
        id("com.android.application") version "8.6.1"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 카카오 SDK 의존성 사용 중이면 유지
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "YakBangApp"
include(":app")
