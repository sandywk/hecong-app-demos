// 示范 App 入口:四个能力页(装配见 DemoTabBarController)。
// 经典 AppDelegate 生命周期,无 storyboard,纯系统控件(零第三方 UI 依赖,与 SDK 同调性)。
import HecongChatSDK
import UIKit

@main
final class AppDelegate: UIResponder, UIApplicationDelegate {
  var window: UIWindow?

  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    // ── 全局接线 ——**无 UI 的那一层**:管身份、未读、上报开关,不打开客服页也能用。
    // 合规:configure 只登记参数、零联网零读存储,所以放启动里没问题;真正联网的是聊天页
    // load 与 startUnreadTracking,那两个要等用户同意隐私政策。
    HecongChat.shared.configure(DemoConfig.buildChatConfig(), listener: DemoFacadeDelegate.shared)
    // ⚠️ 未读跟踪**默认关闭**,SDK 不会自己开。真实接入:按你自己 App 里"消息提醒"开关的
    // 持久化状态决定要不要在启动时开启 —— 下面这行就是这个姿势(示范开关在「身份与会员 → 未读跟踪」)。
    DemoFacadeDelegate.shared.restoreUnreadTrackingIfWanted()

    DemoStyle.installGlobalAppearance()

    // 四个能力页,详 DemoTabBarController 头注释。
    let root = DemoTabBarController()

    let window = UIWindow(frame: UIScreen.main.bounds)
    window.rootViewController = root
    window.makeKeyAndVisible()
    self.window = window

    // App 自身的深浅色(用户上次的选择);聊天页会跟着它走,详 DemoTheme
    DemoTheme.apply(to: window)

