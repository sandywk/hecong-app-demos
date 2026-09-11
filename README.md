# 合从客服 SDK — 接入示范

两个可直接跑的示范 APP,演示如何把合从在线客服嵌进你自己的 APP。
**代码即文档** —— 注释里写了每个能力"什么时候用、不用会怎样"。

| 目录 | 端 | 怎么跑 |
|---|---|---|
| `android/` | Android(Kotlin) | Android Studio 打开,直接运行 |
| `ios/` | iOS(Swift) | Xcode 打开 `HecongChatDemo.xcodeproj`,直接运行 |
| `snippets/` | ObjC / Java / uni-app / RN / Flutter | 单文件接入片段 |

## 不想编译?扫码直接装

手机扫这一个码(自动识别 iPhone / Android;微信里打开会提示「在浏览器打开」):

![扫码安装示范 App](./downloads/demo-app-qr.png)

或直接打开 https://assets.aihecong.com/app-demo/index.html

- Android:未上架商店,安装时系统会提示「未知来源」,继续即可([APK 直链](https://assets.aihecong.com/app-demo/android/HecongChatDemo-latest.apk) · [GitHub 镜像](https://github.com/sandywk/hecong-app-demos/releases))
- iOS:通过 [TestFlight](https://testflight.apple.com/join/Ue1vqhcv) 安装,需先装 TestFlight;构建 90 天有效

装好后进「配置与诊断 → 渠道配置」粘贴你自己的渠道 ID,不用改源码。

## 最小接入

**Android**(`build.gradle.kts`):

```kotlin
implementation("com.aihecong:hecong-chat-sdk:0.6.2")
```

```kotlin
// APP 启动时(用户已同意隐私政策后):只登记参数,零联网
HecongChat.configure(this, HecongChatConfig("你的渠道ID"))
// 打开客服
HecongChatActivity.start(this, HecongChatConfig("你的渠道ID"))
```

**iOS**(Xcode → File → Add Package,填 `https://github.com/sandywk/hecong-ios-sdk`):

```swift
HecongChat.shared.configure(HecongChatConfig(channelId: "你的渠道ID"))
let chat = HecongChatViewController(config: HecongChatConfig(channelId: "你的渠道ID"))
navigationController?.pushViewController(chat, animated: true)
```

渠道 ID 在客服工作台的渠道设置里拿。示范 App 安装与连自己渠道:https://docs.aihecong.com/channels/app/demo ;完整接入手册:https://docs.aihecong.com

---

> ⚠️ **本仓是分发产物,由上游仓库单向同步生成** —— 请不要直接在这里改代码,改动会在下次
> 同步时被覆盖。问题与需求请走 support@aihecong.com。
