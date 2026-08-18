// 组件层 · 列表单元 —— 页面只组装这些件,不自己摆像素。
//
// 落地纪律(设计稿第 4 屏):行高 ≥48,左图标章 34 / 圆角 10,右 chevron 用 chevron token 色,
// 强调色只用一个(选中态 / 图标章 / 未读红点)。空字段整行隐藏,不留空占位。
//
// 用 `.insetGrouped` 列表 + 本文件的单元 = 设计稿那种"白底 16 圆角卡片,靠页面底色分层"。
import UIKit

/// 标准列表行:图标章(可选)+ 标题(+ 说明)+ 未读徽标 / 开关 / chevron
final class DemoListCell: UITableViewCell {
  static let reuseId = "DemoListCell"

  private let chip = UIView()
  private let chipIcon = UIImageView()
  private let titleLabel = UILabel()
  private let subtitleLabel = UILabel()
  private let badge = DemoBadgeView()
  private let texts = UIStackView()
  private let content = UIStackView()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    backgroundColor = DemoColor.surface
    titleLabel.font = DemoFont.body
    titleLabel.textColor = DemoColor.ink
    titleLabel.numberOfLines = 0
    subtitleLabel.font = DemoFont.caption
    subtitleLabel.textColor = DemoColor.ink2
    subtitleLabel.numberOfLines = 0

    chip.layer.cornerRadius = DemoMetric.chipRadius
    chip.clipsToBounds = true
    chipIcon.contentMode = .center
    chip.addSubview(chipIcon)

    texts.axis = .vertical
    texts.spacing = 3
    texts.addArrangedSubview(titleLabel)
    texts.addArrangedSubview(subtitleLabel)

    content.axis = .horizontal
    content.alignment = .center
    content.spacing = 12
    content.addArrangedSubview(chip)
    content.addArrangedSubview(texts)
    content.addArrangedSubview(badge)
    contentView.addSubview(content)

    content.translatesAutoresizingMaskIntoConstraints = false
    chipIcon.translatesAutoresizingMaskIntoConstraints = false
    let pad = DemoMetric.cardPadding
    NSLayoutConstraint.activate([
      content.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: pad),
      content.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -pad),
      content.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 12),
      content.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -12),
      contentView.heightAnchor.constraint(greaterThanOrEqualToConstant: DemoMetric.rowMinHeight),
      chip.widthAnchor.constraint(equalToConstant: DemoMetric.chipSize),
      chip.heightAnchor.constraint(equalToConstant: DemoMetric.chipSize),
      chipIcon.centerXAnchor.constraint(equalTo: chip.centerXAnchor),
      chipIcon.centerYAnchor.constraint(equalTo: chip.centerYAnchor),
    ])
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError() }

  /// [accessory] 传自定义控件(如开关)时不画 chevron
  func configure(
    title: String, subtitle: String? = nil, icon: String? = nil, accent: Bool = false,
    badgeCount: Int = 0, accessory: UIView? = nil, showsChevron: Bool = true
  ) {
    titleLabel.text = title
    subtitleLabel.text = subtitle
    subtitleLabel.isHidden = (subtitle?.isEmpty ?? true) // 空字段整行隐藏,不留空占位

    chip.isHidden = icon == nil
    if let icon = icon {
      chip.backgroundColor = accent ? DemoColor.accentSoft : DemoColor.background
      chipIcon.image = DemoIcon.image(icon, size: 15, weight: .medium)
      chipIcon.tintColor = accent ? DemoColor.accent : DemoColor.ink2
    }

    badge.count = badgeCount
    badge.isHidden = badgeCount <= 0

    if let accessory = accessory {
      accessoryView = accessory
      accessoryType = .none
    } else if showsChevron {
      accessoryView = nil
      let arrow = UIImageView(image: DemoIcon.image(DemoIcon.chevronRight, size: 13, weight: .semibold))
      arrow.tintColor = DemoColor.chevron
      arrow.sizeToFit()
      accessoryView = arrow
    } else {
      accessoryView = nil
      accessoryType = .none
    }
  }
}

/// 未读徽标:红底白字圆点
final class DemoBadgeView: UIView {
  private let label = UILabel()

  var count: Int = 0 {
    didSet { label.text = count > 99 ? "99+" : "\(count)" }
  }

  init() {
    super.init(frame: .zero)
    backgroundColor = DemoColor.danger
    layer.cornerRadius = 10
    label.font = .systemFont(ofSize: 11, weight: .bold)
    label.textColor = .white
    label.textAlignment = .center
    addSubview(label)
    label.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      heightAnchor.constraint(equalToConstant: 20),
      widthAnchor.constraint(greaterThanOrEqualToConstant: 20),
      label.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 5),
      label.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -5),
      label.centerYAnchor.constraint(equalTo: centerYAnchor),
    ])
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError() }
}

/// 个人中心顶部的头像卡(演示用的假资料,接入时不需要)
final class DemoProfileCell: UITableViewCell {
  static let reuseId = "DemoProfileCell"

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    backgroundColor = DemoColor.surface

    let avatar = UILabel()
    avatar.text = "演"
    avatar.textAlignment = .center
    avatar.textColor = .white
    avatar.font = .systemFont(ofSize: 17, weight: .medium)
    avatar.backgroundColor = DemoColor.accent
    avatar.layer.cornerRadius = 26
    avatar.clipsToBounds = true

    let name = UILabel()
    name.text = DemoConfig.demoUserName
    name.font = DemoFont.title
    name.textColor = DemoColor.ink
    let memberId = UILabel()
    memberId.text = "会员 ID · \(DemoConfig.demoUserId)"
    memberId.font = DemoFont.caption
    memberId.textColor = DemoColor.ink2

    let texts = UIStackView(arrangedSubviews: [name, memberId])
    texts.axis = .vertical
    texts.spacing = 4

    let row = UIStackView(arrangedSubviews: [avatar, texts])
    row.axis = .horizontal
    row.alignment = .center
    row.spacing = 14
    contentView.addSubview(row)

    row.translatesAutoresizingMaskIntoConstraints = false
    let pad = DemoMetric.cardPadding
    NSLayoutConstraint.activate([
      row.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: pad),
      row.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -pad),
      row.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 16),
      row.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -16),
      avatar.widthAnchor.constraint(equalToConstant: 52),
      avatar.heightAnchor.constraint(equalToConstant: 52),
    ])
    selectionStyle = .none
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError() }
}
