# 更新日志（Changelog）

本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)，日期为发布日期。

## [1.2.0] - 2026-11-26

本版针对一次外部代码评审做了系统性修复，重点解决「数据新旧判定」与「构建可复现性」。

### 🐛 修复
- **缓存新鲜度判定错误（影响数据可信度）**：此前仓库用 30 分钟、界面用 5 分钟两套阈值，
  缓存 6–30 分钟内的旧数据会被当成实时数据展示，且不显示「离线缓存」标识。
  - 改为以**欧洲央行发布时刻**（工作日 16:00 CET/CEST）为界判定新旧，不再使用挂钟 TTL
  - 数据来源由仓库直接返回（`NETWORK` / `CACHE` / `FALLBACK`），界面不再自行猜测
- **协程取消被吞掉**：`RatesRepository` 与 `RatesApi` 的 `catch (e: Exception)` 会一并捕获
  `CancellationException`，导致取消后仍继续请求下一个端点、甚至把取消当成网络失败
- **并发刷新竞态**：连续点击刷新时旧请求可能覆盖新数据，现改为新请求取消旧请求（带序号防抖）
- **金额输入**：单独输入 `.` 会解析为 0；前导零可无限输入（如 `00012`），现已规范化
- **结果数字溢出**：换算结果固定用 36sp 字号，大额金额（12 位整数 + 千分位 + 小数）
  在手机窄屏会换行或顶出卡片。改为按可用宽度自动缩放（36sp→18sp，1sp 步进），
  始终单行完整显示；平板双栏宽度较大时仍保持原字号

### ✨ 新增
- 已有数据时刷新：顶部显示进度指示，不清空当前内容（此前点击刷新数秒内毫无反馈）
- **单元测试**：5 个测试类共 28 个用例（缓存判定 / 汇率换算 / JSON 解析 / 格式化 / 输入过滤），并接入 CI

### 🔄 变更
- 移除 `settings.gradle.kts` 中的第三方镜像仓库，仅保留 `google()` 与 `mavenCentral()`
- `gradle-wrapper.properties`：`networkTimeout` 10s→30s、`retries` 0→3、恢复分发地址校验
- 删除死代码（`amountValue`、`hasCache()`），移除未使用的 `ACCESS_NETWORK_STATE` 权限

### 📝 文档
- 修正 README：Android Studio 版本要求（需支持 AGP 9.5.2）、命令行不再要求 `cd CurrencyCalculator`
- 澄清「主备双域名」表述：两个 Frankfurter 域名属同一服务的新旧地址，旧域名 301 跳转

## [1.1.1] - 2026-11-26

### 🐛 修复
- **构建配置修复**（此前 clone 源码可能无法构建）：
  - `gradle-wrapper.properties` 由 Gradle 8.9 修正为 9.6.0，与 AGP 9.5.2 匹配
  - 顶层 `buildscript` 显式声明 `google()` / `mavenCentral()` / `gradlePluginPortal()` 仓库
  - Compose 编译器插件改由 buildscript classpath 解析，不再依赖 plugin marker 链路

### 🔧 其他
- 新增 GitHub Actions CI：push / PR 自动构建 debug APK
- 新增 MIT LICENSE、CHANGELOG、Bug Issue 模板
- README 增加徽章、界面截图与下载入口

## [1.1.0] - 2026-11-25

### ✨ 新增
- **平板 / 折叠屏自适应布局**（三档窗口宽度判定，分屏与悬浮窗同样正确）：
  - `< 600dp` 手机竖屏：顶部 Tab + 单栏
  - `600–840dp` 平板竖屏 / 手机横屏：侧边 NavigationRail + 单栏（居中限宽 640dp）
  - `≥ 840dp` 平板横屏 / 折叠屏展开：双栏并排（换算 + 汇率表，各自独立滚动）
- 汇率表点选行可直达换算目标币种

### 🔄 变更
- **最低支持版本提升至 Android 9（API 28）**，不再支持 Android 9 以下设备
- **目标版本升级至 Android 17（API 37）**，满足大屏设备强制自适应新规
- 构建链升级：AGP 9.5.2 + Gradle 9.6 + Kotlin 2.4.21（AGP 9 内置 Kotlin）+ Compose BOM 2026.11

### 🐛 修复
- 汇率表页「重试」按钮未真正触发刷新的问题

## [1.0.0] - 2026-11-24

### ✨ 新增
- 实时汇率换算：30 种常用货币（含中文名与符号），数据来自欧洲央行参考汇率（Frankfurter API）
- Material Design 3 界面：动态取色（Android 12+）、深色模式
- 一键互换币种、快捷金额（100 / 500 / 1000 / 5000）
- 汇率一览表：任选基准币种查看全部汇率
- 离线缓存：断网自动回退最近一次汇率并标注「缓存数据」
- 可交互网页预览 `preview.html`
