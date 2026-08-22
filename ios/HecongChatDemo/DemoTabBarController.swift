// 示范 App 主框架:四个能力页。
//
// | Tab | 覆盖 |
// |---|---|
// | 界面形态 | 四档承载形态、标题栏、深浅色、语言 —— 租户最先关心"长什么样",放第一页 |
// | 身份与会员 | 会员资料演示台、identify / resetUser 成对、未读跟踪 |
// | 高级扩展 | 技能组指派、输入区扩展 |
// | 配置与诊断 | 渠道配置、诊断信息、会话事件流水 |
//
// 2026-08-21 owner 定调:示范 App 只演示**常用且适合在 App 里演示**的场景,
// 不追求覆盖全部接口 —— 纯文字说明类的条目(权限时机、推送接法、宿主接管回调)归接入文档。
// 原「快速接入」页已撤:L0 只有「渠道 ID + 一行打开」两件事,README 首屏即可讲清。
//
// **示范工程自己的脚手架,接入时不需要。** 唯一值得参考的是 [applyUnread]:
// SDK 给出的未读数如何落到宿主自己的 Tab 徽标与入口红点上。
import HecongChatSDK
import UIKit

/// Tab 序号 —— 页面间互跳与自动化钩子按它定位,不写魔法数字
enum DemoTab: Int {
  case appearance = 0
  case identity = 1
  case advanced = 2
  case toolbox = 3
}

final class DemoTabBarController: UITabBarController {
  /// 供全局回调更新徽标(未读回调不在页面上下文里)
  private(set) static weak var live: DemoTabBarController?

  private let identity = IdentityViewController()

  override func viewDidLoad() {
    super.viewDidLoad()
    DemoTabBarController.live = self
    viewControllers = [
      wrap(AppearanceViewController(), title: "界面形态", icon: DemoIcon.tabAppearance),
      wrap(identity, title: "身份与会员", icon: DemoIcon.tabIdentity),
      wrap(AdvancedViewController(), title: "高级扩展", icon: DemoIcon.tabAdvanced),
      wrap(ToolboxViewController(), title: "配置与诊断", icon: DemoIcon.tabToolbox),
    ]
  }

  /// 未读数变化 → 「身份与会员」Tab 徽标 + 该页客服入口示范行的红点。
  /// 真实接入时这里换成你自己的入口(帮助中心那一行 / 个人中心那一格)。
  func applyUnread(_ count: Int) {
    let item = viewControllers?[DemoTab.identity.rawValue].tabBarItem
    item?.badgeValue = count > 0 ? "\(count)" : nil
    identity.applyUnread(count)
  }

  /// 切到某个 Tab 并返回其导航栈(自动化钩子与页面互跳共用)。
  ///
  /// ⚠️ 必须 `loadViewIfNeeded`:`selectedIndex` 只是选中,页面视图要到真正显示时才装载,
  /// 而自动化钩子紧接着就往这个栈里 push —— 根页 viewDidLoad 尚未执行,它在那里设置的
  /// 导航栏形态(返回键收成纯 chevron 等)就会全部落空(2026-08-20 模拟器实测)。
  @discardableResult
  func select(_ tab: DemoTab) -> UINavigationController? {
    selectedIndex = tab.rawValue
    let nav = viewControllers?[tab.rawValue] as? UINavigationController
    nav?.topViewController?.loadViewIfNeeded()
    return nav
  }

  private func wrap(_ page: UIViewController, title: String, icon: String) -> UINavigationController {
    page.title = title
    let nav = UINavigationController(rootViewController: page)
    nav.tabBarItem = UITabBarItem(
      title: title, image: DemoIcon.image(icon, size: 18), selectedImage: nil)
    return nav
  }
}
