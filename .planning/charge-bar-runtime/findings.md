# 源码发现与设计

- 当前 IChargeableAttack 仅有按下、释放、总量；Revelation 仍由两个事件入口硬编码分发。
- 充能实际值和已同步值均为 PlayerAbility 运行时状态，不存档；已有死亡/退出/维度清理。
- 主攻击同步每3tick发送进度，Revelation 每tick发送；客户端仅存 float，造成跳变。
- 保留原单包注册位置，扩展动作/推进速率；手动更新接口继续支持不可预测的充能来源。
- 满蓄必须由服务器确认，预测值上限小于1，避免提前闪烁/隐藏白环。
- 已有白环参数：alpha128，宽1 GUI px，起始边缘间距4px；本轮替换平滑64段几何为缓存像素轮廓。
- 使用实际 AttackType 注册表发现独立蓄力攻击，排除当前主攻击避免双重tick；Revelation 的资格/总量/释放仍在自身类中。
- 同步缓存记录已发送进度、速率和服务器tick；稳定推进每10tick修正，开始、满蓄、重置、消耗及速率变化即时同步。
- 客户端使用tick+partialTick显示时钟；暂停停止推进，死亡/世界卸载/退出清理，仅小误差在3tick内混合。
- 像素白环预生成65组整数行跨度，半径步长1/16px；渲染无需每帧三角函数或光滑顶点。

## 审阅后精简
- 主攻击选择已缓存，无需额外查找；ServerTickEvent/按键入口各读取一次，独立分发接收此结果以排除重复。
- ModAttackTypes恢复纯注册；AttackType提供实例tick/输入分发与临时独立分发，不新增Controller。
- chargesWhileHeld已移除；addCharge仅累加并限制上下限。各攻击自行判断调用条件；Neptunus通过空的按下/释放override保留充能。
- 释放清理由默认onReleased承担；Brimstone/CursedEye/Revelation在finally调用默认处理；取消发射仍清理，外层不强制清理Neptunus。
- 当前运行时攻击选择更新均传入ServerPlayer。删除无调用者的无玩家选择重载，将切换清理移入updateBestAttackType，移除chargeAttackSource轮询字段。
- updateClientCharge及普通攻击后的残留充能重置无有效数据来源，已移除；新同步/客户端预测无需它们。
