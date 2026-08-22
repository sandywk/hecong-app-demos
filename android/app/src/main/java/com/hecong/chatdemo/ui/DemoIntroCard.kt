// 首页顶部说明卡(示范工程自己的脚手架,接入时不需要)。
//
// 租户扫码装完点开,第一眼是「界面形态」列表 —— 这张卡回答"这是什么、怎么用",
// 并直接给出唯一要做的动作(填渠道 ID)。只放首页、不可关闭、同色系不打扰(owner 2026-08-21)。
// 与 iOS `DemoIntroCard.swift` 同构。
package com.hecong.chatdemo.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.hecong.chatdemo.R

fun Context.demoIntroCard(onAction: () -> Unit): View {
  val chip = LinearLayout(this).apply {
    gravity = Gravity.CENTER
    background = shapePx(dim(R.dimen.chip_radius).toFloat(), R.color.accent)
    addView(icon(R.drawable.ic_message_circle, R.dimen.icon_sm, R.color.surface))
  }
  val action = TextView(this).apply {
    text = "填渠道 ID"
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f)
    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    setTextColor(tone(R.color.accent))
    setPadding(0, px(6), px(8), px(2))
    tapFeedback()
    setOnClickListener { onAction() }
  }
  val texts = column {
    add(bodyMd("合从客服 SDK · Demo"))
    add(caption("演示原生 App 接入合从客服的各种承载形态与能力。填入你的渠道 ID,即可连接到自己的工作台进行测试。"), px(4))
    add(action, px(2))
  }
  val rowView = row {
    gravity = Gravity.TOP
    val pad = dim(R.dimen.card_pad)
    setPadding(pad, px(14), pad, px(10))
    addView(chip, LinearLayout.LayoutParams(dim(R.dimen.chip_size), dim(R.dimen.chip_size)).apply { marginEnd = px(12) })
    addView(texts, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
  }
  return column {
    background = shapePx(dim(R.dimen.card_radius).toFloat(), R.color.accent_soft)
    add(rowView)
  }
}
