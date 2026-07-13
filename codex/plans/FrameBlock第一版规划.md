# FrameBlock 第一版实现规划

## 总结

实现一个注册名为 `frame_block` 的不可破坏框架方块。它通过非 ticking 的 BlockEntity 保存一个普通方块的完整 `BlockState`，使用自定义 baked model 显示该状态，并代理碰撞、选择框、遮挡、光照、声音和摩擦等性质。

第一版只支持无 BlockEntity、单格存在的普通方块；不实现箱子、门、床等特殊结构，不使用 Mixin，也不建立涂层类型注册表。

## 文件结构

新增公共系统，统一放入：

```text
block/frame/
|- FrameBlock.java
|- FrameBlockEntity.java
|- FrameBlockHelper.java
|- FrameTargetValidator.java
|- FrameAppearanceBlockGetter.java
`- FrameStairShapeResolver.java
```

职责如下：

- `FrameBlock`：交互、动态形状、外观性质代理和不可破坏行为。
- `FrameBlockEntity`：保存、同步和读取 appearance `BlockState`，不保存 ItemStack，不执行 tick。
- `FrameBlockHelper`：统一查询框架及其 appearance，避免其他系统直接读取 BlockEntity。
- `FrameTargetValidator`：判断方块能否作为 appearance 或被框架包裹。
- `FrameAppearanceBlockGetter`：让外观计算看到相邻框架保存的状态，而不是实际 `FrameBlock`。
- `FrameStairShapeResolver`：按原版楼梯规则刷新当前及相邻框架中的 `StairsShape`，支持直角拐角自动连接。

新增物品：

```text
item/block/FrameBlockItem.java
```

新增客户端模型系统：

```text
client/model/frame/
|- FrameBlockModel.java
|- FrameBlockAndTintGetter.java
`- FrameBlockClientEvents.java
```

不使用 BlockEntityRenderer。`FrameBlockModel` 在区块模型烘焙阶段复用 appearance 对应的 `BakedModel`，保持静态区块渲染性能。

## 核心实现

### 注册和资源

修改：

- `ModBlocks`：注册 `FRAME_BLOCK`，属性基于基岩，并启用 `dynamicShape()` 和 `noOcclusion()`。
- `ModBlockEntities`：注册 `FRAME_BLOCK_ENTITY`。
- `ModItems`：注册同名 `FrameBlockItem`，加入 `MISC_LIST`。
- `ModItemModelProvider`：跳过该 BlockItem 的普通物品模型生成。

新增资源：

```text
assets/isaac_disaster/blockstates/frame_block.json
assets/isaac_disaster/models/block/frame_block.json
assets/isaac_disaster/models/item/frame_block.json
assets/isaac_disaster/textures/block/frame_block.png
data/isaac_disaster/loot_tables/blocks/frame_block.json
data/isaac_disaster/tags/blocks/frame_unsupported.json
```

同时在 `en_us.json` 添加框架方块名称。第一版不添加合成配方，只通过创造栏或命令获得。

### 外观数据

`FrameBlockEntity` 提供：

```java
boolean hasAppearance();
BlockState getAppearance();
boolean setAppearance(BlockState state);
void clearAppearance();
```

- NBT 使用 `NbtUtils.writeBlockState()` 保存到 `Appearance`。
- 更新时调用 `setChanged()`、`sendBlockUpdated()`、光照检查和 `requestModelDataUpdate()`。
- 使用更新数据包和 update tag 同步客户端。
- 非法、丢失注册项或框架自身状态加载为空 appearance。
- 结构方块会自然保存和恢复 BlockEntity NBT。

空框架没有 appearance 时，外观、形状和模型都回退到框架自身，避免递归查询。

### 交互

空框架被普通 `BlockItem` 右键：

- 根据玩家朝向、点击面和点击位置调用目标方块的放置状态计算。
- 保存生成的完整 `BlockState`，包括楼梯方向、半砖上下位置等。
- 不消耗手中方块。
- 方块实体、门、床、流体、系统方块和其他框架不成立，返回正常交互结果。

已有 appearance 的框架被另一个方块右键：

- 创造模式：替换 appearance，不消耗物品。
- 生存模式：不替换，继续执行正常方块物品行为。
- 框架物品不能成为 appearance。

`FrameBlockItem` 重写 `onItemUseFirst()`：

