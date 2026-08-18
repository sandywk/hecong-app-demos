// 极小的异步头像加载(URL → UIImage)。
//
// ⚠️ 真实接入建议用 SDWebImage / Kingfisher,别自己写 —— 这里手写只是因为示范工程要保持
// **零第三方依赖**(与 SDK 本体同调性)。只覆盖演示需要的部分:内存缓存 + 失败回退。
import UIKit

enum AvatarLoader {
  private static var cache: [String: UIImage] = [:]

  /// 加载 [url] 并回调到主线程;失败或图坏了回调 nil(调用方据此回退到文字首字)。
  ///
  /// `token` 用来对齐"这次请求还是不是最新的" —— 身份连续变化时(渠道 → 客服 → 转接),
  /// 回来的旧图不会盖掉新图。
  static func load(_ url: String, token: @escaping () -> String?, completion: @escaping (UIImage?) -> Void) {
    if let cached = cache[url] {
      completion(cached)
      return
    }
    guard let parsed = URL(string: url) else {
      completion(nil)
      return
    }
    URLSession.shared.dataTask(with: parsed) { data, _, _ in
      let image = data.flatMap(UIImage.init(data:))
      DispatchQueue.main.async {
        guard token() == url else { return }
        if let image = image { cache[url] = image }
        completion(image)
      }
    }.resume()
  }
}
