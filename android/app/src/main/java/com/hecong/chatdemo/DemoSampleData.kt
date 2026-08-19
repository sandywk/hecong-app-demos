// 演示用的商品 / 订单假数据。
//
// 🟢 **接入时这一整个文件都不需要** —— 真实接入时这些数据来自**你自己的后端**
// (商品详情页的当前商品、订单列表接口返回的订单)。这里写死几条只是为了让演示 APP
// 不依赖任何后端就能看到效果。
//
// 🔴 **字段名必须照抄,写错不会报错、但会发送失败** —— 这是本文件最大的价值:
// 它是一份**能跑通的标准答案**。字段口径见接入文档「商品/订单选择器」一节。
// (踩过的坑:把 `productId` 写成 `id`、把金额写成 "¥129" 这样的字符串 ——
//  结果消息发出去是失败态,时间显示成 NaN,而且没有任何提示告诉你哪里错了。)
//
// **金额是「最小单位整数 + 币种」**:129 元 = `amount: 12900, currency: "CNY"`。
// 不用小数,免得各端对不齐小数位数。
package com.hecong.chatdemo

import org.json.JSONArray
import org.json.JSONObject

object DemoSampleData {
  /** 商品:模拟"从商品详情页点联系客服"时,把在看的商品带进会话 */
  fun products(): JSONArray =
    JSONArray().apply {
      put(product("P20260812001", "轻量保温杯 500ml · 远山灰", 12900, "保温 12 小时,316 不锈钢内胆"))
      put(product("P20260812002", "降噪蓝牙耳机 Pro", 49900, "主动降噪 42dB,续航 30 小时"))
      put(product("P20260812003", "机械键盘 87 键 · 青轴", 35900, "热插拔轴体,支持三模连接"))
    }

  /** 订单:模拟"售后咨询"时,让访客直接选中要问哪一单 */
  fun orders(): JSONArray =
    JSONArray().apply {
      put(
        order(
          orderId = "20260812007",
          title = "轻量保温杯 500ml · 远山灰",
          totalCents = 12900,
          status = "shipped",
          itemName = "轻量保温杯 500ml · 远山灰",
        ),
      )
      put(
        order(
          orderId = "20260805112",
          title = "降噪蓝牙耳机 Pro",
          totalCents = 49900,
          status = "delivered",
          itemName = "降噪蓝牙耳机 Pro",
        ),
      )
    }

  /** 商品卡片:`title` 必填,其余可省(没有的字段直接别传,不用编假值) */
  private fun product(id: String, title: String, priceCents: Int, desc: String): JSONObject =
    JSONObject()
      .put("cardType", "product")
      .put("productId", id)
      .put("title", title)
      .put("price", money(priceCents))
      .put("description", desc)
      // 详情链接:**有它卡片才可点**。写成一眼能认出的形式,APP 侧好按前缀分流
      // (见 DemoApp 的 onOpenUrl:认出是商品链接就拦下来跳自己的原生页面)
      .put("detailUrl", "$DEMO_SITE/product/$id")

  /**
   * 订单卡片:比商品严格 —— `orderId` / `title` / `total` / `status` / `createdAt` / `items`
   * 都要给。`status` 取值:pending / paid / shipped / delivered / refunded / cancelled。
   */
  private fun order(
    orderId: String,
    title: String,
    totalCents: Int,
    status: String,
    itemName: String,
  ): JSONObject =
    JSONObject()
      .put("cardType", "order")
      .put("orderId", orderId)
      .put("title", title)
      .put("total", money(totalCents))
      .put("status", status)
      // 演示用固定时间戳:真实接入填订单的真实下单时间(毫秒)
      .put("createdAt", 1_755_000_000_000L)
      .put(
        "items",
        JSONArray().put(
          JSONObject().put("name", itemName).put("quantity", 1).put("price", money(totalCents)),
        ),
      )

  /** 演示站点前缀:真实接入换成你自己的域名 —— 用它来认出"这是我家的商品链接" */
  const val DEMO_SITE = "https://demo.hecong.example"

  /** 金额:最小单位整数 + 币种(129 元 = 12900 分) */
  private fun money(cents: Int): JSONObject =
    JSONObject().put("amount", cents).put("currency", "CNY")
}
