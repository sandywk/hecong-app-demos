// 打开客服的统一收口 —— **每个方法就是对应承载形态的标准答案**,接入时照抄这一行即可。
//
// 四档承载形态的定义与选型依据:`docs/architecture/app-sdk-chat-entry.md`。
// 与安卓 `ChatLaunch.kt` 逐条对位,两端方法名一一对应,便于文档并排书写。
import HecongChatSDK
import UIKit

enum ChatLaunch {
  /// 最近打开的聊天页(演示用:供「会话中切换技能组」等命令定位收件人)。
  /// 真实接入按自己的页面管理方式持有即可。
  private(set) static weak var lastChat: HecongChatViewController?

  // MARK: - 四档承载形态(门面直调)

  /// ① 标准档:推入宿主导航栏承载 —— 顶栏即宿主那条,SDK 不绘制任何标题栏。
  /// 安卓对位:`HecongChatActivity.start(context, config)`。
  static func standard(from host: UIViewController) {
    guard ChannelSetup.ensureReady(on: host), let nav = host.navigationController else { return }
    let chat = HecongChat.shared.push(from: nav, config: DemoConfig.buildChatConfig())
    bind(chat)
  }

  /// ② 弹层档:底部卡片承载,支持上拉全屏与下拉关闭。
  /// [useChannelHeader] 为真时卡片内整页交由聊天页绘制(渠道模板标题栏 + ✕ 收起)。
  /// 安卓对位:`HecongChatActivity.startSheet(context, config)`。
  static func sheet(from host: UIViewController, useChannelHeader: Bool = false) {
    guard ChannelSetup.ensureReady(on: host) else { return }
    let chat = HecongChat.shared.presentSheet(
      from: host, config: DemoConfig.buildChatConfig(), useChannelHeader: useChannelHeader)
    bind(chat)
  }

  /// ④ 沉浸档:整页交由聊天页绘制,状态栏明暗经桥自动跟随。
  /// 安卓对位:`HecongChatActivity.startImmersive(context, config)`。
  static func immersive(from host: UIViewController) {
    guard ChannelSetup.ensureReady(on: host) else { return }
    let chat = HecongChat.shared.presentImmersive(from: host, config: DemoConfig.buildChatConfig())
    bind(chat)
  }

  /// ③ 嵌入档:聊天视图装进宿主自己的页面,标题栏由宿主绘制
  /// (客服昵称/头像经 `hecongChatHeaderIdentityDidChange` 实时下发)。
  /// 安卓对位:`HecongChatFragment.newInstance(config)`。
  static func customHeader(from host: UIViewController) {
    guard ChannelSetup.ensureReady(on: host) else { return }
    host.navigationController?.pushViewController(CustomHeaderChatViewController(), animated: true)
  }

  // MARK: - 带参打开(演示各配置项的效果)

  /// 以指定参数推入承载页 —— 演示 `HecongChatConfig` 各字段的实际效果。
  /// 真实接入通常只用到其中一两项,不必逐个设置。
  static func push(
    from host: UIViewController, title: String? = nil, userId: String? = nil,
    colorScheme: String? = nil, extraQuery: [String: String] = [:],
    routing: HecongRouting? = nil, titleFollowsAgent: Bool = false,
    tintedHeader: Bool = false, hideNavBar: Bool = false
  ) {
    guard ChannelSetup.ensureReady(on: host) else { return }
    let config = DemoConfig.buildChatConfig(extraQuery: extraQuery)
    if let routing = routing { config.routing = routing }
    if let colorScheme = colorScheme { config.colorScheme = colorScheme }
    if let title = title { config.title = title }
    config.titleFollowsAgent = titleFollowsAgent
    if tintedHeader {
      // 标题栏配色:不设 = 跟随系统外观(深浅色自动适配),设了 = 宿主品牌色主导
      config.headerBackgroundColor = DemoColor.accent
      config.titleColor = .white
    }

    let chat = HecongChatViewController(config: config)
    chat.title = title
    chat.hidesBottomBarWhenPushed = true // 聊天为沉浸页,推入时收起底部导航
    DemoStyle.applyChatChrome(to: chat)
    if let userId = userId {
      chat.identify(
        userId: userId, profile: DemoMemberProfile.profileDictionary(),
        data: DemoMemberProfile.dataDictionary())
    }
    bind(chat)
    if hideNavBar { host.navigationController?.setNavigationBarHidden(true, animated: true) }
    host.navigationController?.pushViewController(chat, animated: true)
  }

  /// 自动化钩子入口(`simctl launch -hcAuto standard|sheet|sheetH5|immersive`)——
  /// 命令行能启动模拟器但无法点击界面,没有这套钩子就无法自动验收各承载形态。
  static func openArchetype(_ mode: String, from host: UIViewController) {
    switch mode {
    case "standard": standard(from: host)
    case "sheet": sheet(from: host)
    case "sheetH5": sheet(from: host, useChannelHeader: true)
    case "immersive": immersive(from: host)
    default: break
    }
  }

  private static func bind(_ chat: HecongChatViewController) {
    chat.delegate = DemoFacadeDelegate.shared
    lastChat = chat
  }
}
