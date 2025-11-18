package net.luojiuoscar.isaac_disaster.registries.content.trajectory;

import net.luojiuoscar.isaac_disaster.registries.trajectory.AttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Built-in (default) trajectories. Each is an AttackTrajectory instance converted
 * from your original enum entries. Keep these stateless.
 */
public final class BuiltinTrajectories {
    private BuiltinTrajectories() {}

    public static final AttackTrajectory WIGGLE_WORM = ctx -> {
        double phaseScale = 1;
        double t0 = ctx.distance * phaseScale;
        double t1 = (ctx.distance + ctx.deltaDistance) * phaseScale;

        double y0 = (t0 < Math.PI) ? Math.cos(t0) - 1 : 2 * Math.cos(t0);
        double y1 = (t1 < Math.PI) ? Math.cos(t1) - 1 : 2 * Math.cos(t1);

        double deltaY = y1 - y0;

        Vec3 dir = ctx.dir;
        Vec3 right = new Vec3(-dir.z, 0, dir.x).normalize();
        double offsetScale = 0.5 + ctx.amplifier * 0.25;

        Vec3 posOffset = right.scale(deltaY * offsetScale);
        Vec3 velOffset = right.scale(deltaY * 0.05);

        return new TrajectoryResult(posOffset, velOffset);
    };

    public static final AttackTrajectory TINY_PLANET = ctx -> {
        LivingEntity owner = ctx.owner;
        if (owner == null || ctx.pos == null) return TrajectoryResult.ZERO;

        Vec3 bulletPos = ctx.pos;

        double targetRadius = 3.0 + ctx.amplifier;
        double angularSpeed = 0.35;
        double yAmplitude = 0.2;
        double yFrequency = 1.2;

        Vec3 ownerCenter = owner.position().add(0, owner.getBbHeight() * 0.5, 0);
        Vec3 rel = bulletPos.subtract(ownerCenter);

        double currentR = Math.max(0.001, Math.sqrt(rel.x * rel.x + rel.z * rel.z));
        rel = rel.scale(targetRadius / currentR);

        double angle = Math.atan2(rel.z, rel.x);
        double nextAngle = angle + angularSpeed;

        Vec3 desiredRel = new Vec3(
                Math.cos(nextAngle) * targetRadius,
                Math.sin(ctx.distance * yFrequency) * yAmplitude,
                Math.sin(nextAngle) * targetRadius
        );

        Vec3 desiredPos = ownerCenter.add(desiredRel);

        Vec3 desiredVel = desiredPos.subtract(bulletPos).normalize().scale(ctx.deltaDistance);
        Vec3 currentVel = ctx.dir.scale(ctx.deltaDistance);
        Vec3 velOffset = desiredVel.subtract(currentVel);

        return new TrajectoryResult(Vec3.ZERO, velOffset);
    };

