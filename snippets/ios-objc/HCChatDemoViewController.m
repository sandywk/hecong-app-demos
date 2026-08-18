// iOS ObjC 档 1 接入(demo 矩阵 #2):验证壳公共面 ObjC 互操作(app-sdk-plan.md §8.1)。
// 壳公共类全部 NSObject 系 + @objc 导出,ObjC 工程经 Swift 桥接头直接用。
#import "HCChatDemoViewController.h"
#import <HecongChatSDK/HecongChatSDK-Swift.h>

@interface HCChatDemoViewController () <HecongChatDelegate>
@property (nonatomic, strong) HecongChatViewController *chat;
@end

@implementation HCChatDemoViewController

- (void)openChat {
  NSURL *url = [NSURL URLWithString:@"https://<app渠道对话链接>"];
  HecongChatConfig *config = [[HecongChatConfig alloc] initWithUrl:url];
  config.colorScheme = @"auto";

  self.chat = [[HecongChatViewController alloc] initWithConfig:config];
  self.chat.delegate = self;
  [self.navigationController pushViewController:self.chat animated:YES];

  // ready 前调用会自动排队,ready 后补发 —— 不需要等回调再调
  [self.chat identifyWithUserId:@"u123" profile:@{ @"name": @"张三" } data:nil];
}

#pragma mark - HecongChatDelegate(全部可选)

- (void)hecongChatUnreadDidChange:(NSInteger)count {
  // 更新 tab 角标 / 入口红点
}

- (BOOL)hecongChatWithHandleOpenUrl:(NSURL *)url {
  return NO; // NO = 壳跳系统浏览器;YES = 宿主自行处理(如内开 SFSafariViewController)
}

@end
