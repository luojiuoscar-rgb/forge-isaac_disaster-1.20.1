# 恶魔宝宝（Demon Baby）

保留已确认的描边精修和侧面、背面纹理。

- `demon_baby.bbmodel`：Blockbench 可编辑源文件，内嵌完整贴图、图层、骨架和动画。
- `demon_baby.png`：正式贴图，64×64；逻辑 UV 64×64。

源动画：`idle` 1秒循环；`attack` 0.8秒单次。

运行时使用 `src/main` 中独立的 `DemonBabyModel`、`DemonBabyAnimations` 与模型层，按原始比例的60%显示，支持跟班视觉倍率。每帧重置姿态，先计算动画，再叠加对敌人的头部瞄准。

恶魔宝宝沿链槽跟随，自动攻击8格内无遮挡的目标，每7 tick发射一颗红色泪弹。每次成功发射后以16/7倍速完整播放7 tick攻击动作；失去目标后完成本轮动作再回到待机。

模型与动画 Java 可从 `.bbmodel` 重新导出，不额外保存参考副本。
