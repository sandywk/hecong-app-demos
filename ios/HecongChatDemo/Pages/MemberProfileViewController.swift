// 演示台 · 示范会员资料(示范工程自己的脚手架,接入时不需要)。
//
// 填入可辨识的值后,「身份与会员」页的各场景统一使用这份资料 ——
// 便于在工作台侧核对昵称、头像与自定义字段的实际透传结果。
import UIKit

final class MemberProfileViewController: UITableViewController {
  private struct Field {
    let title: String
    let placeholder: String
    let hint: String?
    let read: () -> String
    let write: (String) -> Void
    var multiline: Bool = false
  }

  private let fields: [Field] = [
    Field(
      title: "会员 ID", placeholder: DemoMemberProfile.defaultUserId,
      hint: "对应 identify 的 userId。应使用不可猜测的取值,避免连续数字。",
      read: { DemoMemberProfile.userId }, write: { DemoMemberProfile.userId = $0 }),
    Field(
      title: "昵称", placeholder: DemoMemberProfile.defaultName,
      hint: "对应 profile.name,显示在工作台的客户资料区。",
      read: { DemoMemberProfile.name }, write: { DemoMemberProfile.name = $0 }),
    Field(
      title: "头像地址", placeholder: "https://…",
      hint: "对应 profile.avatar。留空则不传 —— 客服侧不会绘制占位头像。",
      read: { DemoMemberProfile.avatarUrl }, write: { DemoMemberProfile.avatarUrl = $0 }),
    Field(
      title: "自定义字段", placeholder: "level=VIP3",
      hint: "对应 identify 的 data 参数,每行一条「键=值」,透传到工作台的客户资料区。",
      read: { DemoMemberProfile.extraFields }, write: { DemoMemberProfile.extraFields = $0 },
      multiline: true),
  ]

  init() { super.init(style: .insetGrouped) }
  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "示范会员资料"
    DemoStyle.applyPageChrome(to: self, largeTitle: false)
    tableView.register(DemoFieldCell.self, forCellReuseIdentifier: DemoFieldCell.reuseId)
    navigationItem.rightBarButtonItem = UIBarButtonItem(
      title: "恢复默认", style: .plain, target: self, action: #selector(resetAll))
  }

  @objc private func resetAll() {
    view.endEditing(true)
    DemoMemberProfile.reset()
    tableView.reloadData()
    DemoStyle.toast("已恢复默认值")
  }

  override func numberOfSections(in tableView: UITableView) -> Int { fields.count }

  override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
    fields[section].title
  }

  override func tableView(_ tableView: UITableView, titleForFooterInSection section: Int) -> String? {
    fields[section].hint
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { 1 }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath)
    -> UITableViewCell
  {
    let field = fields[indexPath.section]
    // swiftlint:disable:next force_cast
    let cell = tableView.dequeueReusableCell(
      withIdentifier: DemoFieldCell.reuseId, for: indexPath) as! DemoFieldCell
    cell.configure(
      text: field.read(), placeholder: field.placeholder, multiline: field.multiline,
      onChange: field.write)
    return cell
  }
}
