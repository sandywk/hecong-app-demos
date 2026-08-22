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

/// 演示台的输入行:单行文本框 / 多行文本域。**示范工程自己的脚手架**,接入时不需要。
final class DemoFieldCell: UITableViewCell, UITextFieldDelegate, UITextViewDelegate {
  static let reuseId = "DemoFieldCell"

  private let field = UITextField()
  private let area = UITextView()
  private var fieldConstraints: [NSLayoutConstraint] = []
  private var areaConstraints: [NSLayoutConstraint] = []
  private var onChange: ((String) -> Void)?

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    backgroundColor = DemoColor.surface
    selectionStyle = .none

    field.font = DemoFont.body
    field.textColor = DemoColor.ink
    field.delegate = self
    field.autocorrectionType = .no
    field.autocapitalizationType = .none
    field.clearButtonMode = .whileEditing
    field.addTarget(self, action: #selector(fieldChanged), for: .editingChanged)

    area.font = DemoFont.body
    area.textColor = DemoColor.ink
    area.backgroundColor = .clear
    area.delegate = self
    area.autocorrectionType = .no
    area.autocapitalizationType = .none
    area.textContainerInset = .zero
    area.textContainer.lineFragmentPadding = 0

    // 🔴 两个输入件的约束**分组保存、按需激活**:隐藏的视图仍然参与 Auto Layout,
    // 若两组同时激活,单行文本框那一行会被隐藏的多行文本域撑到三倍高(2026-08-20 实测)。
    for (input, group) in [(field, 0), (area, 1)] as [(UIView, Int)] {
      contentView.addSubview(input)
      input.translatesAutoresizingMaskIntoConstraints = false
      let pad = DemoMetric.cardPadding
      var group_constraints = [
        input.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: pad),
        input.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -pad),
        input.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 12),
        input.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -12),
      ]
      if group == 1 { group_constraints.append(input.heightAnchor.constraint(equalToConstant: 76)) }
      if group == 0 { fieldConstraints = group_constraints } else { areaConstraints = group_constraints }
    }
    contentView.heightAnchor.constraint(greaterThanOrEqualToConstant: DemoMetric.rowMinHeight)
      .isActive = true
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError() }

  func configure(
    text: String, placeholder: String, multiline: Bool, onChange: @escaping (String) -> Void
  ) {
    self.onChange = onChange
    field.isHidden = multiline
    area.isHidden = !multiline
    NSLayoutConstraint.deactivate(multiline ? fieldConstraints : areaConstraints)
    NSLayoutConstraint.activate(multiline ? areaConstraints : fieldConstraints)
    if multiline {
      area.text = text
    } else {
      field.text = text
      field.placeholder = placeholder
    }
  }

  @objc private func fieldChanged() { onChange?(field.text ?? "") }

  func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    textField.resignFirstResponder()
    return true
  }

  func textViewDidChange(_ textView: UITextView) { onChange?(textView.text ?? "") }
}
