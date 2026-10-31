# 机器宝宝2.0（Robo-Baby 2.0）

保留已确认的加大躯干、左右双色拼接和缩短脖子的双眼天线模型。

- `robo_baby_2.bbmodel`：Blockbench 可编辑源文件，内嵌完整贴图、图层、骨架和动画；模型导出名为 `RoboBaby2Model`。
- `robo_baby_2.png`：正式贴图，128×64；逻辑 UV 128×64。

动画：`idle` 1 秒循环。

运行时使用 `src/main` 中独立的 `RoboBaby2Model`、`RoboBaby2Animations` 与模型层，按原始比例的60%显示，支持跟班视觉倍率。每帧重置姿态，播放待机动画后叠加自动索敌的头部瞄准。

机器宝宝2.0沿链槽跟随，自动攻击16格内目标，每7 tick发射一条穿墙激光，不依赖玩家攻击输入。模型与动画 Java 可从 `.bbmodel` 重新导出，不额外保存参考副本。
