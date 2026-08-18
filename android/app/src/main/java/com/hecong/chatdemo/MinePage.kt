// Tab1「我的」—— 模拟一个真实 App 的个人中心页:"你的 App 接完就是这个体验"。
//
// **本页有两个真实接入点**:①② 客服入口那一行;③ 深色模式开关(演示"App 换档,聊天页跟随")。
// 头像卡、订单、收货地址是**演示用的假菜单**,接入时不需要,别照抄。
package com.hecong.chatdemo

import android.content.Intent
import android.util.TypedValue
import androidx.appcompat.widget.SwitchCompat
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.avatarCircle
import com.hecong.chatdemo.ui.bigTitleHeader
import com.hecong.chatdemo.ui.bodyMd
import com.hecong.chatdemo.ui.caption
import com.hecong.chatdemo.ui.card
import com.hecong.chatdemo.ui.cardGroup
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.dim
import com.hecong.chatdemo.ui.icon
import com.hecong.chatdemo.ui.listRow
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.row
import com.hecong.chatdemo.ui.shapePx
import com.hecong.chatdemo.ui.tapFeedback
import com.hecong.chatdemo.ui.tone

/** 「我的」页;[view] 挂进容器,[setUnread] 由 SDK 未读回调驱动客服入口的红点。 */
class MinePage(private val activity: MainActivity) {
  private var unread = 0
  private val supportGroup = activity.column()
  val view: View = build()

  /** 未读数变化 → 重画「在线客服」那一行(红点跟着变) */
  fun setUnread(count: Int) {
    unread = count
    supportGroup.removeAllViews()
    supportGroup.add(activity.cardGroup("帮助与支持", listOf(customerServiceRow())))
  }

  /**
   * ① **真实接入点**:带会员身份打开客服。
   *
   * 接入方要抄的就是这一段 —— 打开自己的聊天承载页,把当前登录会员的 ID 传进去,由承载页
   * 调 `identify()` 绑定(见 ChatActivity ③)。未登录场景不传即可:SDK 自动建立匿名访客,
   * 之后再 identify 换人也不会串号。
   */
  private fun openCustomerService() {
    if (!ChannelSetup.ensureReady(activity)) return
    activity.startActivity(
      Intent(activity, ChatActivity::class.java)
        .putExtra(ChatActivity.EXTRA_USER_ID, DemoConfig.DEMO_USER_ID)
        .putExtra(ChatActivity.EXTRA_NATIVE_BAR, true),
    )
  }

  /** ② 客服入口行:红点数字直接来自 SDK 的 onUnreadChanged(接线见 DemoApp ②) */
  private fun customerServiceRow(): View = activity.listRow(
    title = "在线客服",
    sub = "7×24 小时随时为你解答",
    iconRes = R.drawable.ic_message_circle,
    accent = true,
    badgeCount = unread,
    onClick = ::openCustomerService,
  )

  private fun build(): View = with(activity) {
    val body = column {
      setBackgroundColor(tone(R.color.bg))
      val side = dim(R.dimen.page_side)
      setPadding(side, px(6), side, px(24))
      add(profileCard())
      add(
        card(
          listOf(
            listRow("我的订单", iconRes = R.drawable.ic_package, onClick = ::demoHint),
            listRow("收货地址", iconRes = R.drawable.ic_map_pin, onClick = ::demoHint),
            darkModeRow(),
          ),
        ),
        px(14),
      )
      add(supportGroup, dim(R.dimen.group_gap))
    }
    setUnread(0)

    column {
      add(bigTitleHeader("我的"))
      addFill(ScrollView(activity).apply { addView(body) })
    }
  }

  /** 演示用假头像卡(真实接入不需要):示范客服入口与自家会员体系同页共存 */
  private fun profileCard(): View = with(activity) {
    row {
      background = shapePx(dim(R.dimen.card_radius).toFloat(), R.color.surface)
      val pad = dim(R.dimen.card_pad)
      setPadding(pad, px(16), pad, px(16))
      addView(
        avatarCircle("演", 52),
        LinearLayout.LayoutParams(px(52), px(52)).apply { marginEnd = px(14) },
      )
      addView(
        column {
          add(bodyMd(DemoConfig.DEMO_USER_NAME).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
          })
          add(caption("会员 ID · ${DemoConfig.DEMO_USER_ID}"), px(4))
        },
        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
      )
      addView(icon(R.drawable.ic_chevron_right, R.dimen.icon_sm, R.color.chevron))
      tapFeedback()
      setOnClickListener { demoHint() }
    }
  }

  /**
   * ③ **真实接入点**:App 自己的深色模式开关。
   *
   * 拨动时除了 App 自身换档,还会让**已经开着的聊天页立刻跟着换**(见 DemoTheme.setDark);
   * 没开着的聊天页下次打开时从 config 拿到新档位。这就是 App 接客服的标准姿势 ——
   * 宿主主导深浅色,SDK 跟随。
   */
  private fun darkModeRow(): View = activity.listRow(
    title = "深色模式",
    sub = "聊天页会跟着一起切换",
    iconRes = R.drawable.ic_settings,
    chevron = false,
    trailing = SwitchCompat(activity).apply {
      isChecked = DemoTheme.isDark(activity)
      setOnCheckedChangeListener { _, checked -> DemoTheme.setDark(activity, checked) }
    },
  )

  private fun demoHint() {
    Toast.makeText(activity, "演示用的菜单项 —— 模拟你 App 里的普通页面", Toast.LENGTH_SHORT).show()
  }
}
