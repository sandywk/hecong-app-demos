// Tab2「示例」—— 分组场景清单,点进即演示。
//
// **每个场景对应的代码就是该场景的标准答案**:打开方式看本文件的 push / sheet 方法,
// 自己画标题栏看 CustomHeaderChatViewController。本文件是示范工程的脚手架,接入时不需要。
import HecongChatSDK
import UIKit
import WebKit

final class CatalogViewController: UITableViewController, HecongChatDelegate {
  private struct Scene {
    let title: String
    let desc: String
    let action: (CatalogViewController) -> Void
  }

  /// 最近打开的聊天实例(退出登录等命令对它操作;真实接入你自己常驻持有)
  private weak var lastChat: HecongChatViewController?

  private let groups: [(String, [Scene])] = [
    ("打开方式", [
      Scene(title: "快速接入(推荐)", desc: "两行代码打开,权限、返回键、深浅色全内置") {
        $0.pushChat(title: "在线客服")
      },
      Scene(title: "自己画标题栏", desc: "左返回 + 客服头像昵称,内容由 SDK 实时给(转接会变)") {
        guard ChannelSetup.ensureReady(on: $0) else { return }
        $0.navigationController?.pushViewController(CustomHeaderChatViewController(), animated: true)
      },
      Scene(title: "底部弹层", desc: "半屏卡片承载,下拉或点右上角关闭") { $0.presentSheetChat() },
      Scene(title: "聊天页自带标题栏", desc: "不用你的导航栏,标题栏由聊天页自己画") {
        $0.pushChat(extraQuery: ["hh": "0"], hideNavBar: true)
      },
    ]),
    ("身份与推送", [
      Scene(title: "不登录直接进", desc: "不传身份,SDK 自动建立访客(同一台设备连续)") { $0.pushChat() },
      Scene(title: "登录后进入", desc: "把会员 ID 传给 SDK 绑定身份") {
        $0.pushChat(userId: DemoConfig.demoUserId)
      },
      Scene(
        title: "登录时先绑身份", desc: "不打开客服也能绑 —— 登录成功那一刻调,之后进客服自动带上"
      ) {
        // 标准接法:在**你自己的登录成功回调里**调。身份会被记住,用户之后什么时候
        // 点开客服都自动带上;不用你去挑"什么时候调才不早不晚"。
        HecongChat.shared.identify(
          userId: DemoConfig.demoUserId, profile: ["name": DemoConfig.demoUserName])
        DemoStyle.alert(
          on: $0,
          message: "已绑定会员 \(DemoConfig.demoUserId)(客服页还没开也没关系)\n"
            + "现在再打开客服,客服看到的就是这个会员")
      },
      Scene(title: "退出登录", desc: "换人 = 干净重来,上一位的会话不会带过去") {
        // ⚠️ 这一步别省 —— 不调的话,下一个在这台设备上登录的人会看到上一位的聊天记录。
        // **不需要客服页开着**,在你自己的退出登录流程里调即可。
        HecongChat.shared.resetUser()
        DemoStyle.alert(on: $0, message: "已退出登录 —— 身份与会话都清了,下一个人不会看到上一位的记录")
      },
      Scene(title: "访客标识", desc: "没登录的访客,离线推送靠它对上人") {
        let id = DemoFacadeDelegate.shared.lastAnonymousId ?? "(还没有 —— 先打开一次客服)"
        DemoStyle.alert(
          on: $0, title: "当前访客标识",
          message: id + "\n\n接入时:在 hecongChatDidChangeAnonymousId 里把它和你的推送 token 一起报到自己的后端。")
      },
    ]),
    ("外观", [
      Scene(title: "深色 / 浅色", desc: "在「我的」页拨动开关,聊天页会跟着一起切换") { $0.pushChat() },
      Scene(title: "强制深色(不跟 App)", desc: "少数场景才需要:打开时显式指定档位") {
        $0.pushChat(colorScheme: "dark")
      },
      Scene(title: "切换聊天页语言", desc: "打开时指定语言,例如英文") {
        $0.pushChat(extraQuery: ["lang": "en"])
      },
    ]),
    ("客服能力", [
      Scene(title: "未读红点", desc: "有新消息时,底部 Tab 与「我的」页入口一起亮红点") { $0.pushChat() },
      Scene(title: "退出聊天页也收未读", desc: "需手动开启:开启后不进聊天页也能收到未读数") {
        // 一步开启:登记回调 + 开始跟踪。⚠️ 会联网,要在用户同意隐私政策之后。
        HecongChat.shared.startUnreadTracking(listener: DemoFacadeDelegate.shared)
        DemoStyle.alert(on: $0, message: "已开启。先聊一次再退出聊天页,客服回消息后 App 角标会变")
      },
      Scene(title: "相机 / 麦克风权限", desc: "点 + 号选拍摄:系统弹窗自带用途说明") { $0.pushChat() },
      Scene(title: "文件下载与外链", desc: "文件走系统下载,外部链接跳系统浏览器") { $0.pushChat() },
      Scene(title: "会话事件流水", desc: "看 SDK 发了哪些事件:消息到达 / 对话起止 / 网络通断") {
        // 演示用:接入时你不记流水,直接在 hecongChat(didReceiveEvent:) 里做事(接线见 AppDelegate)
        let lines = DemoEventLog.shared.snapshot()
        DemoStyle.alert(
          on: $0, title: "会话事件流水(最近 \(lines.count) 条)",
          message: lines.isEmpty
            ? "还没有事件。\n\n去打开客服发一句话、等客服回一句,再回来看这里 —— "
              + "你会看到 conversation:start、message、message:incoming 依次出现。"
            : lines.joined(separator: "\n"))
      },
    ]),
    ("配置与诊断", [
      Scene(title: "渠道配置", desc: "填上你自己的渠道 ID,不改代码就能连到你的工作台") {
        $0.showChannelSettings()
      },
      Scene(title: "清除本地缓存", desc: "清完再打开:身份与聊天记录仍在(这是 SDK 的能力)") {
        $0.clearLocalData()
      },
      Scene(title: "诊断信息", desc: "当前配置 / 渠道 / 访客标识") {
        $0.navigationController?.pushViewController(DiagnosticsViewController(), animated: true)
      },
    ]),
  ]

