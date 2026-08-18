// 渠道配置与"没配好就别硬跑"的兜底 —— 演示工程自己的脚手架,接入时不需要。
//
// 为什么要有兜底:发布给外部下载的这份 App 走的是**官方演示渠道**,万一那个渠道 ID 还没填,
// 直接打开客服只会得到一个"网络错误"页 —— 对第一次看这个 App 的人来说,这是最糟的第一印象。
// 所以这里拦一道,把"没配"说清楚,并直接给出"填自己的渠道 ID"这条出路。
//
// 与安卓 `ChannelSetup.kt` 同构:判据、文案、按钮都一致。
import UIKit

enum ChannelSetup {
  /// 渠道 ID 是否已经可用(占位符 = 还没配)
  static var isReady: Bool { !DemoConfig.buildChatConfig().channelId.hasPrefix("TODO_") }

  /// 打开客服前先过这一关:配好了返回 true;没配好弹说明 + 引导去填,返回 false。
  /// 每个打开客服的入口都要走它,别绕过去。
  @discardableResult
  static func ensureReady(on controller: UIViewController) -> Bool {
    if isReady { return true }
    let alert = UIAlertController(
      title: "还没有配置渠道",
      message: "这份示范 App 还没有绑定官方演示渠道。\n\n你可以直接填上自己的渠道 ID(在工作台的 App 渠道页复制),不用改一行代码就能连到你自己的工作台。",
      preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "去填渠道 ID", style: .default) { _ in
      showSettings(on: controller)
    })
    alert.addAction(UIAlertAction(title: "取消", style: .cancel))
    controller.present(alert, animated: true)
    return false
  }

  /// 填自己的渠道 ID;清空则回到默认档
  static func showSettings(on controller: UIViewController) {
    let alert = UIAlertController(
      title: "渠道配置",
      message: "当前:\(DemoConfig.describeProfile())\n粘贴你的渠道 ID(在工作台的 App 渠道页复制)",
      preferredStyle: .alert)
    alert.addTextField { $0.text = DemoConfig.customChannelId }
    alert.addAction(UIAlertAction(title: "保存", style: .default) { _ in
      DemoConfig.setCustomChannelId(alert.textFields?.first?.text)
      DemoStyle.alert(on: controller, message: "已保存,当前:\(DemoConfig.describeProfile())")
    })
    alert.addAction(UIAlertAction(title: "清空恢复默认", style: .destructive) { _ in
      DemoConfig.setCustomChannelId(nil)
      DemoStyle.alert(on: controller, message: "已恢复默认:\(DemoConfig.describeProfile())")
    })
    alert.addAction(UIAlertAction(title: "取消", style: .cancel))
    controller.present(alert, animated: true)
  }
}
