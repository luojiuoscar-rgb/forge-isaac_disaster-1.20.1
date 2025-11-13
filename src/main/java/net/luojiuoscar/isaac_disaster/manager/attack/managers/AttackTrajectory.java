package net.luojiuoscar.isaac_disaster.manager.attack.managers;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public enum AttackTrajectory {

    /**
     *  < pi : f(x)=cos(x)-1
     *  else : g(x)=2 cos(x)
     */
    WIGGLE_WORM((velocity, ctx) -> {
        Vec3 dir = velocity.normalize();      // 原速度方向
        double speed = velocity.length();     // 原速度大小

        // 子弹局部水平面
        Vec3 ref = Math.abs(dir.y) > 0.95 ? new Vec3(1,0,0) : new Vec3(0,1,0);
        Vec3 right = dir.cross(ref).normalize();

        // 计算横向偏移增量（位移增量法）
        double frequencyScale = 0.5;   // 降低频率
        double amplitude = 0.5;        // 提高振幅
        double x = ctx.distance * frequencyScale;

        // 分段函数斜率
        double slope = (x < Math.PI) ? -Math.sin(x) : -2 * Math.sin(x);

        // 将斜率转为**横向位移增量**
        Vec3 lateralOffset = right.scale(slope * amplitude);

        // 将横向位移与前进位移叠加
        return dir.add(lateralOffset).normalize().scale(speed);
    });
    // =========================================================

    private static final Map<Integer, AttackTrajectory> BY_ID = new HashMap<>();

    static {
        for (AttackTrajectory t : values()) {
            BY_ID.put(t.getId(), t);
        }
    }

    private final BiFunction<Vec3, TrajectoryContext, Vec3> applier;

    AttackTrajectory(BiFunction<Vec3, TrajectoryContext, Vec3> applier) {
        this.applier = applier;
    }

    public int getId() {
        return ordinal();
    }

    /**
     * 应用轨迹变换
     * @param velocity 当前速度向量
     * @param ctx 上下文信息
     * @return 新的速度向量
     */
    public Vec3 apply(Vec3 velocity, TrajectoryContext ctx) {
        return applier.apply(velocity, ctx);
    }

    /**
     * 根据id反查轨迹
     */
    public static AttackTrajectory byId(int id) {
        return BY_ID.getOrDefault(id, WIGGLE_WORM);
    }

    // ====================== 辅助类 ======================
    /**
     * 轨迹上下文：用于提供轨迹计算所需的环境数据
     */
    public static class TrajectoryContext {
        public Vec3 bulletPos;   // 当前子弹位置
        public Vec3 targetPos;   // 目标位置（可选）
        public Vec3 velocity;    // 当前速度
        public double distance;  // 子弹沿原始速度方向累积位移

        public TrajectoryContext(Vec3 bulletPos, Vec3 velocity, double distance) {
            this.bulletPos = bulletPos;
            this.velocity = velocity;
            this.distance = distance;
        }

        public TrajectoryContext(Vec3 bulletPos, Vec3 targetPos, Vec3 velocity, double distance) {
            this.bulletPos = bulletPos;
            this.targetPos = targetPos;
            this.velocity = velocity;
            this.distance = distance;
        }
    }
}
