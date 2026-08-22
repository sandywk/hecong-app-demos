// 演示清单的骨架层:场景模型 + 能力页构建器(与 iOS `DemoScene.swift` 逐项对位)。
//
// 四个能力页各自只声明"本页有哪些分组、哪些场景",不重复写布局代码。
// ⚠️ 本文件是**示范工程自己的脚手架**,接入 SDK 时不需要。
package com.hecong.chatdemo

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SwitchCompat
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.bigTitleHeader
import com.hecong.chatdemo.ui.caption
import com.hecong.chatdemo.ui.card
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.dim
import com.hecong.chatdemo.ui.groupTitle
import com.hecong.chatdemo.ui.listRow
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.tone

/** 场景右侧的控件形态 */
sealed class SceneControl {
  /** 常规行:右侧 chevron,点击执行动作 */
  object Navigate : SceneControl()
  /** 状态行:右侧显示一段实时取值的文字(如当前渠道档位、当前会员 ID) */
  class Value(val read: () -> String) : SceneControl()
  /** 开关行:右侧 Switch(如宿主深浅色 / 未读跟踪) */
  class Toggle(val isOn: () -> Boolean, val onChange: (Boolean) -> Unit) : SceneControl()
}

/** 一条演示场景:标题 + 一句能力说明 + 点击动作 */
class DemoScene(
  val title: String,
  val detail: String,
  @DrawableRes val iconRes: Int? = null,
  val accent: Boolean = false,
  /** 右侧未读徽标的取值(返回 0 = 不画) */
  val badge: (() -> Int)? = null,
  val control: SceneControl = SceneControl.Navigate,
  val handler: (Activity) -> Unit = {},
)

/** 一组场景:分组标题 + 可选的组尾说明(放接入注意事项,不放到每一行里堆) */
class DemoSceneGroup(val title: String, val footer: String? = null, val scenes: List<DemoScene>)

/**
 * 能力页:大标题 + 可选页头卡 + 数据驱动的分组清单。[refresh] 重建清单(状态行取值随之刷新)。
 */
class ScenePage(
  private val activity: Activity,
  private val title: String,
  private val header: (() -> View)? = null,
  private val makeGroups: () -> List<DemoSceneGroup>,
) {
  private val body: LinearLayout = activity.column {
    setBackgroundColor(activity.tone(R.color.bg))
    val side = activity.dim(R.dimen.page_side)
    setPadding(side, 0, side, activity.px(24))
  }

  val view: View = activity.column {
    add(activity.bigTitleHeader(title))
    addFill(ScrollView(activity).apply { addView(body) })
  }

  init { refresh() }

  fun refresh() {
    body.removeAllViews()
    header?.let { body.add(it(), activity.px(4)) }
    makeGroups().forEach { group -> body.add(sceneGroup(group)) }
  }

  /** 分组 = 小标题(与卡片左缘齐平)+ 卡片 + 页脚说明(与 iOS 侧同规格:13/500 + 12) */
  private fun sceneGroup(group: DemoSceneGroup): View = with(activity) {
    column {
      add(groupTitle(group.title).apply { setPadding(px(2), px(18), px(2), px(8)) })
      add(card(group.scenes.map { sceneRow(it) }))
      group.footer?.let {
        add(caption(it).apply { setPadding(px(2), px(8), px(2), px(6)) })
      } ?: add(View(activity).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, activity.px(12))
      })
    }
  }

  private fun sceneRow(scene: DemoScene): View = with(activity) {
    val badgeCount = scene.badge?.invoke() ?: 0
    when (val c = scene.control) {
      is SceneControl.Navigate -> listRow(
        scene.title, scene.detail, scene.iconRes, scene.accent, badgeCount,
        onClick = { scene.handler(activity) },
      )
      is SceneControl.Value -> listRow(
        scene.title, scene.detail, scene.iconRes, scene.accent, badgeCount,
        chevron = false, trailing = caption(c.read()),
        onClick = { scene.handler(activity) },
      )
      is SceneControl.Toggle -> listRow(
        scene.title, scene.detail, scene.iconRes, scene.accent, badgeCount,
        chevron = false,
        trailing = SwitchCompat(activity).apply {
          isChecked = c.isOn()
          setOnCheckedChangeListener { _, checked -> c.onChange(checked) }
        },
      )
    }
  }
}
