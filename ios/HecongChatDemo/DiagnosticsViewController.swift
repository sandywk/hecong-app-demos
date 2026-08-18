// 诊断页(示范工程自己的脚手架,接入时不需要):当前配置 / 渠道 / 访客标识 —— 排查问题先看这页。
import Foundation
import Security
import UIKit

final class DiagnosticsViewController: UITableViewController {
  private var rows: [(String, String)] = []

  init() { super.init(style: .insetGrouped) }
  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "诊断信息"
    DemoStyle.applyPageChrome(to: self, largeTitle: false) // 详情页用紧凑栏
    rows = [
      ("当前配置", DemoConfig.describeProfile()),
      ("渠道 ID", DemoConfig.describe()),
      ("访客标识", readMirrorAnonymousId() ?? "(尚未建立 —— 第一次打开客服后生成)"),
      ("示范 App 版本", appVariantLabel()),
    ]
  }

  /// 读 SDK 存在钥匙串里的访客标识(service/account 与 SDK 内部一致)。
  /// 仅诊断演示用 —— 接入时不需要读它,SDK 自动管理。
  private func readMirrorAnonymousId() -> String? {
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: "com.hecong.chat-sdk",
      kSecAttrAccount as String: "anonymousId",
      kSecReturnData as String: true,
      kSecMatchLimit as String: kSecMatchLimitOne,
    ]
    var result: AnyObject?
    if SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
      let data = result as? Data, let value = String(data: data, encoding: .utf8) {
      return value
    }
    // 壳的 UserDefaults 降级层(Keychain 受限环境,如无签名模拟器构建)
    return UserDefaults.standard.string(forKey: "hecong.chat.anonymousId")
  }

  private func appVariantLabel() -> String {
    #if DEBUG
    return "0.1.0 (debug)"
    #else
    return "0.1.0 (release)"
    #endif
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { rows.count }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = UITableViewCell(style: .subtitle, reuseIdentifier: nil)
    cell.textLabel?.text = rows[indexPath.row].0
    cell.detailTextLabel?.text = rows[indexPath.row].1
    cell.detailTextLabel?.numberOfLines = 0
    cell.selectionStyle = .none
    return cell
  }
}
