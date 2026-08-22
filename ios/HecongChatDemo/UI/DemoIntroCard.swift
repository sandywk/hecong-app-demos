// 首页顶部说明卡(示范工程自己的脚手架,接入时不需要)。
//
// 租户从 TestFlight 装完点开,第一眼是「界面形态」列表 —— 这张卡回答"这是什么、怎么用",
// 并直接给出唯一要做的动作(填渠道 ID)。只放首页、不可关闭、同色系不打扰(owner 2026-08-21)。
import UIKit

final class DemoIntroCard: UIView {
  private let onAction: () -> Void
  private let card = UIView()

  init(onAction: @escaping () -> Void) {
    self.onAction = onAction
    super.init(frame: .zero)
    build()
  }

  @available(*, unavailable) required init?(coder: NSCoder) { fatalError() }

  private func build() {
    card.backgroundColor = DemoColor.accentSoft
    card.layer.cornerRadius = DemoMetric.cardRadius
    addSubview(card)

    let chip = UIView()
    chip.backgroundColor = DemoColor.accent
    chip.layer.cornerRadius = DemoMetric.chipRadius
    let chipIcon = UIImageView(image: DemoIcon.image(DemoIcon.support, size: 16, weight: .semibold))
    chipIcon.tintColor = .white
    chipIcon.contentMode = .center
    chip.addSubview(chipIcon)

    let title = UILabel()
    title.text = "合从客服 SDK · Demo"
    title.font = DemoFont.body
    title.textColor = DemoColor.ink

    let body = UILabel()
    body.text = "演示原生 App 接入合从客服的各种承载形态与能力。填入你的渠道 ID,即可连接到自己的工作台进行测试。"
    body.font = DemoFont.caption
    body.textColor = DemoColor.ink2
    body.numberOfLines = 0

    let action = UIButton(type: .system)
    action.setTitle("填渠道 ID", for: .normal)
    action.titleLabel?.font = UIFont.systemFont(ofSize: 13, weight: .semibold)
    action.tintColor = DemoColor.accent
    action.contentHorizontalAlignment = .leading
    action.addTarget(self, action: #selector(tapped), for: .touchUpInside)

    let texts = UIStackView(arrangedSubviews: [title, body, action])
    texts.axis = .vertical
    texts.spacing = 4
    texts.setCustomSpacing(2, after: body)

    let row = UIStackView(arrangedSubviews: [chip, texts])
    row.axis = .horizontal
    row.alignment = .top
    row.spacing = 12
    card.addSubview(row)

    for v in [card, chip, chipIcon, row] { v.translatesAutoresizingMaskIntoConstraints = false }
    let pad = DemoMetric.cardPadding
    NSLayoutConstraint.activate([
      // 与 insetGrouped 卡片同一条左右边线(iPhone 上系统卡片左右内缩 20pt,实测对齐)
      card.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20),
      card.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20),
      card.topAnchor.constraint(equalTo: topAnchor, constant: 4),
      card.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -6),
      row.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: pad),
      row.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -pad),
      row.topAnchor.constraint(equalTo: card.topAnchor, constant: 14),
      row.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -10),
      chip.widthAnchor.constraint(equalToConstant: DemoMetric.chipSize),
      chip.heightAnchor.constraint(equalToConstant: DemoMetric.chipSize),
      chipIcon.centerXAnchor.constraint(equalTo: chip.centerXAnchor),
      chipIcon.centerYAnchor.constraint(equalTo: chip.centerYAnchor),
    ])
  }

  @objc private func tapped() { onAction() }

  /// 作为 tableHeaderView 使用时,UITableView 不会自动量高,挂载方在布局后调一次
  func fitHeight(width: CGFloat) {
    let target = CGSize(width: width, height: UIView.layoutFittingCompressedSize.height)
    let h = systemLayoutSizeFitting(
      target, withHorizontalFittingPriority: .required, verticalFittingPriority: .fittingSizeLevel
    ).height
    if frame.height != h || frame.width != width { frame = CGRect(x: 0, y: 0, width: width, height: h) }
  }
}