  init() { super.init(style: .insetGrouped) }
  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "示例"
    DemoStyle.applyPageChrome(to: self)
    tableView.register(DemoListCell.self, forCellReuseIdentifier: DemoListCell.reuseId)
  }

  /// 「聊天页自带标题栏」场景会隐藏导航栏 —— 回到清单时恢复
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(false, animated: animated)
  }

  // MARK: - 场景动作(代码即标准答案)

  func pushChat(
    title: String? = nil, userId: String? = nil, colorScheme: String? = nil,
    extraQuery: [String: String] = [:], hideNavBar: Bool = false
  ) {
    guard ChannelSetup.ensureReady(on: self) else { return }
    let config = DemoConfig.buildChatConfig(extraQuery: extraQuery)
    // 不传就用默认档(跟随 App);只有想强制某一档时才显式设
    if let colorScheme = colorScheme { config.colorScheme = colorScheme }
    let chat = HecongChatViewController(config: config)
    chat.delegate = self
    chat.title = title
    chat.hidesBottomBarWhenPushed = true // 聊天是沉浸页,推入时收起底部 Tab
    DemoStyle.applyChatChrome(to: chat) // 一行紧凑导航栏,不占大标题那块地
    ChatOverflowMenu.install(on: chat) // 右上角「…」(与安卓同两项)
    if let userId = userId {
      chat.identify(userId: userId, profile: ["name": DemoConfig.demoUserName], data: nil)
    }
    lastChat = chat
    if hideNavBar { navigationController?.setNavigationBarHidden(true, animated: true) }
    navigationController?.pushViewController(chat, animated: true)
  }

  /// 底部弹层:用系统自带的 sheet 形态(iOS 15+ detents),零第三方依赖。
  /// 这一档**没有返回键,所以保留右上角关闭按钮** —— 它是唯一出口。
  private func presentSheetChat() {
    guard ChannelSetup.ensureReady(on: self) else { return }
    let chat = HecongChatViewController(config: DemoConfig.buildChatConfig())
    chat.delegate = self
    chat.title = "在线客服"
    DemoStyle.applyChatChrome(to: chat)
    lastChat = chat
    let nav = UINavigationController(rootViewController: chat)
    // 这一档没有返回键,关闭按钮是唯一出口。右侧排列与安卓弹层一致:靠外是 ✕,内侧是「…」
    ChatOverflowMenu.install(on: chat)
    let more = chat.navigationItem.rightBarButtonItem
    let close = UIBarButtonItem(
      image: DemoIcon.image(DemoIcon.close, size: 16, weight: .semibold),
      style: .plain, target: self, action: #selector(dismissChat))
    chat.navigationItem.rightBarButtonItems = [close, more].compactMap { $0 }
    // 系统自带的半屏卡片形态(iOS 15+);更老的系统退化为全屏 modal,行为一致
    if #available(iOS 15.0, *), let sheet = nav.sheetPresentationController {
      sheet.detents = [.large()]
      sheet.prefersGrabberVisible = true
      sheet.preferredCornerRadius = 24
    }
    present(nav, animated: true)
  }

  @objc private func dismissChat() { dismiss(animated: true) }

  /// 演示用(接入时不需要):清掉本地缓存,验证"身份与聊天记录仍在"这个能力
  private func clearLocalData() {
    WKWebsiteDataStore.default().removeData(
      ofTypes: WKWebsiteDataStore.allWebsiteDataTypes(), modifiedSince: .distantPast
    ) { [weak self] in
      guard let self = self else { return }
      DemoStyle.alert(on: self, message: "已清除本地缓存 —— 再打开客服,身份与聊天记录应该都还在")
    }
  }

  /// 演示用(接入时不需要):填自己的渠道 ID,不改代码就能连到你的工作台
  private func showChannelSettings() { ChannelSetup.showSettings(on: self) }

  // MARK: - HecongChatDelegate

  func hecongChatUnreadDidChange(_ count: Int) {
    navigationController?.tabBarItem.badgeValue = count > 0 ? "\(count)" : nil
  }

  // MARK: - UITableView

  override func numberOfSections(in tableView: UITableView) -> Int { groups.count }

  override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
    groups[section].0
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    groups[section].1.count
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let scene = groups[indexPath.section].1[indexPath.row]
    // swiftlint:disable:next force_cast
    let cell = tableView.dequeueReusableCell(withIdentifier: DemoListCell.reuseId, for: indexPath) as! DemoListCell
    cell.configure(title: scene.title, subtitle: scene.desc)
    return cell
  }

  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    tableView.deselectRow(at: indexPath, animated: true)
    groups[indexPath.section].1[indexPath.row].action(self)
  }
}
