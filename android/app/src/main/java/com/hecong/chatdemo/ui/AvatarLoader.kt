// 极小的异步头像加载(URL → Bitmap → 圆形 ImageView)。
//
// ⚠️ 真实接入建议直接用 Glide / Coil,别自己写 —— 这里手写只是因为示范工程要保持**零第三方
// 依赖**(与 SDK 本体同调性)。功能只覆盖演示需要的部分:单线程队列 + 内存缓存 + 失败回退。
package com.hecong.chatdemo.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object AvatarLoader {
  private val cache = HashMap<String, Bitmap>()
  private val worker = Executors.newSingleThreadExecutor()
  private val mainThread = Handler(Looper.getMainLooper())

  /**
   * 把 [url] 的图加载进 [view] 并裁成圆形;失败或图坏了调 [onFailure](调用方据此回退到文字首字)。
   *
   * 用 view.tag 记住本次请求的 url:身份连续变化时(渠道 → 客服 → 转接),回来的旧图不会盖掉新图。
   */
  fun into(view: ImageView, url: String, targetPx: Int, onFailure: () -> Unit) {
    cache[url]?.let { apply(view, it); return }
    view.tag = url
    worker.execute {
      val bitmap = runCatching { download(url, targetPx) }.getOrNull()
      mainThread.post {
        if (view.tag != url) return@post
        if (bitmap == null) onFailure() else { cache[url] = bitmap; apply(view, bitmap) }
      }
    }
  }

  private fun apply(view: ImageView, bitmap: Bitmap) {
    view.setImageDrawable(
      RoundedBitmapDrawableFactory.create(view.resources, bitmap).apply { isCircular = true },
    )
  }

  /** 先读尺寸再按目标边长降采样,避免把一张大图整个解进内存 */
  private fun download(url: String, targetPx: Int): Bitmap? {
    val bytes = openStream(url).use { it.readBytes() }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val shortest = minOf(bounds.outWidth, bounds.outHeight).takeIf { it > 0 } ?: return null
    var sample = 1
    while (shortest / (sample * 2) >= targetPx) sample *= 2
    return BitmapFactory.decodeByteArray(
      bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample },
    )
  }

  private fun openStream(url: String) =
    (URL(url).openConnection() as HttpURLConnection).apply {
      connectTimeout = 8_000
      readTimeout = 8_000
    }.inputStream
}
