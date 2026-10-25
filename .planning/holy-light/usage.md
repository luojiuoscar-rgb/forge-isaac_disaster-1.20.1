# 圣光柱使用与验证

## 调用

在服务器线程使用，同一世界的活体实体作为施法者：

```java
boolean created = HolyLight.strike(level, caster, position, damage, false);
boolean immediate = HolyLight.strike(level, caster, position, damage, 1.2, true);
boolean fasterWarning = HolyLight.strike(level, caster, position, damage, 1.2, false, 2.0);
boolean slowerWarning = HolyLight.strike(level, caster, position, damage, false, 0.5);
```

`false`表示不能创建（输入无效、区块未加载或起点在障碍内等），`true`只表示创建成功。位置在创建时固定，伤害值同时保存。下方1格内有碰撞顶面时吸附，否则保留空中坐标。空中光柱同样伤害附近目标，不生成黑痕，也不会远距离下吸。

`skipWarning=true`在调用内尝试一次伤害；正常路径默认35tick后尝试一次。发射持续15tick（0.75秒），只伤害一次。贴地光柱在发射开始时立即显示黑痕，发射期间保持完整，发射结束后100tick逐渐淡出；空中光柱在发射结束直接移除。

发射柱身前2tick展开，中间保持明亮与宽度至第13tick，最后2tick收束。底部冲击圈使用独立4tick（0.2秒）动画，扩散速度为原20tick动画的5倍，完成后停止绘制。

追加的`warningSpeed`只缩放预警：1原速、2两倍速、0.5半速，实际持续`ceil(35 / warningSpeed)`tick。结果钳制为1..700tick；无效/非正倍率按1处理；跳过预警时倍率不生效。光圈、柱身收缩、蓄力粒子及伤害边界共用实际预警时间。网络协议版本11，联机双方需更新。

默认半径0.8，正且有限的半径最大钳制到16；负的有限伤害按0处理，可单独预览视觉。半径控制伤害、预警及贴花大小，柱高另由天花板和16格上限决定。

圣光伤害区域为落点下方1格至上方2.5格的圆柱，与目标碰撞箱相交后仍需通过遮挡及目标资格检查。水迹接触采用脚底中心，允许脚底高于或低于水迹表面0.5格；水平轮廓及扩散动画的判定保持原有规则。

## 游戏指令

需要权限等级2，站在有支撑的地面附近：

```text
/isd holylight ~ ~ ~ 6
/isd holylight ~ ~ ~ 6 1.2
/isd holylight ~ ~ ~ 6 0.8 true
/isd holylight ~ ~ ~ 6 0.8 false 2
/isd holylight ~ ~3 ~ 6 0.8 true
```

控制台需指定活体执行者：

```text
execute as @p at @s run isd holylight ~ ~ ~ 6
```

## 自动验证

使用Java17。此工作区需暂设正确JAVA_HOME，并使用现有Gradle缓存：

```powershell
$env:JAVA_HOME='C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_USER_HOME='C:\Users\16136\.gradle'
.\gradlew.bat compileJava compileGameTestJava test --tests '*HolyLight*' jar --offline --no-daemon
.\gradlew.bat runGameTestServer -PgameTestNamespaces=isaac_disaster_holylight --offline --no-daemon
```

验证结果及清理范围见同目录progress.md。GameTest编译、JUnit和实际游戏验收分别记录，互不替代。

## 独立发射声音

`holy_light_shot.ogg`注册为`isaac_disaster:holy_light_shot`，由服务器发射事件广播一次；预警是否跳过、伤害是否为0、首个视觉快照是否迟到都不会阻止发射声。客户端快照仅控制冲击粒子，不重复播放声音。

预警使用的原版信标环境音长5.8秒，现在每道圣光保留独立声音实例，仅允许在WARNING阶段单次播放。进入FIRING、取消、离开可见范围、切换世界或断线时停止该实例，避免尾音在发射后继续响起；不会停止其他圣光或普通信标。

用户输入原文件为`master/temp/holy_light.aac`，保持未修改。录音约1.728秒，包含一次使用和五次重叠发射，无法从混音精确还原原始独立素材。当前0.710秒版本使用第一次发射约160ms的独立起音，与第五次发射后的衰减段匹配音量并交叉淡化；保留自然尾音，去掉额外起音。尾音仍可能带少量前几次的混响。提取参数和来源摘要见同目录findings.md；中间音频和提取脚本已清除。

## 客户端验收

使用普通runClient及以上指令检查：预警无焦痕；发射时光柱与焦痕同时出现；柱身持续0.75秒，冲击圈在0.2秒完成；柱身结束后焦痕逐渐淡出；空中发射没有焦痕；跳过预警路径同样成立。再检查半砖、台阶及水迹同时存在的情况。

临时自动截图脚本及测试截图已清理，普通模组只包含正式实现与素材。
