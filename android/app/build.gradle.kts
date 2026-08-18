plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.hecong.chatdemo"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.hecong.chatdemo"
    minSdk = 21
    targetSdk = 34
    versionCode = 1
    // 跟壳版本走(便于对着截图问"你装的哪版");直接读事实源,不留手抄副本
    versionName = Regex("\"version\"\\s*:\\s*\"([^\"]+)\"")
      .find(file("../../../version.json").readText())!!.groupValues[1]
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
  // ⬇️ 这一行就是接入所需的全部依赖声明
  implementation("com.aihecong:hecong-chat-sdk:0.1.1")
  implementation("androidx.appcompat:appcompat:1.6.1")
}
