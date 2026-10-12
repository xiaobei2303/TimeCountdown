# 时间倒计时 - Android 桌面小组件

一个轻量级的 Android 桌面小组件 APP，包含霓虹风格时间倒计时和可自定义的生日倒计时，支持多人生日独立设置。

## 功能特点

### 时间倒计时小组件（2x2，霓虹风格）
- 实时显示本年还剩、本月还剩、本周还剩、今天还剩
- 赛博朋克霓虹发光设计，四色边框（粉/青/橙/蓝）
- 今天还剩不足 100 分钟时自动切换为分钟显示
- 每分钟自动刷新

### 生日倒计时小组件（2x2，米黄风格）
- 可自定义标题（如茉莉生日、妈妈生日）
- 显示距离下一个生日还有多少天（每年自动重复）
- 显示从出生日起已出生多少天
- 支持多人生日：每个桌面组件独立保存设置，可同时添加多人生日
- 点击组件即可修改该组件的生日信息

## 效果图

![桌面小组件效果图](https://aka.doubaocdn.com/s/8oJIXRZ4qh)

## 下载安装

[点击下载 APK（v1.4，约62KB）](https://aka.doubaocdn.com/s/RdY852c8BC)

- 支持 Android 5.0（API 21）及以上
- APK 大小约 62KB，极轻量
- 安装后长按桌面空白，添加小组件，选择时间倒计时或生日倒计时

## 使用说明

### 添加时间倒计时小组件
1. 长按桌面空白处
2. 选择添加小组件
3. 找到时间倒计时，拖到桌面即可

### 添加生日倒计时小组件
1. 长按桌面空白处
2. 选择添加小组件
3. 找到生日倒计时，拖到桌面
4. 自动弹出配置界面，输入标题和出生日期，保存
5. 重复以上步骤可添加多个人的生日
6. 点击已添加的生日组件可随时修改

## 项目结构

```
android/app/src/main/
  AndroidManifest.xml
  java/com/countdown/app/
    MainActivity.java            - 主界面（生日设置）
    CountdownWidget.java         - 四格时间倒计时 widget
    AnniversaryWidget.java       - 生日倒计时 widget
    SettingsActivity.java        - 设置界面
    WidgetConfigureActivity.java - widget 配置界面
  res/
    layout/                      - 界面布局
    drawable/                    - 背景和边框
    values/                      - 字符串和样式
    xml/                         - widget 配置
www/index.html                   - 全屏 Web 版倒计时
```

## 构建说明

本项目使用 Android SDK 命令行工具手动构建，无需 Android Studio：

```bash
# 依赖：Java 11+, Android SDK (platform-33, build-tools 33.0.0), ECJ 编译器
bash build.sh
```

## 技术栈

- 原生 Android（Java）
- AppWidget / RemoteViews
- AlarmManager 定时刷新
- SharedPreferences 本地存储
- 纯 XML 布局 + Shape Drawable，无第三方依赖

## License

MIT License - 自由使用、修改和分发。
