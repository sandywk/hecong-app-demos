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
  compileSdk = 35

  // 版本跟壳走(便于对着截图问"你装的哪版"):直接读事实源,不留手抄副本。
  val shellVersion = "0.6.0" // 同步时固化(公开仓没有 monorepo 的 version.json)
  val (vMajor, vMinor, vPatch) = shellVersion.split('.').map { it.toInt() }

  defaultConfig {
    applicationId = "com.aihecong.chatdemo" // 与正式 App com.aihecong.* 同前缀(owner 2026-08-21)
    minSdk = 21
    targetSdk = 34
    // versionCode 从版本号派生(0.3.1 → 301):扫码装 APK 要能覆盖安装,系统只认 versionCode
    // 递增 —— 写死 1 的话每个新版都会被系统当"降级"拒装。派生 = 机器保证单调,不靠人记得改。
    versionCode = vMajor * 10000 + vMinor * 100 + vPatch
    versionName = shellVersion
  }

  // release 签名:**demo 专用 keystore,不与正式 App 共用**(2026-08-22 扫码分发链路)。
  // 凭据只在 owner 本机 `~/.gradle/gradle.properties`(hecong.demo.*),不进 git ——
  // 没配这几项时 release 照常能编(只是未签名,装不上机),所以公开仓的租户 clone 下来不受影响。
  val demoStore = providers.gradleProperty("hecong.demo.storeFile").orNull
  if (demoStore != null) {
    signingConfigs {
      create("release") {
        storeFile = file(demoStore)
        storePassword = providers.gradleProperty("hecong.demo.storePassword").get()
        keyAlias = providers.gradleProperty("hecong.demo.keyAlias").get()
        keyPassword = providers.gradleProperty("hecong.demo.keyPassword").get()
      }
    }
    buildTypes.getByName("release").signingConfig = signingConfigs.getByName("release")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
  // ⬇️ 这一行就是接入所需的全部依赖声明
  implementation("com.aihecong:hecong-chat-sdk:0.6.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
}
