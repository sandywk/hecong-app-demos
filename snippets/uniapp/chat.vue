<!-- uni-app 档 0 接入(demo 矩阵 #5):web-view 直接装 app 渠道对话链接 URL,零原生开发量。
     App 端 <web-view> 是原生 WebView;小程序端 web-view 走业务域名备案既有限制口径。
     边界与升档条件见 ../README.md「档 0 的共同边界」。 -->
<template>
  <web-view :src="chatUrl" />
</template>

<script>
export default {
  data() {
    return { userId: 'u123', userName: '张三' }
  },
  computed: {
    chatUrl() {
      // 工作台复制的对话链接;身份走 URL 参数(?u= 会员 ID,取值要"不可猜",详接入文档)
      const base = 'https://<对话链接域名>/<链接路径>'
      const q = new URLSearchParams({
        u: this.userId,
        n: this.userName,
        hh: '1', // APP 内嵌:隐藏 H5 标题栏(宿主自己有导航栏)
      })
      return `${base}?${q}`
    },
  },
}
</script>
