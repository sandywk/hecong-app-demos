// 演示清单的骨架层:场景模型 + 列表基类。
//
// 五个能力页(快速接入 / 身份与会员 / 界面形态 / 高级扩展 / 配置与诊断)全部继承
// [SceneListViewController],各自只声明"本页有哪些分组、哪些场景",不重复写表格代码。
//
// ⚠️ 本文件是**示范工程自己的脚手架**,接入 SDK 时不需要。
import UIKit

/// 场景右侧的控件形态
enum DemoSceneControl {
  /// 常规行:右侧 chevron,点击执行动作
  case navigate
  /// 状态行:右侧显示一段实时取值的文字(如当前渠道档位、当前会员 ID)
  case value(() -> String)
  /// 开关行:右侧 UISwitch(如宿主深浅色)
  case toggle(isOn: () -> Bool, onChange: (Bool) -> Void)
}

/// 一条演示场景:标题 + 一句能力说明 + 点击动作
struct DemoScene {
  let title: String
  let detail: String
  var icon: String?
  var accent: Bool = false
  /// 右侧未读徽标的取值(返回 0 = 不画)
  var badge: (() -> Int)?
  var control: DemoSceneControl = .navigate
  var handler: (SceneListViewController) -> Void = { _ in }
}

/// 一组场景:分组标题 + 可选的组尾说明(放接入注意事项,不放到每一行里堆)
struct DemoSceneGroup {
  let title: String
  var footer: String?
  let scenes: [DemoScene]
}

/// 能力页基类:数据驱动的分组列表。子类只覆写 [makeGroups]。
class SceneListViewController: UITableViewController {
  private var groups: [DemoSceneGroup] = []

  init() { super.init(style: .insetGrouped) }
  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  /// 子类覆写:声明本页的分组与场景
  func makeGroups() -> [DemoSceneGroup] { [] }

  /// 参数变化后重建清单(状态行的取值随之刷新)
  func reloadScenes() {
    groups = makeGroups()
    tableView.reloadData()
  }

  override func viewDidLoad() {
    super.viewDidLoad()
    DemoStyle.applyPageChrome(to: self)
    tableView.register(DemoListCell.self, forCellReuseIdentifier: DemoListCell.reuseId)
    tableView.register(
      DemoSectionTextView.self, forHeaderFooterViewReuseIdentifier: DemoSectionTextView.reuseId)
    tableView.sectionHeaderHeight = UITableView.automaticDimension
    tableView.estimatedSectionHeaderHeight = 36
    tableView.sectionFooterHeight = UITableView.automaticDimension
    tableView.estimatedSectionFooterHeight = 44
    reloadScenes()
  }

  /// 从演示台返回 / 从聊天页返回时刷新状态行;顺带恢复被沉浸档隐藏的导航栏
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(false, animated: animated)
    reloadScenes()
  }

  // MARK: - UITableView

  override func numberOfSections(in tableView: UITableView) -> Int { groups.count }

  // 分组标题 / 页脚自己画:系统默认的 header 比卡片再缩进一截、字号偏小(owner 2026-08-21 走查),
  // 设计稿(app-demo-01)是**与卡片左缘齐平**的小标题。
  override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
    let view = tableView.dequeueReusableHeaderFooterView(withIdentifier: DemoSectionTextView.reuseId)
      as? DemoSectionTextView
    view?.configure(groups[section].title, role: .header)
    return view
  }

  override func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
    guard let footer = groups[section].footer else { return nil }
    let view = tableView.dequeueReusableHeaderFooterView(withIdentifier: DemoSectionTextView.reuseId)
      as? DemoSectionTextView
    view?.configure(footer, role: .footer)
    return view
  }

  override func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
    // 没有页脚的分组只留一段组间距;有页脚交给自适应
    groups[section].footer == nil ? 20 : UITableView.automaticDimension
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    groups[section].scenes.count
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath)
    -> UITableViewCell
  {
    let scene = groups[indexPath.section].scenes[indexPath.row]
    // swiftlint:disable:next force_cast
    let cell = tableView.dequeueReusableCell(
      withIdentifier: DemoListCell.reuseId, for: indexPath) as! DemoListCell
    let badgeCount = scene.badge?() ?? 0
    switch scene.control {
    case .navigate:
      cell.configure(
        title: scene.title, subtitle: scene.detail, icon: scene.icon, accent: scene.accent,
        badgeCount: badgeCount)
      cell.selectionStyle = .default
    case .value(let read):
      let label = UILabel()
      label.text = read()
      label.font = DemoFont.caption
      label.textColor = DemoColor.ink2
      label.sizeToFit()
      cell.configure(
        title: scene.title, subtitle: scene.detail, icon: scene.icon, accent: scene.accent,
        badgeCount: badgeCount, accessory: label)
      cell.selectionStyle = .default
    case .toggle(let isOn, let onChange):
      let toggle = DemoSwitch(isOn: isOn(), onChange: onChange)
      cell.configure(
        title: scene.title, subtitle: scene.detail, icon: scene.icon, accent: scene.accent,
        badgeCount: badgeCount, accessory: toggle)
      cell.selectionStyle = .none
    }
    return cell
  }

  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    tableView.deselectRow(at: indexPath, animated: true)
    let scene = groups[indexPath.section].scenes[indexPath.row]
    if case .toggle = scene.control { return } // 开关行由开关自己响应
    scene.handler(self)
  }
}

/// 带闭包回调的开关(UISwitch 的 addAction 要 iOS 14,SDK 基线是 13,所以自己包一层)
final class DemoSwitch: UISwitch {
  private let onChange: (Bool) -> Void

  init(isOn: Bool, onChange: @escaping (Bool) -> Void) {
    self.onChange = onChange
    super.init(frame: .zero)
    self.isOn = isOn
    onTintColor = DemoColor.accent
    addTarget(self, action: #selector(valueChanged), for: .valueChanged)
  }

  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  @objc private func valueChanged() { onChange(isOn) }
}


/// 分组标题 / 页脚(`.insetGrouped` 下 header 视图本身已与卡片同宽,标签贴边即与卡片左缘齐平)
final class DemoSectionTextView: UITableViewHeaderFooterView {
  static let reuseId = "DemoSectionTextView"
  enum Role { case header, footer }

  private let label = UILabel()
  private var top: NSLayoutConstraint!
  private var bottom: NSLayoutConstraint!

  override init(reuseIdentifier: String?) {
    super.init(reuseIdentifier: reuseIdentifier)
    label.numberOfLines = 0
    contentView.addSubview(label)
    label.translatesAutoresizingMaskIntoConstraints = false
    top = label.topAnchor.constraint(equalTo: contentView.topAnchor)
    bottom = label.bottomAnchor.constraint(equalTo: contentView.bottomAnchor)
    NSLayoutConstraint.activate([
      label.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 2),
      label.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -2),
      top, bottom,
    ])
  }

  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  func configure(_ text: String, role: Role) {
    label.text = text
    switch role {
    case .header:
      label.font = DemoFont.groupTitle
      label.textColor = DemoColor.ink2
      top.constant = 18
      bottom.constant = -8
    case .footer:
      label.font = DemoFont.caption
      label.textColor = DemoColor.ink2
      top.constant = 8
      bottom.constant = -14
    }
  }
}
