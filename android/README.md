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

只有要用**未读回调**(不打开客服页也能拿未读数)时,才需要在 Application 里加一句:

```kotlin
HecongChat.configure(this, HecongChatConfig("你的渠道ID"))  // 只登记参数,零联网,放这里合规
HecongChat.startUnreadTracking(myListener)                 // 会联网,放在同意隐私政策之后
```

**深浅色**:聊天页**默认就跟随你的 App**(`config.colorScheme` 默认 `host`),你一行代码都不用写。
本工程「界面形态 → 深浅色」那个开关只切换 App 自己的主题,**没有任何联动聊天页的代码** —— 它就是
这条默认行为的活证据。想强制固定某一档就设 `light`/`dark`,想让渠道后台说了算就设 `auto`(详 `DemoTheme.kt`)。

## 页面结构:四个能力页(与 iOS 示范 App 逐项对位)

示范 App 只演示**常用、且适合在 App 里演示**的场景(给试用者看效果、给对接的技术人员抄作业),
不追求覆盖全部接口 —— 纯文字说明类的内容(权限时机、离线推送接法、宿主接管回调、命令透传)
归接入文档。

| 页 | 覆盖 |
|---|---|
| **界面形态**(首页) | 四档承载形态、标题栏、深浅色、语言 —— 租户最先关心"长什么样" |
| **身份与会员** | 示范会员资料演示台、identify / resetUser **成对**、未读跟踪(开关 + 带徽标的客服入口示范) |
| **高级扩展** | 技能组指派、商品 / 订单选择器 |
| **配置与诊断** | 渠道配置、诊断信息、会话事件流水 |

**跟 iOS 的两处平台差异(不是漏做)**:① 标准档的标题栏在安卓由 SDK 原生绘制(安卓没有宿主导航栈),
所以「标题栏配色」对安卓标准档生效、iOS 只对弹层档生效;② 弹层档安卓只有一种顶栏形态,iOS 有"系统导航栏 /
渠道标题栏"两档。**其余场景两端逐项一致**。

## 文件导览:哪些是接入示范,哪些是 demo 自己的架子

| 文件 | 性质 | 看它干什么 |
|---|---|---|
| `DemoApp.kt` | ✅ **接入示范** | 全局登记 + 未读 / 自定义按钮 / 外链接管 / 会话事件回调怎么接 |
| `ChatLaunch.kt` | ✅ **接入示范** | 四档承载形态的标准写法,每个方法可直接抄 |
| `CustomHeaderChatActivity.kt` | ✅ **接入示范** | 嵌入档:自家顶栏 + `HecongChatFragment` + `onHeaderIdentityChanged` 三态渲染 |
| `DemoTheme.kt` | ✅ **接入示范** | 深浅色:App 主导、聊天页跟随(零联动代码) |
| `DevCapabilityActions.kt` | ✅ 接入示范 | 技能组指派、选择器注册(工作台配不出来、必须写代码的那几样) |
| `*Page.kt` / `DemoScene.kt` / `MainActivity.kt` | 🔧 脚手架 | 四个能力页的清单声明与分页装配,接入时不需要 |
| `DemoMemberProfile.kt` / `MemberProfileActivity.kt` | 🔧 脚手架 | 示范会员资料演示台(可填写并持久化;缺省会员 ID 按设备生成),接入时取你自己的登录用户 |
| `DemoConfig.kt` / `DiagnosticsActivity.kt` / `ChannelSetup.kt` | 🔧 脚手架 | 三档渠道、诊断页、渠道未配拦一道,接入时不需要 |
| `ui/` 目录 | 🔧 脚手架 | 设计 token、组件层、说明卡与极简头像加载,换成你自己的设计体系 / Glide / Coil |

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
> adb reverse tcp:5175 tcp:5175   # 插座 + 聊天窗 chunk(demo:link 默认端口,两者同源)
> adb reverse tcp:3024 tcp:3024   # 后端 API
> adb reverse tcp:17108 tcp:17108 # 本地调试通道
> ```
> (iOS 模拟器与宿主共用 localhost,无需这一步。)

DEBUG 构建默认走内部联调渠道(`src/debug/.../LocalEnv.kt`);要连你自己的渠道,
进 App →「配置与诊断 → 渠道配置」填 ID 即可,不用改代码。
