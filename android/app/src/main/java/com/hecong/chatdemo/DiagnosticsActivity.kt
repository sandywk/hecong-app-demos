// 诊断页(示范工程自己的脚手架,接入时不需要):当前配置 / 渠道 / 访客标识 —— 排查问题先看这页。
package com.hecong.chatdemo

import android.content.Context
import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.bodyMd
import com.hecong.chatdemo.ui.card
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.dim
import com.hecong.chatdemo.ui.groupLabel
import com.hecong.chatdemo.ui.navBar
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.tone

class DiagnosticsActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // SDK 把访客标识存在本 App 的沙盒里,诊断页读同一处
    val mirror = getSharedPreferences("hecong_chat_sdk", Context.MODE_PRIVATE)
      .getString("anonymousId", null)

    val rows = listOf(
      "当前配置" to DemoConfig.describeProfile(this),
      "渠道 ID" to DemoConfig.describe(this),
      "访客标识" to (mirror ?: "(尚未建立 —— 第一次打开客服后生成)"),
      // 版本号**运行时读自己的包**,不硬编码 —— 硬编码就是又一份会漂移的副本
      // (2026-08-18 实测漂过:SDK 已 0.1.1,这里还显示 0.1.0)。
      // versionName 由 build.gradle.kts 从 native/version.json 读,链路全程真同源。
      "示范 App 版本" to
        "${appVersionName()} (${if (LocalEnv.channelIdOrNull != null) "debug" else "release"})",
    )

    val body = column {
      setBackgroundColor(tone(R.color.bg))
      val side = dim(R.dimen.page_side)
      setPadding(side, px(6), side, px(24))
      add(
        card(
          rows.map { (title, value) ->
            column {
              val pad = dim(R.dimen.card_pad)
              setPadding(pad, px(14), pad, px(14))
              add(groupLabel(title))
              add(bodyMd(value).apply { setTextIsSelectable(true) }, px(4))
            }
          },
        ),
      )
    }

    setContentView(
      column {
        add(navBar("诊断信息", onBack = { finish() }))
        addFill(ScrollView(this@DiagnosticsActivity).apply { addView(body) })
      },
    )
  }

  /** 读本 APP 自己的 versionName(真同源:build.gradle.kts ← native/version.json) */
  private fun appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName ?: "?" }
      .getOrDefault("?")
}
