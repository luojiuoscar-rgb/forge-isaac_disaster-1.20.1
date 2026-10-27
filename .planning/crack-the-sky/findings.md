# 实施发现

- 已确认目标工作树与分支正确，开始时git status为空；没有同名物品、能力或素材。
- ItemId末尾追加CRACK_THE_SKY(2)，当前ordinal为232；Wiki ID160仅作身份参考。
- ScheduledFuncHelper支持玩家独立、首tick及固定间隔任务，repeats=-1可持续消费；缺少追加次数接口，因此由本物品保存待生成量，不修改公共调度器。
- HolyLight目前只保存固定基础伤害；需要新增可选亡灵倍率，并在实际命中时判断MobType.UNDEAD，不复制伤害与渲染流程。

> 2026-10-26 圣光被动道具实施更新：用户已将亡灵2倍增伤提升为所有圣光的通用规则。上述可选倍率方案已移除；撕裂苍穹仅传入基础伤害，不再传入或自行应用亡灵倍率。
- 主动公共流程在执行能力前消耗充能；有效入队后正常消耗，包括最终地形不允许生成的情况。原使用取消/充能不足行为不变。
- eligibleFly无条件排除Animal，会漏掉正在攻击玩家的狼。本物品复用eligible的生存/友方/PvP检查，再按Enemy、攻击者、愤怒中立生物或合法玩家筛选；普通HolyLight范围伤害仍使用eligible。

## 裁决

- 独立能力、资源注册可与服务端队列并行；相关文件明确分工，所有Gradle验证由主任务统一执行。
- 按批准计划保留精简Markdown。测试源码可补充，但不声称未执行的JUnit/GameTest通过。
- 首轮静态复审发现调度器在ServerTick.END运行，单纯firstDelay=1可能在使用当tick生成。改为队列保存明确的服务器到期tick，单一schedule每tick检查，到期才生成并将下一次设为+3；追加不修改到期时间。

## 素材与描述

- 图标来自英文Wiki原始PNG：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/0/0e/Collectible_Crack_the_Sky_icon.png/revision/latest?format=original 。32×32，176字节，保留原始索引色PNG；解码检查有透明像素，已查看羽毛外观。
- SHA256：9c512d98e31b567b51f90769f14eac6896e9146250b9fbbba0dc42339369ea6d。
- 首次使用PowerShell/curl时遇到TLS错误，CDN默认又返回WebP；最终用Python/OpenSSL和format=original取得HTTP200 image/png，未转码、未代画，素材不缺失。
- 主描述四行、Shift三行及车载电池提示均静态核对；动态伤害使用StatManager.DAMAGE.description(20)，单一%s占位，无硬编码配置值。未启动客户端测量字体或渲染翻译。
- 未发现项目维护的道具审计/数据库快照脚本；本次以实际注册源码、生成标签、模型与道具池作为核验依据。
- 项目忽略src/generated，模型/标签由runData生成并供构建资源使用，本次没有强制暂存生成目录或手工编写模型。

## 运行时归属调整（2026-10-25）

- 用户确认将物品专属运行状态移到system，避免ability_effect实现目录同时容纳调度和状态管理。
- 新模块为system.combat_effect.crackthesky：CrackTheSkyRuntime持有每玩家状态、单一调度任务、落点纠正及退出/死亡/跨维度/停服清理；CrackTheSkyState为包内可见的数量、时间、五槽位目标历史。
- 原IAbilityEffect仅负责取得服务器玩家、将AMPLIFIER转换为数量并调用CrackTheSkyRuntime.enqueue(player, count)，注册入口保持原类。
- 不借用PlayerAbility或PlayerFamiliarData，不新增存档或网络字段；正常队列结束保留目标历史，生命周期清理的语义不变。
- 保留用户已有的0.75×nearbyRange散布修改、主动描述和语言文件修改。索敌仍为落点周围3格，伤害半径仍为0.8格。
- JUnit和GameTest随模块迁移；groundBelow仍为包内可见，未为测试扩大生产方法可见性。

## 移除近期目标排除（2026-10-26）

- 用户决定删除连续落柱的近期目标排除规则。每道均独立选择落点附近3格内最近的合格敌人，同一敌人可连续被锁定；不保证随机散布的每道都落在某个敌人的索敌范围内。
- 删除五槽位UUID数组、槽位游标、排除集合、命中历史写入及携带目标UUID的Landing结果；落点纠正直接返回Vec3。没有新增Manager或玩家持久数据。
- CrackTheSkyState仅保留待发数量及下一次发射时间；序列耗尽时立即移除玩家状态和本物品任务，不再为下一次使用保留任何目标记录。公共调度器、伤害判定和玩法数值沿用现状。
- 新增真实生成七道光柱的GameTest，控制散布范围保证同一个敌人在每次索敌范围内，通过服务器快照验证七道均纠正到该敌人。最终4项GameTest全部通过。
- 首次测试误以为修改Config.NEARBY_RANGE会影响运行值；当前StatManager读取的是IsaacDatapackData中的GameplayConfig快照。改为在测试中临时安装范围为2的数据包快照，并在finally恢复原快照，不修改生产数据包或为测试扩大生产方法可见性。

## 合回效果类及通用调度（2026-10-26）

- 按用户确认的简化方向删除CrackTheSkyRuntime与CrackTheSkyState，将入队、落点、索敌及退出/死亡/跨维度取消合回原IAbilityEffect实现CrackTheSky；没有嵌套State、临时目标Map或专属Manager。
- ScheduledFuncHelper.scheduleOrAppendForPlayer在现有活跃或待加入任务上原地增加有限次数，保留任务ID、回调、间隔和下一次执行时间；无任务时创建有限次数任务，最早在传入服务器tick之后首次执行，随后沿用已有interval倒计时。次数饱和到int上限，非正次数不创建任务，非正间隔规范化为1；已有无限任务保持无限。
- 次数全部消耗后由调度器自动移除任务及玩家索引；停服仍由调度器统一清理。取消待加入任务时同时删除其玩家索引，已经取消但等待延迟删除的任务不会继续执行或成为追加目标。
- 旧schedule/scheduleForPlayer调用方式及有限/无限任务的既有节奏保持兼容。tick(int)为执行内核，原tick(MinecraftServer)读取服务器tick后委托；新任务首次执行的边界支持计数器溢出。
- 旧状态JUnit由10项调度器行为测试替代；GameTest迁回效果包，维持原方法可见性和4项落点/连续锁敌验证。

## 首发采用自然调度（2026-10-26）

- 用户接受首道在使用当tick末尾执行，要求移除强制跨tick的限制。已删除notBeforeTick、scheduleOrAppendForPlayer的serverTick参数及不再需要的tick(int)重载，恢复由原timeLeft倒计时自然执行。
- 新有限任务在下次调度器运行时首次执行；若在调度回调中创建，则因PENDING_ADD机制到下一次调度才执行。后续仍每3次服务器调度一次；追加不重置现有间隔、回调或次数清理。
- 删除只服务于首发绝对tick边界的溢出测试，其他首发/取消测试按自然调度调整，当前9项调度器行为测试。
