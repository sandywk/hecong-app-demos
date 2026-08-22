// Application —— **全局接线的标准位置**(代码即文档,注释写给接入方开发者)。
//
// `HecongChat` 是无 UI 的那一层(app-sdk-plan.md §10.1):管身份、未读、上报开关,
// **不打开聊天页也能用**。聊天视图只是它的一种展示形态。
//
// ⚠️ 合规(§7.3 延迟初始化):`configure` **只登记参数,零联网零读存储**,所以放 Application
// 里没问题;真正会联网的是聊天页装载 / `startUnreadTracking()` —— 那两个要在**用户同意
// 隐私政策之后**才调。本示范为了演示方便直接调,真实接入请挪到同意之后。
package com.hecong.chatdemo

import android.app.Application
import android.content.Context
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatListener
import com.hecong.chatsdk.HecongHeaderIdentity
import com.hecong.chatsdk.HecongMessage
import org.json.JSONObject

class DemoApp : Application() {
  override fun onCreate() {
    super.onCreate()
    // App 自身的深浅色(用户上次的选择);聊天页会跟着它走,详 DemoTheme
    DemoTheme.applyToApp(this)
    // ① 登记配置(零活动)。渠道 ID 走 demo 的三档逻辑,真实接入就是你自己那一个 ID。
    HecongChat.configure(this, DemoConfig.buildChatConfig(this))

    // ② 全局回调:未读 / 自定义按钮 / 外链接管 / 会话事件。**这些都不需要聊天页开着**。
    HecongChat.listener = object : HecongChatListener {
      // 真实接入:更新你自己的角标 / 入口红点。这里演示两处落点 —— 底部 Tab 徽标、客服入口示范行的红点。
      // 不弹提示:角标本身就是反馈(owner 2026-08-21),飘字只会盖住内容。
      override fun onUnreadChanged(count: Int) = MainActivity.updateUnread(count)

      // ③ **自定义按钮被点了** —— 这就是"点商品入口 → 弹商品列表"的接线处。
      // 真实接入时你在这里做的是:去自己的系统取当前该展示的商品/订单,`setPickerData` 回填,再 `openPicker` 打开。
      // ⚠️ **数据必须在这一刻给,不能在打开聊天页之前提前给** —— SDK 刻意不缓存选择器数据
      // (库存/登录态都会变,缓存重放等于把陈旧列表推给下一个会话)。
      override fun onActionClick(id: String) {
        when (id) {
          DevCapabilityActions.ACTION_PRODUCT -> {
            HecongChat.setPickerData("product", DemoSampleData.products())
            HecongChat.openPicker("product")
          }
          DevCapabilityActions.ACTION_ORDER -> {
            HecongChat.setPickerData("order", DemoSampleData.orders())
            HecongChat.openPicker("order")
          }
          else -> MainActivity.toastFromAnywhere("你点了自定义按钮「$id」—— 这里是你的代码")
        }
      }

      /**
       * ④ **聊天页里任何"要往外跳"的动作都先经过这里** —— 客服发的网址、商品卡片的详情链接、
       * 电话号码、邮箱、页面内的跳转,统统先问你一次。
       * 返回 `true` = 你自己处理了(SDK 什么都不做);返回 `false`/不实现 = SDK 用默认方式
       * (网址跳系统浏览器、电话跳拨号、邮箱跳邮件)。下面是**分流范例**:自家商品链接 → 跳自己 APP 的原生页面;其余 → 交给系统。
       */
      override fun onOpenUrl(url: String): Boolean {
        val prefix = DemoSampleData.DEMO_SITE + "/product/"
        if (url.startsWith(prefix)) {
          val productId = url.removePrefix(prefix)
          // 真实接入这里换成你自己的路由跳转,例如 startActivity(ProductDetailActivity…)
          MainActivity.toastFromAnywhere("拦截成功 → 这里跳你 APP 的商品详情页(商品 $productId)")
          return true
        }
        return false
      }

      /**
       * ⑤ 访客标识变化 —— **离线推送的接线点**:没登录的访客,推送就靠这个号对上人。
       * 真实接入在这里:postToMyBackend(anonymousId, myPushToken)。
       * 示范 App 做不了推送,所以这里不做任何事,接法详见接入文档「离线推送」。
       */
      override fun onAnonymousIdChanged(anonymousId: String) {}

      /** ⑥ 嵌入档 · 自绘标题栏要的数据(渠道身份 → 接待客服 → 转接),转给在场的那页 */
      override fun onHeaderIdentityChanged(identity: HecongHeaderIdentity) =
        CustomHeaderChatActivity.renderIdentity(identity)

      // ⑦ **会话事件通吃入口 —— 优先接这个**。事件名与网页版 `hc.on(name, ...)` 完全同名。
      // 好处:我们以后在 H5 侧新增的事件,**你不用升级 SDK 就能收到** —— 而具名回调每加一个都要等你升级依赖 + 重新发版。
      override fun onEvent(name: String, payload: JSONObject?) {
        DemoEventLog.record(name, payload) // 演示:记进流水,诊断页能看见
        // 真实接入按 name 分流做事,例如 "conversation:start" -> 埋点;"network:offline" -> 页面上显示"连接中断"
      }

      // ⑧ 具名回调(便利糖:有类型、不用解 JSON)。**跟 ⑦ 二选一,别同一件事处理两遍** ——
      // 这里刻意分工:⑦ 只记流水给你看,这个演示真正的业务动作。
      override fun onIncomingMessage(message: HecongMessage) {
        // 只有"对方"的消息会到这里(自己发的不会),所以适合做提醒。
        // 真实接入:App 在前台但用户不在客服页时,用它弹一条自己的本地通知 / 震动一下。
        MainActivity.flashIncoming(message.text)
      }
    }

    // ⚠️ 未读跟踪**默认关闭**,SDK 不会自己开。真实接入:按你自己 App 里"消息提醒"开关的
    // 持久化状态决定要不要在启动时开启 —— 下面这行就是这个姿势(示范开关在「身份与会员 → 未读跟踪」)。
    DemoUnreadTracking.restoreIfWanted(this)
  }
}

/** 示范开关的状态(持久化)—— 模拟宿主 App 自己的"消息提醒"设置项。与 iOS `DemoFacadeDelegate` 同款。 */
object DemoUnreadTracking {
  private const val PREFS = "hecong_demo_app"
  private const val KEY = "unreadTrackingOn"

  fun isOn(c: Context): Boolean =
    c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false)

  /** 拨动示范开关:开 = startUnreadTracking(可在聊天页打开之前调,从没聊过天时零请求),关 = stopUnreadTracking */
  fun set(c: Context, on: Boolean) {
    c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply()
    if (on) {
      HecongChat.startUnreadTracking()
      MainActivity.toastFromAnywhere("已开启未读跟踪 —— 进入客服留言后退出,等客服回复")
    } else {
      HecongChat.stopUnreadTracking()
      MainActivity.toastFromAnywhere("已停止未读跟踪")
    }
  }

  /** App 启动时按上次的选择恢复(真实接入:读你自己的设置项) */
  fun restoreIfWanted(c: Context) {
    if (isOn(c)) HecongChat.startUnreadTracking()
  }
}
