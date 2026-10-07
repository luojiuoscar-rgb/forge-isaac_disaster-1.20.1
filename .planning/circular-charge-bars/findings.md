# Findings

## User requirements
- 截图中的蓄力条是空心绿色圆环，当前直条只是旧的测试素材，不能继续作为最终外观。
- 保留 `registries/charge_bar` 下的 Forge 注册表和最高优先级攻击指示器。
- 保留从右上角顺时针、多圈 8/16/24 的自动布局。
- 图标要更小、距离准星更近；项目工作记忆放在 `.planning` 子目录。

## Verified root cause
- ChargeBarType 仍定义两张水平填充纹理；Overlay 使用横向 scissor，无法产生圆周进度。
- 原纹理 64×64，实际直条区域为 `(0,29,64,6)`，显示为 40×4。
- 布局将 40×4 直条的对角线作为直径，首圈半径约 58 GUI 单位，所以显得过远。
- 注册、独立进度/显隐、排序和同步不需要重写。

## Implementation direction
- 新攻击圆环 12×12 GUI 单位，带暗色描边、暗色未充能轨道和亮绿色充能环带，中心透明。
- 环带逐像素按 `atan2(dx,-dy)` 计算从顶部起的顺时针角度，进度 0～1 对应 0～360 度。
- 新排列以图标直径而非矩形对角线计算，间距 2，首圈半径约 18.3 GUI 单位（中心约 +13,-13）。
- 预览必须调用生产几何代码，并明确区分预览与 Minecraft 实机画面。

## Existing changes to preserve
- 上轮注册、数据缓存、同步包、最大蓄力变化同步、客户端断线清理与定向测试均为本任务已有变更。
- 不删除旧测试纹理，不改动无关玩法代码。

## Verification
- 红阶段：独立目录 baseline compileJava 成功，新测试缺少新接口/圆环几何按预期失败。
- 绿阶段：Java 17.0.11 下 compileJava、11 个定向 JUnit 和 runData 全部通过。
- 生产几何预览：0/25/50/75/100% 显示圆周进度、暗描边及透明中心；单图标位于右上方，9 个图标自动进入第二圈。
- 预览由本目录 ChargeBarPreview.java 调用生产 ChargeRingGeometry 和 ChargeBarLayout 生成，不能视为 Minecraft 实机截图。
- 原 build 目录曾出现 mapped Forge JAR 文件占用和随后 Mixin Slice 缺失；验证转用 build/charge-ring-validation 后消失，未终止任何用户程序。
- 通过自动审批执行精确的 `git add -u -- docs/charge-bars.md`，已清除暂存区残留的上轮 docs 文件。
- 字节码检查确认 GuiGraphics.fill 在非 managed 模式下刷新缓冲；Overlay 使用 drawManaged 合批全部圆环像素，避免逐像素提交。
- 最终合批版本再次编译并通过 11 项定向测试；docs 文件已移除，所有任务记录保存在本目录。实机视觉验证未执行。

## Follow-up: PNG base + code fill
- 用户授权将底图改为 PNG，使用黑白灰颜色；绿色进度仍由代码绘制。
- 注册参数提供底图 ResourceLocation 与充能颜色；尺寸、优先级仍可配置。
- PNG 使用当前生产圆环模板生成，保证透明中心、描边与角度填充环带逐像素对齐。
- 基础图片只负责底色和描边；Overlay 仅绘制 FILLED 像素，其他区域完全由 PNG 决定。
- 保留既有紧凑位置、8/16/24 排列、最高优先级和同步机制。
- 本轮验证再次遇到 JAR 占用。ForgeGradle 6.0.54 MinecraftUserRepo.getCacheRoot 的字节码返回 project.file("build/fg_cache/")，不读取 buildDirectory。
- 只隔离编译输出不足以隔离依赖缓存；新增 prepare-validation.ps1，将 build.gradle/settings.gradle/gradle.properties/src 复制到 build/charge-ring-validation/project 后使用 -p 验证。无需终止其他 Java 进程。
- PNG hybrid 最终结果：Java 17.0.11，项目快照 compileJava、12 项定向 JUnit、runData 均通过（1m38s）。
- SHA256 核实当前注册定义、渲染、几何、布局、PNG 与新增测试和已验证快照一致；git diff --check 通过。
- png-preview.png 读取真实素材并使用生产圆周填充生成，视觉已检查；未执行 Minecraft 客户端实机画面验证。

