// Tab2「示例」—— 分组场景清单,点进即演示。
//
// **每个场景对应的代码就是该场景的标准答案**:打开方式看 ChatActivity / SheetChatDialog /
// CustomHeaderChatActivity,身份与能力看 CatalogActions。本文件只负责"列清单 + 画界面",
// 是示范工程自己的脚手架,接入时不需要。
package com.hecong.chatdemo

import android.content.Intent
import android.view.View
import android.widget.ScrollView
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.bigTitleHeader
import com.hecong.chatdemo.ui.cardGroup
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.dim
import com.hecong.chatdemo.ui.listRow
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.tone

/** 一个演示场景:标题 + 一句人话说明 + 点击动作 */
private class Scene(val title: String, val desc: String, val onClick: () -> Unit)

private class Group(val title: String, val scenes: List<Scene>)

/** 「示例」页(无状态,构建一次即可) */
fun buildCatalogPage(activity: MainActivity): View = with(activity) {
  val actions = CatalogActions(activity)
  val dev = DevCapabilityActions(activity)

  fun openChat(vararg extras: Pair<String, Any>) {
    if (!ChannelSetup.ensureReady(activity)) return
    val intent = Intent(activity, ChatActivity::class.java)
    extras.forEach { (key, value) ->
      when (value) {
        is String -> intent.putExtra(key, value)
        is Boolean -> intent.putExtra(key, value)
      }
    }
    activity.startActivity(intent)
  }

  val groups = listOf(
    Group("打开方式", listOf(
      Scene("快速接入(推荐)", "一行代码打开,文件选择、权限、返回键全内置") { actions.openTurnkeyChat() },
      Scene("嵌入你自己的页面", "用你的原生导航栏,聊天页不画标题栏") {
        openChat(ChatActivity.EXTRA_NATIVE_BAR to true)
      },
      Scene("自己画标题栏", "左返回 + 客服头像昵称,内容由 SDK 实时给(转接会变)") {
        if (!ChannelSetup.ensureReady(activity)) return@Scene
        activity.startActivity(Intent(activity, CustomHeaderChatActivity::class.java))
      },
      Scene("底部弹层", "半屏卡片承载,下拉或点右上角关闭") {
        if (!ChannelSetup.ensureReady(activity)) return@Scene
        activity.showSheetChat()
      },
      Scene("聊天页自带标题栏", "不用你的导航栏,标题栏由聊天页自己画") {
        openChat(ChatActivity.EXTRA_CHAT_OWN_HEADER to true)
      },
    )),
    Group("身份与推送", listOf(
      Scene("不登录直接进", "不传身份,SDK 自动建立访客(同一台设备连续)") { openChat() },
      Scene("登录后进入", "把会员 ID 传给 SDK 绑定身份") {
        openChat(ChatActivity.EXTRA_USER_ID to DemoConfig.DEMO_USER_ID)
      },
      Scene("登录时先绑身份", "不打开客服也能绑 —— 登录成功那一刻调,之后进客服自动带上") {
        actions.identifyFromFacade()
      },
      Scene("退出登录", "换人 = 干净重来,上一位的会话不会带过去") { actions.resetUser() },
      Scene("访客标识", "没登录的访客,离线推送靠它对上人") { actions.showAnonymousId() },
    )),
    Group("外观", listOf(
      Scene("深色 / 浅色", "在「我的」页拨动开关,聊天页会跟着一起切换") { openChat() },
      Scene("强制深色(不跟 App)", "少数场景才需要:打开时显式指定档位") {
        openChat(ChatActivity.EXTRA_COLOR_SCHEME to "dark")
      },
      Scene("切换聊天页语言", "打开时指定语言,例如英文") {
        openChat(ChatActivity.EXTRA_LANG to "en")
      },
    )),
    Group("客服能力", listOf(
      Scene("未读红点", "有新消息时,底部 Tab 与「我的」页入口一起亮红点") { openChat() },
      Scene("退出聊天页也收未读", "需手动开启:开启后不进聊天页也能收到未读数") {
        actions.startUnreadTracking()
      },
      Scene("相机 / 麦克风权限", "点 + 号选拍摄:先说明用途再申请(国内合规要求)") { openChat() },
      Scene("文件下载与外链", "文件走系统下载,外部链接跳系统浏览器") { openChat() },
      Scene("会话事件流水", "看 SDK 发了哪些事件:消息到达 / 对话起止 / 网络通断") {
        actions.showEventLog()
      },
    )),
    Group("开发者能力(工作台配不出来,要写代码)", listOf(
      Scene("指定技能组 · 打开时", "填组名 → 用它打开客服;老版本 SDK 也认(走地址参数)") {
        dev.openWithSkillGroup()
      },
      Scene("指定技能组 · 聊天中切换", "聊到一半转专业组;留空可清除。新对话生效") {
        dev.switchSkillGroup()
      },
      Scene("商品选择器", "附件面板加「商品」入口 → 点了弹商品列表(数据是你自己系统的)") {
        dev.demoProductPicker()
      },
      Scene("订单选择器", "输入框正上方加「订单」入口 → 售后咨询直接选哪一单") {
        dev.demoOrderPicker()
      },
      Scene("自定义按钮 · 两个位置对比", "附件面板(收着) vs 快捷区(显眼),一眼看出差别") {
        dev.demoBothSlots()
      },
      Scene("撤掉自定义按钮", "演示 unregister;同名再注册 = 覆盖,不会重复") {
        dev.clearActions()
      },
    )),
    Group("配置与诊断", listOf(
      Scene("渠道配置", "填上你自己的渠道 ID,不改代码就能连到你的工作台") {
        actions.showChannelSettings()
      },
      Scene("清除本地缓存", "清完再打开:身份与聊天记录仍在(这是 SDK 的能力)") {
        actions.clearLocalData()
      },
      Scene("诊断信息", "当前配置 / 渠道 / 访客标识") {
        activity.startActivity(Intent(activity, DiagnosticsActivity::class.java))
      },
    )),
  )

  val body = column {
    setBackgroundColor(tone(R.color.bg))
    val side = dim(R.dimen.page_side)
    setPadding(side, px(6), side, px(24))
    groups.forEachIndexed { index, group ->
      add(
        cardGroup(group.title, group.scenes.map { scene ->
          listRow(scene.title, scene.desc, onClick = scene.onClick)
        }),
        if (index == 0) 0 else dim(R.dimen.group_gap),
      )
    }
  }

  column {
    add(bigTitleHeader("示例"))
    addFill(ScrollView(activity).apply { addView(body) })
  }
}