- 对支持的普通方块优先于目标方块交互执行。
- 保存原始 `BlockState`，通过服务端玩家正常挖掘流程破坏原方块，因此掉落结果受当前框架物品、游戏模式、权限、BreakEvent 和原战利品表影响。
- 成功后在原位置放置空框架并写入捕获状态。
- 生存消耗一个框架，创造不消耗。
- 破坏被取消或失败时不放置、不消耗，并阻止该次原方块交互。
- 目标有 BlockEntity 或属于其他不支持类型时返回 `PASS`，箱子等仍执行自身正常交互。

### 支持边界

`FrameTargetValidator` 拒绝：

- `EntityBlock` 或位置上实际存在 BlockEntity。
- `FrameBlock`。
- 门、床、双格植物等多位置结构。
- 空气、液体、火、传送门、移动活塞等系统状态。
- `frame_unsupported` 标签中的方块。

普通完整方块、半砖、楼梯和普通模组 baked-model 方块允许使用。动态 BlockEntity 模型留给未来专用框架方块实现。

### 形状与性质代理

`FrameBlock` 使用 appearance 和 `FrameAppearanceBlockGetter` 代理：

- `getShape`
- `getCollisionShape`
- `getOcclusionShape`
- `getVisualShape`
- `getBlockSupportShape`
- 光照、地图颜色、声音、摩擦、速度系数和寻路查询

耐久、爆炸抗性、燃烧、活塞推动和右键交互始终使用框架自身规则，不继承 appearance。

框架使用基岩级硬度和爆炸抗性：

- 生存玩家无法挖掘。
- 原版爆炸、Isaac Bomb 和 Giga Bomb 依据爆炸抗性时无法摧毁。
- 活塞不能推动。
- 创造玩家可以正常左键删除，删除后直接变为空气，不恢复 appearance，也不额外返还保存的方块。

### 客户端渲染

`FrameBlockEntity#getModelData()` 提供 appearance。

`FrameBlockClientEvents`：

- 在 `ModelEvent.ModifyBakingResult` 中用 `FrameBlockModel` 包装框架的基础模型。
- 注册框架方块颜色处理器，将草、树叶等 tint 查询转发给 appearance。

`FrameBlockModel`：

- 获取 appearance 对应的原始 `BakedModel`。
- 使用 `FrameBlockAndTintGetter` 让模型查询当前及相邻框架时得到保存的 appearance。
- 转发材质模型的 ModelData、RenderType、粒子图标、AO 和 tint index。
- 完整方块保留正常面剔除；半砖、楼梯和非完整遮挡形状采用保守剔除，避免缺面和 X-Ray，代价是少量额外 overdraw。
- 不支持依赖 BlockEntityRenderer 的 appearance。

`FrameStairShapeResolver` 在 appearance 设置、替换、相邻框架变化和框架移除时刷新楼梯 `SHAPE`，确保多个框架中保存的楼梯能形成内外拐角。

## 验证

运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot'
.\gradlew.bat compileJava
.\gradlew.bat test
git diff --check
```

游戏内验证：

- 放置空框架，使用完整方块、上下半砖和不同朝向楼梯填充，物品数量不变。
- 相邻楼梯框架形成直线、内拐角和外拐角。
- 草、树叶、玻璃、透明及 cutout 方块颜色和渲染层正确。
- 碰撞箱、选择框和支撑面跟随 appearance。
- 生存无法挖掘或炸毁，创造可以直接删除。
- Isaac Bomb、Giga Bomb 和活塞不能破坏或移动框架。
- 使用框架物品包裹普通方块时优先阻止原交互、执行正常挖掘掉落并消耗框架。
- 对箱子使用框架物品时不包裹，箱子仍正常打开。
- 生存不能替换已有 appearance，创造可以替换。
- 世界重载和结构方块正常方向保存/加载后 appearance 完整保留。
- 框架无法保存另一个框架。

## 明确限制

- 不使用 Mixin。
- 不支持任意 BlockEntity、门、床和多格结构。
- 不代理 appearance 的实际右键功能或红石行为，只保存其显示状态和静态性质。
- 结构方块普通保存和原方向加载受支持；结构加载界面的旋转或镜像不会变换 NBT 内嵌 appearance 的方向。`facing` 属于被保存状态而非真实 FrameBlock 状态，原版无法自动处理。
- 第一版只有不可破坏框架，不创建涂层/框架规则注册表。
