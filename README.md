# 汇率计算器（Currency Calculator）

使用 **Kotlin + Jetpack Compose + Material Design 3** 开发的 Android 汇率换算应用，
针对 **手机 / 平板 / 折叠屏** 做了完整的自适应布局。

## 功能特性

| 功能 | 说明 |
|------|------|
| 实时汇率换算 | 数据来自欧洲央行（ECB）每日参考汇率，经 Frankfurter 开放 API 获取，免密钥 |
| 30 种常用货币 | 美元、人民币、欧元、日元、英镑、港元、韩元等，含中文名称与符号 |
| 一键互换币种 | 点击中间 ⇅ 按钮互换源/目标币种 |
| 汇率一览表 | 以任意基准币种查看全部货币汇率，点选即设为换算目标 |
| 离线缓存 | 网络不可用时自动使用最近一次汇率（30 分钟内视为有效） |
| Material Design 3 | 支持 Material You 动态取色（Android 12+）、深色模式、大字号 |
| 快捷金额 | 100 / 500 / 1000 / 5000 一键填入 |
| **平板自适应布局** | 按窗口宽度三档自动切换（见下节） |

## 自适应布局（平板 / 折叠屏 / 分屏）

| 窗口宽度 | 场景 | 布局 |
|----------|------|------|
| < 600dp（compact） | 手机竖屏 | 顶部 Tab + 单栏 |
| 600–840dp（medium） | 手机横屏 / 平板竖屏 | 侧边 NavigationRail + 单栏（内容居中限宽 640dp） |
| ≥ 840dp（expanded） | 平板横屏 / 折叠屏展开 | **双栏并排**：左「换算」+ 右「汇率表」，各自独立滚动 |

- 以**窗口宽度**（而非屏幕宽度）判定，分屏、悬浮窗场景同样正确
- 满足 Android 17（targetSdk 37）对大屏设备强制自适应的要求（不再锁定屏幕方向/尺寸）

## 技术栈

- 语言：Kotlin 2.4.21（AGP 9 内置 Kotlin + Compose 编译器插件）
- UI：Jetpack Compose + Material 3（compose-bom 2026.11.01）
- 架构：ViewModel + StateFlow 单向数据流
- 网络：OkHttp 4.12（主备双域名容错）
- 构建：Gradle 9.6 / Android Gradle Plugin 9.5.2 / JDK 17+
- 最低支持：**Android 9（API 28）**，目标 **Android 17（API 37）**

## 项目结构

```
app/src/main/java/com/exchangerate/calculator/
├── MainActivity.kt              # 入口 + 三档自适应布局（Tab / NavRail / 双栏）
├── MainViewModel.kt             # 换算输入与汇率加载状态
├── data/
│   ├── Currency.kt              # 币种目录（代码/中文名/符号）
│   ├── RatesApi.kt              # Frankfurter API 封装
│   └── RatesRepository.kt       # 网络 + SharedPreferences 缓存
└── ui/
    ├── theme/                   # Material 3 主题（配色/字体）
    ├── components/CurrencyDropdown.kt
    ├── WindowWidthClass.kt      # 窗口宽度分级（compact/medium/expanded）
    ├── ConverterScreen.kt       # 换算页
    ├── RatesTableScreen.kt      # 汇率表页
    └── FormatUtils.kt           # 数字格式化
```

## 构建方式

### Android Studio（推荐）

1. 用 Android Studio（Ladybug 或更新版本）打开 `CurrencyCalculator` 目录
2. 等待 Gradle Sync 完成（需要 JDK 17+）
3. 连接手机/平板或启动模拟器，点击 Run ▶

### 命令行

```bash
cd CurrencyCalculator
# 确认 local.properties 中 sdk.dir 指向本机 Android SDK
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

## UI 预览

仓库根目录下的 [`preview.html`](preview.html) 是可交互的 Material 3 网页预览，
可在浏览器中体验手机 / 平板双栏两种布局与换算逻辑（直接双击打开即可，无需服务器）。

## 数据来源

汇率数据：[Frankfurter API](https://www.frankfurter.app/)（欧洲央行每个工作日约 16:00 CET 发布参考汇率）。
本应用仅供学习与日常换算参考，不构成任何交易依据。
