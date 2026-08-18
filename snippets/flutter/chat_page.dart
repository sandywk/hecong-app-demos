// Flutter 档 0 接入(demo 矩阵 #7):webview_flutter 直接装 app 渠道对话链接。
// 依赖:webview_flutter ^4。档 2 plugin 按租户诉求再做。
// 边界与升档条件见 ../README.md「档 0 的共同边界」。
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({super.key, required this.userId, required this.userName});
  final String userId;
  final String userName;

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  late final WebViewController _controller;

  @override
  void initState() {
    super.initState();
    final url = Uri.https('<对话链接域名>', '/<链接路径>', {
      'u': widget.userId, // 会员 ID(取值要"不可猜",详接入文档)
      'n': widget.userName,
      'hh': '1', // APP 内嵌:隐藏 H5 标题栏(宿主自己有导航栏)
    });
    _controller = WebViewController()
      // 不开 JS 整个页面不工作(webview_flutter 默认 disabled)
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..loadRequest(url);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('在线客服')),
      body: WebViewWidget(controller: _controller),
    );
  }
}
