// Tab1「我的」—— 模拟一个真实 App 的个人中心页:"你的 App 接完就是这个体验"。
//
// **本页有两个真实接入点**:①② 客服入口那一行;③ 深色模式开关(演示"App 换档,聊天页跟随")。
// 头像卡、订单、收货地址是**演示用的假菜单**,接入时不需要,别照抄。
import HecongChatSDK
import UIKit

final class MineViewController: UITableViewController, HecongChatDelegate {
  private enum Row { case profile, fake(String, String), darkMode, support }

  private let sections: [(String?, [Row])] = [
    (nil, [.profile]),
    (nil, [.fake("我的订单", DemoIcon.order), .fake("收货地址", DemoIcon.address), .darkMode]),
    ("帮助与支持", [.support]),
  ]
  private var unread = 0

  init() { super.init(style: .insetGrouped) }
  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "我的"
    DemoStyle.applyPageChrome(to: self)
    tableView.register(DemoListCell.self, forCellReuseIdentifier: DemoListCell.reuseId)
    tableView.register(DemoProfileCell.self, forCellReuseIdentifier: DemoProfileCell.reuseId)
  }

  /// ① **真实接入点**:带会员身份打开客服。
  ///
  /// 你要抄的就是这一段 —— 建一个聊天页,把当前登录会员的 ID 传进去。未登录场景不调
  /// `identify` 即可:SDK 自动建立访客,之后再 identify 换人也不会串号。
  /// 深浅色**不用管**,默认就跟随你的 App。
  private func openCustomerService() {
    guard ChannelSetup.ensureReady(on: self) else { return }
    let chat = HecongChatViewController(config: DemoConfig.buildChatConfig())
    chat.delegate = self
    chat.title = "在线客服"
    chat.hidesBottomBarWhenPushed = true // 聊天是沉浸页,推入时收起底部 Tab
    DemoStyle.applyChatChrome(to: chat) // 一行紧凑导航栏,不占大标题那块地
    ChatOverflowMenu.install(on: chat) // 右上角「…」(与安卓同两项)
    chat.identify(userId: DemoConfig.demoUserId, profile: ["name": DemoConfig.demoUserName], data: nil)
    navigationController?.pushViewController(chat, animated: true)
  }

  /// ③ **真实接入点**:App 自己的深色模式开关。拨动后聊天页自动跟随,**没有联动代码**(见 DemoTheme)
  @objc private func toggleDarkMode(_ sender: UISwitch) {
    DemoTheme.setDark(sender.isOn, window: view.window)
  }

  // ② 未读 → Tab 角标 + 本页入口红点(数据来自 SDK 的未读回调)
  func hecongChatUnreadDidChange(_ count: Int) {
    unread = count
    navigationController?.tabBarItem.badgeValue = count > 0 ? "\(count)" : nil
    tableView.reloadData()
  }

  // MARK: - UITableView

  override func numberOfSections(in tableView: UITableView) -> Int { sections.count }

  override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
    sections[section].0
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    sections[section].1.count
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    switch sections[indexPath.section].1[indexPath.row] {
    case .profile:
      return tableView.dequeueReusableCell(withIdentifier: DemoProfileCell.reuseId, for: indexPath)
    case let .fake(title, icon):
      let cell = listCell(indexPath)
      cell.configure(title: title, icon: icon)
      return cell
    case .darkMode:
      let toggle = UISwitch()
      toggle.isOn = DemoTheme.isDark
      toggle.onTintColor = DemoColor.accent
      toggle.addTarget(self, action: #selector(toggleDarkMode(_:)), for: .valueChanged)
      let cell = listCell(indexPath)
      cell.configure(
        title: "深色模式", subtitle: "聊天页会跟着一起切换", icon: DemoIcon.settings,
        accessory: toggle)
      cell.selectionStyle = .none
      return cell
    case .support:
      let cell = listCell(indexPath)
      cell.configure(
        title: "在线客服", subtitle: "7×24 小时随时为你解答", icon: DemoIcon.support,
        accent: true, badgeCount: unread)
      return cell
    }
  }

  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    tableView.deselectRow(at: indexPath, animated: true)
    switch sections[indexPath.section].1[indexPath.row] {
    case .support: openCustomerService()
    case .fake: DemoStyle.alert(on: self, message: "演示用的菜单项 —— 模拟你 App 里的普通页面")
    default: break
    }
  }

  private func listCell(_ indexPath: IndexPath) -> DemoListCell {
    // swiftlint:disable:next force_cast
    tableView.dequeueReusableCell(withIdentifier: DemoListCell.reuseId, for: indexPath) as! DemoListCell
  }
}
