# 机器宝宝（Robo-Baby）

使用单眼模型，保留已确认的描边、眼部细节、嘴部轮廓、额头接缝及后脑。

- `robo_baby.bbmodel`：Blockbench 可编辑源文件，内嵌完整贴图、图层、骨架和动画。
- `robo_baby.png`：正式贴图，256×128；逻辑 UV 128×64。

动画：`idle` 1 秒循环。

模型与动画 Java 可从 `.bbmodel` 重新导出，不额外保存参考副本。运行时使用 `src/main` 中独立的 `RoboBabyModel`、`RoboBabyAnimations` 与模型层，按原始比例的 60% 显示，并支持跟班视觉倍率。

每帧重置姿态，播放待机动画后叠加独立头部瞄准。激光从中立头部中心生成，不添加前向偏移。
