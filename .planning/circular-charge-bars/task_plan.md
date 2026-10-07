# Task Plan: 以撒风格圆形充能条

## Goal
将测试用绿色直条替换为小型像素圆环，进度沿圆周填充；保留 Forge 注册、最高优先级和多圈自动排列，靠近准星显示。

## Current Phase
Complete

## Phases
### Phase 1: Requirements & Discovery
- [x] 核实截图、旧纹理裁切和过大布局半径的原因。
- [x] 确认注册、同步和状态缓存可以继续使用。
- [x] 在本目录建立项目工作记忆，并将上轮 docs 记录移入本目录。
- **Status:** complete

### Phase 2: Ring Geometry & Regression Tests
- [x] 测试空心环、顺时针按角度填充、零/满充状态和非法进度。
- [x] 测试 12 GUI 单位的圆环靠近准星、8/16/24 容量、换圈右上角起点和不重叠。
- [x] 运行测试并确认新增要求先失败（缺少圆环几何、圆形类型构造及紧凑布局接口）。
- **Status:** complete

### Phase 3: Implementation
- [x] 将 ChargeBarType 简化为圆环尺寸和颜色定义，攻击圆环为 12×12。
- [x] 用像素圆环绘制替换横向纹理与矩形裁切。
- [x] 使用圆环直径及 2 GUI 单位间隔计算排列半径。
- [x] 保留现有显隐、同步、优先级和客户端断线清理。
- **Status:** complete

### Phase 4: Verification & Handoff
- [x] 使用生产圆环几何生成并检查预览图。
- [x] 运行定向 JUnit、编译和 Forge runData。
- [x] 复核变更并更新 findings.md、progress.md、registration-notes.md。
- **Status:** complete

## Decisions
- 用户明确授权重写填充逻辑。本轮直接修正实现，不再请求确认。
- 圆环本身从顶部顺时针充能；多个圆环的排列从准星右上方开始。
- 使用程序绘制像素环，参考截图的暗色描边、绿色环带、透明中心；不复制截图背景。
- 项目记忆只写入 `.planning/circular-charge-bars/`，不写入 docs 或全局记忆目录。

### Phase 5: PNG Base & Registered Fill Color
- [x] 补充 PNG 灰度、透明中心与代码环带对齐测试，并运行预期失败。
- [x] 将类型参数改为优先级、尺寸、底图 ResourceLocation、充能 ARGB 颜色。
- [x] 生成黑白灰底图 PNG，Overlay 先绘制底图，再按角度覆盖充能像素。
- [x] 更新预览工具为实际读取 PNG，与生产环带算法自动组合。
- **Status:** complete

### Phase 6: Hybrid Render Verification
- [x] 检查 PNG 与组合预览，保持 12×12 和靠近准星的布局。
- [x] 执行编译、定向测试和 runData。
- [x] 更新当前接入说明和工作记忆，不写入 docs。
- **Status:** complete

### Phase 7: Darker Base & Full-charge Flash
- [x] 添加满充循环闪白、未满不闪、颜色透明度保持的测试。
- [x] 将 PNG 轨道由 #D8D8D8 调暗至 #808080，满充以 500ms 周期闪白。
- [x] 更新生产算法预览，运行隔离项目编译与定向测试，更新本目录记录。
- **Status:** complete

### Phase 8: Shared Fixed Size & Charge State Cleanup
- [x] 将 12 GUI 单位定义为唯一尺寸常量，移除注册/几何/布局的可变尺寸参数，更新消费者与测试。
- [x] 清理无引用的 getChargeProgress 和只写不读的 preChargeAmount，保留攻击蓄力与归一化同步进度。
- [x] 运行隔离快照编译和相关测试，更新接入说明与项目记忆。
- **Status:** complete

### Phase 9: Log and Skip Invalid Charge Indicators
- [x] 添加负索引、空底图、空 ID 与损坏同步包的跳过测试。
- [x] 将充能条主动抛异常/requireNonNull 改为日志与无效条目跳过，更新调用方及发送边界。
- [x] 验证有效条目继续显示、有效包格式不变，运行编译/定向测试并更新本目录记录。
- **Status:** complete

### Phase 10: Shared Project Logger
- [x] 将本次四个类的独立 Logger 改为 IsaacDisaster.LOGGER。
- [x] 验证编译与错误输入跳过测试，更新记录。
- **Status:** complete

### Phase 11: Packet Convention & Qualified Registries
- [x] 参考 EntityVisualStateSync/EntityVisualStateS2CPacket，将充能校验移出通用发送方法并统一接收入队顺序。
- [x] 注册表字段改为类名限定，合并重复初始化步骤，保持全部注册名称与顺序。
- [x] 运行相关测试、编译与 runData；临时辅助文件仅放 build，更新项目记录。
- **Status:** complete

## Errors Encountered
| Error | Attempt | Resolution |
|---|---|---|
| python 不在 PATH | 1 | 使用 Codex bundled Python 的绝对路径运行 session-catchup.py。 |
| Forge mapped JAR 被其他进程占用 | 1 | 不终止进程；通过 validation.init.gradle 将验证产物隔离到 build/charge-ring-validation。占用者未确认。 |
| Win32_Process CIM 查询被拒绝访问 | 1 | 不升级权限；使用 Get-Process 做有限只读检查，文件占用进程未确认。 |
| 初次普通目录编译在文件占用后报告 Mixin Slice 类缺失 | 1 | 同次失败记录；独立验证目录重新生成依赖，检查是否消失。 |
| git add -u 写入 .git/index.lock 被沙箱拒绝 | 1 | 精确命令经自动审批成功，上轮 docs 文件从暂存区移除。 |
| javap 初次使用不存在的隔离目录 Forge JAR 路径 | 1 | 使用实际 build/fg_cache 路径检查成功；纯 Java 预览本身不依赖 Forge 类。 |
| PNG follow-up 再次遇到 Forge JAR 占用与 Mixin Slice 缺失 | 1 | 字节码确认 MinecraftUserRepo 把 AT cache 硬编码为 projectDir/build/fg_cache；用 prepare-validation.ps1 创建只含构建输入的项目快照，真正隔离 projectDir。 |
| PNG source patch 含重复的不存在目标行，整次补丁未应用 | 1 | 移除多余 patch hunk 后成功应用，未发生部分生产变更。 |
| 最终 diff 检查发现捕获日志行尾空格 | 1 | 仅规范化本任务 .log 行尾空白，重新检查通过；生产源码无需变更。 |
