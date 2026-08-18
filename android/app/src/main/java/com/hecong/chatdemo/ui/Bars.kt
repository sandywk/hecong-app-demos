// 组件层 · 顶栏与底栏(demo 脚手架,非接入必需)。
//
// 返回键的形态是硬纪律:**纯 chevron 图标,不带灰底按钮、不写"返回"二字**
// (design/exports/app-demo-04-spec.png 落地纪律第 1 条)。系统 ActionBar / 默认 Button
// 都给不了这个形态,所以顶栏一律自绘 —— 这也正好示范了自绘标题栏该长什么样。
package com.hecong.chatdemo.ui

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.hecong.chatdemo.R

// 说明:本 demo 不做沉浸式(edge-to-edge),窗口自身已避开状态栏,所以顶栏**不需要**再补
// 一段状态栏留白 —— 补了会白白多出一条空白(实测踩过)。状态栏底色由主题
// android:statusBarColor 跟 surface 保持一致。

/** 大标题页头(Tab 页用):状态栏留白 + 30/700 大标题,底色同 surface */
fun Context.bigTitleHeader(title: String): View {
  val header = column()
  header.setBackgroundColor(tone(R.color.surface))
  header.add(titleXl(title).apply {
    setPadding(dim(R.dimen.page_side) + px(4), px(10), dim(R.dimen.page_side), px(14))
  })
  return header
}

/**
 * 原生导航栏:左 chevron 返回 + 居中标题 + 可选右操作。
 * [onBack] 为空时不画返回键(根页面)。
 */
fun Context.navBar(
  title: String,
  onBack: (() -> Unit)? = null,
  rightIconRes: Int? = null,
  onRight: (() -> Unit)? = null,
): View {
  val bar = row {
    setPadding(px(6), px(4), px(6), px(10))
  }
  val slot = px(40)

  fun iconButton(res: Int, action: () -> Unit) = LinearLayout(this).apply {
    gravity = Gravity.CENTER
    layoutParams = LinearLayout.LayoutParams(slot, slot)
    addView(icon(res, R.dimen.icon_xl, R.color.ink))
    tapFeedback()
    setOnClickListener { action() }
  }

  bar.addView(
    onBack?.let { iconButton(R.drawable.ic_chevron_left, it) }
      ?: View(this).apply { layoutParams = LinearLayout.LayoutParams(slot, slot) },
  )
  bar.addView(titleMd(title).apply { gravity = Gravity.CENTER },
    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
  bar.addView(
    rightIconRes?.let { iconButton(it) { onRight?.invoke() } }
      ?: View(this).apply { layoutParams = LinearLayout.LayoutParams(slot, slot) },
  )

  val root = column()
  root.setBackgroundColor(tone(R.color.surface))
  root.add(bar)
  return root
}

/** 底部 Tab 项(图标 + 文字 + 可选未读红点) */
class TabItem(val iconRes: Int, val label: String)

/**
 * 底部 TabBar:选中态用强调色,未选中用 ink3;第 [badgeIndex] 项右上角挂未读红点。
 * 返回的 View 上挂了 [TabBarHandle],供页面切换与角标更新。
 */
class TabBarHandle(val view: View, private val cells: List<TabCell>) {
  fun select(index: Int) = cells.forEachIndexed { i, c -> c.setActive(i == index) }
  fun setBadge(index: Int, count: Int) = cells[index].setBadge(count)
}

class TabCell(private val context: Context, item: TabItem, val root: LinearLayout) {
  private val iconView = context.icon(item.iconRes, R.dimen.icon_lg, R.color.ink3)
  private val label = context.groupLabel(item.label).apply { gravity = Gravity.CENTER }
  private val badgeHolder = LinearLayout(context).apply { gravity = Gravity.CENTER }

  init {
    // 角标用 FrameLayout 定位在图标右上角:图标沉底、角标贴顶,**整块留够高度** ——
    // 用负边距/溢出的话会被 TabBar 的上边界裁掉半个红点(实测踩过)
    val iconWrap = FrameLayout(context).apply {
      addView(
        iconView,
        FrameLayout.LayoutParams(
          context.dim(R.dimen.icon_lg), context.dim(R.dimen.icon_lg),
          Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM,
        ),
      )
      addView(
        badgeHolder,
        FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
          Gravity.TOP or Gravity.END,
        ),
      )
    }
    root.orientation = LinearLayout.VERTICAL
    root.gravity = Gravity.CENTER_HORIZONTAL
    root.setPadding(0, context.px(8), 0, context.px(8))
    root.addView(
      iconWrap,
      LinearLayout.LayoutParams(context.px(46), context.px(30)).apply {
        gravity = Gravity.CENTER_HORIZONTAL
      },
    )
    root.addView(
      label,
      LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
      ).apply { topMargin = context.px(4); gravity = Gravity.CENTER_HORIZONTAL },
    )
  }

  fun setActive(on: Boolean) {
    val color = context.tone(if (on) R.color.accent else R.color.ink3)
    iconView.imageTintList = android.content.res.ColorStateList.valueOf(color)
    label.setTextColor(color)
  }

  fun setBadge(count: Int) {
    badgeHolder.removeAllViews()
    context.unreadBadge(count)?.let { badgeHolder.addView(it) }
  }
}

fun Activity.tabBar(items: List<TabItem>, onSelect: (Int) -> Unit): TabBarHandle {
  val bar = row { setBackgroundColor(tone(R.color.surface)) }
  val cells = items.mapIndexed { index, item ->
    val cellRoot = LinearLayout(this)
    cellRoot.tapFeedback()
    cellRoot.setOnClickListener { onSelect(index) }
    bar.addView(cellRoot, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    TabCell(this, item, cellRoot)
  }
  val root = column()
  root.add(hairline())
  root.add(bar)
  return TabBarHandle(root, cells).also { it.select(0) }
}
