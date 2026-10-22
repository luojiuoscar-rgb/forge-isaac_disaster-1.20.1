package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpiderMovementTest {
    @Test void roamingSpeedsVaryWithinTwoTenthsToThreeTenths() {
        var random=RandomSource.create(42);
        double min=1,max=0;
        for(int i=0;i<1000;i++) {
            double speed=SpiderMovement.randomRoamSpeed(random);
            assertTrue(speed>=0.2&&speed<=0.3);
            min=Math.min(min,speed);max=Math.max(max,speed);
        }
        assertTrue(min<0.21&&max>0.29,"Roaming samples across the requested speed range");
    }
    @Test void onlyForwardAndLateralBodyAxesProduceMovementAnimations() {
        assertEquals(SpiderMovement.Gait.FORWARD,SpiderMovement.animation(new Vec3(0,0,1),0));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(new Vec3(0,0,-1),0));
        assertEquals(SpiderMovement.Gait.RIGHT,SpiderMovement.animation(new Vec3(1,0,0),0));
        assertEquals(SpiderMovement.Gait.LEFT,SpiderMovement.animation(new Vec3(-1,0,0),0));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(new Vec3(1,0,1),0));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(new Vec3(-1,0,1),0));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(new Vec3(1,0,-1),0));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(new Vec3(-1,0,-1),0));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(Vec3.ZERO,45));
        assertEquals(SpiderMovement.Gait.RIGHT,SpiderMovement.animation(new Vec3(0,0,1),90));
        assertEquals(SpiderMovement.Gait.FORWARD,SpiderMovement.animation(new Vec3(-1,0,0),90));
        assertEquals(SpiderMovement.Gait.IDLE,SpiderMovement.animation(new Vec3(1,0,0),90));
        assertEquals(SpiderMovement.Gait.LEFT,SpiderMovement.animation(new Vec3(0,0,-1),90));
    }
    @Test void randomMoveDelaysCoverHalfToTwoSeconds() {
        var random=RandomSource.create(9); int min=999,max=0;
        for(int i=0;i<1000;i++){int delay=SpiderMovement.randomMoveDelayTicks(random);min=Math.min(min,delay);max=Math.max(max,delay);}
        assertEquals(10,min);assertEquals(40,max);
    }
    @Test void everyTravelModeTurnsAWorldDiagonalIntoOneBodyAxis() {
        Vec3 goal=new Vec3(4,0,4);
        SpiderMovement.Gait[] modes={SpiderMovement.Gait.FORWARD,SpiderMovement.Gait.LEFT,SpiderMovement.Gait.RIGHT};
        float[] headings={-45,-135,45};
        for(int i=0;i<modes.length;i++) {
            assertEquals(headings[i],SpiderMovement.targetYaw(goal,modes[i]),1e-5);
            assertTrue(SpiderMovement.aligned(goal,headings[i],modes[i]));
            Vec3 travel=SpiderMovement.direction(headings[i],modes[i]);
            assertEquals(Math.sqrt(0.5),travel.x,1e-6);
            assertEquals(Math.sqrt(0.5),travel.z,1e-6);
            assertEquals(modes[i],SpiderMovement.animation(travel,headings[i]));
        }
    }
    @Test void turnsAreBoundedUseTheShortArcAndFinishWithoutOvershooting() {
        assertEquals(27,SpiderMovement.turnYaw(0,90));
        assertEquals(-27,SpiderMovement.turnYaw(0,-90));
        assertEquals(181,SpiderMovement.turnYaw(179,-179));
        assertEquals(-181,SpiderMovement.turnYaw(-179,179));
        assertEquals(90,SpiderMovement.turnYaw(80,90));
        float yaw=0;
        for(int i=0;i<4;i++)yaw=SpiderMovement.turnYaw(yaw,90);
        assertEquals(90,yaw);
        assertFalse(SpiderMovement.aligned(new Vec3(1,0,1),0,SpiderMovement.Gait.FORWARD));
    }
    @Test void movementClockDependsOnlyOnTheSelectedGait() {
        for(var gait: new SpiderMovement.Gait[]{SpiderMovement.Gait.FORWARD,SpiderMovement.Gait.LEFT,SpiderMovement.Gait.RIGHT}) {
            float time=0;
            for(int tick=0;tick<8;tick++)time+=SpiderMovement.gaitTimeStep(gait,1);
            assertEquals(0.6F,time,1e-6,"Eight game ticks advance 0.6 authored seconds at 1.5x");
            assertEquals(0.0375F,SpiderMovement.gaitTimeStep(gait,0.5F),1e-6);
        }
        assertEquals(0,SpiderMovement.gaitTimeStep(SpiderMovement.Gait.IDLE,1),"Idle does not advance the movement clock");
    }
    @Test void climbingUsesTwoBlocksPerSecondAndChecksDropOncePerSecond() {
        assertEquals(0.1,SpiderMovement.CLIMB_SPEED,1e-9);
        int drops=0; var random=RandomSource.create(1); for(int i=0;i<2000;i++) if(SpiderMovement.climbDropCheck(random,20)) drops++; assertTrue(drops>100&&drops<300);
        assertFalse(SpiderMovement.climbDropCheck(RandomSource.create(1),19));
    }
    @Test void pausesCoverSixThroughFourteenTicks() {
        var random=RandomSource.create(42);
        int min=99,max=0;
        for(int i=0;i<1000;i++){int pause=SpiderMovement.pauseTicks(random);min=Math.min(min,pause);max=Math.max(max,pause);}
        assertEquals(6,min);assertEquals(14,max);
    }

}
