// Android Java 档 1 接入(demo 矩阵 #4):验证壳公共面 Java 互操作(app-sdk-plan.md §8.1)。
// HecongChatListener 默认方法已编译成 JVM default method(库 -Xjvm-default=all),
// Java 实现方只覆写关心的回调即可。
package com.example.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hecong.chatsdk.HecongChatConfig;
import com.hecong.chatsdk.HecongChatListener;
import com.hecong.chatsdk.HecongChatView;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends Activity {
  private HecongChatView chat;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    HecongChatConfig config = new HecongChatConfig("https://<app渠道对话链接>");
    chat = new HecongChatView(this); // 必须 Activity context(文件选择/权限都要它)
    chat.listener = new HecongChatListener() {
      @Override
      public void onUnreadChanged(int count) { /* 更新角标 */ }
    };
    setContentView(chat);
    chat.load(config); // 合规:用户同意隐私政策后再调,此前 SDK 零活动

    try {
      // ready 前调用会自动排队,ready 后补发
      chat.identify("u123", new JSONObject().put("name", "张三"), null);
    } catch (JSONException ignored) {
    }
  }

  // 三个转发缺一不可:文件选择(不做 = 点发图片没反应)/ 运行时权限(不做 = 语音被拒)/ 销毁
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (chat.handleFileChooserResult(requestCode, resultCode, data)) return;
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (chat.handlePermissionsResult(requestCode, permissions, grantResults)) return;
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onDestroy() {
    chat.destroy();
    super.onDestroy();
  }
}
