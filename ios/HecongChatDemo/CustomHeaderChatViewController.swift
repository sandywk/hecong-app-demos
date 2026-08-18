// 场景示范:**自己画标题栏**(设计稿 03)。
//
// 形态 = 左返回 + 头像 + 昵称/签名,下面才是聊天页。很多 App 的顶栏要跟自家设计语言统一,
// 不想用聊天页自带的那条 —— 这份代码就是那种情况的标准答案。
//
// 三件必做:
//   ① 打开时声明"标题栏我自己画",聊天页就不画自己的那条(不声明的症状:上下两条标题栏)
//   ② 接 hecongChatHeaderIdentityDidChange 拿头像/昵称/签名 —— 这份数据只有聊天页里有,而且
//      **会变**:会话开始前是渠道身份 → 客服接待后变成客服 → 转接再变一次
//   ③ pending 为真时画骨架占位;空字段整行隐藏,别留空
//      不做的症状:标题栏"先空一下再跳出名字",或某个字段没配时整栏看着歪掉
//   ④ 返回键先问 SDK —— 聊天页里开着图片预览时,返回 = 只关那一层,不是退出整个客服页
//
// 顶栏右侧**刻意不放关闭按钮**:左边已经有返回箭头,同一个页面两个出口是多余的
// (底部弹层那一档没有返回键,所以那里保留了关闭按钮)。
import HecongChatSDK
import UIKit

final class CustomHeaderChatViewController: UIViewController, HecongChatDelegate {
  private let chat: HecongChatViewController
  private let avatarView = UIImageView()
  private let avatarLabel = UILabel()
  private let nameLabel = UILabel()
  private let signLabel = UILabel()
  private var pendingAvatarUrl: String?

  init() {
    // ① hh=1:标题栏我自己画,聊天页别再画一条
    chat = HecongChatViewController(config: DemoConfig.buildChatConfig(extraQuery: ["hh": "1"]))
    super.init(nibName: nil, bundle: nil)
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError("use init()") }

  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = DemoColor.surface
    navigationController?.setNavigationBarHidden(true, animated: false) // 用我自己的顶栏

    let header = buildHeader()
    view.addSubview(header)

    // ② 身份回调走本页
    chat.delegate = self
    addChild(chat)
    view.addSubview(chat.view)
    chat.didMove(toParent: self)

    header.translatesAutoresizingMaskIntoConstraints = false
    chat.view.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      header.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
      header.leadingAnchor.constraint(equalTo: view.leadingAnchor),
      header.trailingAnchor.constraint(equalTo: view.trailingAnchor),
      header.heightAnchor.constraint(equalToConstant: 56),
      chat.view.topAnchor.constraint(equalTo: header.bottomAnchor),
      chat.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
      chat.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
      chat.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
    ])

    // SDK 缓存的那份:页面重建时立刻画对,不用干等下一次变化
    if let cached = HecongChat.shared.headerIdentity { render(cached) }
  }

  private func buildHeader() -> UIView {
    let bar = UIView()
    bar.backgroundColor = DemoColor.surface

    let back = UIButton(type: .system)
    back.setImage(DemoIcon.image(DemoIcon.chevronLeft, size: 19, weight: .semibold), for: .normal)
    back.tintColor = DemoColor.ink
    back.addTarget(self, action: #selector(goBack), for: .touchUpInside)

    let size = DemoMetric.avatarSize
    avatarView.contentMode = .scaleAspectFill
    avatarView.layer.cornerRadius = size / 2
    avatarView.clipsToBounds = true
    avatarView.backgroundColor = DemoColor.accent
    avatarLabel.textAlignment = .center
    avatarLabel.textColor = .white
    avatarLabel.font = .systemFont(ofSize: 13, weight: .medium)
    avatarView.addSubview(avatarLabel)

    nameLabel.font = .systemFont(ofSize: 15, weight: .semibold)
    nameLabel.textColor = DemoColor.ink
    signLabel.font = .systemFont(ofSize: 11)
    signLabel.textColor = DemoColor.ink2

    let texts = UIStackView(arrangedSubviews: [nameLabel, signLabel])
    texts.axis = .vertical
    texts.spacing = 2

    let row = UIStackView(arrangedSubviews: [back, avatarView, texts])
    row.axis = .horizontal
    row.alignment = .center
    row.spacing = 10
    bar.addSubview(row)

    row.translatesAutoresizingMaskIntoConstraints = false
    avatarLabel.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      row.leadingAnchor.constraint(equalTo: bar.leadingAnchor, constant: 14),
      row.trailingAnchor.constraint(lessThanOrEqualTo: bar.trailingAnchor, constant: -14),
      row.centerYAnchor.constraint(equalTo: bar.centerYAnchor),
      back.widthAnchor.constraint(equalToConstant: 26),
      avatarView.widthAnchor.constraint(equalToConstant: size),
      avatarView.heightAnchor.constraint(equalToConstant: size),
      avatarLabel.centerXAnchor.constraint(equalTo: avatarView.centerXAnchor),
      avatarLabel.centerYAnchor.constraint(equalTo: avatarView.centerYAnchor),
    ])
    return bar
  }

  /// ④ 返回键必须先问 SDK:聊天页里开着图片预览 / 浮层时,返回 = 只关那一层,
  /// 直接 pop 会把访客踢出整个客服页。安卓侧同款 handleBackPressed。
  @objc private func goBack() {
    if chat.handleBackPressed() { return }
    navigationController?.popViewController(animated: true)
  }

  // MARK: - HecongChatDelegate

  func hecongChatHeaderIdentityDidChange(_ identity: HecongHeaderIdentity) {
    render(identity)
  }

  /// ③ 三态:pending 画骨架;有值才显示,空字段整行让位(与聊天页的条件渲染同款)
  private func render(_ identity: HecongHeaderIdentity) {
    let pending = identity.pending
    nameLabel.text = pending ? nil : identity.nickname
    nameLabel.isHidden = nameLabel.text?.isEmpty ?? true
    signLabel.text = pending ? nil : identity.signature
    signLabel.isHidden = signLabel.text?.isEmpty ?? true
    renderAvatar(pending: pending, url: identity.avatar, initial: identity.nickname?.prefix(1))
  }

  /// 有图就真加载图,加载中/失败退回文字首字 —— 真实接入换成 SDWebImage / Kingfisher
  private func renderAvatar(pending: Bool, url: String?, initial: Substring?) {
    if pending {
      avatarView.image = nil
      avatarView.backgroundColor = DemoColor.skeleton
      avatarLabel.text = nil
      pendingAvatarUrl = nil
      return
    }
    avatarView.image = nil
    avatarView.backgroundColor = DemoColor.accent
    avatarLabel.text = initial.map(String.init) ?? "客"
    guard let url = url, !url.isEmpty else {
      pendingAvatarUrl = nil
      return
    }
    pendingAvatarUrl = url
    AvatarLoader.load(url, token: { [weak self] in self?.pendingAvatarUrl }) { [weak self] image in
      guard let self = self, let image = image else { return } // 失败保留文字首字兜底
      self.avatarView.image = image
      self.avatarLabel.text = nil
    }
  }
}
