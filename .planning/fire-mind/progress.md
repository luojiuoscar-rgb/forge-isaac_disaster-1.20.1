# 进度

## 2026-10-27

- 已核对feature/feature-c与干净工作区，读取AGENTS和道具skill被动分支。
- 批准计划已落盘；未修改其他分支，未提交。
- 首批JUnit红灯已确认：4项测试因缺失新道具/灼烧类失败；日志build/fire-mind-red.log。
- 首次Gradle因用户缓存锁访问受限未启动；经正常提权运行成功进入测试，无独立编译/build命令。
- 已实现灼烧核心、Entity局部钩子、能力/模块/攻击附件与命中效果，加入语言和两种宝箱房池。
- 绿色验证、GameTest及runData待执行。
- 初版定向JUnit已通过（FireMindTest、DeadlyPoisonEffectTest、HolyLightItemTest）；新附件/语言测试已补充，待再运行。
- 已补17项GameTest，准备实际运行；包含目标NBT保存/恢复验证，不等同于真实存档人工验收。
- 官方图标下载及外观检查完成。
- 最终定向JUnit：20项通过（新增9项，既有剧毒3项、圣光8项）；日志build/fire-mind-validation.log，XML为build/test-results/test/TEST-*.xml。
- 实际GameTest：首次火之意志17项+圣光12项，共29项通过；补充取消放置传送门场景、调整实体恢复顺序后，火之意志18项再次全部通过。日志build/fire-mind-placement-red.log（原用于红灯探测，实际全部通过，无需扩大放置实现）。
- 已验证实体NBT恢复后的40 tick计时与死亡清理；未执行人工存档/客户端画面验收。
- runData已执行且仅执行一次，通过；日志build/fire-mind-datagen.log。生成模型的layer0为isaac_disaster:item/fire_mind，passive_items标签包含新道具。
- 两种宝箱房池各仅包含一次fire_mind；语言JSON无重复键，说明经Minecraft翻译解析验证，排版仅静态审查。
- git diff HEAD --check通过；没有额外执行独立编译或build；Gradle使用Microsoft Java17.0.11。

## 初版改动归属（第二轮替换部分见下）

- 新增核心：BurningEffect、EntityMixin、被动能力FireMind、触发模块FireMind、FireMindAttachment、FireMindImpact。
- 修改现有注册：ModEffects、EffectManager、ItemId、ModPassiveAbilities、ModPassiveItems、ModTriggerModules、ModExecutableEffects、ModBulletColors；LivingEntityMixin和mixin配置接入清理。
- 资源：英文Wiki道具PNG、原版抗火状态PNG、en_us语言条目、treasure/greed_treasure池；模型和标签由datagen生成。
- 测试：FireMindTest、FireMindAttachmentTest、FireMindGameTests。规划只保留本目录3个关键Markdown。
- 爆炸、照明、常驻属性、套装及物品NBT均未添加；没有修改其他道具的行为。

## 第二轮（简化灼烧、地球协同）

- 已更新原灼烧测试并新增10项地球协同GameTest；红灯验证出现12项预期失败，确认旧实现不符合新规则。
- 首次GameTest自动审批超时未执行，按工具允许重试一次后正常运行。
- 已移除三处灼烧注入，改为附加/刷新时点燃、效果tick内检查、Forge移除/到期事件清理自身计时。
- 新增ItemDropHelper与ItemEntityMixin，只扩展掉落物火焰免疫并在合并时继承；未引入Runtime、State或按玩家队列。
- 已接入地球临时掉落作用域、单次SMELTING转换及3格周边防火，补充双方协同说明。
- 图标已改为原版火焰粒子素材适配的18×18透明图标；不是抗火图标。
- 最终20项定向JUnit通过，0失败、0错误；包括4项灼烧计算、5项附件与翻译、3项剧毒、8项圣光回归。
- 40项实际Forge GameTest全部通过：18项火之意志、10项地球协同、12项圣光回归。日志build/fire-mind-revision-validation.log，BUILD SUCCESSFUL。
- 第二轮runData实际执行一次并通过；最终JUnit与runData日志build/fire-mind-revision-final.log，BUILD SUCCESSFUL。模型、标签、两种道具池和语言解析已核对。
- 使用Microsoft Java17.0.11；未执行独立编译或build。两次自动审批超时未运行命令，工具允许的重试成功。
- EntityMixin已删除，LivingEntityMixin恢复原状；ItemEntityMixin仅处理掉落物防火和合并。保留两处用户未暂存的注册修改，不提交。

