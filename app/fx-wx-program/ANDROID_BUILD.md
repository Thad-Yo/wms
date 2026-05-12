# Android APK 打包说明

## 当前结论

`app/fx-wx-program` 不是原生微信小程序，而是 `uni-app Vue2` 项目。

这意味着它可以直接编译为 Android App，不需要重写成原生 Android。

## 已处理的兼容点

已将项目中的关键微信专有依赖改为更适合 `uni-app App` 的写法：

1. 扫码页提示音从 `wx.createInnerAudioContext()` 改为 `uni.createInnerAudioContext()`
2. 自定义导航返回逻辑补充了 Android App 无页面栈时的兜底跳转

## 推荐打包方式

推荐使用 `HBuilderX`：

1. 安装最新版 `HBuilderX`
2. 用 `HBuilderX` 打开目录 `/home/jiaxiaofei/code/wms/app/fx-wx-program`
3. 在 `manifest.json` 中补齐以下信息
4. 选择 `发行 -> 原生App-云打包`
5. 平台选择 `Android`
6. 生成 `APK`

## 打包前必须补齐的配置

请在 [manifest.json](/home/jiaxiaofei/code/wms/app/fx-wx-program/manifest.json) 中检查并补齐：

1. `应用名称`
2. `版本号 / 版本名称`
3. `Android 包名`
4. `应用图标`
5. `启动图`
6. `签名证书`

其中最关键的是：

1. Android 包名示例：`com.yourcompany.wms`
2. 签名证书：没有证书就无法用于正式安装分发

## 手持终端需要重点确认

如果你的手持终端是工业 PDA，需要额外确认下面几项：

1. 终端摄像头是否支持 `uni-app camera` 组件扫码
2. 终端是否带硬件扫描头
3. 硬件扫描头是通过 `键值广播`、`Intent` 还是 `串口` 输出扫码内容

当前项目使用的是“摄像头扫码”方案，不是“PDA 扫码枪 SDK 接入”方案。

如果你的设备是带扫描头的手持终端，后续大概率还要增加一层“扫描枪 SDK / 广播监听”适配，才能达到真正好用的仓库作业体验。

## 现阶段限制

当前代码虽然已经具备 Android 打包基础，但还不等于“已经完成 PDA 专业化改造”。至少还需要继续验证：

1. 扫码页在 Android 真机上的表现
2. 登录、列表、上传图片、文件预览是否都正常
3. Android 端权限弹窗是否稳定
4. 如果是专用手持机，是否需要接扫码头

## 建议的下一步

最稳妥的路径是：

1. 先按当前代码直接打一个基础 APK
2. 在你的手持终端上实测登录、扫码、出入库流程
3. 再根据设备型号决定是否接入专用扫码头能力

如果你愿意，我下一步可以继续直接帮你做两件事中的一件：

1. 继续把这个 `uni-app` 项目补成更完整的 Android 发布配置
2. 按你手持终端的品牌和型号，接入扫码枪 / PDA 扫描头能力
