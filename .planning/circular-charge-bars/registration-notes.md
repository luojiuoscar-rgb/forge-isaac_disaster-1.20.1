# 圆形充能条接入说明

## 当前实现

Forge 类型定义与注册位于 `registries/charge_bar/ChargeBarType.java` 和 `ModChargeBars.java`，由 `ModRegistries` 创建注册表。
注册项不保存玩家进度，不引用客户端类。

攻击蓄力注册 ID 为 `isaac_disaster:attack_charge`，优先级 Integer.MAX_VALUE。
默认圆环显示为 12×12 GUI 单位，底图为 `assets/isaac_disaster/textures/hud/charge/charge_ring_base.png`。
底图包含 #181818 深灰描边、#808080 中灰轨道；中心和外部透明。底色不再通过代码绘制。
Overlay 先绘制 PNG，再以注册的 ARGB 颜色覆盖已充能环带；默认颜色为 0xFF7DE000。
绿色部分仍使用 ChargeRingGeometry，按角度从顶部顺时针增长，0～1 映射到 0～360 度。
所有注册圆环在满充时循环闪白：每 400ms 周期内，白色与注册颜色各显示 200ms，保留注册颜色的 alpha。
ChargeRingAnimation 使用 Overlay 提供的 Util.getMillis() 单调时钟，与帧率无关。进度低于 1 时立即恢复注册颜色；隐藏时不绘制，也不需要清理动画状态。

显示条目按优先级降序、注册 ID 升序排列。每圈容量 8、16、24……，各圈从准星右上方（-45 度）开始顺时针。
默认间隔 2 GUI 单位，首圈半径约 18.3，第一槽中心约 (+13,-13)。隐藏后紧凑补位。
攻击条仍只在手持 IsaacHead 且进度大于零时显示。

## 注册一种圆环

```java
public static final RegistryObject<ChargeBarType> EXAMPLE = CHARGE_BAR_REGISTRY.register(
    "example", () -> new ChargeBarType(100,
        ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
            "textures/hud/charge/charge_ring_base.png"),
        0xFF7DE000));
```

构造参数为优先级、底图 ResourceLocation、充能 ARGB 颜色。
所有注册项共用 ChargeRingGeometry.SIZE = 12 GUI 单位；尺寸不再属于注册样式参数。
ChargeRingGeometry.rasterize(progress) 与 ChargeBarLayout.position(index) 内部使用相同常量，绘制和排列不能分别指定尺寸。
其他注册项使用低于 Integer.MAX_VALUE 的优先级。
底图按标准空心圆环模板制作，与代码环带对齐；整个正方形 PNG 自动映射到显示尺寸。
默认 PNG 为 12×12。编辑底色与描边可直接修改 PNG，不需要修改 Java。

服务端发送独立状态：

```java
ChargeBarSync.syncToPlayer(EXAMPLE.getId(), true, progress, player);
```

隐藏时调用 `ChargeBarSync.syncToPlayer(EXAMPLE.getId(), false, 0f, player)`。
该入口位于 networking/ChargeBarSync.java，参考 EntityVisualStateSync：先校验 ID 与接收玩家，再构造数据包，交给 ModMessages.sentToPlayer 发送；返回值表示是否已提交发送。
数据包接收端参考 EntityVisualStateS2CPacket，先 context.enqueueWork，再在客户端分发调用 ClientPacketHandlers。
客户端线程也可调用 `ClientDataManager.getInstance().updateChargeBar(id, visible, progress)`。
visible=true 可显示零进度空环；非有限进度会隐藏；断线通过 ClientPlayerNetworkEvent.LoggingOut 清空缓存。
单参数同步包构造方法仍映射到 attack_charge。客户端和服务端使用配套版本。

## 攻击蓄力状态

PlayerAbility.chargeAmount 为服务端攻击实际使用的蓄力量，攻击实现累积、消耗或清零；HUD 显示的是 chargeAmount / 当前攻击最大蓄力 的归一化值。
preChargeProgress 保存上次同步的归一化进度，避免重复发包，并能检测最大蓄力变化导致的进度变化。
旧 preChargeAmount 只写不读，已删除字段、访问器和同步处写入。ClientDataManager.getChargeProgress() 已无调用者，也已删除；HUD 直接读取按注册 ID 保存的 getChargeBars()。
setChargeProgress(float) 仍有旧单条包处理入口调用，保留其映射到 attack_charge 的逻辑。

## 无效输入处理

充能条错误输入记录 WARN 日志并跳过：空底图类型由 isValid() 标为无效，排序前剔除，不占槽位；底图错误只在构造时记录，避免每帧刷屏。
所有日志统一调用 IsaacDisaster.LOGGER；该项目 Logger 本身通过 LogUtils.getLogger() 创建。
ChargeBarLayout.position(index) 对负数索引记录日志并返回 null，Overlay 和预览明确跳过该位置。
空同步 ID 或空接收玩家在 ChargeBarSync.syncToPlayer 内记录日志并跳过；ModMessages 保持通用发送，不判断具体充能包类型。无效解码包不编码，也不会提交客户端更新任务。
客户端状态入口拒绝空 ID，以免损坏可见状态快照。包解码读取异常记录日志并将整个包标为无效；有效包字段/编码格式不变。

## 验证与预览

测试覆盖 PNG 文件存在、灰度、透明中心与代码环带对齐，并保留几何/布局/状态/同步测试。
ForgeGradle 的 AT 依赖缓存硬编码为 projectDir/build/fg_cache。若原缓存被占用，应把 build.gradle、settings.gradle、gradle.properties 和 src 复制到独立临时项目目录，再用 gradlew -p <临时项目目录> 验证；仅更改 buildDirectory 无法隔离该缓存。
此前曾用实际 PNG 和生产几何/布局/动画算法生成预览；这类算法预览不能替代 Minecraft 实机画面验证。临时预览工具、验证脚本和原始构建日志已清理，不属于项目接入文件。
所有项目工作记忆保存在本目录，docs 中的上轮 charge-bars.md 已移走。
本目录只保留 Markdown 项目记录；后续日志、预览工具与临时验证脚本放入 build 或系统临时目录。
