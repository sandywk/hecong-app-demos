// Tab ①「界面形态」——对应接入文档 L2「换形态」与 L3「完全一致」。首页:租户最先关心"长什么样"。
//
// 四档承载形态的定义、顶栏归属与出口规则:`docs/architecture/app-sdk-chat-entry.md §二`。
// 与 iOS `AppearanceViewController` 逐项对位;平台差异:安卓标准档的标题栏由 SDK 原生绘制
// (安卓没有宿主导航栈),所以「标题栏配色」这组在安卓对标准档生效(iOS 只对弹层档生效)。
package com.hecong.chatdemo

import android.app.Activity
import com.hecong.chatdemo.ui.demoIntroCard

fun appearancePage(activity: Activity): ScenePage = ScenePage(
  activity, "界面形态",
  header = { activity.demoIntroCard { ChannelSetup.showSettings(activity) } },
) {
  listOf(
    DemoSceneGroup(
      "承载形态",
      "四档形态的区别在于「标题栏由谁绘制」与「退出口在哪里」。标准档与嵌入档有返回键;弹层档与沉浸档无返回栈,关闭键是唯一出口。",
      listOf(
        DemoScene(
          "标准档 · SDK 原生标题栏", "推荐形态 —— 一行调用,左返回 + 标题,文件选择、权限申请、返回键行为均已内置",
          R.drawable.ic_copy, accent = true, handler = { ChatLaunch.standard(it) },
        ),
        DemoScene(
          "弹层档 · 系统标题栏", "底部卡片承载,壳画标题 + 关闭键 + 抓手条;上拉全屏 / 下拉关闭",
          R.drawable.ic_panel_bottom, handler = { ChatLaunch.sheet(it) },
        ),
        DemoScene(
          "弹层档 · 渠道标题栏", "卡片内整页交聊天页绘制,顶栏取工作台配置的渠道模板样式",
          R.drawable.ic_panel_bottom, handler = { ChatLaunch.sheetChannelHeader(it) },
        ),
        DemoScene(
          "沉浸档 · 整页交由聊天页", "适用于品牌感优先的场景;状态栏明暗随聊天页顶栏自动切换",
          R.drawable.ic_square, handler = { ChatLaunch.immersive(it) },
        ),
        DemoScene(
          "嵌入档 · 宿主自绘标题栏", "HecongChatFragment 嵌入宿主页面,客服昵称与头像经身份回调实时下发",
          R.drawable.ic_layout_template, handler = { ChatLaunch.customHeader(it) },
        ),
      ),
    ),
    DemoSceneGroup(
      "标题栏",
      "标题文案缺省为「在线客服」(按系统语言取中英两档)。跟随接待身份为可选项:会话推进过程中昵称会发生变化(渠道身份 → 接待客服 → 转接后再变),默认关闭。",
      listOf(
        DemoScene(
          "自定义标题文案", "config.title —— 覆盖默认的「在线客服」",
          R.drawable.ic_type, handler = { ChatLaunch.open(it, title = "售后咨询") },
        ),
        DemoScene(
          "标题跟随接待身份", "config.titleFollowsAgent —— 标题随当前接待客服的昵称变化",
          R.drawable.ic_circle_user, handler = { ChatLaunch.open(it, titleFollowsAgent = true) },
        ),
        DemoScene(
          "标题栏配色", "config.headerBackgroundColor / titleColor —— 不设置则跟随系统外观",
          R.drawable.ic_palette, handler = { ChatLaunch.open(it, title = "在线客服", tintedHeader = true) },
        ),
      ),
    ),
    DemoSceneGroup(
      "深浅色",
      "colorScheme 缺省为 host:聊天页自动跟随宿主 App 的当前外观,无需编写任何联动代码。仅在需要固定档位或交由工作台配置时才显式设置。",
      listOf(
        DemoScene(
          "宿主 App 深色模式", "切换本 App 的外观 —— 聊天页自动跟随,验证零代码联动",
          R.drawable.ic_contrast,
          control = SceneControl.Toggle({ DemoTheme.isDark(activity) }, { DemoTheme.setDark(activity, it) }),
        ),
        DemoScene(
          "强制深色", "config.colorScheme = \"dark\" —— 不跟随宿主",
          R.drawable.ic_moon, handler = { ChatLaunch.open(it, colorScheme = "dark") },
        ),
        DemoScene(
          "强制浅色", "config.colorScheme = \"light\" —— 不跟随宿主",
          R.drawable.ic_sun, handler = { ChatLaunch.open(it, colorScheme = "light") },
        ),
        DemoScene(
          "交由工作台配置", "config.colorScheme = \"auto\" —— 渠道后台配置生效",
          R.drawable.ic_settings, handler = { ChatLaunch.open(it, colorScheme = "auto") },
        ),
      ),
    ),
    DemoSceneGroup(
      "语言",
      "缺省跟随系统语言。显式指定时与工作台的多语言配置协同,详见接入文档「多语言」章节。",
      listOf(
        DemoScene(
          "指定聊天页语言", "extraQuery[\"lang\"] —— 此处以英文为例",
          R.drawable.ic_globe, handler = { ChatLaunch.open(it, extraQuery = mapOf("lang" to "en")) },
        ),
      ),
    ),
  )
}