## Follow-up: Darker base and full-charge flash
- 默认 PNG 轨道从 #D8D8D8 调暗至 #808080；保持 #181818 描边、透明中心、12×12 尺寸和原布局。
- 满充填充使用 ChargeRingAnimation，500ms 周期，250ms 白色 / 250ms 注册原色；保留注册 alpha，未满和非法进度不闪。
- 每帧由 Overlay 读取一次 Util.getMillis()，所有可见圆环共用时间；不保存动画状态，不需要网络同步。
- 新增 3 项动画测试先因缺少 ChargeRingAnimation 编译失败（red），随后实现该类并接入 Overlay。
- flash-preview.png 读取真实 PNG 与生产动画逻辑，并列展示满充两种颜色；仅为算法预览。
- 最终 Java 17.0.11 隔离快照 compileJava 与 15 项定向 JUnit 全通过（1m15s），0 失败/错误/跳过；新动画、Overlay、PNG 与动画测试的 SHA256 和快照一致。
- git diff --check 通过；未执行 Minecraft 客户端实机视觉验证。本次未改变注册/数据生成，因此没有重复 runData。

## Follow-up: Shared fixed size and obsolete state
- 用户要求所有圆环尺寸统一。唯一尺寸定义为 ChargeRingGeometry.SIZE = 12；类型移除 size 构造参数/字段/访问器，几何和布局也移除尺寸参数，Overlay 与预览共用常量。
- 全 src 搜索确认 ClientDataManager.getChargeProgress() 只有声明，无调用，删除；getChargeBars() 是当前 HUD 消费入口。setChargeProgress 仍有 ClientPacketHandlers 调用，保留。
- chargeAmount 是攻击蓄力的服务端原始量，硫磺火、诅咒之眼、剖腹产、Neptunus 等仍在累积、判定、消耗/清零，不能删除。
- preChargeAmount 只有初始化、getter/setter 和 updateClientCharge 的赋值，没有读取者。当前同步比较使用 preChargeProgress，删除残留字段、访问器和写入。
- preChargeProgress 是上次同步的归一化值，保留以检测 chargeAmount 或总蓄力上限改变引起的 HUD 进度变化。
- 首次验证编译通过、15 项测试中 1 项动画测试失败；检查生产与快照均已是 400ms 周期 / 200ms 白色，旧测试仍假设 500/250。保留已有生产节奏，更新测试边界与当前接入说明/预览文案。
- 预览独立编译首次恰逢 Gradle 重编译后输出类暂不存在，补充 ChargeBarType 源文件并以 -proc:none 编译成功。固定尺寸迁移前后生成图像 hash 相同（仅后续更新了节奏说明文字）。
- 最终隔离快照 compileJava + 15 项定向 JUnit 通过（1m20s），0 失败/错误。11 项本次生产/几何布局测试文件与快照 hash 一致，动画测试修正后也与快照一致；git diff --check 通过。未运行 Minecraft 实机视觉验证。

