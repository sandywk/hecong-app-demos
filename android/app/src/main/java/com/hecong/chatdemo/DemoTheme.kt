// 深浅色:**你的 App 说了算,聊天页自动跟随** —— 这是 App 接客服的默认行为,**你不用写代码**。
//
// 本文件从头到尾**没有一行是在联动聊天页**:它只管切换这个示范 App 自己的主题。聊天页跟着变,
// 是 SDK 的默认档(`colorScheme = "host"`,读宿主当前深浅色 + 系统切换时自动同步)带来的。
//
// 什么时候才需要写代码:
//   · 想强制聊天页固定某一档(不跟 App)→ 打开时设 `config.colorScheme = "light" / "dark"`;
//   · 想让渠道后台的配置说了算 → 设 `config.colorScheme = "auto"`;
//   · 聊天页已经开着、要临时改档 → `chatView.setColorScheme(...)`。
package com.hecong.chatdemo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object DemoTheme {
  private const val PREFS = "hecong_demo_app"
  private const val KEY_DARK = "darkMode"

  /** 当前是不是深色。真实接入:换成读你自己 App 的主题设置 */
  fun isDark(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DARK, false)

  /** App 启动时调一次,让界面按上次的选择渲染 */
  fun applyToApp(context: Context) {
    AppCompatDelegate.setDefaultNightMode(
      if (isDark(context)) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
    )
  }

  /**
   * 用户拨动开关:**只切自己 App 的主题**。聊天页会自己跟上 —— 这里刻意不写任何联动代码,
   * 就是为了证明"零代码即跟随"。
   */
  fun setDark(context: Context, dark: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
      .putBoolean(KEY_DARK, dark).apply()
    applyToApp(context)
  }
}
