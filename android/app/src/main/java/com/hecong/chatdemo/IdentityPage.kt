// Tab ②「身份与会员」——对应接入文档 L1「接上业务」。
//
// 本页覆盖:会员身份绑定(identify / resetUser **成对**)与未读跟踪。
// 规划 `app-sdk-chat-entry.md §六` 明确要求 identify 与 resetUser 同框呈现 ——
// 只接一半不会报错,出事时已是客诉。与 iOS `IdentityViewController` 逐项对位。
package com.hecong.chatdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.hecong.chatsdk.HecongChat

fun identityPage(activity: Activity): ScenePage = ScenePage(activity, "身份与会员") {
  val c = activity
  listOf(
    DemoSceneGroup(
      "演示参数",
      "本页所有场景统一使用该会员资料。填入可辨识的值后,可在工作台侧核对昵称、头像与自定义字段是否按预期透传。",
      listOf(
        DemoScene(
          "示范会员资料", "会员 ID / 昵称 / 头像地址 / 自定义字段",
          R.drawable.ic_circle_user,
          control = SceneControl.Value { DemoMemberProfile.userId(c) },
          handler = { it.startActivity(Intent(it, MemberProfileActivity::class.java)) },
        ),
      ),
    ),
    DemoSceneGroup(
      "会员身份绑定",
      "⚠️ identify 与 resetUser 必须成对接入:宿主登出时若未调用 resetUser,下一位在同一台设备上登录的用户将看到上一位的会话记录。resetUser 无需聊天页在场 —— 未在场时记录待兑现,下次进入聊天页即刻生效。若本设备从未绑定过会员,调用它不产生任何动作(匿名标识仅代表本设备,更换无实际意义)。",
      listOf(
        DemoScene(
          "匿名访客接入", "不传身份打开,SDK 自动建立访客标识并在同一设备上持续复用",
          R.drawable.ic_user, handler = { ChatLaunch.open(it) },
        ),
        DemoScene(
          "绑定会员身份 · 打开聊天页时", "打开前调用 identify,适用于入口处即可取到登录态的场景",
          R.drawable.ic_circle_user,
          handler = { ChatLaunch.open(it, userId = DemoMemberProfile.userId(it)) },
        ),
        DemoScene(
          "绑定会员身份 · 登录成功时", "无需聊天页在场;身份被记住并在页面装载后自动重放",
          R.drawable.ic_log_in,
          handler = { host ->
            HecongChat.identify(
              DemoMemberProfile.userId(host), DemoMemberProfile.profileJson(host), DemoMemberProfile.dataJson(host),
            )
            alert(host, "已绑定会员 ${DemoMemberProfile.userId(host)}。\n\n此时聊天页尚未打开;之后任意时刻进入客服都会自动携带该身份。")
          },
        ),
        DemoScene(
          "更新会员资料", "updateUser 为增量更新:未传入的字段保持不变",
          R.drawable.ic_square_pen,
          handler = { host ->
            HecongChat.updateUser(DemoMemberProfile.profileJson(host), DemoMemberProfile.dataJson(host))
            alert(host, "已提交资料更新 —— 可在工作台的客户资料区核对。")
          },
        ),
        DemoScene(
          "会员登出与会话清理", "resetUser 清除会员绑定并更换访客标识,须在宿主登出流程中调用",
          R.drawable.ic_log_out, accent = true,
          handler = { host ->
            HecongChat.resetUser()
            alert(host, "已提交登出。\n\n聊天页在场时立即生效;不在场时于下次进入聊天页时兑现。\n\n之后下一位在本设备上使用的人不会看到上一位的记录。")
          },
        ),
      ),
    ),
    DemoSceneGroup(
      "未读跟踪",
      "未读跟踪默认关闭,需宿主显式开启;开启后会产生定时请求,应置于用户同意隐私政策之后。" +
        "\n\n验证步骤:开启 → 进入客服发送一条消息并退出 → 在工作台回复。聊天页关闭期间由轮询获取(本示范设为 30 秒一次),开启期间由实时连接即时更新;变化时上方入口徽标与底部 Tab 徽标同步刷新。",
      listOf(
        DemoScene(
          "开启未读跟踪", "startUnreadTracking / stopUnreadTracking —— 无需进入聊天页也能收到未读数变化",
          R.drawable.ic_bell,
          control = SceneControl.Toggle({ DemoUnreadTracking.isOn(c) }, { DemoUnreadTracking.set(c, it) }),
        ),
        DemoScene(
          "客服入口示范", "宿主自有入口 · 徽标由未读回调驱动,携带当前会员身份打开",
          R.drawable.ic_message_circle, badge = { HecongChat.unreadCount },
          handler = { ChatLaunch.open(it, title = "在线客服", userId = DemoMemberProfile.userId(it)) },
        ),
      ),
    ),
  )
}

/** 用弹窗不用 Toast:两行以上的说明 Toast 会被截断,而那句恰恰是场景要讲的重点 */
internal fun alert(activity: Activity, message: String, title: String? = null) {
  AlertDialog.Builder(activity).setTitle(title).setMessage(message).setPositiveButton("好", null).show()
}
