// 场景示范:**嵌入档 · 自绘标题栏**(设计稿 03)—— 聊天装进你自己的页面,顶栏用你家的。
//
// 承载用 `HecongChatFragment`:文件选择 / 运行时权限 / 返回键三段系统交互 + WebView 销毁
// **全在 SDK 内部**,本页只剩"画顶栏 + 接身份回调"两件真正属于宿主的事。
// (裸 `HecongChatView` 自嵌也是公开 API,但要自己转发三段回调 —— 文档里提,demo 不演示。)
//
// 三件必做:
//   ① 顶栏自己画(左返回 + 头像 + 昵称/签名),Fragment 贴在它下面
//   ② 接 onHeaderIdentityChanged 拿头像/昵称/签名(走全局 HecongChat.listener,范本 DemoApp)——
//      这份数据只有聊天页里有,而且**会变**:会话开始前是渠道身份 → 客服接待后变成客服 → 转接再变一次
//   ③ pending=true 画骨架占位;空字段整行隐藏(GONE),别 setText("") 留空
//      不做的症状:标题栏"先空一下再跳出名字",或某个字段没配时整栏看着歪掉
//
// 返回键:自己画的那颗也走 `onBackPressedDispatcher` —— Fragment 已在里面注册了"H5 有可关层只关一层"
// 的回调,没有可关层时自然落到系统默认 = 退出本页。宿主**不需要**再问 SDK 一次。
// 顶栏右侧**刻意不放关闭按钮**:左边已经有返回箭头,同一个页面两个出口是多余的。
package com.hecong.chatdemo

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.AvatarLoader
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.icon
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.row
import com.hecong.chatdemo.ui.shape
import com.hecong.chatdemo.ui.tapFeedback
import com.hecong.chatdemo.ui.tone
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatFragment
import com.hecong.chatsdk.HecongHeaderIdentity
import java.lang.ref.WeakReference

class CustomHeaderChatActivity : AppCompatActivity() {
  companion object {
    /** 在场的实例(全局 listener 把身份变化转给它,见 DemoApp) */
    private var live: WeakReference<CustomHeaderChatActivity>? = null

    fun renderIdentity(identity: HecongHeaderIdentity) {
      live?.get()?.header?.render(identity)
    }
  }

  private lateinit var header: HeaderBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    live = WeakReference(this)

    // ① 你自己的顶栏 + 下面一个容器
    header = HeaderBar(this, onBack = { onBackPressedDispatcher.onBackPressed() })
    val container = FrameLayout(this).apply { id = ViewGroup.generateViewId() }
    setContentView(
      column {
        add(header.view)
        addFill(container)
      },
    )

    // 就这一行 —— 聊天装进去了(三段系统回调、返回键拦截、WebView 销毁全在 SDK 内部)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(container.id, HecongChatFragment.newInstance(DemoConfig.buildChatConfig(this)))
        .commit()
    }

    // SDK 缓存的那份:页面重建时立刻画对,不用干等下一次变化
    HecongChat.headerIdentity?.let { header.render(it) }
  }

  override fun onDestroy() {
    if (live?.get() === this) live = null
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

  /**
   * 有图就真加载图 —— 真实接入把 AvatarLoader 换成 Glide/Coil。
   *
   * 🔴 **有头像时:灰骨架 → 真头像,中间不插彩色首字**(2026-08-20 owner 走查,iOS 侧同款改动)。
   * 原先是「灰骨架 → 彩底首字 → 真头像」,那个彩色圆只存在几十到几百毫秒,**看着像闪了一下**,
   * 很突兀 —— 占位的意义是"安静地占住位置",不是"先给个别的东西"。
   * ② **确实没有头像(没配 / 加载失败)→ 整个头像不画**,而不是退成彩底首字 ——
   *    彩色圆本身就难看,而且那是**我们替租户编了一个不存在的东西**。
   *    不画时文字直接紧挨返回键,与 SDK 标题栏"空字段整行让位"同一条规矩。
   */
  private fun renderAvatar(pending: Boolean, url: String?, initial: String?) {
    if (pending) {
      avatarSlot.visibility = View.VISIBLE
      swapAvatar(skeletonCircle())
      return
    }
    if (url.isNullOrBlank()) {
      avatarSlot.visibility = View.GONE // 没头像:不画,不编(见上 ②)
      return
    }
    avatarSlot.visibility = View.VISIBLE
    // 有头像:安静占位等真图。**骨架色直接铺在 ImageView 的背景上** ——
    // 先 swap 骨架再 swap 图片是没用的(第二次 swap 立刻把骨架换走,加载期间反而是个空圆)。
    val image = ImageView(context).apply {
      scaleType = ImageView.ScaleType.CENTER_CROP
      background = context.shape(17, R.color.skeleton) // 图没到之前露出来的就是它
      layoutParams = LinearLayout.LayoutParams(avatarSize, avatarSize)
    }
    AvatarLoader.into(
      image, url, avatarSize,
      onFailure = { avatarSlot.visibility = View.GONE }, // 加载失败也不画
    )
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
