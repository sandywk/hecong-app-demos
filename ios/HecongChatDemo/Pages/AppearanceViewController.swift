// Tab ③「界面形态」——对应接入文档 L2「换形态」与 L3「完全一致」。
//
// 四档承载形态的定义、顶栏归属与出口规则:`docs/architecture/app-sdk-chat-entry.md §二`。
// 选型只需一次:确定形态后,后续所有入口沿用同一档即可。
import HecongChatSDK
import UIKit

final class AppearanceViewController: SceneListViewController {
  private lazy var intro = DemoIntroCard { [weak self] in
    guard let self = self else { return }
    ChannelSetup.showSettings(on: self)
  }

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "界面形态"
    tableView.tableHeaderView = intro // 首页说明卡:这是什么、怎么用(详 DemoIntroCard)
  }

  override func viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    let width = tableView.bounds.width
    guard width > 0 else { return }
    let before = intro.frame.height
    intro.fitHeight(width: width)
    if intro.frame.height != before { tableView.tableHeaderView = intro } // 重新挂一次让表格吃到新高度
  }

  override func makeGroups() -> [DemoSceneGroup] {
    [
      DemoSceneGroup(
        title: "承载形态",
        footer: "四档形态的区别在于「标题栏由谁绘制」与「退出口在哪里」。标准档与嵌入档由宿主提供返回;弹层档与沉浸档无返回栈,关闭键是唯一出口。",
        scenes: [
          DemoScene(
            title: "标准档 · 宿主导航栏承载", detail: "推荐形态 —— 顶栏即宿主那条,SDK 不绘制标题栏",
            icon: DemoIcon.layout, accent: true,
            handler: { ChatLaunch.standard(from: $0) }),
          DemoScene(
            title: "弹层档 · 系统导航栏", detail: "底部卡片承载,支持上拉全屏与下拉关闭;顶栏形态与宿主其它弹层一致",
            icon: DemoIcon.sheet,
            handler: { ChatLaunch.sheet(from: $0) }),
          DemoScene(
            title: "弹层档 · 渠道标题栏", detail: "卡片内整页交由聊天页绘制,顶栏取工作台配置的渠道模板样式",
            icon: DemoIcon.sheet,
            handler: { ChatLaunch.sheet(from: $0, useChannelHeader: true) }),
          DemoScene(
            title: "沉浸档 · 整页交由聊天页", detail: "适用于品牌感优先的场景;状态栏明暗随聊天页顶栏自动切换",
            icon: DemoIcon.immersive,
            handler: { ChatLaunch.immersive(from: $0) }),
          DemoScene(
            title: "嵌入档 · 宿主自绘标题栏", detail: "聊天视图嵌入宿主页面,客服昵称与头像经身份回调实时下发",
            icon: DemoIcon.embed,
            handler: { ChatLaunch.customHeader(from: $0) }),
        ]),

      DemoSceneGroup(
        title: "标题栏",
        footer: "标题文案缺省为「在线客服」(按系统语言取中英两档)。跟随接待身份为可选项:会话推进过程中昵称会发生变化(渠道身份 → 接待客服 → 转接后再变),默认关闭。",
        scenes: [
          DemoScene(
            title: "自定义标题文案", detail: "config.title —— 覆盖默认的「在线客服」",
            icon: DemoIcon.text,
            handler: { ChatLaunch.push(from: $0, title: "售后咨询") }),
          DemoScene(
            title: "标题跟随接待身份", detail: "config.titleFollowsAgent —— 标题随当前接待客服的昵称变化",
            icon: DemoIcon.member,
            handler: { ChatLaunch.push(from: $0, titleFollowsAgent: true) }),
          DemoScene(
            title: "标题栏配色", detail: "config.headerBackgroundColor / titleColor —— 不设置则跟随系统外观",
            icon: DemoIcon.palette,
            handler: { ChatLaunch.push(from: $0, title: "在线客服", tintedHeader: true) }),
        ]),

      DemoSceneGroup(
        title: "深浅色",
        footer: "colorScheme 缺省为 host:聊天页自动跟随宿主 App 的当前外观,无需编写任何联动代码。仅在需要固定档位或交由工作台配置时才显式设置。",
        scenes: [
          DemoScene(
            title: "宿主 App 深色模式", detail: "切换本 App 的外观 —— 聊天页自动跟随,验证零代码联动",
            icon: DemoIcon.theme,
            control: .toggle(
              isOn: { DemoTheme.isDark },
              onChange: { DemoTheme.setDark($0, window: DemoStyle.keyWindow()) })),
          DemoScene(
            title: "强制深色", detail: "config.colorScheme = \"dark\" —— 不跟随宿主",
            icon: DemoIcon.dark,
            handler: { ChatLaunch.push(from: $0, colorScheme: "dark") }),
          DemoScene(
            title: "强制浅色", detail: "config.colorScheme = \"light\" —— 不跟随宿主",
            icon: DemoIcon.light,
            handler: { ChatLaunch.push(from: $0, colorScheme: "light") }),
          DemoScene(
            title: "交由工作台配置", detail: "config.colorScheme = \"auto\" —— 渠道后台配置生效",
            icon: DemoIcon.settings,
            handler: { ChatLaunch.push(from: $0, colorScheme: "auto") }),
        ]),

      DemoSceneGroup(
        title: "语言",
        footer: "缺省跟随系统语言。显式指定时与工作台的多语言配置协同,详见接入文档「多语言」章节。",
        scenes: [
          DemoScene(
            title: "指定聊天页语言", detail: "extraQuery[\"lang\"] —— 此处以英文为例",
            icon: DemoIcon.language,
            handler: { ChatLaunch.push(from: $0, extraQuery: ["lang": "en"]) }),
        ]),
    ]
  }
}
