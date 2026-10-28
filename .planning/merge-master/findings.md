# 合并发现

- master新增通用CustomExplosion、ExplosionParameters、BombEffectSource，替换旧IsaacBombExplosion；同时带入跟班、蓝蜘蛛、蝗虫和一批道具。
- feature-c保留地面效果模块、圣光、撕裂苍穹、终末天启新视觉、灼烧、地球熔炼及吞饰品营养。
- 冲突15个文件，绝大多数是相同位置的独立新增注册或语言；双方都需保留。
- master完成recursive_module到periodic_module更名，feature-c新添FireMind及其GameTest需对应更新。
- 两分支ItemId末尾冲突，用户已明确采用master顺序后追加c的3个道具；物品NBT使用注册名，主动使用记录也是ResourceLocation。
- FlyRenderer两边敌我判定表达式等价；c的OwnerHostility提取与master多色蝗虫渲染兼容，保留共享判定和多色渲染。
- master添加爆炸网络包，c协议14、master协议5；合并包集合需要新协议15，保留全部包与c的ID会话隔离修复。
- ModEntities合并出的LemonEffectCloud import来自master旧实体；c已删除该实体，保留BlueSpiderEntity等新增类型即可。

## 限制与错误

- 初次沙箱fetch无法解析github.com-personal SSH别名；使用正常提权重试成功，未改远程地址。
- 无提交授权：本轮只保留解决后的合并工作区和索引，master引用保持不动。

## 验证结论

- 全量743项JUnit通过，无失败、错误或跳过。
- 128项实际GameTest通过：圣光道具12、爆炸击退4、火之意志18、终末天启12、吞饰品4、撕裂苍穹4、水迹30、圣光模块22、地球协同9、爆炸核心9、爆炸迁移4。
- runData本轮执行一次成功，20个生成文件更新，包含双方新道具模型/标签/数据。
- 无冲突标记、无recursive旧API或IsaacBombExplosion等遗留代码引用；相对master的diff --check通过。相对c的尾空白提示仅来自master既有blockbench导出源，未修改该源素材。
- 客户端画面、多人真实连接、人工存档重进未执行；不以JUnit或GameTest替代这些验收。
