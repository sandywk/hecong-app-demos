// 页面外观的统一入口:导航栏 / 列表底色 / 分隔线 / 弹窗。
//
// 两条硬纪律:
//   · 返回键 = **纯 chevron,不带文字**(设计稿落地纪律第 1 条)。iOS 默认返回键会带上一页
//     标题,靠 `backButtonDisplayMode = .minimal` 收成纯箭头。
//   · **聊天页不用大标题** —— 聊天页寸土寸金,顶栏就该是一行紧凑导航栏(设计稿 S02 ①)。
//     大标题只给「我的」/「示例」这种个人中心式的 Tab 根页。
import UIKit

enum DemoStyle {
  /// 列表页共用:导航栏形态 + 页面底色 + 细分隔线。
  /// [largeTitle] = false 用于详情 / 聊天类页面(紧凑一行栏)。
  static func applyPageChrome(to controller: UITableViewController, largeTitle: Bool = true) {
    controller.view.backgroundColor = DemoColor.background
    controller.tableView.backgroundColor = DemoColor.background
    controller.tableView.separatorColor = DemoColor.line
    controller.tableView.separatorInset = UIEdgeInsets(
      top: 0, left: DemoMetric.cardPadding, bottom: 0, right: 0)
    if #available(iOS 14.0, *) { controller.navigationItem.backButtonDisplayMode = .minimal }
    // prefersLargeTitles 是**导航栈级**开关,详情页把它关掉会连累返回后的根页 ——
    // 所以恒开,由每页自己的 largeTitleDisplayMode 决定要不要大标题
    controller.navigationController?.navigationBar.prefersLargeTitles = true
    if !largeTitle { controller.navigationItem.largeTitleDisplayMode = .never }
  }

  /// App 启动时装一次:导航栏与 Tab 栏的全局外观(用 token,不用系统默认灰)。
  ///
  /// ⚠️ **刻意不覆盖 `scrollEdgeAppearance`**(2026-08-18 iOS 26 模拟器实测定位):
  /// 一旦给它设不透明的自定义背景,iOS 26 上**大标题整个不绘制**,顶上只剩一片空白
  /// (红色文字实验证明不是配色问题,是根本没画);iOS 18 上同样的代码正常。
  /// 交还系统之后两个版本都正常,大标题区跟页面同底色 —— 这也是当下 iOS 的原生形态。
  static func installGlobalAppearance() {
    let nav = UINavigationBarAppearance()
    nav.configureWithOpaqueBackground()
    nav.backgroundColor = DemoColor.surface
    nav.shadowColor = .clear // 无描边,靠页面底色分层
    nav.titleTextAttributes = [.foregroundColor: DemoColor.ink, .font: DemoFont.title]
    nav.setBackIndicatorImage(
      DemoIcon.image(DemoIcon.chevronLeft, size: 18, weight: .semibold),
      transitionMaskImage: DemoIcon.image(DemoIcon.chevronLeft, size: 18, weight: .semibold))
    UINavigationBar.appearance().standardAppearance = nav
    UINavigationBar.appearance().tintColor = DemoColor.ink

    // TabBar 同理只设 standard;iOS 26 起系统把 TabBar 画成悬浮胶囊样式,那是系统演进,
    // 原生 App 本就该跟随,不去覆写(README 已注明:外观随系统版本变化,非实现差异)。
    let tab = UITabBarAppearance()
    tab.configureWithOpaqueBackground()
    tab.backgroundColor = DemoColor.surface
    tab.shadowColor = DemoColor.line
    UITabBar.appearance().standardAppearance = tab
    UITabBar.appearance().tintColor = DemoColor.accent
    UITabBar.appearance().unselectedItemTintColor = DemoColor.ink3
  }

  /// 聊天承载页:一行紧凑导航栏(设计稿 S02 ①),不要大标题占掉半屏
  static func applyChatChrome(to chat: UIViewController) {
    chat.navigationItem.largeTitleDisplayMode = .never
    if #available(iOS 14.0, *) { chat.navigationItem.backButtonDisplayMode = .minimal }
  }

  /// 当前主窗口。iOS 13 基线:不能用 iOS 15 才有的 `UIWindowScene.keyWindow`
  /// (SDK 最低支持 13,示范工程跟同一条基线走 —— 否则示范代码本身就编不过老 deployment target)
  static func keyWindow() -> UIWindow? {
    UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
      .flatMap { $0.windows }
      .first { $0.isKeyWindow }
  }

  /// 顶部飘一条轻提示(**演示用**:真实接入请换成你自己的本地通知)。
  ///
  /// 用在"收到客服消息"上 —— 用户可能正在 App 的别的页面,不提醒的话他要等下次
  /// 点进客服才知道有回复。信号源是 `hecongChat(didReceiveIncomingMessage:)`。
  static func toast(_ message: String) {
    guard let window = keyWindow() else { return }

    let label = PaddedLabel()
    label.text = message
    label.font = .systemFont(ofSize: 14, weight: .medium)
    label.textColor = .white
    label.numberOfLines = 2
    label.backgroundColor = UIColor.black.withAlphaComponent(0.82)
    label.layer.cornerRadius = 10
    label.clipsToBounds = true
    label.alpha = 0
    label.translatesAutoresizingMaskIntoConstraints = false
    window.addSubview(label)
    NSLayoutConstraint.activate([
      label.centerXAnchor.constraint(equalTo: window.centerXAnchor),
      label.topAnchor.constraint(equalTo: window.safeAreaLayoutGuide.topAnchor, constant: 12),
      label.widthAnchor.constraint(lessThanOrEqualTo: window.widthAnchor, constant: -32),
    ])

    UIView.animate(withDuration: 0.2) { label.alpha = 1 } completion: { _ in
      UIView.animate(withDuration: 0.25, delay: 2.2) { label.alpha = 0 } completion: { _ in
        label.removeFromSuperview()
      }
    }
  }

  static func alert(on controller: UIViewController, title: String? = nil, message: String) {
    let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "好", style: .default))
    controller.present(alert, animated: true)
  }
}


/// 带内边距的 label(toast 用;系统 UILabel 没有 padding)
private final class PaddedLabel: UILabel {
  private let inset = UIEdgeInsets(top: 10, left: 14, bottom: 10, right: 14)

  override func drawText(in rect: CGRect) {
    super.drawText(in: rect.inset(by: inset))
  }

  override var intrinsicContentSize: CGSize {
    let size = super.intrinsicContentSize
    return CGSize(
      width: size.width + inset.left + inset.right,
      height: size.height + inset.top + inset.bottom)
  }
}