## Follow-up: Log and skip invalid charge indicators
- 本轮范围是当前充能条系统：ChargeBarLayout.position 的主动 IllegalArgumentException，以及类型底图/同步包 ID 的 requireNonNull。
- 无效索引返回 null 并记录日志，Overlay/预览明确跳过；空底图类型在创建时记录一次日志，不参与排序和槽位分配。
- 空同步 ID 不应写出损坏的数据或进入网络通道；在 ModMessages 的发送边界跳过该包。客户端状态入口也拒绝空 ID，防止 Map.copyOf 的间接异常。
- 损坏充能包仅在解码边界捕获 RuntimeException 并标为无效；正常包字段和格式保持不变，不扩大到其他包或整个渲染过程。
- 新增 5 项错误输入回归测试，正在运行 red 验证。
- red 实测 13 项 ChargeBar 测试中新增 5 项全部按预期失败：负索引 IllegalArgumentException、底图/包 ID requireNonNull NPE、损坏包读取越界，以及 null 状态 ID 引起 Map.copyOf NPE。
- 生产实现使用 LogUtils/SLF4J WARN；类型无效性在排序前处理，避免日志每帧重复。发送入口在访问通道前检查包 isValid，非法解码包也不会排入客户端任务。
- 最终 Java 17.0.11 隔离项目 compileJava + 20 项定向 JUnit 通过（2m21s），0 失败/错误；9 项修改的生产/测试文件 hash 与快照一致，git diff --check 通过。
- 生产充能条几何/布局/类型/包中已无主动 throw、requireNonNull、orElseThrow。验证含正常排序/位置/同步编码和错误输入跳过；未执行 Minecraft 实机视觉或网络联机验证。

## Follow-up: Project logger convention
- IsaacDisaster.java:39 定义 public static final Logger LOGGER = LogUtils.getLogger()。LogUtils 创建的是 Minecraft 的 SLF4J 日志入口，能进入 Forge 运行日志；此前各类独立创建 Logger 只改变来源类别，不影响输出。
- 根据用户约定将 ChargeBarType、ChargeBarLayout、ClientDataManager、ChargeBarUpdateS2CPacket 的新增日志统一为 IsaacDisaster.LOGGER，移除独立 Logger/import。日志等级与错误输入跳过逻辑保持一致。
- compileJava + 20 项定向 JUnit 通过（2m10s），0 失败/错误；四个类 hash 与验证快照一致，git diff --check 通过。测试 XML 的 system-out 实际含 [WARN] [ne.lu.is.IsaacDisaster/]，确认无效包/状态/索引的日志已由项目 Logger 输出。

## Planning directory cleanup
- 用户要求清理本目录与项目记忆无关的辅助产物。已确认 11 个 .log 是构建/测试过程输出，ChargeBarPreview.java 是离线效果预览工具，prepare-validation.ps1 和 validation.init.gradle 是纠错验证脚本，无生产代码依赖。
- 已删除上述 14 个文件，并更新当前接入说明，不再指向已移除的工具。历史验证结论保留在 Markdown 记录中；今后临时辅助文件放在 build 或系统临时目录。

## Follow-up: Communication conventions and registry qualifiers
- 参考 EntityVisualStateSync 新增 ChargeBarSync.syncToPlayer，校验放在业务同步入口，随后仍调用 ModMessages.sentToPlayer(new ChargeBarUpdateS2CPacket(...), player)。移除通用发送方法里 4 个充能包 instanceof 判断。
- 参考 EntityVisualStateS2CPacket/IsaacFlightStateS2CPacket 的 handle：context.enqueueWork 包住 DistExecutor 客户端分发，最后 setPacketHandled；保留损坏包记录日志并跳过。
- ServerTickEvent 显式使用注册 ID、显隐与进度，只有同步入口返回成功才更新已同步进度。旧单进度构造器暂保留兼容。
- ModRegistries 全部字段改为所属类限定，包括 TRAJECTORY_RULE_KEY。21 个注册表创建/注册共用泛型 registerRegistry，移除静态字段和具体条目类型 import；转换时机械核对注册名称和顺序不变。
- 调整通信测试为验证业务入口跳过非法 ID/玩家，正常包字段格式不变。red 因缺少新 ChargeBarSync 类型按预期编译失败。
- 验证日志仅写入 build/charge-ring-validation；本目录继续只保留四个 Markdown 文件。
- 最终 Java 17.0.11 隔离项目 compileJava、20 项相关 JUnit 和 runData 全通过（3m10s），0 测试失败/错误，数据生成写入 0 文件。6 个本次生产/测试文件 hash 与快照一致，git diff --check 通过。未执行 Minecraft 实机联机验证。
