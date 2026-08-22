// 打开客服的统一收口 —— **每个方法就是对应承载形态的标准答案**,接入时照抄这一行即可。
//
// 四档承载形态的定义与选型依据:`docs/architecture/app-sdk-chat-entry.md`。
// 与 iOS `ChatLaunch.swift` 逐条对位,两端方法名一一对应,便于文档并排书写。
package com.hecong.chatdemo

import android.app.Activity
import android.content.Intent
import com.hecong.chatdemo.ui.tone
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatActivity
import com.hecong.chatsdk.HecongRouting

object ChatLaunch {
  // MARK: 四档承载形态(门面直调)

  /** ① 标准档:SDK 原生标题栏(左返回 + 标题)承载。iOS 对位:`HecongChat.shared.push(from:config:)`。 */
  fun standard(host: Activity) {
    if (!ChannelSetup.ensureReady(host)) return
    HecongChatActivity.start(host, DemoConfig.buildChatConfig(host))
  }

  /** ② 弹层档 · 系统标题栏:底部卡片,壳画标题 + ✕ + 抓手。iOS 对位:`presentSheet`。 */
  fun sheet(host: Activity) {
    if (!ChannelSetup.ensureReady(host)) return
    HecongChatActivity.startSheet(host, DemoConfig.buildChatConfig(host))
  }

  /** ② 弹层档 · 渠道标题栏:卡片内整页交 H5 画彩色顶栏 + ✕。iOS 对位:`presentSheet(useChannelHeader:)`。 */
  fun sheetChannelHeader(host: Activity) {
    if (!ChannelSetup.ensureReady(host)) return
    HecongChatActivity.startSheet(host, DemoConfig.buildChatConfig(host), useChannelHeader = true)
  }

  /** ④ 沉浸档:整页交由聊天页绘制,状态栏明暗自动跟随。iOS 对位:`presentImmersive`。 */
  fun immersive(host: Activity) {
    if (!ChannelSetup.ensureReady(host)) return
    HecongChatActivity.startImmersive(host, DemoConfig.buildChatConfig(host))
  }

  /** ③ 嵌入档:`HecongChatFragment` 装进宿主自己的页面,标题栏由宿主绘制(范本 CustomHeaderChatActivity)。 */
  fun customHeader(host: Activity) {
    if (!ChannelSetup.ensureReady(host)) return
    host.startActivity(Intent(host, CustomHeaderChatActivity::class.java))
  }

  // MARK: 带参打开(演示各配置项的效果)

  /**
   * 以指定参数打开标准档 —— 演示 `HecongChatConfig` 各字段的实际效果。
   * 真实接入通常只用到其中一两项,不必逐个设置。
   *
   * [userId] 有值 = "打开聊天页时绑定会员":先 `HecongChat.identify` 再打开,页面装载后自动携带。
   */
  fun open(
    host: Activity,
    title: String? = null,
    userId: String? = null,
    colorScheme: String? = null,
    extraQuery: Map<String, String> = emptyMap(),
    routing: HecongRouting? = null,
    titleFollowsAgent: Boolean = false,
    tintedHeader: Boolean = false,
  ) {
    if (!ChannelSetup.ensureReady(host)) return
    val config = DemoConfig.buildChatConfig(host, extraQuery)
    routing?.let { config.routing = it }
    colorScheme?.let { config.colorScheme = it }
    title?.let { config.title = it }
    config.titleFollowsAgent = titleFollowsAgent
    if (tintedHeader) {
      // 标题栏配色:不设 = 跟随系统外观(深浅色自动适配),设了 = 宿主品牌色主导
      config.headerBackgroundColor = host.tone(R.color.accent)
      config.titleColor = 0xFFFFFFFF.toInt()
    }
    userId?.let {
      HecongChat.identify(it, DemoMemberProfile.profileJson(host), DemoMemberProfile.dataJson(host))
    }
    HecongChatActivity.start(host, config)
  }
}
