// 演示台 · 示范会员资料(示范工程自己的脚手架,接入时不需要)。
//
// 填入可辨识的值后,「身份与会员」页的各场景统一使用这份资料 ——
// 便于在工作台侧核对昵称、头像与自定义字段的实际透传结果。与 iOS `MemberProfileViewController` 同构。
package com.hecong.chatdemo

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hecong.chatdemo.ui.add
import com.hecong.chatdemo.ui.addFill
import com.hecong.chatdemo.ui.bodyMd
import com.hecong.chatdemo.ui.caption
import com.hecong.chatdemo.ui.card
import com.hecong.chatdemo.ui.column
import com.hecong.chatdemo.ui.dim
import com.hecong.chatdemo.ui.groupTitle
import com.hecong.chatdemo.ui.navBar
import com.hecong.chatdemo.ui.px
import com.hecong.chatdemo.ui.tapFeedback
import com.hecong.chatdemo.ui.tone

class MemberProfileActivity : AppCompatActivity() {
  private class Field(
    val title: String,
    val placeholder: String,
    val hint: String,
    val read: () -> String,
    val write: (String) -> Unit,
    val multiline: Boolean = false,
  )

  private val fields by lazy {
    listOf(
      Field(
        "会员 ID", DemoMemberProfile.defaultUserId(this),
        "对应 identify 的 userId。应使用不可猜测的取值,避免连续数字。",
        { DemoMemberProfile.userId(this) }, { DemoMemberProfile.setUserId(this, it) },
      ),
      Field(
        "昵称", DemoMemberProfile.DEFAULT_NAME,
        "对应 profile.name,显示在工作台的客户资料区。",
        { DemoMemberProfile.name(this) }, { DemoMemberProfile.setName(this, it) },
      ),
      Field(
        "头像地址", "https://…",
        "对应 profile.avatar。留空则不传 —— 客服侧不会绘制占位头像。",
        { DemoMemberProfile.avatarUrl(this) }, { DemoMemberProfile.setAvatarUrl(this, it) },
      ),
      Field(
        "自定义字段", "level=VIP3",
        "对应 identify 的 data 参数,每行一条「键=值」,透传到工作台的客户资料区。",
        { DemoMemberProfile.extraFields(this) }, { DemoMemberProfile.setExtraFields(this, it) },
        multiline = true,
      ),
    )
  }

  private val editors = mutableListOf<EditText>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val body = column {
      setBackgroundColor(tone(R.color.bg))
      val side = dim(R.dimen.page_side)
      setPadding(side, px(6), side, px(24))
      fields.forEach { add(fieldGroup(it)) }
      add(resetRow(), px(10))
    }
    setContentView(
      column {
        add(navBar("示范会员资料", onBack = { finish() }))
        addFill(ScrollView(this@MemberProfileActivity).apply { addView(body) })
      },
    )
  }

  private fun fieldGroup(f: Field): View = column {
    add(groupTitle(f.title).apply { setPadding(px(2), px(18), px(2), px(8)) })
    val editor = EditText(this@MemberProfileActivity).apply {
      background = null
      setText(f.read())
      hint = f.placeholder
      setTextColor(tone(R.color.ink))
      setHintTextColor(tone(R.color.ink3))
      textSize = 16f
      val pad = dim(R.dimen.card_pad)
      setPadding(pad, px(14), pad, px(14))
      inputType = if (f.multiline) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
      } else {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
      }
      if (f.multiline) minLines = 3
      addTextChangedListener(object : android.text.TextWatcher {
        override fun afterTextChanged(s: android.text.Editable?) { f.write(s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
      })
    }
    editors += editor
    add(card(listOf(editor)))
    add(caption(f.hint).apply { setPadding(px(2), px(8), px(2), 0) })
  }

  /** 恢复缺省值(本机生成的缺省会员 ID 保留) */
  private fun resetRow(): View = card(
    listOf(
      bodyMd("恢复默认值", R.color.danger).apply {
        gravity = Gravity.CENTER
        val pad = dim(R.dimen.card_pad)
        setPadding(pad, px(14), pad, px(14))
        tapFeedback()
        setOnClickListener {
          DemoMemberProfile.reset(this@MemberProfileActivity)
          editors.forEachIndexed { i, e -> e.setText(fields[i].read()) }
          Toast.makeText(this@MemberProfileActivity, "已恢复示范会员资料默认值", Toast.LENGTH_SHORT).show()
        }
      },
    ),
  )
}
