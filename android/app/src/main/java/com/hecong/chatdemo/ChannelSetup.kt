// 渠道配置与"没配好就别硬跑"的兜底 —— 演示工程自己的脚手架,接入时不需要。
//
// 为什么要有兜底:发布给外部下载的这份 APK 走的是**官方演示渠道**,万一那个渠道 ID 还没填,
// 直接打开客服只会得到一个"网络错误"页 —— 对第一次看这个 App 的人来说,这是最糟的第一印象。
// 所以这里拦一道,把"没配"说清楚,并直接给出"填自己的渠道 ID"这条出路。
package com.hecong.chatdemo

import android.app.Activity
import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast

object ChannelSetup {
  /** 渠道 ID 是否已经可用(占位符 = 还没配) */
  fun isReady(activity: Activity): Boolean =
    !DemoConfig.buildChatConfig(activity).channelId.startsWith("TODO_")

  /**
   * 打开客服前先过这一关:配好了返回 true;没配好弹说明 + 引导去填,返回 false。
   * 每个打开客服的入口都要走它,别绕过去。
   */
  fun ensureReady(activity: Activity): Boolean {
    if (isReady(activity)) return true
    AlertDialog.Builder(activity)
      .setTitle("还没有配置渠道")
      .setMessage("这份示范 App 还没有绑定官方演示渠道。\n\n你可以直接填上自己的渠道 ID(在工作台的 App 渠道页复制),不用改一行代码就能连到你自己的工作台。")
      .setPositiveButton("去填渠道 ID") { _, _ -> showSettings(activity) }
      .setNegativeButton("取消", null)
      .show()
    return false
  }

  /** 填自己的渠道 ID;清空则回到默认档 */
  fun showSettings(activity: Activity) {
    val input = EditText(activity).apply {
      hint = "粘贴你的渠道 ID(在工作台的 App 渠道页复制)"
      setText(DemoConfig.customChannelId(activity) ?: "")
    }
    AlertDialog.Builder(activity)
      .setTitle("渠道配置(当前:${DemoConfig.describeProfile(activity)})")
      .setView(input)
      .setPositiveButton("保存") { _, _ ->
        DemoConfig.setCustomChannelId(activity, input.text.toString())
        toast(activity, "已保存,当前:${DemoConfig.describeProfile(activity)}")
      }
      .setNeutralButton("清空恢复默认") { _, _ ->
        DemoConfig.setCustomChannelId(activity, null)
        toast(activity, "已恢复默认:${DemoConfig.describeProfile(activity)}")
      }
      .setNegativeButton("取消", null)
      .show()
  }

  private fun toast(activity: Activity, message: String) =
    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
}
