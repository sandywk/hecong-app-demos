// 演示参数 · 示范会员资料(接入时不需要 —— 真实接入取你自己登录体系里的当前用户)。
//
// 为什么要做成可输入并持久化:身份透传的效果**只能在工作台侧核对** —— 客服看到的昵称、
// 头像、自定义字段是不是你传的那份。写死一个假会员就永远只能验证"没报错",验证不了"对不对"。
// 与 iOS `DemoMemberProfile.swift` 同构。
package com.hecong.chatdemo

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

object DemoMemberProfile {
  private const val PREFS = "hecong_demo_app"
  private const val KEY_USER_ID = "member.userId"
  private const val KEY_NAME = "member.name"
  private const val KEY_AVATAR = "member.avatar"
  private const val KEY_EXTRA = "member.extra"
  private const val KEY_GENERATED = "member.generatedUserId"

  const val DEFAULT_NAME = "演示会员"

  private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

  /**
   * 缺省会员 ID:**每台设备首次启动生成一次并持久化**(`demo-user-` + 6 位随机 hex)。
   * 不能写死同一个值 —— 同一渠道下所有装了 demo 的人会被后端合并成同一个会员,互相看到
   * 对方的记录(官方演示渠道上尤其致命,owner 2026-08-21 指出)。
   * 同时也示范了"会员 ID 应不可猜测、不用连续数字"这条接入要求。
   */
  fun defaultUserId(c: Context): String {
    prefs(c).getString(KEY_GENERATED, null)?.takeIf { it.isNotBlank() }?.let { return it }
    val generated = "demo-user-" + "%06x".format(Random.nextInt(0, 0x1000000))
    prefs(c).edit().putString(KEY_GENERATED, generated).apply()
    return generated
  }

  /** 你在资料页填的值优先;没填就用本机生成的缺省值 */
  fun userId(c: Context): String = read(c, KEY_USER_ID) ?: defaultUserId(c)
  fun name(c: Context): String = read(c, KEY_NAME) ?: DEFAULT_NAME
  /** 头像地址(留空则不传 —— SDK 侧"没有头像就不绘制",不会替你编一个占位图形) */
  fun avatarUrl(c: Context): String = read(c, KEY_AVATAR) ?: ""
  /** 自定义字段:`键=值` 每行一条,透传到工作台的客户资料区 */
  fun extraFields(c: Context): String = read(c, KEY_EXTRA) ?: ""

  fun setUserId(c: Context, v: String) = write(c, KEY_USER_ID, v)
  fun setName(c: Context, v: String) = write(c, KEY_NAME, v)
  fun setAvatarUrl(c: Context, v: String) = write(c, KEY_AVATAR, v)
  fun setExtraFields(c: Context, v: String) = write(c, KEY_EXTRA, v)

  /** `identify` / `updateUser` 的 profile 参数(标准字段) */
  fun profileJson(c: Context): JSONObject = JSONObject().apply {
    put("name", name(c))
    avatarUrl(c).takeIf { it.isNotBlank() }?.let { put("avatar", it) }
  }

  /** `identify` / `updateUser` 的 data 参数(宿主自定义字段);没填返回 null */
  fun dataJson(c: Context): JSONObject? {
    val out = JSONObject()
    extraFields(c).lines().forEach { line ->
      // 半角 `=` 与**全角 `＝`** 都认:中文输入法默认打出的是全角,只认半角的话租户填了
      // 却静默不透传,还以为是 SDK 的问题(2026-08-22 端测实测踩到)。
      val i = line.indexOfFirst { it == '=' || it == '＝' }
      if (i <= 0) return@forEach
      val key = line.substring(0, i).trim()
      val value = line.substring(i + 1).trim()
      if (key.isNotEmpty()) out.put(key, value)
    }
    return if (out.length() == 0) null else out
  }

  /** 恢复缺省值(本机生成的缺省会员 ID 保留,不重新生成) */
  fun reset(c: Context) {
    prefs(c).edit().remove(KEY_USER_ID).remove(KEY_NAME).remove(KEY_AVATAR).remove(KEY_EXTRA).apply()
  }

  private fun read(c: Context, key: String): String? =
    prefs(c).getString(key, null)?.takeIf { it.isNotBlank() }

  private fun write(c: Context, key: String, value: String) {
    val trimmed = value.trim()
    prefs(c).edit().apply {
      if (trimmed.isEmpty()) remove(key) else putString(key, trimmed)
    }.apply()
  }
}
