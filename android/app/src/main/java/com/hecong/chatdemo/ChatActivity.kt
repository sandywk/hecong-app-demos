// 客服承载页 —— **把聊天嵌进自己页面的完整示范**。
//
// 只想最省事的话看「示例 → 打开方式 → 快速接入」:一行 `HecongChatActivity.start(...)`,
// 下面 ④⑤⑥ 三段转发它已内置,本文件的活它全替你干了。
//
// 自己嵌就得把这 6 件事做全:
//   ① 用 Activity context 创建 HecongChatView(文件选择 / 运行时权限都要它)
//   ② load(config) 装载 —— **会联网**,要在用户同意隐私政策之后才调
//   ③ 登录态进入时 identify 绑定会员(ready 前调会排队,ready 后自动补发)
//   ④ onActivityResult 转发   —— 不做:点「发图片」完全没反应
//   ⑤ onRequestPermissionsResult 转发 —— 不做:语音消息的麦克风直接被拒
//   ⑥ onBackPressed 先问 SDK + onDestroy 调 destroy()
//      —— 不问的症状:图片预览开着时点返回,访客被直接踢出整个客服页
package com.hecong.chatdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.navBar
import com.hecong.chatsdk.HecongChatListener
import com.hecong.chatsdk.HecongChatView
import org.json.JSONObject
import java.lang.ref.WeakReference

class ChatActivity : AppCompatActivity() {
  companion object {
    const val EXTRA_USER_ID = "userId"
    const val EXTRA_NATIVE_BAR = "nativeBar"
    const val EXTRA_CHAT_OWN_HEADER = "h5Header"
    const val EXTRA_LANG = "lang"

    /** 演示「强制某一档深浅色」用;不传 = 默认跟随 App */
    const val EXTRA_COLOR_SCHEME = "colorScheme"

    /** 演示用:清单里的命令发给"最近打开的那个实例"(接入时你自己持有实例) */
    var activeChat: WeakReference<HecongChatView>? = null
  }

  private lateinit var chat: HecongChatView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // ① 必须 Activity context
    chat = HecongChatView(this)
    chat.listener = object : HecongChatListener {
      // 未读数 → 你自己的 Tab 角标 / 入口红点
      override fun onUnreadChanged(count: Int) = MainActivity.updateUnread(count)
    }
    activeChat = WeakReference(chat)

    // 承载形态:用你的原生导航栏时,聊天页不画自己的标题栏;反之让它自己画
    val withNativeBar = intent.getBooleanExtra(EXTRA_NATIVE_BAR, false)
    setContentView(
      column {
        if (withNativeBar) {
          add(navBar("在线客服", onBack = { onBackPressed() },
            rightIconRes = R.drawable.ic_ellipsis,
            onRight = { ChatOverflowMenu.show(this@ChatActivity, chat) }))
        }
        addFill(chat)
      },
    )

    // ② 装载(会联网:放在用户同意隐私政策之后)
    val extraQuery = mutableMapOf<String, String>()
    if (intent.getBooleanExtra(EXTRA_CHAT_OWN_HEADER, false)) extraQuery["hh"] = "0"
    intent.getStringExtra(EXTRA_LANG)?.let { extraQuery["lang"] = it }
    val config = DemoConfig.buildChatConfig(this, extraQuery)
    // 不传就用默认档(跟随 App);只有想强制某一档时才显式设
    intent.getStringExtra(EXTRA_COLOR_SCHEME)?.let { config.colorScheme = it }
    chat.load(config)

    // ③ 登录态进入 → 绑定会员;未登录就别调,SDK 自动走匿名访客
    intent.getStringExtra(EXTRA_USER_ID)?.let { userId ->
      chat.identify(userId, JSONObject().put("name", DemoConfig.DEMO_USER_NAME), null)
    }
  }

  // ④ 前后台联动转发(onStop/onStart,**不是 onPause/onResume**)——
  //    进后台主动断连,后端才判得出"访客离线",离线推送才发得出去(桥协议 §三.1)
  override fun onStart() {
    super.onStart()
    chat.onHostStart()
  }

  override fun onStop() {
    chat.onHostStop()
    super.onStop()
  }

  // ⑤ 文件选择结果转发
  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (chat.handleFileChooserResult(requestCode, resultCode, data)) return
    @Suppress("DEPRECATION")
    super.onActivityResult(requestCode, resultCode, data)
  }

  // ⑥ 运行时权限结果转发
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray,
  ) {
    if (chat.handlePermissionsResult(requestCode, permissions, grantResults)) return
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  // ⑦ 返回键先问 SDK:返回 true 说明它关掉了 聊天页里的预览/浮层那一层,你别再退页面
  @Deprecated("Deprecated in Java")
  override fun onBackPressed() {
    if (chat.handleBackPressed()) return
    @Suppress("DEPRECATION")
    super.onBackPressed()
  }

  // ⑧ 销毁:防内存泄漏 + 让聊天页干净收尾
  override fun onDestroy() {
    if (activeChat?.get() === chat) activeChat = null
    chat.destroy()
    super.onDestroy()
  }
}
