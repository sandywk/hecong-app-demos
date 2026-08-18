// iOS ObjC 档 1 接入(demo 矩阵 #2):验证壳公共面 ObjC 互操作(app-sdk-plan.md §8.1)。
// 壳公共类全部 NSObject 系 + @objc 导出,ObjC 工程经 Swift 桥接头直接用。
#import "HCChatDemoViewController.h"
#import <HecongChatSDK/HecongChatSDK-Swift.h>

@interface HCChatDemoViewController () <HecongChatDelegate>
@property (nonatomic, strong) HecongChatViewController *chat;
@end

@implementation HCChatDemoViewController

- (void)openChat {
  // 只要渠道 ID(工作台的 App 渠道页复制)——**不是对话链接 URL**:页面由壳内置骨架承载,
  // 渠道 ID 决定连哪个渠道(2026-08-17 改版,租户零域名接入)
  HecongChatConfig *config = [[HecongChatConfig alloc] initWithChannelId:@"你的渠道ID"];
  // 深浅色默认跟随宿主 App(host 档),一般不用设;要让渠道后台说了算才填 @"auto"

  self.chat = [[HecongChatViewController alloc] initWithConfig:config];
  self.chat.delegate = self;
  [self.navigationController pushViewController:self.chat animated:YES];

  // ready 前调用会自动排队,ready 后补发 —— 不需要等回调再调
  [self.chat identifyWithUserId:@"u123" profile:@{ @"name": @"张三" } data:nil];

  // 也可以**不打开聊天页就先绑身份**(登录成功那一刻调,之后进客服自动带上):
  // [[HecongChat shared] identifyWithUserId:@"u123" profile:@{ @"name": @"张三" } data:nil];
}

#pragma mark - HecongChatDelegate(全部可选)

- (void)hecongChatUnreadDidChange:(NSInteger)count {
  // 更新 tab 角标 / 入口红点
}

- (BOOL)hecongChatWithHandleOpenUrl:(NSURL *)url {
  return NO; // NO = 壳跳系统浏览器;YES = 宿主自行处理(如内开 SFSafariViewController)
}

// 会话事件通吃入口(消息到达 / 对话起止 / 网络通断)——**优先接这个**:
// 以后 H5 新增的事件不用升级 SDK 就能收到。事件名与网页版 hc.on() 同名。
- (void)hecongChatWithDidReceiveEvent:(NSString *)name payload:(NSDictionary<NSString *, id> *)payload {
  // 例:[@"message:incoming" isEqualToString:name] → 弹本地通知
}

@end
