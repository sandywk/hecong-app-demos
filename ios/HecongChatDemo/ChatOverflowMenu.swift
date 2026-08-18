// 聊天页顶栏右侧「…」菜单 —— 两端(iOS / 安卓)**必须一致**的那两项。
//
// ⚠️ 跨语言没法机器强制同步,所以两边各自只有一处定义,互相点名:
//    安卓对应文件 = `native/examples/android-app/.../ChatOverflowMenu.kt`。
//    改动这里的**项数 / 文案 / 顺序 / 行为**,必须同步改那边(owner 2026-08-18 要求两端对齐:
//    租户会问"安卓有这个菜单,iOS 怎么没有")。
//
// 形态按各端习惯来:iOS 用系统 `UIMenu`(iOS 14+),更老的系统退化成 action sheet;
// 安卓用列表弹窗。**对齐的是"有哪些能力、叫什么名字",不是像素。**
//
// 演示用 —— 真实接入时换成你自己的操作(客服评价、订单跳转、举报等)。
import HecongChatSDK
import UIKit

enum ChatOverflowMenu {
  private static let diagnosticsTitle = "诊断信息"
  private static let resetUserTitle = "退出登录并清空会话"

  /// 给聊天页装上右上角「…」。[chat] 既是承载页也是命令收件人。
  static func install(on chat: HecongChatViewController) {
    let icon = DemoIcon.image(DemoIcon.more, size: 16, weight: .semibold)
    if #available(iOS 14.0, *) {
      chat.navigationItem.rightBarButtonItem = UIBarButtonItem(
        image: icon, menu: UIMenu(children: [
          UIAction(title: diagnosticsTitle) { [weak chat] _ in showDiagnostics(from: chat) },
          UIAction(title: resetUserTitle) { [weak chat] _ in chat?.resetUser() },
        ]))
    } else {
      chat.navigationItem.rightBarButtonItem = UIBarButtonItem(
        image: icon, style: .plain, target: LegacyTarget.shared,
        action: #selector(LegacyTarget.present(_:)))
      LegacyTarget.shared.chat = chat
    }
  }

  private static func showDiagnostics(from chat: HecongChatViewController?) {
    chat?.navigationController?.pushViewController(DiagnosticsViewController(), animated: true)
  }

  /// iOS 13 兜底:没有 UIMenu,退化成 action sheet(项与顺序完全相同)
  private final class LegacyTarget: NSObject {
    static let shared = LegacyTarget()
    weak var chat: HecongChatViewController?

    @objc func present(_ sender: Any) {
      guard let chat = chat else { return }
      let sheet = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
      sheet.addAction(UIAlertAction(title: diagnosticsTitle, style: .default) { _ in
        showDiagnostics(from: chat)
      })
      sheet.addAction(UIAlertAction(title: resetUserTitle, style: .default) { _ in
        chat.resetUser()
      })
      sheet.addAction(UIAlertAction(title: "取消", style: .cancel))
      chat.present(sheet, animated: true)
    }
  }
}
