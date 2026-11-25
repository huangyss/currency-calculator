// 顶层构建脚本：声明插件版本，供各模块引用
// AGP 9.5.2 支持到 API 37（Android 17），要求 Gradle 9.6+ / JDK 17+
// 注意：AGP 9.0+ 内置 Kotlin（built-in Kotlin），不再需要 org.jetbrains.kotlin.android；
// 这里通过 buildscript classpath 显式指定 Kotlin 编译器与 Compose 插件版本，
// 避免依赖 plugin marker 解析（该链路在部分 CI 网络环境下不稳定）。
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.5.2" apply false
}
