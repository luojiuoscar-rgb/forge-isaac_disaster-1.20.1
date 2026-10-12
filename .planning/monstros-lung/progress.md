# 执行进度

- 2026-10-11：用户批准计划；当前代码与脏文件已核对，未触碰无关改动。
- 已检查 Java 17 的 java.exe 存在；后续仅执行一次 runData。
- 已实现三类与物品/能力/攻击注册；实际新增道具 ordinal=180、品质=2。
- 英文 Wiki 因 Accept 中含 WebP 而返回 WebP；请求 image/png 后返回 HTTP200、224 bytes、真实 PNG32×32，已保存。
- treasure、greed_treasure JSON 校验通过，均恰好一次且无权重；五行描述已按批准顺序写入。
- 静态检查：充能不足不发射；取消也 finally 清理；一次 ATTACK_PLAN、委托 PREPARE_AND_EXECUTE；已有妈妈的眼影逐弹概率和 Ipecac/Haemolacria 分裂路径保留。
- 单份逻辑数 1/2/3/4 对应 14/16/19/21；两份单发 26，最多 50；垂直瞄准使用非平行基向量。
- 即将运行一次 runData，未执行独立测试或客户端验证。
- 首次 wrapper 因沙箱无法创建 C:\.gradle 缓存锁而在 Gradle 启动前退出；正常权限流程后同一命令成功。
- Microsoft OpenJDK 17.0.11 执行 runData --offline --no-daemon：BUILD SUCCESSFUL，退出码 0（1m23s），含 compileJava 与所有数据生成器；未独立执行 compile/build/tests。
- 已核验生成 monstros_lung.json 模型引用正确图标，passive_items 标签出现一次，latest.log 无 ERROR/Exception，改动范围 git diff HEAD --check 通过。
- 现有 SoulStateEffect.java 使用过时 API 的编译警告仍存在；没有模型/标签/数据生成错误。
- 最终静态检查确认原有 ItemId 序列完整不变、获取/移除对称、五行中文精确值和顺序、两个池条目。实现完成；游戏内效果、JUnit、GameTest、客户端与渲染宽度未验证。

## 后续反馈修正

- 按用户要求将 AttackPrio 按 tier、priority 升序排列并加一行说明；保留全部数值。
- 移除 MonstrosLung 自身的 FIRING_MODIFIER 获取/移除注册及导入。原注册用于复用弹数逻辑，但六个相关道具已自行注册，肺无须额外注册。
- 充能继续由 MonstrosLungAttack.getTotalCharge 计算 ceil(4.3 * max(1, getShotDelay))；不修改 TEARS/TEARS_CORRECTION。相关道具已有的弹数与发射间隔计算路径保留。
- 本次只修改以上两处代码；保留用户已调整的描述和其他文件。不改 isActive/onReleased，待验证完成后解释其职责。
- 优先级升序、模块移除、六个相关道具获取/移除路径静态检查通过；Microsoft Java 17.0.11 的 runData --offline --no-daemon 成功（53s，退出码 0），各数据生成器完成，无新增生成文件；未做游戏内验证。

## 组合与独立散射状态

- 用户要求两个组合 Rule、拆分肺的 onReleased，并确认硫磺火散射的 360°、每次3～5道、整轮方向固定与原硫磺火属性；进一步要求各光柱独立判定效果，并允许 TriggerModule 实现。
- 已添加 C_SECTION_MONSTROS_LUNG 与 BRIMSTONE_MONSTROS_LUNG Rule 和升序优先级，移除肺 isActive 特例。三者同时存在时剖腹产仍优先。
- 肺 onReleased 的规划、方向分组、散射展开与弹丸配置已提取私有方法，保留一次规划事件、逐弹准备、基础武器执行与 finally 清理。
- 新增 BrimstoneMonstrosLung 模块、BrimstoneMonstrosLungAttackPlan 执行效果及注册；肺通过 StatManager 对称获取/移除模块，未重新注册 FIRING_MODIFIER。
- 额外光柱取自新建、未准备的硫磺火上下文，与原光柱分别经过准备事件。TheCommonCold.attachToBullet 每次调用独立概率判定，中毒颜色和触发器保存在各光柱自身；调度中保留该光柱整轮状态。
- 静态核对规则结果、优先级、四个私有方法、模块生命周期、每次3～5与新建上下文；独立数学检查6000个方向涵盖水平/斜向/垂直瞄准，均在指定平面且为单位方向。Java运行与游戏内表现尚待验证，接下来仅运行runData。
- Microsoft Java17.0.11 的 runData --offline --no-daemon 已成功（39s、退出码0），包含 compileJava 与新组合/模块/执行效果注册初始化；所有数据生成器完成，written:0。原有 SoulStateEffect 弃用警告仍存在。
- 实现与验证完成：未改变普通硫磺火代码或攻击公共接口，未改变用户的两行道具描述及其他无关文件；组合中各光柱的独立状态经静态路径核对，JUnit、GameTest、客户端效果未执行。

## 命名及覆盖优先级修正

- 按用户反馈将执行效果类/文件改名为 BrimstonePlusMonstrosLung，同时注册键改为 brimstone_plus_monstros_lung、常量改为 BRIMSTONE_PLUS_MONSTROS_LUNG，更新散射模块引用；行为及模块自身标识保持原样。
- 两个新增覆盖组合以肺的 (200,300) 为基准，剖腹产＋肺 (200,301)，硫磺火＋肺 (200,302)，保留升序与其他已有优先级。修正前剖腹产优先的记录已被本次要求覆盖。
- 重力添加位于 MonstrosLungAttack.configureProjectile，针对每颗根类型为 BULLET 的散射弹丸附加 GRAVITY；并非在被动能力中给玩家添加全局模块，激光不添加。复用 GravityTrajectoryModule.ACCELERATION=0.05。
- 接下来核对旧名称引用与排序，并执行一次 runData。
- 旧 Java 类/常量引用已清除，类文件迁移与注册键/模块引用一致；所有优先级仍升序排列。
- Microsoft Java17.0.11 的 runData --offline --no-daemon 成功（1m56s、退出码0），compileJava 和全部数据生成器通过，written:0；仅有既有弃用与环境警告。没有执行 JUnit/GameTest/客户端。
