// 会话事件流水(**演示用**,接入时不需要这个类)。
//
// 存在的意义是让你**亲眼看见** SDK 在什么时候发了什么事件 —— 打开客服、发一句话、
// 等客服回、断个网,再回诊断页看这份流水,就知道每个事件该怎么用了。
// 真实接入时你不需要记流水,而是在收到事件的那一刻**直接做事**(弹通知 / 埋点 / 改 UI)。
package com.hecong.chatdemo

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DemoEventLog {
  /** 只留最近这些条 —— 演示够用,且不会无限占内存 */
  private const val MAX = 60

  private val entries = ArrayDeque<String>()
  private val clock = SimpleDateFormat("HH:mm:ss", Locale.US)

  @Synchronized
  fun record(name: String, payload: JSONObject?) {
    val detail = summarize(name, payload)
    entries.addFirst("${clock.format(Date())}  $name${if (detail.isEmpty()) "" else "  $detail"}")
    while (entries.size > MAX) entries.removeLast()
  }

  @Synchronized
  fun snapshot(): List<String> = entries.toList()

  @Synchronized
  fun clear() = entries.clear()

  /** 挑几个关键字段显示,不整包打印(消息正文可能很长,而且流水只是给人看个大概) */
  private fun summarize(name: String, payload: JSONObject?): String {
    payload ?: return ""
    return when {
      name.startsWith("message") -> {
        val from = payload.optString("from")
        val text = payload.optString("text").take(20)
        val type = payload.optString("contentType")
        if (type == "text") "$from:「$text」" else "$from:[$type]"
      }
      name.startsWith("conversation") -> payload.optString("conversationId").take(12)
      else -> ""
    }
  }
}
