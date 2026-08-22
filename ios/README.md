# iOS 示范 App(模拟一个真实 App 接入合从客服)

能直接跑的样板工程:左边是"你的 App",右边是"接完客服之后的样子"。
**代码就是接入文档** —— 每个场景对应的那段代码可以直接复制进你自己的工程。

## 最少要写几行:**两行**

```swift
let chat = HecongChatViewController(config: HecongChatConfig(channelId: "你的渠道ID"))
navigationController?.pushViewController(chat, animated: true)
```

就这两行(iOS 没有安卓那种"一行起 Activity"的形态,因为页面由你自己的导航栈承载)。
文件选择、相机/麦克风权限、键盘避让、深浅色跟随全部内置。
渠道 ID 在工作台「App 渠道」页复制,**不需要配任何域名**。

只有要用**未读回调**(不打开客服页也能拿未读数、访客标识)时,才需要在启动时加一句:

```swift
HecongChat.shared.configure(config, listener: self)   // 只登记参数,零联网,放启动里合规
HecongChat.shared.startUnreadTracking(listener: self) // 会联网,放在同意隐私政策之后
```

**深浅色**:聊天页**默认就跟随你的 App**(`config.colorScheme` 默认 `host`),你一行代码都不用写。
本工程「界面形态 → 深浅色」那个开关只切换 App 自己的外观,**没有任何联动聊天页的代码** ——
它就是这条默认行为的活证据。想强制固定某一档就设 `light`/`dark`,想让渠道后台说了算就设 `auto`(详 `DemoTheme.swift`)。

## 页面结构:四个能力页

示范 App 只演示**常用、且适合在 App 里演示**的场景(给试用者看效果、给对接的技术人员抄作业),
不追求覆盖全部接口 —— 纯文字说明类的内容(权限时机、离线推送接法、宿主接管回调、命令透传)
归接入文档。L0「渠道 ID + 一行打开」上面两行代码已经讲完,不单设页面。

| 页 | 覆盖 |
|---|---|
| **界面形态**(首页) | 四档承载形态、标题栏、深浅色、语言 —— 租户最先关心"长什么样" |
| **身份与会员** | 示范会员资料演示台、identify / resetUser **成对**、未读跟踪(开关 + 带徽标的客服入口示范) |
| **高级扩展** | 技能组指派、商品 / 订单选择器 |
| **配置与诊断** | 渠道配置、诊断信息、会话事件流水 |

## 文件导览:哪些是接入示范,哪些是这个工程自己的架子

| 文件 | 性质 | 看它干什么 |
|---|---|---|
| `AppDelegate.swift` | ✅ **接入示范** | 全局登记 + 未读 / 访客标识两个回调怎么接 |
| `CustomHeaderChatViewController.swift` | ✅ **接入示范** | 自己画标题栏 + 身份三态渲染 + 真头像加载 + 返回键先问 SDK |
| `DemoTheme.swift` | ✅ **接入示范** | 深浅色:App 主导、聊天页跟随(零联动代码) |
| `ChatLaunch.swift` | ✅ **接入示范** | 四档承载形态的标准写法,每个方法可直接抄 |
| `Pages/` 目录 | 🔧 脚手架 | 四个能力页的清单声明(装配见 `DemoTabBarController.swift`)|
| `DemoScene.swift` / `DemoTabBarController.swift` | 🔧 脚手架 | 清单骨架与分页装配,接入时不需要 |
| `DemoMemberProfile.swift` / `Pages/MemberProfileViewController.swift` | 🔧 脚手架 | 示范会员资料演示台(可填写并持久化),接入时取你自己的登录用户 |
| `DemoConfig.swift` / `DiagnosticsViewController.swift` | 🔧 脚手架 | 三档渠道与诊断页,接入时不需要 |
| `ChannelSetup.swift` | 🔧 脚手架 | 渠道 ID 没配好时拦一道并引导去填,接入时不需要 |
| `UI/` 目录 | 🔧 脚手架 | 设计 token、列表单元与极简头像加载,换成你自己的设计体系 / SDWebImage |

## 界面与设计系统

按定稿设计稿实现(`design/exports/app-demo-0{1..4}.png`,第 4 屏是施工规格),与安卓示范同一套
色板 / 字阶 / 尺寸 / 落地纪律。iOS 侧用系统能力表达:`.insetGrouped` 列表 = 设计稿的分组卡片,
SF Symbols = 设计稿的 lucide 线性图标,`sheetPresentationController` = 底部弹层。
**零第三方 UI 依赖**,与 SDK 本体调性一致。

## 跟安卓示范工程的两处平台差异(不是漏做)

1. **标准档的标题栏归属**:iOS 推进宿主自己的导航栏(栏是租户的,SDK 不绘制);安卓没有宿主导航栈,
   `HecongChatActivity` 由 SDK 绘制一条原生栏 —— 所以「标题栏配色」在安卓对标准档生效,iOS 只对弹层档生效。
2. **弹层档顶栏**:iOS 有"系统导航栏 / 渠道标题栏"两档,安卓只有一档。

**其余场景两端逐项一致**(四页结构、分组、文案、行为)。

## 跟系统版本有关的两点(**不是实现差异,别去"修"**)

1. **TabBar 外观随系统版本变化**:iOS 26 起系统把 TabBar 画成悬浮胶囊样式,iOS 18 上是设计稿
   那种满宽底栏。同一份代码,系统演进 —— 原生 App 本就该跟随,这里刻意不覆写。
   同理 iOS 26 的返回键被系统画成圆形玻璃按钮。
2. **导航栏不覆盖 `scrollEdgeAppearance`**:2026-08-18 在 iOS 26 模拟器实测定位 —— 一旦给它设
   不透明的自定义背景,**大标题整个不绘制**,顶上只剩一片空白(用红色文字实验排除了配色因素,
   是根本没画);iOS 18 上同样代码正常。交还系统后两个版本都正常。改 `DemoStyle` 前先读那段注释。

## ⚠️ 当前是"本地源码依赖",SDK 正式发版后要换成坐标

工程现在通过**本地 Swift Package**(`XCLocalSwiftPackageReference "../../ios"`)引本仓源码,
**发版后必须换成正式依赖**,否则租户拿到工程跑不起来(他们没有 `native/ios` 这个目录)。两条路:

- **SPM 远程(对外唯一方式)**:删掉本地包引用,改 Add Package 填公开分发仓地址 + 版本 tag;
- **SPM**:把工程里的本地包引用换成远程仓库引用(`XCRemoteSwiftPackageReference` + 版本区间)。

**其余代码一字不改** —— 这也正是这份工程要证明的事:换依赖形式不影响接入写法。

## 跑起来

```bash
xcodebuild -project HecongChatDemo.xcodeproj -scheme HecongChatDemo \
  -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16 Pro' build
```

DEBUG 构建默认走内部联调渠道(`DemoConfig.swift` 的 `#if DEBUG` 段,本地 5175 插座);
要连你自己的渠道,进 App →「配置与诊断 → 渠道配置」填 ID 即可,不用改代码。
(iOS 模拟器与 Mac 共用 localhost,不需要安卓那套端口转发。)
