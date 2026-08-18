// 渠道三档配置(一处生效,零密钥零敏感值)。
//
// 装载形态 2026-08-17 改版后,接入 = **渠道 ID 一个值**(SDK 自带骨架页 + 静态域加载,
// 零域名):local 档额外把加载地址指到本地(5177),demo/custom 档走 SDK 默认线上地址。
//
// | 档 | 谁用 | 说明 |
// |---|---|---|
// | local | SDK 开发自测 | 内部联调渠道 + 本地插座;**仅 DEBUG 构建存在**(src/debug 源集,
// |       |             | release 是空实现,发布构建编译期不含内部渠道 ID) |
// | demo  | 发布版默认 | 官方公开演示渠道(占位待建) |
// | custom| 接入方试用 | 设置页填自己的渠道 ID(本地持久化)→ 零代码连自己渠道 —— 核心卖点 |
//
// 生效优先级:custom(填了)> local(DEBUG)> demo。诊断页显示当前档。
package com.hecong.chatdemo

import android.content.Context
import com.hecong.chatsdk.HecongChatConfig

object DemoConfig {
  private const val PREFS = "hecong_demo_app"
  private const val KEY_CUSTOM_CHANNEL = "customChannelId"

  // TODO(owner 建好官方演示渠道后填)
  private const val DEMO_CHANNEL_ID = "TODO_OFFICIAL_DEMO_CHANNEL_ID"

  enum class Profile { LOCAL, DEMO, CUSTOM }

  fun customChannelId(context: Context): String? =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .getString(KEY_CUSTOM_CHANNEL, null)?.takeIf { it.isNotBlank() }

  /** 设置页写入;传 null = 清空恢复默认档 */
  fun setCustomChannelId(context: Context, channelId: String?) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
      .apply {
        if (channelId.isNullOrBlank()) remove(KEY_CUSTOM_CHANNEL)
        else putString(KEY_CUSTOM_CHANNEL, channelId.trim())
      }
      .apply()
  }

  fun activeProfile(context: Context): Profile = when {
    customChannelId(context) != null -> Profile.CUSTOM
    LocalEnv.channelIdOrNull != null -> Profile.LOCAL
    else -> Profile.DEMO
  }

  /** 当前档的 SDK 配置;extraQuery 用于场景演示(聊天页自带标题栏 / 指定语言等) */
  fun buildChatConfig(context: Context, extraQuery: Map<String, String> = emptyMap()): HecongChatConfig {
    val config = when (activeProfile(context)) {
      Profile.CUSTOM -> HecongChatConfig(customChannelId(context)!!)
      Profile.LOCAL -> HecongChatConfig(LocalEnv.channelIdOrNull!!).apply {
        LocalEnv.loaderUrlOrNull?.let { loaderUrl = it }
      }
      Profile.DEMO -> HecongChatConfig(DEMO_CHANNEL_ID)
    }
    // 深浅色**一行都不用写**:SDK 默认就跟随你 APP 当前的深浅色(config.colorScheme = "host")。
    // 想强制某一档、或让渠道后台说了算,才需要显式设 colorScheme —— 详 DemoTheme。
    config.extraQuery.putAll(extraQuery)
    return config
  }

  /** 诊断展示用:渠道 ID(接入只需要这一个值) */
  fun describe(context: Context): String = buildChatConfig(context).channelId

  /** 诊断展示用:当前用的是哪一档配置,说人话 */
  fun describeProfile(context: Context): String = when (activeProfile(context)) {
    Profile.CUSTOM -> "你填的渠道"
    Profile.LOCAL -> "本地联调渠道"
    Profile.DEMO -> "官方演示渠道"
  }

  /** 演示用会员身份(真实接入 = 你自己登录体系里的用户;ID 要"不可猜",详接入文档) */
  const val DEMO_USER_ID = "demo-user-8f3a2c"
  const val DEMO_USER_NAME = "演示会员"
}
