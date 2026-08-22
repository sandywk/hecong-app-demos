// 渠道三档配置(一处生效,零密钥零敏感值)。
//
// 接入 = **渠道 ID 一个值**(SDK 自带骨架页 + 静态域加载,零域名):local 档额外把加载地址
// 指到本地(5175),demo/custom 档走 SDK 默认线上地址。
//
// | 档 | 谁用 | 说明 |
// |---|---|---|
// | local | SDK 开发自测 | 内部联调渠道 + 本地插座;**仅 DEBUG 构建存在**(#if DEBUG) |
// | demo  | 发布版默认 | 官方公开演示渠道(占位待建) |
// | custom| 接入方试用 | 「渠道配置」填自己的渠道 ID(UserDefaults 持久化)—— 核心卖点 |
//
// 生效优先级:custom(填了)> local(DEBUG)> demo。诊断页显示当前档。
import Foundation
import HecongChatSDK

enum DemoConfig {
  private static let customKey = "hecong.demo.customChannelId"

  // 官方演示渠道(owner 2026-08-22 提供)—— **发布版默认走这一档**,租户装完点开即可试用。
  // 以后换渠道只改这一行(安卓侧 `DemoConfig.kt` 同款一行,两端要一起换)。
  private static let demoChannelId = "01a02733-32ca-723e-9826-e2417506387c"

  #if DEBUG
  // local 档(仅 DEBUG):内部联调渠道 + 本地插座(PORT=5175 pnpm demo:link + 后端 3024/17108;
  // iOS 模拟器与宿主 Mac 共享网络 localhost 直通)。发布构建这段代码不存在。
  private static let localChannelId: String? = "01a00edc-a820-71d1-bf6a-dd78a494ac79"
  private static let localLoaderUrl: String? = "http://localhost:5175/hecong-link.js"
  #else
  private static let localChannelId: String? = nil
  private static let localLoaderUrl: String? = nil
  #endif

  enum Profile: String { case local, demo, custom }

  static var customChannelId: String? {
    UserDefaults.standard.string(forKey: customKey).flatMap { $0.isEmpty ? nil : $0 }
  }

  /// 设置页写入;传 nil = 清空恢复默认档
  static func setCustomChannelId(_ id: String?) {
    let trimmed = id?.trimmingCharacters(in: .whitespacesAndNewlines)
    if let trimmed = trimmed, !trimmed.isEmpty {
      UserDefaults.standard.set(trimmed, forKey: customKey)
    } else {
      UserDefaults.standard.removeObject(forKey: customKey)
    }
  }

  static var activeProfile: Profile {
    if customChannelId != nil { return .custom }
    if localChannelId != nil { return .local }
    return .demo
  }

  /// 当前档的 SDK 配置;extraQuery 用于场景演示(聊天页自带标题栏 / 指定语言等)。
  /// 深浅色**一行都不用写**:SDK 默认就跟随你 App 当前的外观(colorScheme = "host")。
  static func buildChatConfig(extraQuery: [String: String] = [:]) -> HecongChatConfig {
    let config: HecongChatConfig
    switch activeProfile {
    case .custom: config = HecongChatConfig(channelId: customChannelId!)
    case .local:
      config = HecongChatConfig(channelId: localChannelId!)
      if let loader = localLoaderUrl { config.loaderUrl = loader }
    case .demo: config = HecongChatConfig(channelId: demoChannelId)
    }
    config.extraQuery = extraQuery
    // 演示缩短到下限 30 秒,让「工作台回复 → 入口徽标亮起」更快看到;真实接入保持默认 60 即可。
    // 下限由 SDK 强制(低于 30 会被抬到 30),不必担心租户配出高频请求。
    config.unreadPollInterval = 30
    // 验收用:`-hcBadLoader` 把插座地址指到没人监听的端口,模拟"装载失败"
    // (验证「标记必须等 ready 才划掉」——页面没起来就划掉 = 这次登出永久丢失)
    if ProcessInfo.processInfo.arguments.contains("-hcBadLoader") {
      config.loaderUrl = "http://127.0.0.1:1/hecong-link.js"
    }
    return config
  }

  /// 诊断展示用:渠道 ID(接入只需要这一个值)
  static func describe() -> String { buildChatConfig().channelId }

  /// 诊断展示用:当前用的是哪一档配置,说人话
  static func describeProfile() -> String {
    switch activeProfile {
    case .custom: return "你填的渠道"
    case .local: return "本地联调渠道"
    case .demo: return "官方演示渠道"
    }
  }

  /// 演示用会员身份已迁至 [DemoMemberProfile](可在「身份与会员 → 示范会员资料」中填写并持久化)——
  /// 写死一个假会员只能验证"没报错",验证不了"透传的内容对不对"。
}
