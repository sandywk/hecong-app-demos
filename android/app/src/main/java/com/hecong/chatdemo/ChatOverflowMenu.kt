// 聊天页顶栏右侧「…」菜单 —— 两端(安卓 / iOS)**必须一致**的那两项。
//
// ⚠️ 跨语言没法机器强制同步,所以两边各自只有一处定义,互相点名:
//    iOS 对应文件 = `native/examples/ios-app/HecongChatDemo/ChatOverflowMenu.swift`。
//    改动这里的**项数 / 文案 / 顺序 / 行为**,必须同步改那边(owner 2026-08-18 要求两端对齐:
//    租户会问"安卓有这个菜单,iOS 怎么没有")。
//
// 演示用 —— 真实接入时换成你自己的操作(客服评价、订单跳转、举报等)。
package com.hecong.chatdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.hecong.chatsdk.HecongChatView

object ChatOverflowMenu {
  private const val DIAGNOSTICS = "诊断信息"
  private const val RESET_USER = "退出登录并清空会话"

  fun show(activity: Activity, chat: HecongChatView) {
    AlertDialog.Builder(activity)
      .setItems(arrayOf(DIAGNOSTICS, RESET_USER)) { _, which ->
        if (which == 0) {
          activity.startActivity(Intent(activity, DiagnosticsActivity::class.java))
        } else {
          chat.resetUser()
        }
      }
      .show()
  }
}
