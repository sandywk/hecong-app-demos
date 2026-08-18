// 示范 APP(= 模拟租户 APP 嵌入 app-sdk)。
//
// **两种引入方式,一个开关切换**(2026-08-18 发版链路):
//   · 默认(开发档):project path 引壳源码 —— 改壳代码立刻生效,不用先发包;
//   · `-PusePublishedSdk=true`(验收档):**按租户实际拿到的方式**引 Maven 坐标。
//
// 🔴 为什么必须能切到验收档:平时跑的是源码直引,而**租户装的是发布出去的 AAR** ——
// 两者不等价(POM 依赖传递、ProGuard consumer 规则、资源合并、混淆后的反射都只在真包里
// 才暴露)。发版前不用真依赖跑一遍,等于把最后一公里留给租户去踩。
// 用法见 native/PUBLISHING.md「发版前验收」。
pluginManagement {
  repositories {
    // 阿里云镜像置首(国内网络 dl.google.com/Central 偶发 TLS 握手失败;与官方 APP 仓同配)
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/google")
    maven("https://maven.aliyun.com/repository/gradle-plugin")
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositories {
    // 验收档(-PusePublishedSdk=true)要能取到 `publishToMavenLocal` 出来的包 —— 置首,
    // 好让"刚打的本地包"优先于线上同版本(否则会静默取到线上旧包,验收就白做了)。
    // 只影响示范工程,租户接入用不到这条。
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/google")
    google()
    mavenCentral()
  }
}

rootProject.name = "HecongChatDemo"
include(":app")

// 公开示范工程固定引用线上发布的依赖(与租户处境完全一致)
