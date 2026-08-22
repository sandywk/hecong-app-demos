// Tab ③「高级扩展」——对应接入文档 L4。
//
// 本页各项的共同点:**工作台中没有对应配置项,必须由宿主编码实现** ——
// 数据在宿主自己的系统里(商品、订单),或需由宿主业务逻辑决定(该由谁接待)。
// 契约:`sdk-public-api-contract.md §九` / `app-sdk-plan.md §10.7`。
//
// 同时这里也是**问题复现台**:租户反馈「技能组指派无效」时,让其在本 App 填入自己的组名 ——
// 此处有效说明宿主侧接法有误,此处同样无效则是组名或渠道配置问题,可二分掉一半排查面。
package com.hecong.chatdemo

import android.app.Activity

fun advancedPage(activity: Activity): ScenePage = ScenePage(activity, "高级扩展") {
  listOf(
    DemoSceneGroup(
      "技能组指派",
      "组名取自工作台的技能组名称(允许中文,SDK 自动编码)。指派在新对话创建时生效,进行中的对话不会被改派。",
      listOf(
        DemoScene(
          "启动时指派", "config.routing —— 值随聊天页地址下发,旧版本壳同样识别",
          R.drawable.ic_git_branch, handler = { DevCapabilityActions.openWithSkillGroup(it) },
        ),
        DemoScene(
          "会话中切换", "setRouting —— 咨询过程中转接至专业组;传空值可清除指派",
          R.drawable.ic_git_branch, handler = { DevCapabilityActions.switchSkillGroup(it) },
        ),
      ),
    ),
    DemoSceneGroup(
      "输入区扩展",
      "选择器数据必须在 onActionClick 回调中实时提供,SDK 刻意不做缓存 —— 商品与订单列表随登录态和库存变化,重放陈旧数据会把错误卡片发给客服。",
      listOf(
        DemoScene(
          "商品选择器", "在附件面板注册入口,点击后由宿主回填商品列表",
          R.drawable.ic_shopping_bag, handler = { DevCapabilityActions.demoProductPicker(it) },
        ),
        DemoScene(
          "订单选择器", "在快捷区注册入口,适用于售后咨询直接指定订单",
          R.drawable.ic_package, handler = { DevCapabilityActions.demoOrderPicker(it) },
        ),
        DemoScene(
          "注销自定义按钮", "unregisterAction —— 同 id 重复注册为覆盖,不会产生重复项",
          R.drawable.ic_trash_2, handler = { DevCapabilityActions.clearActions(it) },
        ),
      ),
    ),
  )
}
