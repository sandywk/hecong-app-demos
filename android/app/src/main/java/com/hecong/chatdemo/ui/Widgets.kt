// 组件层 · 卡片与列表 —— 页面只组装这些件,不自己摆像素。
//
// ⚠️ 本文件是 **demo 脚手架**(界面组件),不是接入 SDK 必需的东西。接入方接自己 APP 时
// 用你们自己的设计体系即可;放这里是为了示范"demo 也该有设计系统",以及让下面的页面
// 代码短到能一眼看懂接入点在哪。
//
// 落地纪律(design/exports/app-demo-04-spec.png 第 4 屏,违反就是返工):
//   · 分组卡片 16 圆角 + 无描边,靠页面底色分层
//   · 行高 ≥48,左图标章 34/圆角 10,右 chevron 用 chevron token 色
//   · 强调色只有一个,只用在选中态 / 图标章 / 我方气泡
//   · 空字段整行隐藏(GONE),不要 setText("") 留空占位
package com.hecong.chatdemo.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.hecong.chatdemo.R

/** 圆形头像章(有图用图片库加载,这里是纯文字占位版) */
fun Context.avatarCircle(text: String, sizeDp: Number, @ColorRes fill: Int = R.color.accent): TextView =
  TextView(this).apply {
    this.text = text
    gravity = Gravity.CENTER
    setTextColor(0xFFFFFFFF.toInt())
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sizeDp.toFloat() / 3.2f)
    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    background = shape(sizeDp.toFloat() / 2f, fill)
    layoutParams = LinearLayout.LayoutParams(px(sizeDp), px(sizeDp))
  }

/** 图标章:34 方章 + 10 圆角;accent=true 时用强调色底 + 强调色图标 */
fun Context.iconChip(@DrawableRes iconRes: Int, accent: Boolean = false): View {
  val box = LinearLayout(this).apply {
    gravity = Gravity.CENTER
    background = shapePx(dim(R.dimen.chip_radius).toFloat(),
      if (accent) R.color.accent_soft else R.color.bg)
    layoutParams = LinearLayout.LayoutParams(dim(R.dimen.chip_size), dim(R.dimen.chip_size))
  }
  box.addView(icon(iconRes, R.dimen.icon_sm, if (accent) R.color.accent else R.color.ink2))
  return box
}

/** 未读徽标:红底白字圆点,0 或负数时返回 null(调用方据此整块不画) */
fun Context.unreadBadge(count: Int): View? {
  if (count <= 0) return null
  return TextView(this).apply {
    text = if (count > 99) "99+" else count.toString()
    gravity = Gravity.CENTER
    setTextColor(0xFFFFFFFF.toInt())
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    background = shape(10, R.color.danger)
    minWidth = px(20)
    setPadding(px(5), px(1), px(5), px(1))
  }
}

/** 卡片内的细分隔线(只在行与行之间,卡片外沿不画 —— 无描边纪律) */
fun Context.hairline(insetStartPx: Int = 0): View = View(this).apply {
  setBackgroundColor(tone(R.color.line))
  layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px(0.5f).coerceAtLeast(1))
    .apply { marginStart = insetStartPx }
}

/**
 * 列表行:[iconRes] 图标章(可空)+ 标题(+ 说明)+ 未读徽标(可空)+ chevron。
 * 空字段整行隐藏,不留空占位。
 */
fun Context.listRow(
  title: String,
  sub: String? = null,
  @DrawableRes iconRes: Int? = null,
  accent: Boolean = false,
  badgeCount: Int = 0,
  chevron: Boolean = true,
  trailing: View? = null,
  onClick: (() -> Unit)? = null,
): View {
  val root = row {
    minimumHeight = dim(R.dimen.row_min_height)
    val pad = dim(R.dimen.card_pad)
    setPadding(pad, px(14), pad, px(14))
  }
  iconRes?.let {
    root.addView(iconChip(it, accent), LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { marginEnd = px(12) })
  }

  val texts = column()
  texts.add(bodyMd(title))
  sub?.takeIf { it.isNotBlank() }?.let { texts.add(caption(it), px(3)) }
  root.addView(texts, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

  trailing?.let {
    root.addView(it, LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { marginStart = px(8) })
  }
  unreadBadge(badgeCount)?.let {
    root.addView(it, LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { marginStart = px(8) })
  }
  if (chevron) {
    root.addView(icon(R.drawable.ic_chevron_right, R.dimen.icon_sm, R.color.chevron),
      LinearLayout.LayoutParams(dim(R.dimen.icon_sm), dim(R.dimen.icon_sm))
        .apply { marginStart = px(6) })
  }
  onClick?.let { handler ->
    root.tapFeedback()
    root.setOnClickListener { handler() }
  }
  return root
}

/** 分组卡片:白底 16 圆角,行间细线,无描边 */
fun Context.card(rows: List<View>): View = column {
  background = shapePx(dim(R.dimen.card_radius).toFloat(), R.color.surface)
  clipToOutline = true
  rows.forEachIndexed { i, r ->
    if (i > 0) add(hairline(dim(R.dimen.card_pad)))
    add(r)
  }
}

/** 带小标题的分组(标题在卡片外上方,Linear 风) */
fun Context.cardGroup(title: String?, rows: List<View>): View = column {
  title?.let {
    add(groupLabel(it).apply { setPadding(px(6), 0, px(6), px(8)) })
  }
  add(card(rows))
}
