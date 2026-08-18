// 深浅色:**你的 App 说了算,聊天页自动跟随** —— 这是 App 接客服的默认行为,**你不用写代码**。
//
// 本文件从头到尾**没有一行是在联动聊天页**:它只切换这个示范 App 自己的外观
// (`window.overrideUserInterfaceStyle`)。聊天页跟着变,是 SDK 的默认档
// (`config.colorScheme = "host"`,读宿主 traitCollection + 变化时自动同步)带来的。
//
// 什么时候才需要写代码:
//   · 想强制聊天页固定某一档(不跟 App)→ 打开时设 `config.colorScheme = "light" / "dark"`;
//   · 想让渠道后台的配置说了算 → 设 `config.colorScheme = "auto"`;
//   · 聊天页已经开着、要临时改档 → `chatViewController.setColorScheme(...)`。
import UIKit

enum DemoTheme {
  private static let key = "hecong.demo.darkMode"

  /// 当前是不是深色。真实接入:换成读你自己 App 的主题设置
  static var isDark: Bool { UserDefaults.standard.bool(forKey: key) }

  /// App 启动时调一次,让界面按上次的选择渲染
  static func apply(to window: UIWindow?) {
    window?.overrideUserInterfaceStyle = isDark ? .dark : .light
  }

  /// 用户拨动开关:**只切自己 App 的外观**。聊天页会自己跟上 —— 这里刻意不写任何联动代码,
  /// 就是为了证明"零代码即跟随"。
  static func setDark(_ dark: Bool, window: UIWindow?) {
    UserDefaults.standard.set(dark, forKey: key)
    apply(to: window)
  }
}
