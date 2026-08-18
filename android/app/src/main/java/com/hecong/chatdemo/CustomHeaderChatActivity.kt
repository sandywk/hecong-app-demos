// 场景示范:**自己画标题栏**(设计稿 03)。
//
// 形态 = 左返回 + 头像 + 昵称/签名,下面才是聊天页。很多 App 的顶栏要跟自家设计语言统一,
// 不想用聊天页自带的那条 —— 这份代码就是那种情况的标准答案。
//
// 三件必做:
//   ① 打开时声明"标题栏我自己画",聊天页就不画自己的那条(不声明的症状:上下两条标题栏)
//   ② 接 onHeaderIdentityChanged 拿头像/昵称/签名 —— 这份数据只有聊天页里有,而且**会变**:
//      会话开始前是渠道身份 → 客服接待后变成客服 → 转接再变一次
//   ③ pending=true 画骨架占位;空字段整行隐藏(GONE),别 setText("") 留空
//      不做的症状:标题栏"先空一下再跳出名字",或某个字段没配时整栏看着歪掉
//
// 顶栏右侧**刻意不放关闭按钮**:左边已经有返回箭头,同一个页面两个出口是多余的
// (底部弹层那一档没有返回键,所以那里保留了关闭按钮)。
package com.hecong.chatdemo

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.AvatarLoader
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.avatarCircle
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.icon
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.row
import com.hecong.chatdemo.ui.shape
import com.hecong.chatdemo.ui.tapFeedback
import com.hecong.chatdemo.ui.tone
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatListener
import com.hecong.chatsdk.HecongChatView
import com.hecong.chatsdk.HecongHeaderIdentity

class CustomHeaderChatActivity : AppCompatActivity() {
  private lateinit var chat: HecongChatView
  private lateinit var header: HeaderBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    chat = HecongChatView(this)
    chat.listener = object : HecongChatListener {
      // ② 身份变了就重画顶栏
      override fun onHeaderIdentityChanged(identity: HecongHeaderIdentity) = header.render(identity)
    }
    header = HeaderBar(this, onBack = ::goBack)

    setContentView(
      column {
        add(header.view)
        addFill(chat)
      },
    )

    // ① 声明"标题栏我自己画"(深浅色跟着 App 走,由 DemoConfig 统一带上)
    chat.load(DemoConfig.buildChatConfig(this, mutableMapOf("hh" to "1")))

    // SDK 缓存的那份:页面重建时立刻画对,不用干等下一次变化
    HecongChat.headerIdentity?.let { header.render(it) }
  }

  /** 返回键(自己画的那颗)也要先问 SDK —— 不问的症状:图片预览开着时点返回,直接退出整个客服页 */
  private fun goBack() {
    if (!chat.handleBackPressed()) finish()
  }

  // 三段系统回调转发(嵌进自己页面时必做;用一行代码的快速接入则已内置)
  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (chat.handleFileChooserResult(requestCode, resultCode, data)) return
    @Suppress("DEPRECATION")
    super.onActivityResult(requestCode, resultCode, data)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
  ) {
    @Suppress("UNCHECKED_CAST")
    if (chat.handlePermissionsResult(requestCode, permissions as Array<String>, grantResults)) return
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  @Deprecated("Deprecated in Java")
  override fun onBackPressed() {
    if (chat.handleBackPressed()) return
    @Suppress("DEPRECATION")
    super.onBackPressed()
  }

  override fun onDestroy() {
    chat.destroy()
    super.onDestroy()
  }
}

/**
 * 自己画的标题栏:视图 + 三态渲染。抽成独立类是为了让上面的 Activity 只剩"接线",
 * 一眼看清接入点在哪。
 */
private class HeaderBar(private val context: AppCompatActivity, onBack: () -> Unit) {
  private val avatarSize = context.px(34)
  private val avatarSlot = LinearLayout(context).apply { gravity = Gravity.CENTER }
  private val nameLabel = label(15f, bold = true, color = R.color.ink)
  private val signLabel = label(11f, bold = false, color = R.color.ink2)
  private val skeleton = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    visibility = View.GONE
    addView(skeletonBar(11), LinearLayout.LayoutParams(context.px(96), context.px(11)))
    addView(
      skeletonBar(9),
      LinearLayout.LayoutParams(context.px(62), context.px(9)).apply { topMargin = context.px(6) },
    )
  }

  val view: View = build(onBack)

  /** ③ 三态:pending 画骨架;有值才显示,空字段整行让位(与聊天页的条件渲染同款) */
  fun render(identity: HecongHeaderIdentity) {
    val pending = identity.pending
    skeleton.visibility = if (pending) View.VISIBLE else View.GONE
    show(nameLabel, if (pending) null else identity.nickname)
    show(signLabel, if (pending) null else identity.signature)
    renderAvatar(pending, identity.avatar, identity.nickname?.take(1))
  }

  /** 有图就真加载图,加载中/失败退回文字首字 —— 真实接入把 AvatarLoader 换成 Glide/Coil */
  private fun renderAvatar(pending: Boolean, url: String?, initial: String?) {
    if (pending) {
      swapAvatar(skeletonCircle())
      return
    }
    swapAvatar(context.avatarCircle(initial ?: "客", 34))
    if (url.isNullOrBlank()) return
    val image = ImageView(context).apply {
      scaleType = ImageView.ScaleType.CENTER_CROP
      layoutParams = LinearLayout.LayoutParams(avatarSize, avatarSize)
    }
    AvatarLoader.into(image, url, avatarSize, onFailure = { /* 保留文字首字兜底 */ })
    swapAvatar(image)
  }

  private fun swapAvatar(child: View) {
    avatarSlot.removeAllViews()
    avatarSlot.addView(child)
  }

  private fun build(onBack: () -> Unit): View {
    val bar = context.row {
      setPadding(context.px(6), context.px(4), context.px(14), context.px(10))
      addView(backButton(onBack))
      addView(
        avatarSlot,
        LinearLayout.LayoutParams(avatarSize, avatarSize).apply { marginEnd = context.px(10) },
      )
      addView(
        context.column {
          addView(skeleton)
          addView(nameLabel)
          addView(signLabel)
        },
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
      )
    }
    return context.column {
      setBackgroundColor(context.tone(R.color.surface))
      add(bar)
    }
  }

  private fun backButton(action: () -> Unit) = LinearLayout(context).apply {
    gravity = Gravity.CENTER
    layoutParams = LinearLayout.LayoutParams(context.px(40), context.px(40))
    addView(context.icon(R.drawable.ic_chevron_left, R.dimen.icon_xl, R.color.ink))
    tapFeedback()
    setOnClickListener { action() }
  }

  private fun label(sizeSp: Float, bold: Boolean, color: Int) = TextView(context).apply {
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sizeSp)
    typeface = android.graphics.Typeface.create(
      if (bold) "sans-serif-medium" else "sans-serif", android.graphics.Typeface.NORMAL,
    )
    setTextColor(context.tone(color))
    includeFontPadding = false
  }

  private fun skeletonBar(heightDp: Int) = View(context).apply {
    background = context.shape(heightDp / 2f, R.color.skeleton)
  }

  private fun skeletonCircle() = View(context).apply {
    background = context.shape(17, R.color.skeleton)
    layoutParams = LinearLayout.LayoutParams(avatarSize, avatarSize)
  }

  private fun show(label: TextView, text: String?) {
    label.text = text ?: ""
    label.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
  }
}
