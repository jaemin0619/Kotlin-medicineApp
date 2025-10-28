pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // (선택) 카카오 플러그인이 필요할 때만
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 카카오 SDK 의존성 사용 중이면 필요
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "YakBangApp"
include(":app")
