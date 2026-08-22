// 示范 App 主框架:四个能力页(与 iOS `DemoTabBarController` 逐项对位)。
//
// | Tab | 覆盖 |
// |---|---|
// | 界面形态 | 四档承载形态、标题栏、深浅色、语言 —— 租户最先关心"长什么样",放第一页 |
// | 身份与会员 | 会员资料演示台、identify / resetUser 成对、未读跟踪 |
// | 高级扩展 | 技能组指派、输入区扩展 |
// | 配置与诊断 | 渠道配置、诊断信息、会话事件流水 |
//
// 2026-08-21 owner 定调:示范 App 只演示**常用且适合在 App 里演示**的场景,不追求覆盖全部接口 ——
// 纯文字说明类的条目(权限时机、推送接法、宿主接管回调)归接入文档。
//
// **示范工程自己的脚手架,接入时不需要。** 唯一值得参考的是 [updateUnread]:
// SDK 给出的未读数如何落到宿主自己的 Tab 徽标与入口红点上(接线源头见 DemoApp)。
package com.hecong.chatdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.TabBarHandle
import com.hecong.chatdemo.ui.TabItem
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.tabBar
import java.lang.ref.WeakReference

/** Tab 序号 —— 页面间互跳与自动化钩子按它定位,不写魔法数字 */
enum class DemoTab { APPEARANCE, IDENTITY, ADVANCED, TOOLBOX }

class MainActivity : AppCompatActivity() {
  companion object {
    private var live: WeakReference<MainActivity>? = null

    /** 未读数变化 → 「身份与会员」Tab 徽标 + 该页客服入口示范行的红点 */
    fun updateUnread(count: Int) {
      val activity = live?.get() ?: return
      activity.runOnUiThread { activity.applyUnread(count) }
    }

    /**
     * 收到客服消息时的提醒(**演示用**:这里只弹个 Toast)。
     * 真实接入应该在这里弹**你自己的本地通知**或震动一下 —— 用户可能正在 App 的别的页面。
     * 信号源是 `onIncomingMessage`(接线见 DemoApp)。
     */
    @JvmStatic
    fun flashIncoming(text: String) = toastFromAnywhere("客服:${text.take(20)}")

    /** 演示用:从任意处弹一句提示(自定义按钮点击回调用) */
    @JvmStatic
    fun toastFromAnywhere(text: String) {
      val activity = live?.get() ?: return
      activity.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_LONG).show() }
    }
  }

  private lateinit var pages: List<ScenePage>
  private var tabs: TabBarHandle? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    live = WeakReference(this)

    pages = listOf(appearancePage(this), identityPage(this), advancedPage(this), toolboxPage(this))
    val container = FrameLayout(this)
    pages.forEachIndexed { index, page ->
      page.view.visibility = if (index == 0) View.VISIBLE else View.GONE
      container.addView(page.view)
    }

    val tabBar = tabBar(
      listOf(
        TabItem(R.drawable.ic_copy, "界面形态"),
        TabItem(R.drawable.ic_circle_user, "身份与会员"),
        TabItem(R.drawable.ic_sliders_horizontal, "高级扩展"),
        TabItem(R.drawable.ic_wrench, "配置与诊断"),
      ),
    ) { selected -> select(selected) }
    tabs = tabBar

    setContentView(
      column {
        addFill(container)
        add(tabBar.view)
      },
    )

    applyAutomationHooks(intent)
  }

  /**
   * 自动化钩子(验收用,接入时不需要):
   *   `--ei hc.autoTab 0…3` 直达某能力页;`--es hc.autoOpen standard|sheet|sheetH5|immersive|embed` 直开某承载档。
   *
   * ⚠️ **onCreate 与 onNewIntent 都要走**:MainActivity 是 singleTop 复用的,
   * 实例还在时 `am start` 只走 onNewIntent —— 只挂 onCreate 的话第二次起就静默失效(2026-08-22 实测)。
   */
  private fun applyAutomationHooks(intent: Intent) {
    intent.getIntExtra("hc.autoTab", -1).takeIf { it in pages.indices }?.let { select(it) }
    intent.getStringExtra("hc.autoOpen")?.let { mode ->
      window.decorView.post {
        when (mode) {
          "standard" -> ChatLaunch.standard(this)
          "sheet" -> ChatLaunch.sheet(this)
          "sheetH5" -> ChatLaunch.sheetChannelHeader(this)
          "immersive" -> ChatLaunch.immersive(this)
          "embed" -> ChatLaunch.customHeader(this)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    applyAutomationHooks(intent)
  }

  private fun select(index: Int) {
    pages.forEachIndexed { i, page -> page.view.visibility = if (i == index) View.VISIBLE else View.GONE }
    tabs?.select(index)
  }

  /** 从聊天页 / 演示台返回时刷新状态行(渠道档位、会员 ID、事件条数) */
  override fun onResume() {
    super.onResume()
    pages.forEach { it.refresh() }
  }

  private fun applyUnread(count: Int) {
    tabs?.setBadge(DemoTab.IDENTITY.ordinal, count)
    pages[DemoTab.IDENTITY.ordinal].refresh()
  }

  override fun onDestroy() {
    if (live?.get() === this) live = null
    super.onDestroy()
  }
}
