// Tab ②「身份与会员」——对应接入文档 L1「接上业务」。
//
// 本页覆盖:会员身份绑定(identify / resetUser **成对**)与未读跟踪。
// 规划 `app-sdk-chat-entry.md §六` 明确要求 identify 与 resetUser 同框呈现 ——
// 只接一半不会报错,出事时已是客诉。
//
// 刻意不放的(2026-08-21 owner 定调,归接入文档):离线推送的访客标识上报、权限申请时机、
// 隐私政策与初始化时机 —— 这些在示范 App 里只能"弹一段文字",看不到任何效果。
import HecongChatSDK
import UIKit

final class IdentityViewController: SceneListViewController {
  /// 未读数(由 SDK 未读回调驱动,见 DemoTabBarController)
  private var unread = 0

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "身份与会员"
  }

  /// 未读数变化 → 重画客服入口示范那一行的徽标
  func applyUnread(_ count: Int) {
    guard unread != count else { return }
    unread = count
    reloadScenes()
  }

  override func makeGroups() -> [DemoSceneGroup] {
    [
      DemoSceneGroup(
        title: "演示参数",
        footer: "本页所有场景统一使用该会员资料。填入可辨识的值后,可在工作台侧核对昵称、头像与自定义字段是否按预期透传。",
        scenes: [
          DemoScene(
            title: "示范会员资料", detail: "会员 ID / 昵称 / 头像地址 / 自定义字段",
            icon: DemoIcon.member,
            control: .value { DemoMemberProfile.summary() },
            handler: {
              $0.navigationController?.pushViewController(
                MemberProfileViewController(), animated: true)
            }),
        ]),

      DemoSceneGroup(
        title: "会员身份绑定",
        footer: "⚠️ identify 与 resetUser 必须成对接入:宿主登出时若未调用 resetUser,下一位在同一台设备上登录的用户将看到上一位的会话记录。resetUser 无需聊天页在场 —— 未在场时记录待兑现,下次进入聊天页即刻生效。若本设备从未绑定过会员,调用它不产生任何动作(匿名标识仅代表本设备,更换无实际意义)。",
        scenes: [
          DemoScene(
            title: "匿名访客接入", detail: "不传身份打开,SDK 自动建立访客标识并在同一设备上持续复用",
            icon: DemoIcon.visitor,
            handler: { ChatLaunch.push(from: $0) }),
          DemoScene(
            title: "绑定会员身份 · 打开聊天页时", detail: "承载页创建后调用 identify,适用于入口处即可取到登录态的场景",
            icon: DemoIcon.member,
            handler: { ChatLaunch.push(from: $0, userId: DemoMemberProfile.userId) }),
          DemoScene(
            title: "绑定会员身份 · 登录成功时", detail: "无需聊天页在场;身份被记住并在页面装载后自动重放",
            icon: DemoIcon.login,
            handler: { host in
              HecongChat.shared.identify(
                userId: DemoMemberProfile.userId, profile: DemoMemberProfile.profileDictionary(),
                data: DemoMemberProfile.dataDictionary())
              DemoStyle.alert(
                on: host,
                message: "已绑定会员 \(DemoMemberProfile.userId)。\n\n此时聊天页尚未打开;之后任意时刻进入客服都会自动携带该身份。")
            }),
          DemoScene(
            title: "更新会员资料", detail: "updateUser 为增量更新:未传入的字段保持不变",
            icon: DemoIcon.edit,
            handler: { host in
              HecongChat.shared.updateUser(
                profile: DemoMemberProfile.profileDictionary(),
                data: DemoMemberProfile.dataDictionary())
              DemoStyle.alert(on: host, message: "已提交资料更新 —— 可在工作台的客户资料区核对。")
            }),
          DemoScene(
            title: "会员登出与会话清理", detail: "resetUser 清除会员绑定并更换访客标识,须在宿主登出流程中调用",
            icon: DemoIcon.logout, accent: true,
            handler: { host in
              HecongChat.shared.resetUser()
              DemoStyle.alert(
                on: host,
                message: "已提交登出。\n\n聊天页在场时立即生效;不在场时于下次进入聊天页时兑现。"
                  + "\n\n之后下一位在本设备上使用的人不会看到上一位的记录。")
            }),
        ]),

      DemoSceneGroup(
        title: "未读跟踪",
        footer: "未读跟踪默认关闭,需宿主显式开启;开启后会产生定时请求,应置于用户同意隐私政策之后。"
          + "\n\n验证步骤:开启 → 进入客服发送一条消息并退出 → 在工作台回复。聊天页关闭期间由轮询获取(本示范设为 30 秒一次),开启期间由实时连接即时更新;变化时上方入口徽标、底部 Tab 徽标与 App 图标角标同步刷新。",
        scenes: [
          DemoScene(
            title: "开启未读跟踪", detail: "startUnreadTracking / stopUnreadTracking —— 无需进入聊天页也能收到未读数变化",
            icon: DemoIcon.bell,
            control: .toggle(
              isOn: { DemoFacadeDelegate.shared.isUnreadTrackingOn },
              onChange: { DemoFacadeDelegate.shared.setUnreadTracking($0) })),
          DemoScene(
            title: "客服入口示范", detail: "宿主自有入口 · 徽标由未读回调驱动,携带当前会员身份打开",
            icon: DemoIcon.support, badge: { HecongChat.shared.unreadCount },
            handler: {
              ChatLaunch.push(from: $0, title: "在线客服", userId: DemoMemberProfile.userId)
            }),
        ]),
    ]
  }
}
