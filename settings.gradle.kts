pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS：统一用下方仓库解析依赖（兼容 init.gradle 注入的项目级镜像仓库）
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        google()
        mavenCentral()
    }
}

rootProject.name = "CurrencyCalculator"
include(":app")
