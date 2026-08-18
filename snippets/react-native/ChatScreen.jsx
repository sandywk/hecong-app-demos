// React Native 档 0 接入(demo 矩阵 #6):react-native-webview 直接装 app 渠道对话链接。
// 依赖:npm i react-native-webview。档 2 原生 wrapper(桥/角标/稳定锚点)按租户诉求再做。
// 边界与升档条件见 ../README.md「档 0 的共同边界」。
import React from 'react'
import { WebView } from 'react-native-webview'

const BASE = 'https://<对话链接域名>/<链接路径>'

export default function ChatScreen({ userId, userName }) {
  const q = new URLSearchParams({
    u: userId, // 会员 ID(取值要"不可猜",详接入文档)
    n: userName,
    hh: '1', // APP 内嵌:隐藏 H5 标题栏(宿主自己有导航栏)
  })
  return (
    <WebView
      source={{ uri: `${BASE}?${q}` }}
      // 语音/视频消息需要:媒体内联播放 + 免手势自动播放
      allowsInlineMediaPlayback
      mediaPlaybackRequiresUserAction={false}
      // 访客身份存 localStorage,必须开(不开每次都是新访客)
      domStorageEnabled
    />
  )
}
