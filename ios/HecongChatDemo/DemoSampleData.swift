// 演示用的商品 / 订单假数据(与安卓 DemoSampleData.kt 逐条同构)。
//
// 🟢 **接入时这一整个文件都不需要** —— 真实接入时这些数据来自**你自己的后端**
// (商品详情页的当前商品、订单列表接口返回的订单)。这里写死几条只是为了让演示 APP
// 不依赖任何后端就能看到效果。
//
// 🔴 **字段名必须照抄,写错不会报错、但会发送失败** —— 这是本文件最大的价值:
// 它是一份**能跑通的标准答案**。
// (踩过的坑:把 `productId` 写成 `id`、把金额写成 "¥129" 这样的字符串 ——
//  结果消息发出去是失败态、时间显示成 NaN,而且没有任何提示告诉你哪里错了。)
//
// **金额是「最小单位整数 + 币种」**:129 元 = `amount: 12900, currency: "CNY"`。
import Foundation

enum DemoSampleData {
  /// 演示站点前缀:真实接入换成你自己的域名 —— 用它来认出"这是我家的商品链接"
  static let demoSite = "https://demo.hecong.example"

  /// 商品:模拟"从商品详情页点联系客服"时,把在看的商品带进会话
  static func products() -> [[String: Any]] {
    [
      product("P20260812001", "轻量保温杯 500ml · 远山灰", 12900, "保温 12 小时,316 不锈钢内胆"),
      product("P20260812002", "降噪蓝牙耳机 Pro", 49900, "主动降噪 42dB,续航 30 小时"),
      product("P20260812003", "机械键盘 87 键 · 青轴", 35900, "热插拔轴体,支持三模连接"),
    ]
  }

  /// 订单:模拟"售后咨询"时,让访客直接选中要问哪一单
  static func orders() -> [[String: Any]] {
    [
      order("20260812007", "轻量保温杯 500ml · 远山灰", 12900, "shipped"),
      order("20260805112", "降噪蓝牙耳机 Pro", 49900, "delivered"),
    ]
  }

  /// 商品卡片:`title` 必填,其余可省(没有的字段直接别传,不用编假值)
  private static func product(
    _ id: String, _ title: String, _ priceCents: Int, _ desc: String
  ) -> [String: Any] {
    [
      "cardType": "product",
      "productId": id,
      "title": title,
      "price": money(priceCents),
      "description": desc,
      // 详情链接:**有它卡片才可点**。写成一眼能认出的形式,APP 侧好按前缀分流
      // (见 AppDelegate 的 handleOpenUrl:认出是商品链接就拦下来跳自己的原生页面)
      "detailUrl": "\(demoSite)/product/\(id)",
    ]
  }

  /// 订单卡片:比商品严格 —— orderId / title / total / status / createdAt / items 都要给。
  /// `status` 取值:pending / paid / shipped / delivered / refunded / cancelled。
  private static func order(
    _ orderId: String, _ title: String, _ totalCents: Int, _ status: String
  ) -> [String: Any] {
    [
      "cardType": "order",
      "orderId": orderId,
      "title": title,
      "total": money(totalCents),
      "status": status,
      // 演示用固定时间戳:真实接入填订单的真实下单时间(毫秒)
      "createdAt": 1_755_000_000_000,
      "items": [["name": title, "quantity": 1, "price": money(totalCents)]],
    ]
  }

  /// 金额:最小单位整数 + 币种(129 元 = 12900 分)
  private static func money(_ cents: Int) -> [String: Any] {
    ["amount": cents, "currency": "CNY"]
  }
}