## 当前验证限制

- 未执行客户端画面、真实双客户端观察或人工退出重进存档。
- 已通过服务器测试的实体NBT保存/恢复，不替代人工存档验收。
- 普通火块的接触伤害、光照、蔓延，以及身上着火的原版周期伤害均保留；灼烧额外按独立40 tick时钟尝试伤害，遵守原版受伤规则。

## 2026-10-28：协同说明归属

- 灼烧图标与MC原版textures/particle/flame.png逐像素比对通过（放大后加透明边框），继续复用原素材。
- 火之意志不再展示地球协同；只在地球处展示，并将三条action翻译改为terra.synergy.fire_mind.1至3。
- 保留用户将火之意志详情改为EffectManager.BURNING.getExplainDesc()及删除旧detail翻译的修改；翻译测试适配实际描述入口。
- 本轮5项FireMindAttachmentTest通过，runData实际运行一次通过，日志build/fire-mind-tooltip-validation.log；未重跑GameTest、客户端或人工存档验收，游戏机制未修改。
- ItemEntityMixin维持局部火焰免疫与合并继承；ItemDropHelper维持标记读写及单次熔炼工具职责，不新增功能。

## 2026-10-28：方块火焰图标、原版无敌与吞饰品进食

- 用户确认改用原版火焰方块图像和掉落物无敌；已删除ItemEntityMixin与配置、两项标记工具，仅保留ItemDropHelper.smeltOnce。
- burning.png已从block/fire_0.png提取首帧，18×18透明画布，已查看实际图像。
- CurioSlotManager实际吞饰品成功后，每件恢复Foods.BREAD的饥饿值和饱和度；空槽、失败、重复调用与存档恢复不会额外进食。
- 更新9项地球测试，去除专用合并继承用例；新增4项实际吞饰品营养GameTest。红灯运行13项，11项因旧行为预期失败，日志build/fire-mind-simplify-red.log。
- 自动提权审批两次超时未执行；改用工作区已存在的.gradle用户缓存和已安装Gradle8.8，无需写入外部缓存。首次测试修正了测试自身的capability存取方法名；随后实际红灯符合预期。
- 绿色验证：12项定向JUnit通过（灼烧计算4、剧毒回归3、附件和翻译5），0失败、0错误；31项实际Forge GameTest通过（灼烧/火之意志18、地球9、吞饰品营养4）。本轮runData实际执行一次通过；日志build/fire-mind-simplify-validation.log，BUILD SUCCESSFUL in 2m10s。
- 已核对图标逐像素匹配block/fire_0.png首帧；生产和测试源码没有ItemEntityMixin或旧防火标记引用，编译输出中也不再有该Mixin类。git diff HEAD --check通过。
- 未单独执行编译或build；客户端画面、真实双客户端和人工退出重进存档未验收。实体NBT保存恢复及读取吞饰品存储不重复进食已由GameTest验证。
- 后续小重构：smeltOnce移为唯一调用方BreakBlockAndDrop的私有静态方法，删除ItemDropHelper和import；行为不变。9项实际地球协同GameTest与本轮一次runData均通过，日志build/terra-smelting-inline-validation.log，BUILD SUCCESSFUL；无遗留源码引用，未新增测试、未重跑JUnit及客户端。
