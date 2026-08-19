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

      // ③ **自定义按钮被点了** —— 这就是"点商品入口 → 弹商品列表"的接线处。
      // 真实接入时你在这里做的是:去自己的系统取当前该展示的商品/订单,
      // `setPickerData` 回填,再 `openPicker` 打开。本 demo 数据是写死的假数据,
      // 所以直接打开即可(数据在点按钮之前就已经喂进去了)。
      override fun onActionClick(id: String) {
        when (id) {
          // ⚠️ **数据必须在这一刻给,不能在打开聊天页之前提前给** —— SDK 刻意不缓存
          // 选择器数据(库存/登录态都会变,缓存重放等于把陈旧列表推给下一个会话)。
          // 真实接入把下面这行换成"去你自己的后端取当前该展示的商品"。
          DevCapabilityActions.ACTION_PRODUCT -> {
            HecongChat.setPickerData("product", DemoSampleData.products())
            HecongChat.openPicker("product")
          }
          DevCapabilityActions.ACTION_ORDER -> {
            HecongChat.setPickerData("order", DemoSampleData.orders())
            HecongChat.openPicker("order")
          }
          // 两个位置对比那两个按钮:只弹提示,说明"点了会回到你的代码里"
          else -> MainActivity.toastFromAnywhere("你点了自定义按钮「$id」—— 这里是你的代码")
        }
      }

      /**
       * ④ **聊天页里任何"要往外跳"的动作都先经过这里** —— 客服发的网址、商品卡片的详情链接、
       * 电话号码、邮箱、页面内的跳转,统统先问你一次。
       *
       * 返回 `true` = 你自己处理了(SDK 什么都不做);返回 `false`/不实现 = SDK 用默认方式
       * (网址跳系统浏览器、电话跳拨号、邮箱跳邮件)。
       *
       * 下面这段是**分流范例**:自家商品链接 → 跳自己 APP 的原生页面;其余 → 交给系统。
       */
      override fun onOpenUrl(url: String): Boolean {
        val prefix = DemoSampleData.DEMO_SITE + "/product/"
        if (url.startsWith(prefix)) {
          val productId = url.removePrefix(prefix)
          // 真实接入这里换成你自己的路由跳转,例如 startActivity(ProductDetailActivity…)
          MainActivity.toastFromAnywhere("拦截成功 → 这里跳你 APP 的商品详情页(商品 $productId)")
          return true // 我处理了,SDK 别再管
        }
        return false // 其余交给 SDK 默认处理(网址跳系统浏览器)
      }

      // ⑤ 未登录访客的离线推送靠这个号对上人
      override fun onAnonymousIdChanged(anonymousId: String) {
        lastAnonymousId = anonymousId
        // 真实接入在这里:postToMyBackend(anonymousId, myPushToken)
      }

      // ⑥ **会话事件通吃入口 —— 优先接这个**。
      //
      // 事件名与网页版 `hc.on(name, ...)` 完全同名(消息到达 / 对话起止 / 网络通断)。
      // 好处:我们以后在 H5 侧新增的事件,**你不用升级 SDK 就能收到** —— 而具名回调
      // (下面 ⑦)每加一个都要等你升级依赖 + 重新发版,APP 的升级链条比网页长得多。
      override fun onEvent(name: String, payload: JSONObject?) {
        DemoEventLog.record(name, payload) // 演示:记进流水,诊断页能看见
        // 真实接入按 name 分流做事,例如:
        //   "conversation:start" -> 埋点"发起了咨询"
        //   "network:offline"    -> 自己页面上显示"连接中断"
      }

      // ⑦ 具名回调(便利糖:有类型、不用解 JSON)。**跟 ⑥ 二选一,别同一件事处理两遍** ——
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