    applyAutomationHooks(root: root)
    return true
  }

  /// 验收用:`-hcUser <id>` 指定本次要绑的会员,缺省用演示台里那份。
  /// 参数化才测得了"A 退出 → 登录 B"这类换人场景。
  static func automationUserId() -> String {
    let args = ProcessInfo.processInfo.arguments
    if let i = args.firstIndex(of: "-hcUser"), i + 1 < args.count, !args[i + 1].isEmpty {
      return args[i + 1]
    }
    return DemoMemberProfile.userId
  }

  /// 自动化测试钩子(simctl launch 传参驱动)——**演示工程自己用的,接入时不需要**。
  ///
  /// 命令行能启动模拟器却无法点击界面,没有这套钩子就无法自动验收各承载形态。
  /// 分类改造后钩子名保持不变(旧脚本继续可用),只是各自先切到所属 Tab 再执行。
  private func applyAutomationHooks(root: DemoTabBarController) {
    let args = ProcessInfo.processInfo.arguments
    let known = [
      "-autoOpenChat", "-autoIdentify", "-autoH5Header", "-autoDiagnostics", "-autoCustomHeader",
      "-autoCatalog", "-autoStandard", "-autoSheet", "-autoSheetH5", "-autoImmersive",
      "-autoTab", "-autoMemberProfile", "-autoResetUser", "-autoDumpState", "-autoUnreadOn", "-autoUnreadOff", "-autoIdentifyOnly", "-autoResetInChat",
    ]
    guard args.contains(where: known.contains) else { return }
    DispatchQueue.main.async {
      if args.contains("-autoCatalog") {
        root.select(.appearance) // 只停在首页,不再往下推页面
        return
      }
      // 直达某个能力页(-autoTab 0…3)—— 用于逐页截图验收
      if let idx = args.firstIndex(of: "-autoTab"), idx + 1 < args.count,
        let tab = Int(args[idx + 1]).flatMap(DemoTab.init(rawValue:))
      {
        root.select(tab)
        return
      }
      // 四档承载形态统一在「界面形态」页发起
      for (flag, mode) in [
        ("-autoStandard", "standard"), ("-autoSheetH5", "sheetH5"),
        ("-autoSheet", "sheet"), ("-autoImmersive", "immersive"),
      ] where args.contains(flag) {
        guard let nav = root.select(.appearance), let host = nav.topViewController else { return }
        ChatLaunch.openArchetype(mode, from: host)
        return
      }
      if args.contains("-autoUnreadOn") {
        HecongChat.shared.startUnreadTracking(listener: DemoFacadeDelegate.shared); return
      }
      if args.contains("-autoUnreadOff") { HecongChat.shared.stopUnreadTracking(); return }
      if args.contains("-autoDumpState") {
        // 状态读出(验收用):把壳侧持久化的三个值打进系统日志。
        // 用日志而不是读 plist —— 模拟器的 preferences 守护进程有缓存回写,
        // 隔进程读盘会拿到过期快照(2026-08-21 实测踩到,一度误判为"号变回去了")。
        NSLog(
          "HCSTATE anonId=\(UserDefaults.standard.string(forKey: "hecong.chat.anonymousId") ?? "无")"
            + " pendingReset=\(UserDefaults.standard.bool(forKey: "hecong.chat.pendingIdentityReset"))"
            + " unreadWanted=\(UserDefaults.standard.bool(forKey: "hecong.chat.unreadTrackingWanted"))"
            + " unread=\(HecongChat.shared.unreadCount)")
        return
      }
      if args.contains("-autoIdentifyOnly") {
        // 只绑身份、**不打开聊天页** —— 租户"登录成功回调里调 identify"的真实形态
        HecongChat.shared.identify(
          userId: Self.automationUserId(), profile: DemoMemberProfile.profileDictionary(),
          data: DemoMemberProfile.dataDictionary())
        root.select(.identity)
        return
      }
      if args.contains("-autoResetInChat") {
        // 聊天页**开着**的时候退出 —— 与 -autoResetUser(设置页形态)互为另一半
        guard let nav = root.select(.identity), let host = nav.topViewController else { return }
        ChatLaunch.push(from: host, title: "在线客服", userId: Self.automationUserId())
        DispatchQueue.main.asyncAfter(deadline: .now() + 7) { HecongChat.shared.resetUser() }
        return
      }
      if args.contains("-autoResetUser") {
        // 退出登录:**不打开聊天页**,模拟租户在自己的设置页点退出(这正是它唯一的真实调用时机)
        HecongChat.shared.resetUser()
        root.select(.identity)
        return
      }
      if args.contains("-autoMemberProfile") {
        root.select(.identity)?.pushViewController(MemberProfileViewController(), animated: false)
      } else if args.contains("-autoDiagnostics") {
        root.select(.toolbox)?.pushViewController(DiagnosticsViewController(), animated: false)
      } else if args.contains("-autoCustomHeader") {
        root.select(.appearance)?
          .pushViewController(CustomHeaderChatViewController(), animated: false)
      } else if args.contains("-autoH5Header") {
        guard let nav = root.select(.appearance), let host = nav.topViewController else { return }
        ChatLaunch.push(from: host, extraQuery: ["hh": "0"], hideNavBar: true)
      } else {
        let tab: DemoTab = args.contains("-autoIdentify") ? .identity : .appearance
        guard let nav = root.select(tab), let host = nav.topViewController else { return }
        ChatLaunch.push(
          from: host, title: "在线客服",
          userId: args.contains("-autoIdentify") ? Self.automationUserId() : nil)
      }
    }
  }
}

/// 全局回调(未读 + 访客标识)——**这两件事不需要客服页开着**。
/// 真实接入常挂在 AppDelegate / 你自己的单例上,与推送 token 一起管理。
final class DemoFacadeDelegate: NSObject, HecongChatDelegate {
  static let shared = DemoFacadeDelegate()

  private let unreadWantedKey = "hecong.demo.unreadTrackingOn"

  /// 示范开关的状态(持久化)—— 模拟宿主 App 自己的"消息提醒"设置项
  var isUnreadTrackingOn: Bool { UserDefaults.standard.bool(forKey: unreadWantedKey) }

  /// 拨动示范开关:开 = startUnreadTracking(可在聊天页打开之前调,从没聊过天时零请求),
  /// 关 = stopUnreadTracking。
  func setUnreadTracking(_ on: Bool) {
    UserDefaults.standard.set(on, forKey: unreadWantedKey)
    if on {
      HecongChat.shared.startUnreadTracking(listener: self)
      DemoStyle.toast("已开启未读跟踪 —— 进入客服留言后退出,等客服回复")
    } else {
      HecongChat.shared.stopUnreadTracking()
      DemoStyle.toast("已停止未读跟踪")
    }
  }

  /// App 启动时按上次的选择恢复(真实接入:读你自己的设置项)
  func restoreUnreadTrackingIfWanted() {
    if isUnreadTrackingOn { HecongChat.shared.startUnreadTracking(listener: self) }
  }

