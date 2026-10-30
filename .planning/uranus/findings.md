# 发现

- 伤害来源当前没有实际弹丸上下文；需要保持原有伤害归属和击退计算并增加可选IBulletObject。
- 共享冻结已有视觉、AI与滑行/撞墙/火焰碎裂；扩展现有FrozenEffect和事件，不建立天王星管理器。
- GroundEffectManager已支持null owner的无来源水迹；需要在接触和追踪（含已缓存目标）精确排除FROZEN。
- 纯净环形弹不能走普通玩家发射入口，否则会附加玩家其他效果；使用RING与底层BulletRuntime。
- 英文Wiki已核验原ID596、品质3、星象池；中文正文403，未完成双站核验。图标需下载真实32×32 PNG。
- 现有调度器在ServerTick.END吸收新任务；单纯schedule(1)不保证跨游戏tick。碎裂任务仅在同tick被消费时再延后一次，调度器内部发生死亡的路径不会额外多等一tick。
- GroundEffectManager还承载独立PotionPuddle接触；冻结排除只作用于伤害和追踪，保留其药水接触语义。
- 冰弹成品使用用户原图抠去纯灰背景并最近邻缩放为32×32透明PNG；原生imagegen调用未返回可用文件，未使用CLI或代用道具图标。
- 来源对齐核对：feature-b在e60df89e的工作区干净。实际扩展的是SimpleTrigger，其第四参数为ResourceLocation，绑定ContextKeys.SOURCE；AddFamiliars/SpawnFamiliars等用该ID区分生成道具。EFFECT_SOURCE则是Entity，经POTIONS传给addEffect用于冻结玩家归属。两者语义及类型不同，不能合成一个强类型key。按用户授权对齐B的SOURCE和SimpleTrigger接口，保留独立实体来源。
- 用户进一步确认冻结可使用专属施加路径：现以mob.addEffect(FROZEN实例, source)直接传冻结者，移除EFFECT_SOURCE及其全部代码/测试引用；普通减速仍走POTIONS且不携带施加者。onFrozenAdded从原版事件取得来源，冻结UUID保存与首片水迹归属保持。此方案取代上一条保留通用实体来源Key的决定。
