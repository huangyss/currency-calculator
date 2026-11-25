plugins {
    id("com.android.application")
    // AGP 9 内置 Kotlin，仅需 Compose 编译器插件
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.exchangerate.calculator"
    // Android 17（API 37.2，QPR 更新）；minSdk 28 = Android 9，不再支持 Android 9 以下设备
    // Compose 1.13+ 要求 compileSdk ≥ 37.1
    compileSdk = 37
    compileSdkMinor = 2

    defaultConfig {
        applicationId = "com.exchangerate.calculator"
        minSdk = 28
        targetSdk = 37
        versionCode = 2
        versionName = "1.1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.11.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // 网络请求（汇率数据）
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
