# 安卓示范 App(模拟一个真实 App 接入合从客服)

一个能直接跑的样板工程:左边是"你的 App",右边是"接完客服之后的样子"。
**代码就是接入文档** —— 每个场景对应的那段代码可以直接复制进你自己的工程。

## 最少要写几行:**一行**

```kotlin
// 在你想打开客服的地方(会联网,放在用户同意隐私政策之后)
HecongChatActivity.start(context, HecongChatConfig("你的渠道ID"))
```

就这一行。文件选择、运行时权限、返回键、键盘避让、深浅色跟随全部内置。
渠道 ID 在工作台「App 渠道」页复制,**不需要配任何域名**。

只有要用**未读回调**(不打开客服页也能拿未读数、访客标识)时,才需要在 Application 里加一句:

```kotlin
HecongChat.configure(this, HecongChatConfig("你的渠道ID"))  // 只登记参数,零联网,放这里合规
HecongChat.startUnreadTracking(myListener)                 // 会联网,放在同意隐私政策之后
```

> 想把聊天嵌进自己的页面(自己画顶栏 / 底部弹层 / 分栏)才需要 `HecongChatView`,
> 那时有三段系统回调要自己转发 —— 范本见 `ChatActivity.kt`,文件头把"不做会怎样"逐条写清了。

**深浅色**:聊天页**默认就跟随你的 App**(`config.colorScheme` 默认 `host`),你一行代码都不用写。
本工程「我的」页那个开关只切换 App 自己的主题,**没有任何联动聊天页的代码** —— 它就是这条默认行为
的活证据。想强制固定某一档就设 `light`/`dark`,想让渠道后台说了算就设 `auto`(详 `DemoTheme.kt`)。

## 文件导览:哪些是接入示范,哪些是 demo 自己的架子

| 文件 | 性质 | 看它干什么 |
|---|---|---|
| `DemoApp.kt` | ✅ **接入示范** | 全局登记 + 未读 / 访客标识两个回调怎么接(①②③) |
| `ChatActivity.kt` | ✅ **接入示范** | 嵌进自己页面的六件事(①~⑥),每条注明不做会怎样 |
| `CustomHeaderChatActivity.kt` | ✅ **接入示范** | 自己画标题栏 + `onHeaderIdentityChanged` 三态渲染 + 真头像加载 |
| `SheetChatDialog.kt` | ✅ **接入示范** | 底部弹层承载;含 Dialog 场景要自己设键盘策略这个坑 |
| `CatalogActions.kt` | ✅ 接入示范 + 演示混编 | 每个方法注释里标了是哪一类,照抄前先看那一行 |
| `MinePage.kt` | 🔶 半架子 | 「在线客服」入口(①②)与「深色模式」开关(③)是真接入点,其余是假菜单 |
| `DemoTheme.kt` | ✅ **接入示范** | 深浅色:App 主导、聊天页跟随 |
| `MainActivity.kt` / `CatalogPage.kt` / `DiagnosticsActivity.kt` | 🔧 示范工程脚手架 | 双 Tab 框架、场景清单、诊断页,接入时不需要 |
| `DemoConfig.kt` | 🔧 示范工程脚手架 | 三档渠道(local/demo/custom)是为了演示切换,你实际接入只有一个渠道 ID |
| `ChannelSetup.kt` | 🔧 示范工程脚手架 | 渠道 ID 没配好时拦一道并引导去填,接入时不需要 |
| `ui/` 目录 | 🔧 示范工程脚手架 | 设计 token、组件层与极简头像加载,换成你自己的设计体系 / Glide / Coil |

## 界面与设计系统

界面按定稿设计稿实现(`design/exports/app-demo-0{1..4}.png`,第 4 屏是施工规格)。
风格:Linear 风极简 —— 大留白、无重边框、单一强调色、线性图标。

- 色板 / 尺寸 / 字阶集中在 `app/src/main/res/values{,-night}/`,页面代码只引 token 不写死色值;
- 图标由 `tools/gen-icons.py` 从 lucide 源码生成成 VectorDrawable(要加图标改那个脚本再跑,别手抄路径);
- **零第三方 UI 依赖**(不引 Material Components / Compose)—— 与 SDK 本体"只依赖 androidx.webkit + core-ktx"
  的调性一致,也证明这套观感不需要额外包袱。

## ⚠️ 当前是"本地源码依赖",SDK 正式发版后要换成坐标

这份示范工程现在直接引本仓的 SDK 源码,**发版后必须换成正式依赖**,否则租户拿到工程跑不起来
(他们没有 `native/android` 这个目录)。要改的就两处:

```diff
  // settings.gradle.kts
- include(":hecong-chatsdk")
- project(":hecong-chatsdk").projectDir = file("../../android")

  // app/build.gradle.kts
- implementation(project(":hecong-chatsdk"))
+ implementation("com.hecong:chat-sdk:<版本号>")
```

**其余代码一字不改** —— 这也正是这份工程要证明的事:换依赖形式不影响接入写法。

## 跑起来

```bash
ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> **SDK 开发者本地联调**:模拟器要把四个端口都 `adb reverse` 过去,少一个聊天页会报 Network error:
> ```bash
> adb reverse tcp:5177 tcp:5177   # 插座 / 骨架页
> adb reverse tcp:5175 tcp:5175   # 聊天窗 chunk(2026-08-18 补:漏了它就是 Network error)
> adb reverse tcp:3024 tcp:3024   # 后端 API
> adb reverse tcp:17108 tcp:17108 # 本地调试通道
> ```
> (iOS 模拟器与宿主共用 localhost,无需这一步。)

DEBUG 构建默认走内部联调渠道(`src/debug/.../LocalEnv.kt`);要连你自己的渠道,
进 App →「示例 → 配置与诊断 → 渠道配置」填 ID 即可,不用改代码。
