// 设计 token 的代码取用层 —— 色值 / 尺寸 / 字阶集中一处,页面只引用不写死。
//
// 事实源:design/exports/app-demo-04-spec.png(第 4 屏「照这套改代码」),与安卓示范同一套值。
// 深浅两档用 UIColor 的动态构造,跟随当前 traitCollection,页面代码零分支。
//
// ⚠️ 本目录是**示范工程自己的脚手架**,不是接入 SDK 必需的东西 —— 接入时换成你自己的设计体系。
import UIKit

enum DemoColor {
  static let accent = solid(0x4F46E5)
  static let danger = solid(0xE5484D)
  static let accentSoft = dynamic(light: 0xEEEDFC, dark: 0x2A2952)
  static let ink = dynamic(light: 0x0F1420, dark: 0xF2F4F8)
  static let ink2 = dynamic(light: 0x5B6577, dark: 0x9AA3B2)
  static let ink3 = dynamic(light: 0x9AA3B2, dark: 0x7B8494)
  static let line = dynamic(light: 0xEDF0F5, dark: 0x24262D)
  static let surface = dynamic(light: 0xFFFFFF, dark: 0x17181D)
  static let background = dynamic(light: 0xF5F7FA, dark: 0x0E0F13)
  static let chevron = dynamic(light: 0xC7CEDA, dark: 0x3A3D46)
  static let skeleton = dynamic(light: 0xE7EBF2, dark: 0x24262D)

  private static func solid(_ hex: Int) -> UIColor { color(hex) }

  private static func dynamic(light: Int, dark: Int) -> UIColor {
    UIColor { $0.userInterfaceStyle == .dark ? color(dark) : color(light) }
  }

  private static func color(_ hex: Int) -> UIColor {
    UIColor(
      red: CGFloat((hex >> 16) & 0xFF) / 255,
      green: CGFloat((hex >> 8) & 0xFF) / 255,
      blue: CGFloat(hex & 0xFF) / 255,
      alpha: 1)
  }
}

enum DemoMetric {
  static let pageSide: CGFloat = 16
  static let cardPadding: CGFloat = 18
  static let cardRadius: CGFloat = 16
  static let chipSize: CGFloat = 34
  static let chipRadius: CGFloat = 10
  static let rowMinHeight: CGFloat = 48
  static let avatarSize: CGFloat = 34
}

enum DemoFont {
  /// 列表主文 16 / medium
  static let body = UIFont.systemFont(ofSize: 16, weight: .medium)
  /// 页标题 17 / semibold
  static let title = UIFont.systemFont(ofSize: 17, weight: .semibold)
  /// 说明文 12
  static let caption = UIFont.systemFont(ofSize: 12)
  /// 分组标题 13 / semibold(列表页每组上方的小标题)
  static let groupTitle = UIFont.systemFont(ofSize: 13, weight: .semibold)
}

enum DemoIcon {
  /// 线性图标统一走 SF Symbols(系统自带,与设计稿的 lucide 同为线性风,且零依赖)
  static func image(_ name: String, size: CGFloat, weight: UIImage.SymbolWeight = .regular) -> UIImage? {
    UIImage(systemName: name, withConfiguration: UIImage.SymbolConfiguration(pointSize: size, weight: weight))
  }

  // Tab 图标(iOS 13 起即有的符号,避免老系统取不到图)
  static let tabIdentity = "person.crop.circle"
  static let tabAppearance = "rectangle.on.rectangle"
  static let tabAdvanced = "slider.horizontal.3"
  static let tabToolbox = "wrench"

  // 场景行图标
  static let channel = "antenna.radiowaves.left.and.right"
  static let member = "person.crop.circle"
  static let visitor = "person"
  static let login = "arrow.right.square"
  static let logout = "arrow.backward.square"
  static let edit = "square.and.pencil"
  static let bell = "bell"
  static let layout = "rectangle.on.rectangle"
  static let sheet = "rectangle.bottomthird.inset.fill"
  static let immersive = "rectangle.fill"
  static let embed = "square.on.square"
  static let text = "pencil"
  static let palette = "paintbrush"
  static let theme = "circle.lefthalf.fill"
  static let dark = "moon"
  static let light = "sun.max"
  static let language = "globe"
  static let route = "arrow.triangle.branch"
  static let product = "bag"
  static let trash = "trash"
  static let info = "info.circle"
  static let log = "list.bullet.rectangle"

  static let order = "shippingbox"
  static let settings = "gearshape"
  static let support = "bubble.left"
  static let chevronRight = "chevron.right"
  static let chevronLeft = "chevron.left"
}
