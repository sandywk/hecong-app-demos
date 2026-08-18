// 会话事件流水(**演示用**,接入时不需要这个类)。
//
// 存在的意义是让你**亲眼看见** SDK 在什么时候发了什么事件 —— 打开客服、发一句话、
// 等客服回、断个网,再回诊断页看这份流水,就知道每个事件该怎么用了。
// 真实接入时你不需要记流水,而是在收到事件的那一刻**直接做事**(弹通知 / 埋点 / 改 UI)。

import Foundation

final class DemoEventLog {
  static let shared = DemoEventLog()

  /// 只留最近这些条 —— 演示够用,且不会无限占内存
  private let maxEntries = 60
  private var entries: [String] = []
  private let lock = NSLock()
  private let clock: DateFormatter = {
    let f = DateFormatter()
    f.dateFormat = "HH:mm:ss"
    f.locale = Locale(identifier: "en_US_POSIX")
    return f
  }()

  private init() {}

  func record(_ name: String, _ payload: [String: Any]?) {
    let detail = summarize(name, payload)
    lock.lock()
    defer { lock.unlock() }
    entries.insert(
      "\(clock.string(from: Date()))  \(name)\(detail.isEmpty ? "" : "  \(detail)")", at: 0)
    if entries.count > maxEntries { entries.removeLast(entries.count - maxEntries) }
  }

  func snapshot() -> [String] {
    lock.lock()
    defer { lock.unlock() }
    return entries
  }

  func clear() {
    lock.lock()
    defer { lock.unlock() }
    entries.removeAll()
  }

  /// 挑几个关键字段显示,不整包打印(消息正文可能很长,而且流水只是给人看个大概)
  private func summarize(_ name: String, _ payload: [String: Any]?) -> String {
    guard let payload = payload else { return "" }
    if name.hasPrefix("message") {
      let from = payload["from"] as? String ?? ""
      let type = payload["contentType"] as? String ?? ""
      let text = String((payload["text"] as? String ?? "").prefix(20))
      return type == "text" ? "\(from):「\(text)」" : "\(from):[\(type)]"
    }
    if name.hasPrefix("conversation") {
      return String((payload["conversationId"] as? String ?? "").prefix(12))
    }
    return ""
  }
}
