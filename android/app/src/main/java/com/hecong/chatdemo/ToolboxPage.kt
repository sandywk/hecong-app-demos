// Tab ④「配置与诊断」—— 排查工具,不属于接入梯度的任何一层。
//
// 租户报障时先看本页:当前连的是哪个渠道、访客标识是否已建立、SDK 实际发出了哪些会话事件。
package com.hecong.chatdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent

fun toolboxPage(activity: Activity): ScenePage {
  lateinit var page: ScenePage
  page = ScenePage(activity, "配置与诊断") {
    listOf(
      DemoSceneGroup(
        "渠道与环境",
        "本示范 App 提供三档渠道来源:填入的自有渠道、内部联调渠道(仅调试构建)、官方演示渠道。填入自有渠道 ID 后优先生效,无需修改代码。",
        listOf(
          DemoScene(
            "渠道配置", "填入自有渠道 ID,即可连接到自己的工作台",
            R.drawable.ic_radio,
            control = SceneControl.Value { DemoConfig.describeProfile(activity) },
            handler = { ChannelSetup.showSettings(it) },
          ),
          DemoScene(
            "诊断信息", "当前渠道来源、渠道 ID、访客标识、示范 App 版本",
            R.drawable.ic_info,
            handler = { it.startActivity(Intent(it, DiagnosticsActivity::class.java)) },
          ),
        ),
      ),
      DemoSceneGroup(
        "会话事件",
        "事件名与网页版 hc.on 完全一致。接入时建议实现通吃回调 onEvent —— 聊天页后续新增的事件无需升级原生包即可收到。",
        listOf(
          DemoScene(
            "会话事件流水", "消息到达 / 对话起止 / 网络通断 —— 按时间倒序记录最近 60 条",
            R.drawable.ic_scroll_text,
            control = SceneControl.Value { "${DemoEventLog.snapshot().size} 条" },
            handler = { host ->
              val lines = DemoEventLog.snapshot()
              val dialog = AlertDialog.Builder(host)
                .setTitle("会话事件流水")
                .setMessage(
                  if (lines.isEmpty()) {
                    "暂无事件记录。\n\n进入客服发送一条消息并等待回复后再返回本页,可依次观察到 conversation:start、message、message:incoming。"
                  } else {
                    lines.joinToString("\n")
                  },
                )
                .setPositiveButton("关闭", null)
              // 流水弹窗自带「清空」—— 复现问题前清空,便于对照本次操作产生的事件
              if (lines.isNotEmpty()) {
                dialog.setNegativeButton("清空") { _, _ -> DemoEventLog.clear(); page.refresh() }
              }
              dialog.show()
            },
          ),
        ),
      ),
    )
  }
  return page
}
