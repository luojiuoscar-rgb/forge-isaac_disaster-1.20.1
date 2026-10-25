# Mowzie’s Mobs 垂直太阳光柱源码调研

核对范围：官方公开仓库 BobMowzie/MowziesMobs-Public 的 1.20 分支，提交 64d15248c4421fdf8555165134b306e8cfc0a6c6。build.gradle 确认模组版本 1.8.2、Minecraft 1.20.1、Forge 47.2.32、Java 17。本文是源码与贴图分析，未启动游戏进行视觉复现。

本记录保留原调研结论。下载的上游源码、贴图、分析图及来源JSON已按用户要求清除；固定提交和原始路径保留在文末，可重新定位。

**结论**：目标攻击是 Sunstrike，由 EntitySunstrike 承载计时、位置和伤害，由 RenderSunstrike 绘制。画面由竖直朝向镜头的单个四边形、地面光圈、落点粒子、地面焦痕组合而成。柱身没有使用实体模型的圆柱网格。

**1. 光柱的几何**

RenderSunstrike.drawStrike（136 行）先画水平地面光圈，再让 PoseStack 绕 Y 轴旋转摄像机 yaw 的相反角，之后 drawBeam（168 行）提交四个顶点。局部坐标中，底边为 (-r,0,0) 到 (r,0,0)，顶边为 (-r,H,0) 到 (r,H,0)。因此它是只绕竖直轴转向镜头的 billboard；摄像机俯仰不参与旋转，光柱始终垂直于地面。

H = 256 - 实体世界 Y。源码写死世界坐标 Y=256；复用时应按需求设置终点高度。不能把这个常量解释为当前维度的真实世界上限。

**2. 贴图如何变成长光柱**

原图 assets/mowziesmobs/textures/effects/sunstrike.png 为 256×32，包含两排地面光圈帧、焦痕及右侧 32×32 的柱身区域。布局与采样方式见下文。


drawBeam 只使用 x=224..255 的区域。每次绘制，V 取 frame/32 到 (frame+1)/32，等于从右侧图案中取一条 32×1 的横向颜色切片，并把该切片映射到整根长四边形。横向明暗和透明度形成亮芯与外沿；随计时更换行，得到冲击动画。这不是把右边整张 32×32 小图直接纵向拉伸，也不是沿柱身循环滚动 UV。

代码提供 0..31 的行索引范围，但实际渲染在插值计时大于 40 时优先切换为焦痕，不能据此宣称全部 32 行都会在攻击中完整播放。

**3. 蓄力与爆发**

计时来自 EntitySunstrike，并使用上一 tick 与当前 tick 的插值。以光柱实体的 strikeTime 为起点、正常 20 TPS 估算：

| 时段 | 行为 |
|---|---|
| 0≤t<35，约 1.75 秒 | 蓄力光圈、光柱；柱身半宽从 2 收缩到 0.25，透明度从 0 增至 0.7；早期生成聚拢光球粒子 |
| t=35 | 服务器进行一次范围伤害判定；客户端生成火焰、熔岩粒子；柱身半宽改为 1 |
| 35≤t≤40 | 柱身切换颜色切片，地面播放冲击光圈 |
| t>40 | 渲染器优先画焦痕，不再画柱身；实体可继续生成余烟 |
| t≥1243 | 达到最长存活时间并移除；天空遮挡等情况可提前移除 |

注意 STRIKE_LENGTH=43 是归一化动画参数，并不代表柱身必定渲染至第 43 tick。焦痕在源码中按附近地表方块拆分绘制，通过 UV 保持图案位置连续，并作随机镜像；表面有细小高度偏移。

**4. 发光与透明**

MMRenderType.getGlowingEffect（25 行）使用原版 RENDERTYPE_BEACON_BEAM_SHADER、TRANSLUCENT_TRANSPARENCY、NO_CULL、COLOR_WRITE。即原版信标光束着色器、普通半透明混合、不剔除背面，只写颜色而不写深度。该方法没有关闭深度测试；另一个 getSolarFlare 才显式设置 NO_DEPTH_TEST，不能混淆。

这条渲染路径没有自行实现体积散射或专用后期 Bloom；“看起来发光”与“为周围方块提供动态光照”也应区分。

**5. 落点、伤害、联网**

EntityUmvuthi.SunstrikeAbility（963 行）按目标近期位移预估落点，在服务器上生成 EntitySunstrike。EntitySunstrike.moveDownToGround 向下做方块碰撞射线，调整到落点地面，并处理下半砖高度；相关位置通过实体传送包同步。

EntitySunstrike.damageEntityLivingBaseNearby（194 行）在 t=35 触发一次。先查询从落点附近向上延伸到世界最大建造高度+20 的 AABB，再筛选 XZ 平面距离小于 3 格的实体，排除掉落物、部分友方/施法者。分别尝试普通投射物伤害与火焰伤害，并根据配置调整数值。柱身绘制半宽为 1，不等于伤害半径 3。

Forge 的 NetworkHooks 负责实体生成包；IEntityAdditionalSpawnData 写入 strikeTime，客户端接收后本地推进动画；随机 variant 使用 SynchedEntityData 同步，使焦痕随机变体可复现。不需要每帧传送柱身四个顶点。

**复用思路（建议）**：用地面落点实体或特效状态记录中心位置和起始时间；客户端分别画水平光圈与仅绕 Y 轴转向镜头的竖直四边形；以宽度、alpha 和纹理切片控制蓄力/冲击；服务器单独在命中时刻判定范围伤害。先保持这种结构，是否增加外层光晕、圆柱网格或更多粒子，再按实际画面需要决定。

## 固定上游出处

以下为原调研时核对的固定提交，未在清理时重新联网核对。

- [build.gradle](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/build.gradle)；Git blob `c1d8fe9833cb077b9b9f5c381d8b63cc825a153f`。
- [EntitySunstrike.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/effects/EntitySunstrike.java)；Git blob `3c1f8097f11001139ecfa38e030a298e33915cbb`。
- [EntityUmvuthi.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/server/entity/umvuthana/EntityUmvuthi.java)；Git blob `0b87783c979a44d2b93411be5f854d8bc07a5e0a`。
- [gradle.properties](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/gradle.properties)；Git blob `29b2ac43999492f72663392b82b4e6e9a9ac74a0`。
- [MMRenderType.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/client/render/MMRenderType.java)；Git blob `0ec752b58ad0434ec5cf60ceadcc90f07c3cf7f1`。
- [RenderSunstrike.java](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/java/com/bobmowzie/mowziesmobs/client/render/entity/RenderSunstrike.java)；Git blob `f3aa9f101ad2022b5775fe742526c6ffb6cbc504`。
- [sunstrike.png](https://github.com/BobMowzie/MowziesMobs-Public/blob/64d15248c4421fdf8555165134b306e8cfc0a6c6/src/main/resources/assets/mowziesmobs/textures/effects/sunstrike.png)；Git blob `36926a77b9fbce17b3c6f348f95f9753c70f7d70`。

原调研仅验证源码及贴图关系，未运行Mowzie原模组复现画面。Isaac Disaster圣光的实现与验证见../holy-light/中的记录。
