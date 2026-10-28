package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SpiderMotionAttemptTest {
    @Test void diagonalGroundStepUsesOneSpeedBudgetAndStopsAtGoal() {
        var burst=new SpiderMotionController.Attempt(RandomSource.create(3), new Vec3(100,0,100), SpiderMotionController.Behavior.ROAM, Vec3.ZERO);
        Vec3 step=burst.step(new Vec3(10,9,10),0.7);
        assertEquals(0.5,step.horizontalDistance(),1e-9);
        assertEquals(0,step.y);
        assertEquals(new Vec3(0.01,0,0.02),burst.step(new Vec3(0.01,4,0.02),0.7));
        assertEquals(Vec3.ZERO,burst.step(new Vec3(0,4,0),0.7));
        assertEquals(SpiderMovement.MAX_SPEED,burst.step(new Vec3(100,4,0),5).horizontalDistance(),1e-9);
    }
    @Test void horizontalBurstIgnoresClimbVerticalDistance() {
        var burst=new SpiderMotionController.Attempt(RandomSource.create(2), new Vec3(100,0,100), SpiderMotionController.Behavior.ROAM, Vec3.ZERO);double budget=burst.remainingDistance();
        burst.record(new Vec3(0,2,0));burst.record(new Vec3(2,0,0));
        assertEquals(budget-2,burst.remainingDistance(),1e-8);
    }
    @Test void everyBurstHasSixToNineBlocksOfActualTravelBudget() {
        var random=RandomSource.create(73);
        for(int i=0;i<100;i++) {
            var burst=new SpiderMotionController.Attempt(random, new Vec3(100,0,100), SpiderMotionController.Behavior.ROAM, Vec3.ZERO);double budget=burst.remainingDistance();
            assertTrue(budget>=6&&budget<=9);double travelled=0;
            while(!burst.finished()) {Vec3 step=burst.step(new Vec3(100,0,100),0.7);travelled+=step.horizontalDistance();burst.record(step);}
            assertEquals(budget,travelled,1e-8);assertEquals(Vec3.ZERO,burst.step(new Vec3(1,0,0),0.7));
        }
    }
    @Test void turnsCountActualPathLengthAndBlockedAttemptsDoNotConsumeDistance() {
        var burst=new SpiderMotionController.Attempt(RandomSource.create(1), new Vec3(100,0,100), SpiderMotionController.Behavior.ROAM, Vec3.ZERO);double budget=burst.remainingDistance();
        burst.record(new Vec3(2,0,0));burst.record(new Vec3(-2,0,0));burst.record(Vec3.ZERO);
        assertEquals(budget-4,burst.remainingDistance(),1e-8);
        assertEquals(new Vec3(0.1,0,0),burst.step(new Vec3(0.1,0,0),0.7));
    }
}
