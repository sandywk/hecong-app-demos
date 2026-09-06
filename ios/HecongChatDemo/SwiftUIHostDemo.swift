// SwiftUI 宿主档(2026-09-06 补)—— 本示范工程此前**纯 UIKit,SwiftUI 零覆盖**,
// 而真实租户多数是 SwiftUI(客服 App iOS 侧首次接入就是,0.4.0 的 presenter 改造正因它而起)。
// 演示工程测不到的形态,就等于让租户替我们测,这一页把它补上。
//
// 🔴 SwiftUI 租户的真实处境:**按钮回调里没有 self、没有 UIViewController**。
// 所以这一页四个入口全部用「不传 presenter」的重载 —— 每档就一行,与 UIKit 页写法完全相同。
// 这正是本页要证明的事:SDK 在 SwiftUI 里不需要租户自己去爬控制器层级。
//
// ⚠️ 一条**必须知道的行为差异**(单测钉在 `HecongPresenterResolverSwiftUITests`):
// SwiftUI 的 `NavigationStack` / `NavigationView` 都**不是** UIKit 的 UINavigationController,
// 所以「标准档」在纯 SwiftUI 宿主里找不到导航栈,会**退化成全屏弹页 + 系统导航栏 + ✕**
// (功能完整,不崩)。想要"推入自己的导航栏",宿主那一层得是 UIKit 导航栈(混合工程即可)。
import HecongChatSDK
import SwiftUI
import UIKit

/// 纯 SwiftUI 写法的接入页(内容层)。
///
/// ⚠️ 标 `@available(iOS 15+)` 只是**这个演示页**用了 15 才有的 SwiftUI 写法
/// (带标题的 `Section`),**不是 SDK 的下限** —— SDK 本身 iOS 13+,
/// 上面四个打开调用在 13 上一样能用。
@available(iOS 15.0, *)
struct SwiftUIHostDemoView: View {
  /// 用状态回显"打开是否成功",让退化行为对接入者可见,而不是只在文档里写一句
  @State private var lastResult: String?

  /// ⚠️ 长文案必须外提成常量:直接写在 body 里做 `+` 拼接会让 Swift 类型检查器超时
  /// (实测本页首版就是这么编译失败的:"unable to type-check this expression in reasonable time")
  private static let footerText = """
    每档都是一行调用,不需要递控制器 —— SwiftUI 按钮回调里本来就没有 self,SDK 自己找前台最上层页面。

    ⚠️ 标准档例外:SwiftUI 的 NavigationStack / NavigationView 不是 UIKit 导航栈,SDK 推不进去,\
    会退成全屏弹页(有系统导航栏与 ✕,功能不缺)。需要真正推入导航栏的话,宿主那一层要是 UIKit 导航栈。
    """

  var body: some View {
    List {
      Section {
        row("标准档 push", "SwiftUI 宿主下会退化成全屏弹页(见下方说明)") {
          open("标准档") { HecongChat.shared.push(config: DemoConfig.buildChatConfig()) }
        }
        row("弹层档 presentSheet", "底部卡片,SwiftUI 下行为与 UIKit 完全一致") {
          open("弹层档") { HecongChat.shared.presentSheet(config: DemoConfig.buildChatConfig()) }
        }
        row("弹层档(渠道标题栏)", "卡片内整页交聊天页绘制") {
          open("弹层档 H5 头") {
            HecongChat.shared.presentSheet(
              config: DemoConfig.buildChatConfig(), useChannelHeader: true)
          }
        }
        row("沉浸档 presentImmersive", "整页交聊天页绘制") {
          open("沉浸档") { HecongChat.shared.presentImmersive(config: DemoConfig.buildChatConfig()) }
        }
      } header: {
        Text("四档承载形态(SwiftUI 写法)")
      } footer: {
        Text(Self.footerText)
      }

      if let lastResult {
        Section("上次结果") { Text(lastResult).font(.footnote).foregroundColor(.secondary) }
      }
    }
    .navigationTitle("SwiftUI 宿主")
  }

  private func row(_ title: String, _ detail: String, action: @escaping () -> Void) -> some View {
    Button(action: action) {
      VStack(alignment: .leading, spacing: 2) {
        Text(title).foregroundColor(.primary)
        Text(detail).font(.caption).foregroundColor(.secondary)
      }
    }
  }

  /// 统一收口:打开 + 回显结果。返回 nil 说明前台没找到可承载的窗口(SDK 不 crash,见 §0)。
  private func open(_ label: String, _ launch: () -> HecongChatViewController?) {
    // SwiftUI 侧不弹 UIKit alert(那要 presenter),直接回显 —— 演示页够用
    guard ChannelSetup.isReady else {
      lastResult = "\(label):还没配渠道 ID,先去「配置与诊断 → 渠道配置」填"
      return
    }
    if let chat = launch() {
      chat.delegate = DemoFacadeDelegate.shared
      lastResult = "\(label):已打开(presenter 由 SDK 自行解析)"
    } else {
      lastResult = "\(label):未能打开 —— 前台没有可承载的窗口"
    }
  }
}

/// UIKit 侧的入口:把上面的 SwiftUI 页装进宿主导航栈。
/// (示范工程本身是 UIKit 生命周期,所以这里用 UIHostingController 承载 ——
///  这也正是**存量 App 迁移期最常见的混合形态**,顺带覆盖到。)
enum SwiftUIHostDemo {
  static func push(from host: UIViewController) {
    guard #available(iOS 15.0, *) else {
      let alert = UIAlertController(
        title: "需要 iOS 15+",
        message: "这个演示页用了 iOS 15 才有的 SwiftUI 写法。SDK 本身支持 iOS 13+,四档打开调用在老系统上一样可用。",
        preferredStyle: .alert)
      alert.addAction(UIAlertAction(title: "知道了", style: .cancel))
      host.present(alert, animated: true)
      return
    }
    let vc = UIHostingController(rootView: SwiftUIHostDemoView())
    vc.title = "SwiftUI 宿主"
    host.navigationController?.pushViewController(vc, animated: true)
  }
}
