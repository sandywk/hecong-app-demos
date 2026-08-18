// 底部弹层承载 —— 第三种承载形态(设计稿 02 第 ③ 种)。
//
// 形态 = 半屏卡片 + 抓手条 + 遮罩,下拉或点遮罩关闭。很多电商 / 工具类 App 喜欢这种"聊一句
// 就走、不离开当前页"的方式。这一档**没有返回键,所以保留右上角关闭按钮**(它是唯一出口)。
//
// 接入要点(三段系统回调由承载它的 Activity 代办 —— 范本见 MainActivity):
//   ① 聊天视图仍用 **Activity context** 创建,不要用 Dialog context;
//   ② 弹层关闭时调 destroy();
//   ③ ⚠️ 装在 Dialog 里时,**键盘避让要自己给 Dialog 的 window 设一次** —— SDK 设的是
//      Activity 的 window,够不到 Dialog 这一层。不设的症状:输入法弹起来把输入框顶出屏幕。
package com.hecong.chatdemo

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.icon
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.row
import com.hecong.chatdemo.ui.shape
import com.hecong.chatdemo.ui.tapFeedback
import com.hecong.chatdemo.ui.titleMd
import com.hecong.chatdemo.ui.tone
import com.hecong.chatdemo.ui.topRoundedShape
import com.hecong.chatsdk.HecongChatView

/** 弹层高度占屏比(设计稿 690/844) */
private const val SHEET_HEIGHT_RATIO = 0.82f

/** 下拉超过这个距离即关闭 */
private const val DISMISS_DRAG_DP = 90

class SheetChatDialog(private val activity: Activity) {
  // ① Activity context
  val chat: HecongChatView = HecongChatView(activity)

  private val dialog = Dialog(activity, R.style.Theme_Demo_Sheet)
  private lateinit var sheet: View

  fun show() {
    dialog.setContentView(buildContent())
    dialog.window?.apply {
      setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
      // ③ Dialog 自己的键盘策略
      setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
    dialog.setOnDismissListener { chat.destroy() } // ②
    dialog.show()
    chat.load(DemoConfig.buildChatConfig(activity))
  }

  fun dismiss() = dialog.dismiss()

  private fun buildContent(): View = with(activity) {
    sheet = column {
      background = topRoundedShape(24, R.color.surface)
      add(grabber())
      add(sheetHeader())
      addFill(chat)
    }

    FrameLayout(activity).apply {
      setBackgroundColor(tone(R.color.scrim))
      setOnClickListener { dismiss() } // 点遮罩关闭
      val height = (resources.displayMetrics.heightPixels * SHEET_HEIGHT_RATIO).toInt()
      addView(
        sheet,
        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height, Gravity.BOTTOM),
      )
    }
  }

  /** 抓手条:按住下拉即可关闭(标题区一起响应,手指落点更宽容) */
  private fun grabber(): View = with(activity) {
    column {
      setPadding(0, px(10), 0, px(6))
      gravity = Gravity.CENTER_HORIZONTAL
      addView(
        View(activity).apply { background = shape(2, R.color.grabber) },
        LinearLayout.LayoutParams(px(36), px(4)),
      )
      setOnTouchListener(DragToDismiss(px(DISMISS_DRAG_DP)) { dismiss() })
    }
  }

  private fun sheetHeader(): View = with(activity) {
    row {
      setPadding(px(14), px(2), px(14), px(10))
      // 左侧占位跟右侧两个图标等宽,标题才是真居中
      addView(View(activity), LinearLayout.LayoutParams(px(56), px(22)))
      addView(
        titleMd("在线客服").apply { gravity = Gravity.CENTER },
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
      )
      // 「…」与页面推入档同一份菜单(两端两档都一致,详 ChatOverflowMenu)
      addView(
        icon(R.drawable.ic_ellipsis, R.dimen.icon_lg, R.color.ink2).apply {
          tapFeedback()
          setOnClickListener { ChatOverflowMenu.show(activity, chat) }
        },
        LinearLayout.LayoutParams(px(22), px(22)).apply { marginEnd = px(12) },
      )
      addView(
        icon(R.drawable.ic_x, R.dimen.icon_lg, R.color.ink2).apply {
          tapFeedback()
          setOnClickListener { dismiss() }
        },
      )
    }
  }

  /** 跟手下拉 + 松手判定:超过阈值关闭,否则弹回 */
  private inner class DragToDismiss(
    private val thresholdPx: Int,
    private val onDismiss: () -> Unit,
  ) : View.OnTouchListener {
    private var startY = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> startY = event.rawY
        MotionEvent.ACTION_MOVE ->
          sheet.translationY = (event.rawY - startY).coerceAtLeast(0f)
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
          if (sheet.translationY > thresholdPx) onDismiss() else sheet.animate().translationY(0f)
      }
      return true
    }
  }
}
