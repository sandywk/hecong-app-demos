// Application —— **全局接线的标准位置**(代码即文档,注释写给接入方开发者)。
//
// `HecongChat` 是无 UI 的那一层(app-sdk-plan.md §10.1):管身份、未读、上报开关,
// **不打开聊天页也能用**。聊天视图只是它的一种展示形态。
//
// ⚠️ 合规(§7.3 延迟初始化):`configure` **只登记参数,零联网零读存储**,所以放 Application
// 里没问题;真正会联网的是 `load()` / `startUnreadTracking()` —— 那两个要在**用户同意
// 隐私政策之后**才调。本示范为了演示方便直接调,真实接入请挪到同意之后。
package com.hecong.chatdemo

import android.app.Application
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatListener
import com.hecong.chatsdk.HecongMessage
import org.json.JSONObject

class DemoApp : Application() {
  companion object {
    /**
     * 最近一次生效的访客标识(演示用;真实接入应该存到**你自己的后端**,
     * 与推送 token 绑在一起 —— 未登录访客的离线推送就靠这个号对上人,§10.5)。
     */
    @JvmStatic
    var lastAnonymousId: String? = null
      private set
  }

  override fun onCreate() {
    super.onCreate()
    // App 自身的深浅色(用户上次的选择);聊天页会跟着它走,详 DemoTheme
    DemoTheme.applyToApp(this)
    // ① 登记配置(零活动)。渠道 ID 走 demo 的三档逻辑,真实接入就是你自己那一个 ID。
    HecongChat.configure(this, DemoConfig.buildChatConfig(this, mutableMapOf()))

    // ② 全局回调:未读 + 访客标识。**这两件事不需要聊天页开着**。
    HecongChat.listener = object : HecongChatListener {
      override fun onUnreadChanged(count: Int) {
        MainActivity.updateUnread(count)
      }

      // ③ 未登录访客的离线推送靠这个号对上人
      override fun onAnonymousIdChanged(anonymousId: String) {
        lastAnonymousId = anonymousId
        // 真实接入在这里:postToMyBackend(anonymousId, myPushToken)
      }

      // ④ **会话事件通吃入口 —— 优先接这个**。
      //
      // 事件名与网页版 `hc.on(name, ...)` 完全同名(消息到达 / 对话起止 / 网络通断)。
      // 好处:我们以后在 H5 侧新增的事件,**你不用升级 SDK 就能收到** —— 而具名回调
      // (下面 ⑤)每加一个都要等你升级依赖 + 重新发版,APP 的升级链条比网页长得多。
      override fun onEvent(name: String, payload: JSONObject?) {
        DemoEventLog.record(name, payload) // 演示:记进流水,诊断页能看见
        // 真实接入按 name 分流做事,例如:
        //   "conversation:start" -> 埋点"发起了咨询"
        //   "network:offline"    -> 自己页面上显示"连接中断"
      }

      // ⑤ 具名回调(便利糖:有类型、不用解 JSON)。**跟 ④ 二选一,别同一件事处理两遍** ——
      // 这里刻意分工:④ 只记流水给你看,⑤ 演示真正的业务动作。
      override fun onIncomingMessage(message: HecongMessage) {
        // 只有"对方"的消息会到这里(自己发的不会),所以适合做提醒。
        // 真实接入:App 在前台但用户不在客服页时,用它弹一条自己的本地通知 / 震动一下。
        MainActivity.flashIncoming(message.text)
      }
    }

    // ⚠️ 未读跟踪**默认关闭**,这里刻意不开 —— 由「示范目录 → 未读跟踪」场景手动开启,
    // 好让你看清"不开 = 完全不活动"这个默认状态。真实接入若要,在用户同意隐私
    // 政策后调 HecongChat.startUnreadTracking() 即可。
  }
}
