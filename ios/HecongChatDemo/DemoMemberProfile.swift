// 演示参数 · 示范会员资料(接入时不需要 —— 真实接入取你自己登录体系里的当前用户)。
//
// 为什么要做成可输入并持久化:身份透传的效果**只能在工作台侧核对** —— 客服看到的昵称、
// 头像、自定义字段是不是你传的那份。写死一个假会员就永远只能验证"没报错",验证不了"对不对"。
// 与渠道 ID 同一套模式(`DemoConfig` 的 custom 档,UserDefaults 持久化)。
import Foundation

enum DemoMemberProfile {
  private static let keyUserId = "hecong.demo.member.userId"
  private static let keyName = "hecong.demo.member.name"
  private static let keyAvatar = "hecong.demo.member.avatar"
  private static let keyExtra = "hecong.demo.member.extra"

  private static let keyGeneratedUserId = "hecong.demo.member.generatedUserId"

  /// 缺省会员 ID:**每台设备首次启动生成一次并持久化**(`demo-user-` + 6 位随机 hex)。
  /// 不能写死同一个值 —— 同一渠道下所有装了 demo 的人会被后端合并成同一个会员,互相看到
  /// 对方的记录(官方演示渠道上尤其致命,owner 2026-08-21 指出)。
  /// 同时也示范了"会员 ID 应不可猜测、不用连续数字"这条接入要求。
  static var defaultUserId: String {
    if let existing = read(keyGeneratedUserId) { return existing }
    let suffix = String(format: "%06x", UInt32.random(in: 0...0xFFFFFF))
    let generated = "demo-user-\(suffix)"
    UserDefaults.standard.set(generated, forKey: keyGeneratedUserId)
    return generated
  }
  static let defaultName = "演示会员"

  /// 你在资料页填的值优先;没填就用本机生成的缺省值
  static var userId: String {
    get { read(keyUserId) ?? defaultUserId }
    set { write(keyUserId, newValue) }
  }

  static var name: String {
    get { read(keyName) ?? defaultName }
    set { write(keyName, newValue) }
  }

  /// 头像地址(留空则不传 —— SDK 侧"没有头像就不绘制",不会替你编一个占位图形)
  static var avatarUrl: String {
    get { read(keyAvatar) ?? "" }
    set { write(keyAvatar, newValue) }
  }

  /// 自定义字段:`键=值` 每行一条,透传到工作台的客户资料区
  static var extraFields: String {
    get { read(keyExtra) ?? "" }
    set { write(keyExtra, newValue) }
  }

  /// `identify` / `updateUser` 的 profile 参数(标准字段)
  static func profileDictionary() -> [String: Any] {
    var out: [String: Any] = ["name": name]
    if !avatarUrl.isEmpty { out["avatar"] = avatarUrl }
    return out
  }

  /// `identify` / `updateUser` 的 data 参数(宿主自定义字段);没填返回 nil
  static func dataDictionary() -> [String: Any]? {
    var out: [String: Any] = [:]
    for line in extraFields.split(separator: "\n") {
      // 半角 `=` 与**全角 `＝`** 都认:中文输入法默认打出的是全角,只认半角的话租户填了
      // 却静默不透传,还以为是 SDK 的问题(2026-08-22 安卓端测实测踩到,两端同款)。
      guard let sep = line.firstIndex(where: { $0 == "=" || $0 == "＝" }) else { continue }
      let key = line[line.startIndex..<sep].trimmingCharacters(in: .whitespaces)
      let value = line[line.index(after: sep)...].trimmingCharacters(in: .whitespaces)
      if !key.isEmpty { out[key] = value }
    }
    return out.isEmpty ? nil : out
  }

  /// 一句话摘要(清单状态行用)
  static func summary() -> String { userId }

  /// 恢复缺省值(本机生成的缺省会员 ID 保留,不重新生成)
  static func reset() {
    for key in [keyUserId, keyName, keyAvatar, keyExtra] {
      UserDefaults.standard.removeObject(forKey: key)
    }
  }

  private static func read(_ key: String) -> String? {
    UserDefaults.standard.string(forKey: key).flatMap { $0.isEmpty ? nil : $0 }
  }

  private static func write(_ key: String, _ value: String) {
    let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmed.isEmpty {
      UserDefaults.standard.removeObject(forKey: key)
    } else {
      UserDefaults.standard.set(trimmed, forKey: key)
    }
  }
}
