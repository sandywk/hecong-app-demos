// 设计 token 的代码取用层 —— 色值/尺寸/字阶一律从 res 取,**代码里不写裸数字裸色值**。
//
// 事实源:design/exports/app-demo-04-spec.png(第 4 屏「照这套改代码」)。
// 深浅两档由 res/values 与 res/values-night 一一对应,本层只认名字。
//
// 字重说明:安卓不带可变字重,500/600 统一用 sans-serif-medium,700 用 bold —— 这是
// 系统字体能表达的最接近档位,别为了对齐设计稿数字去打包字体(demo 不该给接入方塞包袱)。
package com.hecong.chatdemo.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.hecong.chatdemo.R
import kotlin.math.roundToInt

/** dp → px(所有布局数值的唯一入口) */
fun Context.px(dp: Number): Int =
  (dp.toFloat() * resources.displayMetrics.density).roundToInt()

/** 取 dimen token(res 里已定义的尺寸)→ px */
fun Context.dim(id: Int): Int = resources.getDimensionPixelSize(id)

/** 取色 token */
fun Context.tone(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

/** 圆角实底形状(卡片 / 图标章 / 徽标共用),半径按 px 给 */
fun Context.shapePx(radiusPx: Float, @ColorRes fill: Int): GradientDrawable =
  GradientDrawable().apply {
    cornerRadius = radiusPx
    setColor(tone(fill))
  }

/** 同上,半径按 dp 给 */
fun Context.shape(radiusDp: Number, @ColorRes fill: Int): GradientDrawable =
  shapePx(px(radiusDp).toFloat(), fill)

/** 顶部两角圆的形状(底部弹层用) */
fun Context.topRoundedShape(radiusDp: Number, @ColorRes fill: Int): GradientDrawable =
  GradientDrawable().apply {
    val r = px(radiusDp).toFloat()
    cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
    setColor(tone(fill))
  }

/**
 * 系统点按反馈(自绘控件也要有触感,否则"像张图片")。
 *
 * 走 foreground 而不是 background —— 卡片自己的圆角白底是 background,用 background 挂
 * 涟漪会把它顶掉(卡片变透明)。API 22 及以下没有 View.foreground,退化为只在无底色时挂。
 */
fun View.tapFeedback() {
  val out = TypedValue()
  context.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true)
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
    foreground = ContextCompat.getDrawable(context, out.resourceId)
  } else if (background == null) {
    setBackgroundResource(out.resourceId)
  }
}

// ── 字阶(设计稿第 4 屏)──────────────────────────────────
private fun Context.text(size: Float, family: String, @ColorRes color: Int, bold: Boolean) =
  TextView(this).apply {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    typeface = Typeface.create(family, if (bold) Typeface.BOLD else Typeface.NORMAL)
    setTextColor(tone(color))
    includeFontPadding = false
  }

/** 大标题 30/700 */
fun Context.titleXl(s: String) = text(30f, "sans-serif", R.color.ink, true).apply { text = s }

/** 页标题 17/600 */
fun Context.titleMd(s: String) = text(17f, "sans-serif-medium", R.color.ink, false).apply { text = s }

/** 列表主文 16/500 */
fun Context.bodyMd(s: String, @ColorRes color: Int = R.color.ink) =
  text(16f, "sans-serif-medium", color, false).apply { text = s }

/** 说明文 12/normal */
fun Context.caption(s: String, @ColorRes color: Int = R.color.ink2) =
  text(12f, "sans-serif", color, false).apply { text = s }

/** 分组标签 11/600(字距略放,Linear 风) */
fun Context.groupLabel(s: String) =
  text(11f, "sans-serif-medium", R.color.ink3, false).apply {
    text = s
    letterSpacing = 0.06f
  }

// ── 图标(全部来自 lucide 生成的 vector,tint 由 token 决定)────
fun Context.icon(@DrawableRes res: Int, sizeDimen: Int, @ColorRes tint: Int): ImageView =
  ImageView(this).apply {
    setImageResource(res)
    imageTintList = android.content.res.ColorStateList.valueOf(tone(tint))
    val s = dim(sizeDimen)
    layoutParams = ViewGroup.LayoutParams(s, s)
  }

/** 竖向线性容器(页面/卡片的默认骨架) */
fun Context.column(build: LinearLayout.() -> Unit = {}): LinearLayout =
  LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; build() }

/** 横向线性容器,默认竖直居中 */
fun Context.row(build: LinearLayout.() -> Unit = {}): LinearLayout =
  LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = android.view.Gravity.CENTER_VERTICAL
    build()
  }

/**
 * 线性布局里追加子 View 的简写:默认宽 match / 高 wrap + 上边距。
 *
 * ⚠️ **子 View 已经自带 LinearLayout.LayoutParams 时一律沿用它**,只补上边距 —— 不能无脑
 * 覆盖成 wrap_content:裸 `View` 的默认 onMeasure 在 wrap_content 下会**吃满可用空间**
 * (安卓经典坑),1px 的分隔线会因此撑满整屏,把页面挤没(2026-08-18 本 demo 实测踩过)。
 */
fun LinearLayout.add(child: View, topMarginPx: Int = 0) {
  val params = child.layoutParams as? LinearLayout.LayoutParams
    ?: LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
    )
  params.topMargin = topMarginPx
  addView(child, params)
}

/** 追加并让子 View 吃掉剩余高度(页面主体区用) */
fun LinearLayout.addFill(child: View) {
  addView(child, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
}
