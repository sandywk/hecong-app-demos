// 示范 App 主框架:双 Tab ——「我的」(模拟你自家的页面)+「示例」(能力清单)。
//
// 示范工程自己的脚手架,接入时不需要。唯一值得看的是 [updateUnread]:SDK 给的未读数
// 怎么落到你自己的 Tab 角标和入口红点上(接线源头见 DemoApp ②)。
package com.hecong.chatdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.TabItem
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.tabBar
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
  companion object {
    private var live: WeakReference<MainActivity>? = null

    /** 未读数变化 → 同时更新底部 Tab 红点与「我的」页客服入口红点 */
    fun updateUnread(count: Int) {
      val activity = live?.get() ?: return
      activity.runOnUiThread { activity.applyUnread(count) }
    }

    /**
     * 收到客服消息时的提醒(**演示用**:这里只弹个 Toast)。
     *
     * 真实接入应该在这里弹**你自己的本地通知**或震动一下 —— 用户可能正在 App 的别的页面,
     * 不弹的话他要等下次点进客服才知道有回复。信号源是 `onIncomingMessage`(接线见 DemoApp ⑤)。
     */
    @JvmStatic
    fun flashIncoming(text: String) {
      val activity = live?.get() ?: return
      activity.runOnUiThread {
        android.widget.Toast.makeText(
          activity, "客服:${text.take(20)}", android.widget.Toast.LENGTH_SHORT,
        ).show()
      }
    }
  }

  private var minePage: MinePage? = null
  private var tabs: com.hecong.chatdemo.ui.TabBarHandle? = null

  /** 底部弹层场景的活动实例 —— 文件选择/权限回调经本 Activity 转发给它 */
  private var activeSheet: SheetChatDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    live = WeakReference(this)

    val mine = MinePage(this).also { minePage = it }
    val catalog = buildCatalogPage(this)
    val pages = listOf(mine.view, catalog)

    val container = FrameLayout(this)
    pages.forEachIndexed { index, page ->
      page.visibility = if (index == 0) View.VISIBLE else View.GONE
      container.addView(page)
    }

    val tabBar = tabBar(
      listOf(
        TabItem(R.drawable.ic_layout_grid, "我的"),
        TabItem(R.drawable.ic_list, "示例"),
      ),
    ) { selected ->
      pages.forEachIndexed { index, page ->
        page.visibility = if (index == selected) View.VISIBLE else View.GONE
      }
      tabs?.select(selected)
    }
    tabs = tabBar

    setContentView(
      column {
        addFill(container)
        add(tabBar.view)
      },
    )
  }

  private fun applyUnread(count: Int) {
    minePage?.setUnread(count)
    tabs?.setBadge(0, count)
  }

  /** 「示例 → 底部弹层」场景入口 */
  fun showSheetChat() {
    activeSheet = SheetChatDialog(this).also { it.show() }
  }

  // 底部弹层里的聊天视图挂在本 Activity 上,所以两段系统回调要由本 Activity 转发
  // (不转发的症状:弹层里点发图片没反应 / 麦克风权限点了没用)
  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (activeSheet?.chat?.handleFileChooserResult(requestCode, resultCode, data) == true) return
    @Suppress("DEPRECATION")
    super.onActivityResult(requestCode, resultCode, data)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray,
  ) {
    if (activeSheet?.chat?.handlePermissionsResult(requestCode, permissions, grantResults) == true) return
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  override fun onDestroy() {
    if (live?.get() === this) live = null
    super.onDestroy()
  }
}
