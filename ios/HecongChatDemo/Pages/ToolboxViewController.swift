// Tab ④「配置与诊断」—— 排查工具,不属于接入梯度的任何一层。
//
// 租户报障时先看本页:当前连的是哪个渠道、访客标识是否已建立、SDK 实际发出了哪些会话事件。
import HecongChatSDK
import UIKit

final class ToolboxViewController: SceneListViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    title = "配置与诊断"
  }

  override func makeGroups() -> [DemoSceneGroup] {
    [
      DemoSceneGroup(
        title: "渠道与环境",
        footer: "本示范 App 提供三档渠道来源:填入的自有渠道、内部联调渠道(仅调试构建)、官方演示渠道。填入自有渠道 ID 后优先生效,无需修改代码。",
        scenes: [
          DemoScene(
            title: "渠道配置", detail: "填入自有渠道 ID,即可连接到自己的工作台",
            icon: DemoIcon.channel,
            control: .value { DemoConfig.describeProfile() },
            handler: { ChannelSetup.showSettings(on: $0) }),
          DemoScene(
            title: "诊断信息", detail: "当前渠道来源、渠道 ID、访客标识、示范 App 版本",
            icon: DemoIcon.info,
            handler: {
              $0.navigationController?.pushViewController(DiagnosticsViewController(), animated: true)
            }),
        ]),

      DemoSceneGroup(
        title: "会话事件",
        footer: "事件名与网页版 hc.on 完全一致。接入时建议实现通吃回调 didReceiveEvent —— 聊天页后续新增的事件无需升级原生包即可收到。",
        scenes: [
          DemoScene(
            title: "会话事件流水", detail: "消息到达 / 对话起止 / 网络通断 —— 按时间倒序记录最近 60 条",
            icon: DemoIcon.log,
            control: .value { "\(DemoEventLog.shared.snapshot().count) 条" },
            handler: { ($0 as? ToolboxViewController)?.showEventLog() }),
        ]),
    ]
  }

  /// 流水弹窗自带「清空」—— 复现问题前清空,便于对照本次操作产生的事件
  private func showEventLog() {
    let lines = DemoEventLog.shared.snapshot()
    let alert = UIAlertController(
      title: "会话事件流水",
      message: lines.isEmpty
        ? "暂无事件记录。\n\n进入客服发送一条消息并等待回复后再返回本页,可依次观察到 conversation:start、message、message:incoming。"
        : lines.joined(separator: "\n"),
      preferredStyle: .alert)
    if !lines.isEmpty {
      alert.addAction(
        UIAlertAction(title: "清空", style: .destructive) { [weak self] _ in
          DemoEventLog.shared.clear()
          self?.reloadScenes()
        })
    }
    alert.addAction(UIAlertAction(title: "关闭", style: .cancel))
    present(alert, animated: true)
  }
}
