package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Movement policy shared by spider variants; distances are blocks per server tick. */
public final class SpiderMovement {
    public enum Gait {
        IDLE, FORWARD, LEFT, RIGHT;
        private static final Gait[] BY_ID = values();
        static Gait fromId(int id) {
            return id >= 0 && id < BY_ID.length ? BY_ID[id] : IDLE;
        }
    }
    public static final float TURN_DEGREES_PER_TICK=27;
    public static final float GAIT_PLAYBACK_RATE=1.5F;
    public static final double MIN_ROAM_SPEED=0.2, MAX_ROAM_SPEED=0.3, MAX_SPEED=0.5;
    public static final double CLIMB_SPEED=2.0/20;
    public static final int CLIMB_CHECK_TICKS=20;
    private SpiderMovement() { }
    public static double randomRoamSpeed(RandomSource random) {
        return MIN_ROAM_SPEED+(MAX_ROAM_SPEED-MIN_ROAM_SPEED)*random.nextDouble();
    }
    public static int pauseTicks(RandomSource random) {return 6+random.nextInt(9);}
    public static int randomMoveDelayTicks(RandomSource random) {return 10+random.nextInt(31);}
    public static boolean climbDropCheck(RandomSource random,int climbTicks) {
        return climbTicks>0 && climbTicks%CLIMB_CHECK_TICKS==0 && random.nextFloat()<0.10F;
    }
    private static double forward(Vec3 delta,float yaw) {
        double angle=Math.toRadians(yaw);
        return -Math.sin(angle)*delta.x+Math.cos(angle)*delta.z;
    }
    private static double sideways(Vec3 delta,float yaw) {
        double angle=Math.toRadians(yaw);
        return Math.cos(angle)*delta.x+Math.sin(angle)*delta.z;
    }
    static Gait randomTravelMode(RandomSource random) {
        return switch(random.nextInt(3)) {case 0->Gait.FORWARD;case 1->Gait.LEFT;default->Gait.RIGHT;};
    }
    private static float yawOffset(Gait mode) {
        return switch(mode) {
            case FORWARD->0;case LEFT->-90;case RIGHT->90;
            default->throw new IllegalArgumentException("Unsupported spider travel mode: "+mode);
        };
    }
    public static float targetYaw(Vec3 delta,Gait mode) {
        return (float)Math.toDegrees(Math.atan2(-delta.x,delta.z))+yawOffset(mode);
    }
    public static float turnYaw(float current,float target) {
        // Keep an unwrapped result so crossing +/-180 never produces a full visual spin.
        return current+Mth.clamp(Mth.wrapDegrees(target-current),-TURN_DEGREES_PER_TICK,TURN_DEGREES_PER_TICK);
    }
    public static boolean aligned(Vec3 delta,float yaw,Gait mode) {
        return Math.abs(Mth.wrapDegrees(targetYaw(delta,mode)-yaw))<=0.001F;
    }
    public static Vec3 direction(float yaw,Gait mode) {
        double angle=Math.toRadians(yaw-yawOffset(mode));
        return new Vec3(-Math.sin(angle),0,Math.cos(angle));
    }
    public static Gait animation(Vec3 delta,float yaw) {
        if(delta.horizontalDistanceSqr()<1e-6)return Gait.IDLE;
        double front=forward(delta,yaw),side=sideways(delta,yaw);
        double tolerance=Math.max(1e-7,delta.horizontalDistance()*0.001);
        if(Math.abs(side)<=tolerance&&front>0)return Gait.FORWARD;
        if(Math.abs(front)<=tolerance)return side<0?Gait.LEFT:Gait.RIGHT;
        return Gait.IDLE;
    }
    /** Movement state gates playback; travel speed never changes the authored clock. */
    public static float gaitTimeStep(Gait gait, float ticks) {
        return gait == Gait.IDLE ? 0 : ticks * GAIT_PLAYBACK_RATE / 20F;
    }
}