  func hecongChatUnreadDidChange(_ count: Int) {
    // 真实接入:更新你自己的角标 / 入口红点。这里同时演示三处落点 ——
    // App 图标角标、底部 Tab 徽标、「身份与会员」页客服入口示范行的红点。
    UIApplication.shared.applicationIconBadgeNumber = count
    DemoTabBarController.live?.applyUnread(count)
    // 不弹提示:角标本身就是反馈(owner 2026-08-21),飘字只会盖住内容
  }

  /// **自定义按钮被点了** —— 这就是"点商品入口 → 弹商品列表"的接线处。
  ///
  /// 真实接入时你在这里做的是:去自己的系统取当前该展示的商品/订单,`setPickerData` 回填,
  /// 再 `openPicker` 打开。
  /// ⚠️ **数据必须在这一刻给,不能在打开聊天页之前提前给** —— SDK 刻意不缓存选择器数据
  /// (库存/登录态都会变,缓存重放等于把陈旧列表推给下一个会话)。
  func hecongChat(didClickAction id: String) {
    switch id {
    case DevCapabilityActions.actionProduct:
      HecongChat.shared.setPickerData("product", items: DemoSampleData.products())
      HecongChat.shared.openPicker("product")
    case DevCapabilityActions.actionOrder:
      HecongChat.shared.setPickerData("order", items: DemoSampleData.orders())
      HecongChat.shared.openPicker("order")
    default:
      DemoStyle.toast("你点了自定义按钮「\(id)」—— 这里是你的代码")
    }
  }

  /// **聊天页里任何"要往外跳"的动作都先经过这里** —— 客服发的网址、商品卡片的详情链接、
  /// 电话号码、邮箱、页面内的跳转,统统先问你一次。
  ///
  /// 返回 `true` = 你自己处理了(SDK 什么都不做);返回 `false`/不实现 = SDK 用默认方式
  /// (网址跳 Safari、电话跳拨号、邮箱跳邮件)。
  ///
  /// 下面这段是**分流范例**:自家商品链接 → 跳自己 APP 的原生页面;其余 → 交给系统。
  func hecongChat(handleOpenUrl url: URL) -> Bool {
    let prefix = DemoSampleData.demoSite + "/product/"
    if url.absoluteString.hasPrefix(prefix) {
      let productId = String(url.absoluteString.dropFirst(prefix.count))
      // 真实接入这里换成你自己的路由跳转,例如 pushViewController(ProductDetailVC…)
      DemoStyle.toast("拦截成功 → 这里跳你 APP 的商品详情页(商品 \(productId))")
      return true // 我处理了,SDK 别再管
    }
    return false // 其余交给 SDK 默认处理(网址跳 Safari)
  }

  /// 访客标识变化 —— **离线推送的接线点**:没登录的访客,推送就靠这个号对上人。
  /// 真实接入在这里:postToMyBackend(anonymousId, myPushToken)。
  /// 示范 App 做不了推送,所以这里不做任何事,接法详见接入文档「离线推送」。
  func hecongChatDidChangeAnonymousId(_ anonymousId: String) {}

  // MARK: - 会话事件

  /// **会话事件通吃入口 —— 优先接这个**。
  ///
  /// 事件名与网页版 `hc.on(name, ...)` 完全同名(消息到达 / 对话起止 / 网络通断)。
  /// 好处:我们以后在 H5 侧新增的事件,**你不用升级 SDK 就能收到** —— 而具名回调
  /// (下面那个)每加一个都要等你升级依赖 + 重新发版,APP 的升级链条比网页长得多。
  func hecongChat(didReceiveEvent name: String, payload: [String: Any]?) {
    DemoEventLog.shared.record(name, payload)  // 演示:记进流水,诊断页能看见
    // 真实接入按 name 分流做事,例如:
    //   "conversation:start" -> 埋点"发起了咨询"
    //   "network:offline"    -> 自己页面上显示"连接中断"
  }

  /// 具名回调(便利糖:有类型、不用解字典)。**跟上面那个二选一,别同一件事处理两遍** ——
  /// 这里刻意分工:通吃只记流水给你看,这个演示真正的业务动作。
  func hecongChat(didReceiveIncomingMessage message: HecongMessage) {
    // 只有"对方"的消息会到这里(自己发的不会),所以适合做提醒。
    // 真实接入:App 在前台但用户不在客服页时,用它弹一条自己的本地通知 / 震动一下。
    DemoStyle.toast("客服:\(message.text.prefix(20))")
  }
}
