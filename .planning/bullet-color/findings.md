# 发现

- 当前工作区为feature/feature-c，开始时干净。
- 旧BulletColor虽有RGB/alpha字段，但玩家提前压缩为单个bestBulletColor；附件同级保留当前色，与玩家按ID排序不同。
- AttackContext只保存一个colorRl；BulletState已同步RGB和alpha，激光仅同步RGB，水迹自己计算透明度，无需改网络格式。
- BASE实际表示各攻击方式自己的默认颜色，宝瓶座默认蓝色；不能把通用泪弹默认粉色强加到激光或水迹。
- 冰弹等不接受RGB染色的贴图仍使用BulletState.alpha；无需扩大材质系统。
- 除普通发射入口，科技2、胎儿、悲伤炸弹、洛基角、塔米头也提前读取最佳色，必须一并迁移。
- 胎儿派生激光原来仅传RGB获胜ID，会丢失只适用于派生类型的候选，现改为传完整候选快照。
- AttackContext按候选和类型缓存解析结果，offerColor、setAttackType和bindAttackTypeOrCopy均失效缓存；freeze后不允许增加候选。复制保留快照，派生重新按类型解析。
- 公共接口：BulletColor.builder().color(...).alpha(...).priority(BulletColorPrio.POISON或tier/priority).onlyTypes(...).onlyRoots(...).build()；两种范围同时设置为并集。节点用offerColor/offerColors收集候选，不提前比较。
- PlayerAbility仅保存ID计数，getBulletColorIds返回不可变候选快照；bestBulletColor及其NBT字段已删除，不添加旧读写兼容。
- 首轮实际GameTest已70项通过；新增真实Cricket's Body生成器回归，以确认完整候选经过环形分裂和继承流程，不只验证toBuilder。

## 最终验证
- Microsoft Java17.0.11，test、runGameTestServer和一次runData联合执行，退出0、BUILD SUCCESSFUL，日志build/bullet-color-final.log。
- 全量JUnit161类782项通过，0失败/错误/跳过；新增选择器8项、上下文快照/切换/冻结4项。
- 实际Forge GameTest85项全部通过：新增颜色9项，星座16、圣光泪弹12、火之意志18、天王星16、鼻窦炎14。包括真实Cricket's Body分裂生成、概率结果与颜色竞争、类型和根类型限制、alpha-only、旧弹快照、玩家NBT计数回读。
- runData完成所有provider，写入0个文件，生成资源无差异；未额外执行独立compile/build。
- 对照HEAD脚本核对17个原有优先级：tier均0，priority完全保留。无旧get/setColorRl、bestBulletColor缓存或全局排序入口；未修改网络、渲染包及协议15。
- 客户端画面、真实多人观察与真实存档重登未执行；NBT保存/读取仅为上述真实GameTest中的往返，不等同于完整存档验收。特殊贴图RGB限制与alpha渲染分离沿用现有代码路径。

## 后续用户确认
- 层级调整：POISON、CHARM、FEAR、PETRIFIED、DARK_MATTER、FIRE_MIND、HOLY_LIGHT、URANUS为效果层100；原基础染色保持0和原priority。幽灵SPECTRAL为-1/0，不提供RGB，alpha0.75。
- DEAD_ONION只保留棕色，透明度候选复用SPECTRAL。OuijaBoard和DeadOnion各自在获得/移除时对称增减SPECTRAL计数；不改变其他幽灵能力来源或全局StatManager.addSpectral。
- 回归覆盖同持/多份/两种移除顺序、旧弹快照和分裂继承0.75；共享选择器原有0.5下限不变，其他效果仍可提供比幽灵更透明的alpha。
- 本次最终验证：782项JUnit通过，85项真实GameTest全部通过，一次runData成功且written=0；修复了水迹测试中仍要求基础Ipecac覆盖FireMind效果层的旧断言。日志build/bullet-color-spectral-validation.log（JUnit）与build/bullet-color-spectral-final.log（GameTest/data）。
- 外观、多人及实际存档重登仍未验收。原说明无需拆分或修改，无新素材和网络字段，保留未提交修改。
