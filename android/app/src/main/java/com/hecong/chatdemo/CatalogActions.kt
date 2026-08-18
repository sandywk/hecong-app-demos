// 「示例」页各场景的动作实现 —— 每个方法都是对应场景的**标准答案**,可直接复制。
//
// 分两类,注释里逐条标明:
//   · 接入示范:快速接入、退出登录、收未读、访客标识 —— 接入时照抄;
//   · 演示用:渠道配置弹窗、清除本地缓存 —— 只为让示范工程能换渠道/验证身份不丢,接入时不需要。
package com.hecong.chatdemo

import android.app.AlertDialog
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatActivity
import org.json.JSONObject

class CatalogActions(private val activity: MainActivity) {

  /**
   * **一行代码打开聊天页**,绝大多数接入用这个就够。
   *
   * 它把文件选择、权限申请、返回键三段系统回调都内置了(不接的症状:点发图片没反应、
   * 麦克风权限点了没用、图片预览开着时点返回被直接踢出客服页)。你既不用在清单文件里
   * 声明这个页面,也不用配键盘避让。
   *
   * 想把聊天嵌进自己的页面(自己画顶栏 / 底部弹层)才需要 HecongChatView —— 那时上面
   * 三段转发要自己写,范本见 ChatActivity。
   */
  fun openTurnkeyChat() {
    if (!ChannelSetup.ensureReady(activity)) return
    HecongChatActivity.start(activity, DemoConfig.buildChatConfig(activity))
  }

  /**
   * **不打开客服页就把会员身份告诉 SDK** —— 这才是标准接法:在你自己的**登录成功那一刻**调。
   *
   * 身份会被记住,用户之后什么时候点开客服都自动带上;不用你去挑"什么时候调才不早不晚"。
   * 已经开着客服页时立即生效。
   */
  fun identifyFromFacade() {
    HecongChat.identify(
      DemoConfig.DEMO_USER_ID,
      JSONObject().put("name", DemoConfig.DEMO_USER_NAME),
    )
    // 用弹窗不用 Toast:Toast 装不下两行说明会被系统截断(实测尾巴那句"再打开客服"看不到),
    // 而那句恰恰是这个场景要讲的重点。iOS 侧同款用 alert,两端一致。
    AlertDialog.Builder(activity)
      .setTitle("已绑定会员")
      .setMessage(
        "${DemoConfig.DEMO_USER_ID}\n\n" +
          "注意此刻客服页还没打开 —— 身份已经记住了。\n" +
          "现在再打开客服,客服那边看到的就是这个会员。",
      )
      .setPositiveButton("知道了", null)
      .show()
  }

  /**
   * 退出登录:换人 = 干净重来。上一位的身份、聊天记录、未读一并清掉,不会串到下一个人。
   *
   * **在你自己的退出登录流程里调即可,不需要客服页开着**。
   * ⚠️ 这一步别省 —— 不调的话,下一个在这台设备上登录的人会看到上一个人的聊天记录。
   */
  fun resetUser() {
    HecongChat.resetUser()
    toast("已退出登录 —— 身份与会话都清了,下一个人不会看到上一位的记录")
  }

  /**
   * 会话事件流水(**演示用**):看 SDK 都发了哪些事件、什么时候发。
   *
   * 接入时你不需要记流水 —— 直接在 `onEvent` / `onIncomingMessage` 里做事(接线见 DemoApp ④⑤)。
   */
  fun showEventLog() {
    val lines = DemoEventLog.snapshot()
    AlertDialog.Builder(activity)
      .setTitle("会话事件流水(最近 ${lines.size} 条)")
      .setMessage(
        if (lines.isEmpty()) {
          "还没有事件。\n\n去打开客服发一句话、等客服回一句,再回来看这里 —— " +
            "你会看到 conversation:start、message、message:incoming 依次出现。"
        } else {
          lines.joinToString("\n")
        },
      )
      .setPositiveButton("知道了", null)
      .setNeutralButton("清空") { _, _ -> DemoEventLog.clear() }
      .show()
  }

  /**
   * 退出客服页也能收未读:**需手动开启**,不开则完全不活动。
   *
   * ⚠️ 会联网,所以要在**用户同意隐私政策之后**再调。三道闸门任一不过就完全不活动:
   * 没手动开 / 本地还没有访客标识(从没聊过天)/ App 不在前台。
   */
  fun startUnreadTracking() {
    HecongChat.startUnreadTracking()
    toast("已开启。先聊一次再退出聊天页,客服回消息后底部 Tab 会亮红点")
  }

  /**
   * 访客标识:没登录的访客,离线推送靠它对上人 —— 在 onAnonymousIdChanged 里把它和你的
   * 推送 token 一起报到你自己的后端(接线见 DemoApp ③)。
   *
   * ⚠️ 别假设它等于你传的 deviceId,以回调给你的值为准。
   */
  fun showAnonymousId() {
    val id = DemoApp.lastAnonymousId ?: "(还没有 —— 先打开一次客服)"
    AlertDialog.Builder(activity)
      .setTitle("当前访客标识")
      .setMessage("$id\n\n接入时:在 onAnonymousIdChanged 里把它和你的推送 token 一起报到自己的后端。")
      .setPositiveButton("知道了", null)
      .show()
  }

  /** 演示用(接入时不需要):填自己的渠道 ID,不改代码就能连到你的工作台 */
  fun showChannelSettings() = ChannelSetup.showSettings(activity)

  /** 演示用(接入时不需要):清掉本地缓存,验证"身份与聊天记录仍在"这个能力 */
  fun clearLocalData() {
    // 这三行全部要求系统 WebView 可用 —— 被用户停用时会抛(同 HecongChatView.setupWebView 那族),
    // 不兜住的话这个演示动作会把示范 APP 一起带崩
    runCatching {
      WebStorage.getInstance().deleteAllData()
      CookieManager.getInstance().removeAllCookies(null)
      WebView(activity).apply { clearCache(true); destroy() }
    }.onFailure {
      toast("系统 WebView 不可用,清不了缓存:${it.javaClass.simpleName}")
      return
    }
    toast("已清除本地缓存 —— 再打开客服,身份与聊天记录应该都还在")
  }

  /** 演示用:清单里的命令发给"最近打开的那个聊天页"(接入时你自己持有实例,不需要这样) */
  private fun withLiveChat(okMessage: String, action: (com.hecong.chatsdk.HecongChatView) -> Unit) {
    val live = ChatActivity.activeChat?.get()
    if (live == null) {
      toast("请先从「打开方式」里任选一种打开客服")
      return
    }
    action(live)
    toast(okMessage)
  }

  private fun toast(message: String) =
    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
}
