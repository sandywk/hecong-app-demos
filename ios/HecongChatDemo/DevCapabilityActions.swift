// 「开发者能力」这一组的动作实现:指定技能组 / 商品·订单选择器 / 自定义按钮。
// 与安卓 DevCapabilityActions.kt 逐条同构。
//
// 这三样跟别的示例有个本质区别:**它们在客服工作台里配不出来**,必须写代码。
// 原因是数据在租户自己的系统里(商品、订单),或者要由租户的业务逻辑决定(该谁接待)。
// 契约:sdk-public-api-contract.md §九 / app-sdk-plan.md §10.7
//
// 🔴 这一组同时也是**问题复现台**:租户报"我传技能组没效",让他用本 APP 填一次自己的组名 ——
// 这里有效 = 他自己那侧的接法有问题;这里也无效 = 组名/渠道配置的问题。二分掉一半排查面。
import HecongChatSDK
import UIKit

enum DevCapabilityActions {
  static let actionProduct = "demo-product"
  static let actionOrder = "demo-order"

  /// 记住上次填的组名,方便反复试(演示用)
  private static var lastGroup = ""

  /// **打开时就指定技能组**(启动档)。
  ///
  /// 接入时你要抄的就是这两行:`config.routing = HecongRouting(skillGroup:)` 然后照常打开。
  /// 值会拼进聊天页地址,**老版本 SDK 的壳也认**(纯 URL,不依赖新命令)。
  static func openWithSkillGroup(on host: UIViewController) {
    askSkillGroup(on: host, title: "打开客服并指定技能组", hint: "填你工作台里的技能组名称") { group in
      // 接入时就这一行:config.routing = HecongRouting(skillGroup: 组名)
      ChatLaunch.push(from: host, title: "在线客服", routing: HecongRouting(skillGroup: group))
    }
  }

  /// **聊天页开着时换组**(运行时档)。留空 = 清除指派。
  /// ⚠️ 指派在**新对话创建时**生效 —— 已经在进行中的对话不会被中途改派。
  static func switchSkillGroup(on host: UIViewController) {
    askSkillGroup(on: host, title: "切换技能组", hint: "留空 = 清除指派,回到默认分派") { group in
      if group.isEmpty {
        HecongChat.shared.setRouting(nil as String?)
        DemoStyle.toast("已清除技能组指派")
      } else {
        HecongChat.shared.setRouting(group)
        DemoStyle.toast("已指派到「\(group)」—— 下一次新对话生效")
      }
    }
  }

  /// **商品选择器**:附件面板加「商品」入口,点了弹商品列表。两步:
  ///   1. **这里**:`registerAction` 加入口按钮;
  ///   2. **在 `hecongChat(didClickAction:)` 回调里**(见 AppDelegate):先 `setPickerData`
  ///      回填你系统里的当前商品,再 `openPicker` 打开。
  ///
  /// ⚠️ 第 2 步的数据**必须在点击那一刻给**,不能提前:SDK 刻意不缓存选择器数据。
  static func demoProductPicker(on host: UIViewController) {
    HecongChat.shared.registerAction(id: actionProduct, label: "商品", icon: nil, slot: "attach")
    ChatLaunch.push(from: host)
    DemoStyle.toast("已加「商品」入口 —— 点输入框旁的 + 号就能看到")
  }

  /// **订单选择器**:同商品,换个数据源、换个位置(快捷区更显眼)。
  static func demoOrderPicker(on host: UIViewController) {
    HecongChat.shared.registerAction(id: actionOrder, label: "订单", icon: nil, slot: "quick")
    ChatLaunch.push(from: host)
    DemoStyle.toast("已加「订单」入口 —— 在输入框正上方")
  }

  /// 撤掉本页注册过的所有自定义按钮(同 id 再注册 = 覆盖,不会重复)
  static func clearActions() {
    for id in [actionProduct, actionOrder] {
      HecongChat.shared.unregisterAction(id)
    }
    DemoStyle.toast("已撤掉所有自定义按钮")
  }

  // ---------------- 演示用脚手架(接入时不需要)----------------

  /// 弹个输入框收技能组名 —— 演示 APP 要能让租户填自己的组名,所以不写死
  private static func askSkillGroup(
    on vc: UIViewController, title: String, hint: String, onOk: @escaping (String) -> Void
  ) {
    let alert = UIAlertController(title: title, message: nil, preferredStyle: .alert)
    alert.addTextField { field in
      field.placeholder = hint
      field.text = lastGroup
    }
    alert.addAction(UIAlertAction(title: "取消", style: .cancel))
    alert.addAction(
      UIAlertAction(title: "确定", style: .default) { _ in
        let g = (alert.textFields?.first?.text ?? "").trimmingCharacters(in: .whitespaces)
        lastGroup = g
        onOk(g)
      })
    vc.present(alert, animated: true)
  }
}
