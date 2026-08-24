# 示范 App 扫码安装

> 本目录由 `pnpm native:demo-qr` 生成,别手改。链接事实源 `links.json`(iOS 那条是人填的)。

**对外只印这一个码**(单码落地页:自动判 iPhone / Android 给按钮,微信等内置浏览器里提示「在浏览器打开」):

| 主码 | 链接 |
|---|---|
| ![示范 App](./demo-app-qr.png) | https://assets.aihecong.com/app-demo/index.html |

两端直链码(给明确要某一端直链的人;⚠️ 安卓直链码**微信扫不了**,要用系统相机/浏览器):

| 端 | 二维码 | 链接 | 说明 |
|---|---|---|---|
| Android | ![Android APK](./android-apk-qr.png) | https://assets.aihecong.com/app-demo/android/HecongChatDemo-latest.apk | 直接下载 APK 安装。**未上架应用商店**,系统会提示「未知来源 / 风险应用」,选择继续安装即可;GitHub 镜像:https://github.com/sandywk/hecong-app-demos/releases |
| iOS | ![iOS TestFlight](./ios-testflight-qr.png) | https://testflight.apple.com/join/Ue1vqhcv | 通过 TestFlight 安装:扫码 → 装 TestFlight → 一键安装。构建 90 天有效,随版本更新 |

装好后进「配置与诊断 → 渠道配置」粘贴你自己的渠道 ID,**不用改源码、不用重新编译**。
