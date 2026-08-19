plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  // 🔴 **把 Java 互操作示例纳入编译**(2026-08-18)。
  //
  // 病根:`native/examples/` 下的 ObjC / Java 示例是**单文件片段,不属于任何工程** ——
  // 编译器从来没检查过它们。结果同一份 ObjC 示例接连出过两个错(用了根本不存在的
  // `initWithUrl:`、用了 SPM 下不存在的 `-Swift.h` 引入路径),而且都是**公开给租户抄的代码**。
  // 靠"改 API 时记得同步示例"这条规则挡不住 —— 要机制不要规则(architecture.md §3.0.1 同款判据)。
  //
  // 现在它跟示范工程一起编译:改了公共面而示例没跟上,**下次编译当场报错**。
  // (iOS 侧对等做法:`ios-objc/` 已挂进 HecongChatDemo.xcodeproj 的同步组。)
  // 这个类不进 manifest、不会被运行,只为让编译器过一遍。
  sourceSets {
    getByName("main") {
      java.srcDirs("src/main/java", "../../android-java")
    }
  }

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
  implementation("com.aihecong:hecong-chat-sdk:0.2.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
}
