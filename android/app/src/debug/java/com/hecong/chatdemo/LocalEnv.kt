// local 档(仅 DEBUG 源集):内部联调渠道 + 本地插座 —— release 源集的同名文件是空实现,
// 发布构建**编译期**就不含这个渠道 ID(10883 是内部测试租户,绝不进发布版)。
//
// 本地环境:PORT=5175 pnpm demo:link + 本地后端 3024/17108;
// 安卓模拟器先跑:adb reverse tcp:5175 tcp:5175 && adb reverse tcp:3024 tcp:3024 && adb reverse tcp:17108 tcp:17108
// 骨架形态下 localhost 插座是 http → 壳自动放行混合内容(仅此调试形态,HecongChatView 注释)。
package com.hecong.chatdemo

internal object LocalEnv {
  val channelIdOrNull: String? = "01a00edc-a820-71d1-bf6a-dd78a494ac79"
  val loaderUrlOrNull: String? = "http://localhost:5175/hecong-link.js"
}
