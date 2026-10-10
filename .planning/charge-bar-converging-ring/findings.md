# 调研与决策

## 最新显示规则（覆盖下文首版参数）

- 用户反馈：降低白环不透明度、初始位置更接近图标、满蓄力隐藏。
- 白环 alpha 从 255 改为 128（约 50%），初始边缘间距从 8 改为 4 GUI 像素。
- 共用白环入口只在 `0 < progress < 1` 时绘制；满蓄力的原底图、填充和闪烁保留。无 ID 分支、额外状态或网络变更。
- 新间距在 0／25／50／75／100% 时为 4／3／2／1／0 GUI 像素，100% 的半径仅是收缩极限，实际不提交白环顶点。
- 修正后 Microsoft Java 17.0.11 的 `runData --offline --no-daemon` 通过（39 秒），11 tasks（4 executed、7 up-to-date），provider 202 ms；本次 latest.log 无 ERROR／Exception，差异格式及显示区间静态核对通过。实际客户端仍待视觉验收。

- 现有共用入口：`client/gui/charge_bar/ChargeBarHudOverlay.java`。通过注册表样式、优先级和客户端进度绘制所有栏；只显示正进度且持 IsaacHead 的条目。
- `ChargeRingGeometry.SIZE=12`；原圆环外半径 `SIZE / 2 - 0.25`，中心与现有整数坐标底图对齐。
- `ChargeRingAnimation` 已承载纯进度／时间视觉计算，新增收缩半径计算放在这里。
- `ChargeBarType` 只记录优先级、底图和填充色。新增功能不改变它、客户端进度存储或同步协议。
- 原 Overlay 已使用 `GuiGraphics.drawManaged` 批处理填充像素。新白环使用 `RenderType.gui()` 共享缓冲，并单独一遍 managed draw 在全部图标之前完成，避免后刷新的白色顶点覆盖图标。
- 64 段圆周坐标一次预计算；半径按浮点绘制，不逐帧栅格化新白环。保持目前 12×12 图标和槽位布局，多环可以交叠但不能盖住图标。
- 本轮是通用 HUD 表现修改，无道具身份、Wiki 数值、资源图标、音效或道具池变更。
- 轻量记忆检索未找到 HUD／蓄力条相关条目，使用当前源码确认行为。

## 已实现

- `ChargeRingAnimation.approachRadius`：`5.75 + 0.5 + 8 × (1 - progress)`，进度有限值钳制到 0～1。白环内缘在 0／25／50／75／100% 时距图标外缘 8／6／4／2／0 GUI 像素。
- `ChargeBarHudOverlay`：64 段预计算圆周，每栏 256 个白色顶点，1 GUI 像素线宽；使用现有矩阵和 `RenderType.gui()` 缓冲。
- 确认 Forge 1.20.1 的 `GuiGraphics` 提供 `pose()`／`bufferSource()`／`drawManaged()`，GUI RenderType 使用 POSITION_COLOR + QUADS。薄环顶点绕序一致，并与原 GUI 四边形方向一致。
- 两遍 managed draw 保证全部白环先完成提交，再绘制任何底图和进度像素；布局和排序复用同一列表及同一中心计算。
- 无按 ID 分支，无类型构造器、显示条件、同步协议、实际蓄力或资源文件变更。

## 验证结果

- Microsoft Java 17.0.11，`runData --offline --no-daemon`：`BUILD SUCCESSFUL in 36s`，11 tasks（4 executed、7 up-to-date），provider 163 ms，生成缓存 written=0；最新日志无 ERROR／Exception。
- 差异格式、半径单调性／满蓄力接触边界、64 段绕序、共用入口和原显示门槛静态核对通过。
- 未执行客户端／多人游戏验收；GUI 比例下的细线观感、多环交叠、快速充能的运动及完成接触感仍需实际观察。
