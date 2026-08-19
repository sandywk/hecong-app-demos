// 示范 App 入口:双 Tab ——「我的」(模拟你自家的页面)+「示例」(能力清单)。
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
    // ⚠️ 未读跟踪**默认关闭**,这里刻意不开 —— 让你看清"不开 = 完全不活动"的默认状态,
    // 由「示例 → 客服能力 → 退出客服页也收未读」手动开启。

    DemoStyle.installGlobalAppearance()

    let mine = UINavigationController(rootViewController: MineViewController())
    mine.tabBarItem = UITabBarItem(
      title: "我的", image: DemoIcon.image(DemoIcon.mine, size: 20), tag: 0)
    let catalog = UINavigationController(rootViewController: CatalogViewController())
    catalog.tabBarItem = UITabBarItem(
      title: "示例", image: DemoIcon.image(DemoIcon.catalog, size: 20), tag: 1)

    let tabs = UITabBarController()
    tabs.viewControllers = [mine, catalog]

    let window = UIWindow(frame: UIScreen.main.bounds)
    window.rootViewController = tabs
    window.makeKeyAndVisible()
    self.window = window
    // App 自身的深浅色(用户上次的选择);聊天页会跟着它走,详 DemoTheme
    DemoTheme.apply(to: window)

    applyAutomationHooks(tabs: tabs, catalog: catalog)
    return true
  }

  /// 自动化测试钩子(simctl launch 传参驱动)——**演示工程自己用的,接入时不需要**
  private func applyAutomationHooks(tabs: UITabBarController, catalog: UINavigationController) {
    let args = ProcessInfo.processInfo.arguments
    let known = [
      "-autoOpenChat", "-autoIdentify", "-autoH5Header", "-autoDiagnostics", "-autoCustomHeader",
      "-autoCatalog",
    ]
    guard args.contains(where: known.contains) else { return }
    tabs.selectedIndex = 1
    DispatchQueue.main.async {
      let list = catalog.viewControllers.first as? CatalogViewController
      if args.contains("-autoCatalog") {
        return // 只切到「示例」Tab,不再往下推页面
      }
      if args.contains("-autoDiagnostics") {
        list?.navigationController?.pushViewController(DiagnosticsViewController(), animated: false)
      } else if args.contains("-autoCustomHeader") {
        list?.navigationController?.pushViewController(CustomHeaderChatViewController(), animated: false)
      } else if args.contains("-autoH5Header") {
        list?.pushChat(extraQuery: ["hh": "0"], hideNavBar: true)
      } else {
        list?.pushChat(
          title: "在线客服", userId: args.contains("-autoIdentify") ? DemoConfig.demoUserId : nil)
      }
    }
  }
}

/// 全局回调(未读 + 访客标识)——**这两件事不需要客服页开着**。
/// 真实接入常挂在 AppDelegate / 你自己的单例上,与推送 token 一起管理。
final class DemoFacadeDelegate: NSObject, HecongChatDelegate {
  static let shared = DemoFacadeDelegate()

  /// 最近一次生效的访客标识(演示用;真实接入应存到**你自己的后端**,与推送 token 绑一起 ——
  /// 没登录的访客,离线推送就靠这个号对上人)。
  private(set) var lastAnonymousId: String?

  func hecongChatUnreadDidChange(_ count: Int) {
    // 真实接入:更新 Tab 角标 / 入口红点
    UIApplication.shared.applicationIconBadgeNumber = count
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
      // 两个位置对比那两个按钮:只提示,说明"点了会回到你的代码里"
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

  func hecongChatDidChangeAnonymousId(_ anonymousId: String) {
    lastAnonymousId = anonymousId
    // 真实接入在这里:postToMyBackend(anonymousId, myPushToken)
  }

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
