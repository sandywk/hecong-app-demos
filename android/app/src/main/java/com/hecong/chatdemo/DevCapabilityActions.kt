// 「开发者能力」这一组的动作实现:指定技能组 / 商品·订单选择器 / 自定义按钮。
//
// 这三样跟别的示例有个本质区别:**它们在客服工作台里配不出来**,必须写代码。
// 原因是数据在租户自己的系统里(商品、订单),或者要由租户的业务逻辑决定(该谁接待)。
// 契约:sdk-public-api-contract.md §九 / app-sdk-plan.md §10.7
//
// 🔴 这一组同时也是**问题复现台**:租户报"我传技能组没效",让他用本 APP 填一次自己的组名 ——
// 这里有效 = 他自己那侧的接法有问题;这里也无效 = 组名/渠道配置的问题。二分掉一半排查面。
package com.hecong.chatdemo

import android.app.Activity
import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast
import com.hecong.chatsdk.HecongChat
import com.hecong.chatsdk.HecongChatActivity
import com.hecong.chatsdk.HecongRouting

object DevCapabilityActions {

  /**
   * **打开时就指定技能组**(启动档)。
   *
   * 接入时你要抄的就是这两行:`config.routing = HecongRouting(组名)` 然后照常打开。
   * 值会拼进聊天页地址,**老版本 SDK 的壳也认**(纯 URL,不依赖新命令)。
   * 适合"进客服之前就知道该谁接"的场景:VIP 会员进专属组、售后入口进售后组。
   */
  fun openWithSkillGroup(activity: Activity) {
    if (!ChannelSetup.ensureReady(activity)) return
    askSkillGroup(activity, "打开客服并指定技能组", "填你工作台里的技能组名称") { group ->
      val config = DemoConfig.buildChatConfig(activity)
      config.routing = HecongRouting(group) // ← 接入时就这一行
      HecongChatActivity.start(activity, config)
    }
  }

  /**
   * **聊天页开着时换组**(运行时档)。
   *
   * 用在"聊到一半要转专业组"的场景。传 null 清除指派,回到渠道默认分派。
   * ⚠️ 指派在**新对话创建时**生效 —— 已经在进行中的对话不会被中途改派。
   */
  fun switchSkillGroup(activity: Activity) {
    askSkillGroup(activity, "切换技能组", "留空 = 清除指派,回到默认分派") { group ->
      if (group.isEmpty()) {
        HecongChat.setRouting(null as String?)
        toast(activity, "已清除技能组指派")
      } else {
        HecongChat.setRouting(group)
        toast(activity, "已指派到「$group」—— 下一次新对话生效")
      }
    }
  }

  /**
   * **商品选择器**:在附件面板里加一个「商品」入口,点了弹出商品列表让访客选。
   *
   * 这就是"从商品详情页点联系客服"的标准做法 —— 两步:
   *   1. **这里**:`registerAction` 加入口按钮(本例放附件面板;要显眼就用 "quick" 放输入框上方);
   *   2. **在 `onActionClick` 回调里**(见 [DemoApp] ③):先 `setPickerData` 回填你系统里的
   *      当前商品,再 `openPicker` 打开选择器。
   *
   * ⚠️ 第 2 步的数据**必须在点击那一刻给**,不能提前:SDK 刻意不缓存选择器数据
   * (库存/登录态都会变,缓存重放等于把陈旧列表推给下一个会话)。
   */
  fun demoProductPicker(activity: Activity) {
    if (!ChannelSetup.ensureReady(activity)) return
    HecongChat.registerAction(ACTION_PRODUCT, "商品", "attach")
    // ⚠️ 这里**故意不预先** setPickerData —— 数据在 onActionClick 回调里现给(见 DemoApp ③)。
    // SDK 刻意不缓存选择器数据:提前给会在页面重建后丢失,而且陈旧数据比没有更糟。
    HecongChatActivity.start(activity, DemoConfig.buildChatConfig(activity))
    toast(activity, "已加「商品」入口 —— 点输入框旁的 + 号就能看到")
  }

  /** **订单选择器**:同商品,换个数据源。售后咨询让访客直接选中要问哪一单。 */
  fun demoOrderPicker(activity: Activity) {
    if (!ChannelSetup.ensureReady(activity)) return
    HecongChat.registerAction(ACTION_ORDER, "订单", "quick")
    // 同上:数据在点击回调里现给
    HecongChatActivity.start(activity, DemoConfig.buildChatConfig(activity))
    toast(activity, "已加「订单」入口 —— 在输入框正上方")
  }


  /** 撤掉本页注册过的所有自定义按钮(演示 `unregisterAction`;同 id 再注册 = 覆盖) */
  fun clearActions(activity: Activity) {
    listOf(ACTION_PRODUCT, ACTION_ORDER).forEach { HecongChat.unregisterAction(it) }
    toast(activity, "已撤掉所有自定义按钮")
  }

  // ---------------- 演示用脚手架(接入时不需要)----------------

  /** 弹个输入框收技能组名 —— 演示 APP 要能让租户填自己的组名,所以不写死 */
  private fun askSkillGroup(activity: Activity, title: String, hint: String, onOk: (String) -> Unit) {
    val input = EditText(activity).apply {
      this.hint = hint
      setText(lastGroup)
    }
    AlertDialog.Builder(activity)
      .setTitle(title)
      .setView(input)
      .setPositiveButton("确定") { _, _ ->
        val g = input.text.toString().trim()
        lastGroup = g
        onOk(g)
      }
      .setNegativeButton("取消", null)
      .show()
  }

  private fun toast(activity: Activity, message: String) = Toast.makeText(activity, message, Toast.LENGTH_LONG).show()

  internal const val ACTION_PRODUCT = "demo-product"
  internal const val ACTION_ORDER = "demo-order"

  /** 记住上次填的组名,方便反复试(演示用) */
  private var lastGroup: String = ""
}