    public static final AttackTrajectory RING_WORM = ctx -> {
        Vec3 dir = ctx.dir.normalize();

        // 构建局部坐标系，确保 dir 是 x 轴，y-z 平面为圆面
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.dot(up)) > 0.999) up = new Vec3(0, 0, 1);
        Vec3 right = dir.cross(up).normalize();
        Vec3 localUp = right.cross(dir).normalize();

        // 螺旋参数
        double radius = 1 + ctx.amplifier * 0.5; // 圆半径
        double angularSpeed = Math.PI * 0.25; // 每单位距离旋转 0.5 * PI 弧度

        // 累积角度
        double angle0 = ctx.distance * angularSpeed;           // 当前帧起始角度
        double angle1 = (ctx.distance + ctx.deltaDistance) * angularSpeed; // 当前帧结束角度

        // 计算增量偏移
        Vec3 offset0 = right.scale(Math.cos(angle0) * radius)
                .add(localUp.scale(Math.sin(angle0) * radius));
        Vec3 offset1 = right.scale(Math.cos(angle1) * radius)
                .add(localUp.scale(Math.sin(angle1) * radius));

        Vec3 offset = offset1.subtract(offset0);

        return new TrajectoryResult(offset, Vec3.ZERO);
    };

    public static final AttackTrajectory OUROBOROS_WORM = ctx -> {
        Vec3 forward = ctx.dir.normalize(); // 前进方向
        Vec3 up = new Vec3(0, 1, 0);        // 世界竖直方向

        // 参数
        double radius = 1.0 + ctx.amplifier * 0.5;      // 半径固定 1
        double angularSpeed = 2.0; // 角速度 = 2 rad/unit distance

        // 当前帧角度
        double angle0 = Math.PI + angularSpeed * ctx.distance;                  // 初始角度 pi
        double angle1 = Math.PI + angularSpeed * (ctx.distance + ctx.deltaDistance);

        // 圆周偏移在前进方向 + 竖直方向平面
        Vec3 pos0 = forward.scale(-Math.cos(angle0) * radius) // -cos 对应初始 B 在左边
                .add(up.scale(Math.sin(angle0) * radius));
        Vec3 pos1 = forward.scale(-Math.cos(angle1) * radius)
                .add(up.scale(Math.sin(angle1) * radius));

        // 每帧偏移量 = 圆周增量 + 圆心前进
        Vec3 offset = pos1.subtract(pos0).add(forward.scale(ctx.deltaDistance));

        return new TrajectoryResult(offset, Vec3.ZERO);
    };

    public static final AttackTrajectory HOOK_WORM = ctx -> {
        // 当前方向
        Vec3 forward = ctx.dir.normalize();

        // 世界上方向
        Vec3 worldUp = new Vec3(0, 1, 0);

        // 计算局部右方向，如果 forward 与 worldUp 接近平行则换一个轴
        Vec3 right = forward.cross(worldUp);
        if (right.lengthSqr() < 1e-6) right = forward.cross(new Vec3(0, 0, 1));
        right = right.normalize();

        // 局部上方向
        Vec3 localUp = right.cross(forward).normalize();

        // ===== 方向命令序列 =====
        int[] sequence = {0, +1, -1, -1, 0, +1, +1, 0};
        int cycleStart = 2;

        // 当前格子段
        int currentIndex = (int) ctx.distance;
        // 下一帧格子段（根据 deltaDistance 推算）
        int nextIndex = (int) (ctx.distance + ctx.deltaDistance);

        double xRot = 0.0;
        double yRot = 0.0;

        // 仅在下一帧跨格子段时旋转
        if (currentIndex != nextIndex) {
            int cmd = (nextIndex < sequence.length) ? sequence[nextIndex]
                    : sequence[cycleStart + ((nextIndex - cycleStart) % (sequence.length - cycleStart))];

            if (cmd == +1) yRot = -Math.PI / 2;   // 右转
            else if (cmd == -1) yRot = Math.PI / 2; // 左转
        }

        return new TrajectoryResult(Vec3.ZERO, Vec3.ZERO, 0.0, xRot, yRot);
    };

    public static final AttackTrajectory MY_REFLECTION = ctx -> {
        Vec3 dir = ctx.dir.normalize();   // 发射方向
        Vec3 right = new Vec3(-dir.z, 0, dir.x).normalize(); // XZ平面垂直向量

        double a = 21;  // 长轴
        double b = 3;   // 短轴
        double amplifier = 0.3;

        // 椭圆角度增量
        double theta = ctx.distance / a;
        if(theta > Math.PI) theta = Math.PI; // 超过半椭圆就停

        double sign = ctx.startYRot <= 0 ? 1.0 : -1.0; // 正 -> 右偏, 负 -> 左偏

        // 椭圆偏移
        double xOffset = a * (Math.cos(theta) - 1); // 左侧起点在原点
        double zOffset = b * Math.sin(theta) * sign; // 只改这里，乘上 sign

        Vec3 posOffset = dir.scale(xOffset).add(right.scale(zOffset * amplifier));
        Vec3 velOffset = right.scale(zOffset * 0.1 * amplifier);

        return new TrajectoryResult(posOffset, velOffset);
    };
}
