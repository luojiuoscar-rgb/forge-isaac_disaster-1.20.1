package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import java.util.function.Consumer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;

/** Deterministic controller ticks on real world collision shapes; no asynchronous fixture races. */
@GameTestHolder("isaac_disaster_blue_spider")
@PrefixGameTestTemplate(false)
public final class SpiderBehaviorGameTests {
    private static final ResourceLocation TYPE=ResourceLocation.parse("isaac_disaster:blue_spider");
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void diagonalGoalTurnsBeforeMovingAndKeepsItsTravelBudget(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            f.spider.setYRot(135);
            begin(f,f.start.add(4,0,4),SpiderMotionController.Behavior.ROAM);
            set(f.spider,"travelMode",SpiderMovement.Gait.FORWARD);
            f.spider.setOnGround(true);
            helper.assertTrue(f.spider.getYRot()==135,"Selecting a route never snaps body yaw");
            var burst=(SpiderMotionController.Attempt)get(f.spider,"attempt");
            double budget=burst.remainingDistance();
            for(int tick=0;tick<7;tick++) {
                float before=f.spider.getYRot();
                f.spider.tickFamiliar();
                helper.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(f.spider.getYRot()-before))<=27.001,
                        "Body rotation is bounded each tick");
                helper.assertTrue(f.spider.position().subtract(f.start).horizontalDistanceSqr()<1e-10,
                        "Turning completes before horizontal travel starts");
                helper.assertTrue(f.spider.getMovementAnimation()==SpiderMovement.Gait.IDLE,"Turning uses idle");
                helper.assertTrue(burst.remainingDistance()==budget,"Turning spends no route distance");
            }
            f.spider.tickFamiliar();
            Vec3 delta=f.spider.position().subtract(f.start);
            helper.assertTrue(delta.x>0&&delta.z>0&&delta.horizontalDistance()>=0.2-1e-6&&delta.horizontalDistance()<=0.3+1e-6,
                    "Aligned spider advances toward the diagonal world destination at the movement speed");
            helper.assertTrue(f.spider.getMovementAnimation()==SpiderMovement.Gait.FORWARD,
                    "The world diagonal is purely forward relative to the turned body");
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void wanderingChoosesBothTurningInPlaceAndBodyAxisTravel(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            int turns=0,moves=0;
            for(int attempt=0;attempt<48;attempt++) {
                f.spider.setPos(f.start);f.spider.setYRot(0);f.spider.setOnGround(true);
                choose(f,false);
                Vec3 destination=(Vec3)get(f.spider,"goal");
                SpiderMovement.Gait mode=(SpiderMovement.Gait)get(f.spider,"travelMode");
                float targetYaw=SpiderMovement.targetYaw(destination.subtract(f.start),mode);
                for(int tick=0;tick<10;tick++) {
                    f.spider.tickFamiliar();
                    if(f.movement.behavior()==SpiderMotionController.Behavior.IDLE
                            ||f.spider.position().subtract(f.start).horizontalDistanceSqr()>1e-10)break;
                }
                if(f.movement.behavior()==SpiderMotionController.Behavior.IDLE) {
                    turns++;
                    helper.assertTrue(f.spider.position().distanceToSqr(f.start)<1e-8,"Turn-only wandering never translates");
                    helper.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(f.spider.getYRot()-targetYaw))<0.001,
                            "Turn-only action finishes at its selected heading");
                    helper.assertTrue(f.movement.phase()==SpiderMotionController.Phase.WAITING,
                            "Turning in place completes a movement attempt and begins recovery");
                    helper.assertTrue(f.spider.getMovementAnimation()==SpiderMovement.Gait.IDLE,"Turning keeps idle animation");
                    for(int tick=0;tick<5;tick++) {
                        f.spider.tickFamiliar();
                        helper.assertTrue(f.spider.position().distanceToSqr(f.start)<1e-8,"Turn-only action obeys the normal wait");
                    }
                } else {
                    moves++;
                    helper.assertTrue(f.movement.behavior()==SpiderMotionController.Behavior.ROAM
                                    &&f.spider.position().subtract(f.start).horizontalDistance()>0.1,
                            "Travel action actually moves");
                    helper.assertTrue(SpiderMovement.animation(f.spider.position().subtract(f.start),f.spider.getYRot())==mode,
                            "Wandering travel stays on its selected forward, left or right body axis");
                }
            }
            helper.assertTrue(turns>0&&moves>0,"Wandering includes both turn-only and travel actions");
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void followingAlwaysSelectsTravelRatherThanTurningInPlace(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            f.owner.setPos(f.start.add(11,0,0));
            for(int attempt=0;attempt<16;attempt++) {
                f.spider.setPos(f.start);f.spider.setYRot(0);f.spider.setOnGround(true);
                choose(f,true);
                for(int tick=0;tick<10;tick++) {
                    f.spider.tickFamiliar();
                    if(f.spider.position().subtract(f.start).horizontalDistanceSqr()>1e-10)break;
                }
                helper.assertTrue(f.movement.behavior()==SpiderMotionController.Behavior.FOLLOW
                                &&Math.abs(f.spider.position().subtract(f.start).horizontalDistance()-0.5)<1e-6,
                        "Follow always advances at its normal speed after turning");
            }
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void forwardLeftAndRightRoutesUseTheirSelectedBodyAxis(GameTestHelper helper) {
        SpiderMovement.Gait[] modes={SpiderMovement.Gait.FORWARD,SpiderMovement.Gait.LEFT,SpiderMovement.Gait.RIGHT};
        float[] headings={-90,-180,0};
        for(int i=0;i<modes.length;i++) {
            try(var f=new Fixture(helper)) {
                begin(f,f.start.add(4,0,0),SpiderMotionController.Behavior.ROAM);
                set(f.spider,"travelMode",modes[i]);
                f.spider.setYRot(headings[i]);f.spider.setOnGround(true);
                f.spider.tickFamiliar();
                Vec3 delta=f.spider.position().subtract(f.start);
                helper.assertTrue(delta.x>=0.2-1e-6&&delta.x<=0.3+1e-6&&Math.abs(delta.z)<1e-6,
                        "Every selected body axis reaches the same world direction");
                helper.assertTrue(f.spider.getMovementAnimation()==modes[i],"Selected gait matches actual travel");
                set(f.spider,"goal",f.start.add(-4,0,0));
                Vec3 before=f.spider.position();float yaw=f.spider.getYRot();f.spider.tickFamiliar();
                Vec3 continued=f.spider.position().subtract(before);
                helper.assertTrue(Math.abs(continued.x-delta.x)<1e-6&&Math.abs(continued.z)<1e-6,
                        "A running segment keeps its original body axis instead of steering toward a changed goal");
                helper.assertTrue(f.spider.getYRot()==yaw&&f.movement.phase()==SpiderMotionController.Phase.MOVING,
                        "Body heading stays locked until the movement segment ends");
            }
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void roamingKeepsItsSampledSpeedWhileFollowingUsesMaximum(GameTestHelper helper) {
        for(var state:List.of(SpiderMotionController.Behavior.ROAM,
                SpiderMotionController.Behavior.FOLLOW)) {
            try(var f=new Fixture(helper)) {
                begin(f,f.start.add(7,0,0),state);
                set(f.spider,"travelMode",SpiderMovement.Gait.FORWARD);
                f.spider.setYRot(-90);f.spider.setOnGround(true);
                double firstSpeed=0;
                for(int tick=0;tick<4;tick++) {
                    Vec3 before=f.spider.position();f.spider.tickFamiliar();
                    double speed=f.spider.position().subtract(before).horizontalDistance();
                    if(tick==0)firstSpeed=speed;
                    helper.assertTrue(Math.abs(speed-firstSpeed)<1e-6,"Speed stays constant within a burst: "+state);
                    helper.assertTrue(state==SpiderMotionController.Behavior.ROAM
                                    ? speed>=0.2-1e-6&&speed<=0.3+1e-6 : Math.abs(speed-0.5)<1e-6,
                            "Movement speed matches behavior: "+state+" speed="+speed);
                }
            }
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void largeTargetChangeEndsTheSegmentWithoutSteering(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var target = EntityType.ZOMBIE.create(f.level);
            target.setNoAi(true); target.setNoGravity(true); target.setPos(f.start.add(4, 0, 0));
            f.level.addFreshEntity(target);
            try {
                begin(f, target.position(), SpiderMotionController.Behavior.ATTACK);
                set(f.spider, "activeTarget", target);
                var mode = (SpiderMovement.Gait) get(f.spider, "travelMode");
                f.spider.setYRot(SpiderMovement.targetYaw(target.position().subtract(f.start), mode));
                f.spider.setOnGround(true); f.spider.tickFamiliar();
                float yaw = f.spider.getYRot();
                target.setPos(f.start.add(0, 0, 4));
                Vec3 before = f.spider.position();
                f.spider.tickFamiliar();
                helper.assertTrue(f.spider.position().subtract(before).horizontalDistanceSqr() < 1e-10,
                        "A clearly obsolete interception stops rather than running to the old position");
                helper.assertTrue(f.spider.getYRot() == yaw && f.movement.behavior() == SpiderMotionController.Behavior.IDLE,
                        "Aborting never bends the running segment or instantly turns the body");
                helper.assertTrue(f.spider.getMovementAnimation() == SpiderMovement.Gait.IDLE,
                        "Aborting an attack returns to idle");
                for (int tick = 0; tick < 4; tick++) {
                    f.spider.tickFamiliar();
                    helper.assertTrue(f.spider.position().subtract(before).horizontalDistanceSqr() < 1e-10,
                            "An obsolete attack still observes normal recovery before another attempt");
                }
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void movingTargetGetsALeadAndOneDepartureRecalibration(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var target = EntityType.ZOMBIE.create(f.level);
            target.setNoAi(true); target.setNoGravity(true); target.setPos(f.start.add(4, 0, 0));
            f.level.addFreshEntity(target);
            try {
                sampleTargetMotion(f, target, new Vec3(0, 0, 0.2));
                f.spider.setOnGround(true); f.spider.tickFamiliar();
                try {
                    var method = SpiderMotionController.class.getDeclaredMethod("chooseMovement", ServerPlayer.class,
                            net.minecraft.world.entity.LivingEntity.class, boolean.class);
                    method.setAccessible(true); method.invoke(f.movement, f.owner, target, false);
                } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
                Vec3 initialGoal = (Vec3) get(f.spider, "goal");
                helper.assertTrue(initialGoal.z > target.getZ() + 0.5,
                        "A laterally moving enemy is intercepted ahead of its current position");
                helper.assertTrue(initialGoal.subtract(target.position()).horizontalDistance() <= 3.3,
                        "Prediction and side spread stay bounded");
                var mode = (SpiderMovement.Gait) get(f.spider, "travelMode");
                f.spider.setYRot(SpiderMovement.targetYaw(initialGoal.subtract(f.start), mode));
                target.setPos(target.position().add(0, 0, 0.8));
                sampleTargetMotion(f, target, new Vec3(0, 0, 0.2));
                f.spider.setOnGround(true); f.spider.tickFamiliar();
                Vec3 corrected = (Vec3) get(f.spider, "goal");
                helper.assertTrue(corrected.distanceToSqr(initialGoal) > 1e-5,
                        "Completing the initial turn recalibrates against the newest target position");
                for (int tick = 0; tick < 10 && !(boolean) get(f.spider, "travelStarted"); tick++) {
                    f.spider.tickFamiliar();
                }
                helper.assertTrue((boolean) get(f.spider, "travelStarted"), "Recalibration does not cause endless turning");
                float yaw = f.spider.getYRot();
                Vec3 before = f.spider.position();
                target.setPos(target.position().add(0, 0, 0.05));
                f.spider.tickFamiliar();
                Vec3 movement = f.spider.position().subtract(before);
                helper.assertTrue(Math.abs(movement.horizontalDistance() - 0.5) < 1e-6,
                        "A small subsequent change preserves the attack speed");
                helper.assertTrue(f.spider.getYRot() == yaw && get(f.spider, "goal").equals(corrected),
                        "After departure, small target motion never steers or retargets the straight segment");
                helper.assertTrue(f.spider.getMovementAnimation() == mode, "The selected body-axis animation remains correct");
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    private static void sampleTargetMotion(Fixture f, Zombie target, Vec3 velocity) {
        // Explicit snapshot times simulate two actual positions without ticking unrelated fixtures.
        long tick = f.level.getGameTime();
        Vec3 current = target.position();
        target.setPos(current.subtract(velocity));
        var frame = SpiderOwnerFrame.get(f.owner);
        frame.refresh(f.owner, tick - 1);
        target.setPos(current);
        frame.refresh(f.owner, tick);
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void sweptContactUsesPanelAndConsumesOnce(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.start.add(1,0,0));
            helper.getLevel().addFreshEntity(target);
            try {
                helper.assertTrue(SpiderOwnerFrame.validTarget(f.owner,target),"Hostile qualifies: hp="+target.getHealth()+" owner="+f.owner.position()+" bb="+target.getBoundingBox());
                var from=f.start;var to=target.position().add(0.5,0,0);
                double h=f.spider.getBbHeight()/2;
                helper.assertTrue(SweptCollision.segmentAabb(from.add(0,h,0),to.add(0,h,0),target.getBoundingBox().inflate(0.0625,h,0.0625)).hit(),"Body sweep hits target");
                helper.assertTrue(SpiderTerrain.clearSweep(f.spider,from,to),"Body sweep is unobstructed");
                helper.assertTrue(f.data.containsEntity(TYPE,f.spider.getUUID()),"Spider is linked");
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
                helper.assertTrue(f.spider.tryContact(f.owner,from,to),"Actual contact consumes");
                helper.assertTrue(f.spider.isRemoved()&&f.data.getCount(TYPE)==0,"No replacement requirement remains");
                helper.assertTrue(Math.abs(target.getHealth()-2.5)<0.01,"Blue spider deals 2.5 times current panel damage: health="+target.getHealth());
                helper.assertTrue(!f.spider.tryContact(f.owner,from,to),"Cannot contact twice");
            } finally {target.discard();}
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void waitingThenPendingLetsFollowBypassTheWanderDelay(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var timing=(SpiderMotionController.Delay)get(f.spider,"delay");
            timing.reset(10,40);f.spider.setOnGround(true);
            for(int i=0;i<10;i++)f.spider.tickFamiliar();
            helper.assertTrue(f.movement.phase()==SpiderMotionController.Phase.PENDING,"Mandatory wait ends in pending");
            helper.assertTrue(f.spider.position().distanceToSqr(f.start)<1e-8,"Pending wander has not moved");
            f.owner.setPos(f.start.add(11,0,0));f.spider.tickFamiliar();
            helper.assertTrue(f.movement.behavior()==SpiderMotionController.Behavior.FOLLOW,"Follow skips extra random delay");
            Vec3 goal=(Vec3)get(f.spider,"goal");
            helper.assertTrue(goal.subtract(f.owner.position()).horizontalDistanceSqr()<=9.0001,"Follow goal within owner radius3");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void completedBurstPausesBeforeSelectingTheNextRoute(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            begin(f,f.start.add(0.5,0,0),SpiderMotionController.Behavior.ROAM);f.spider.setOnGround(true);
            for(int i=0;i<30&&f.movement.phase()!=SpiderMotionController.Phase.WAITING;i++)f.spider.tickFamiliar();
            helper.assertTrue(f.movement.phase()==SpiderMotionController.Phase.WAITING,"Arrival schedules mandatory wait");
            Vec3 stopped=f.spider.position();
            for(int i=0;i<5;i++) {f.spider.tickFamiliar();helper.assertTrue(f.spider.position().distanceToSqr(stopped)<1e-8,"Arrival pauses before moving again");}
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void damageCancellationStillConsumesBeforeReentrantCallback(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.start.add(1,0,0));helper.getLevel().addFreshEntity(target);
            int[] callbacks={0};
            Consumer<LivingAttackEvent> listener=event->{
                if(event.getEntity()!=target||event.getSource().getDirectEntity()!=f.spider)return;
                callbacks[0]++;
                helper.assertTrue(f.data.getCount(TYPE)==0&&!f.data.containsEntity(TYPE,f.spider.getUUID()),"Ownership consumed before callback");
                helper.assertTrue(event.getSource().getEntity()==f.owner&&!f.spider.isRemoved(),"Direct source alive and owner credited during damage");
                helper.assertTrue(!f.spider.tryContact(f.owner,f.spider.position(),target.position()),"Reentrancy cannot consume again");
                event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,LivingAttackEvent.class,listener);
            try {
                helper.assertTrue(f.spider.tryContact(f.owner,f.start,target.position()),"Contact attempts damage");
                helper.assertTrue(callbacks[0]==1&&target.getHealth()==20&&f.spider.isRemoved(),"Blocked damage still consumes exactly one");
                helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner),"Consumed request does not respawn");
            } finally {MinecraftForge.EVENT_BUS.unregister(listener);target.discard();}
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void contactsCannotPassThroughWallsAndStationaryContactsWork(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.start.add(2,0,0));helper.getLevel().addFreshEntity(target);
            BlockPos wall=BlockPos.containing(f.start.add(1,0,0));helper.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
            try {
                helper.assertTrue(!f.spider.tryContact(f.owner,f.start,target.position()),"Wall blocks a swept contact");
                helper.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());
                f.spider.setPos(target.position());
                helper.assertTrue(f.spider.tryContact(f.owner,f.spider.position(),f.spider.position()),"Stationary overlap also contacts");
            } finally {target.discard();}
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void sharedTargetAssignmentMovesSpiderToEnemy(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.start.add(2,0,0));helper.getLevel().addFreshEntity(target);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3);
            ((SpiderMotionController.Delay)get(f.spider,"delay")).reset(0,0);f.spider.setOnGround(true);
            for(int i=0;i<40&&!f.spider.isRemoved();i++)f.spider.tickFamiliar();
            helper.assertTrue(f.spider.isRemoved()||target.getHealth()<20,"Assigned target is reached by contact movement");
            target.discard();
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void recallAndSharedSearchBudgetReuseFamiliarInfrastructure(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            UUID id=f.spider.getUUID();f.spider.setPos(f.start.add(17,0,0));f.spider.tick();
            helper.assertTrue(f.spider.isRecallPending(),"Beyond16queues recall");
            helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner),"Shared scheduler recalls");
            helper.assertTrue(f.spider.getUUID().equals(id)&&f.spider.isValidFamiliar()&&!f.spider.isRecallPending(),"Recall retains existing familiar");
            helper.assertTrue(Math.abs(f.spider.getY()-f.start.y)<1e-6,"Recall places on ground rather than waist");
            helper.assertTrue(!f.spider.isPushable()&&!f.spider.isNoGravity()&&!f.spider.noPhysics,"No entity pushes, with gravity and terrain collision");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void spiderPassesThroughBoatsButStillStopsAtBlocks(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var boat=EntityType.BOAT.create(f.level);
            boat.setNoGravity(true);boat.setPos(f.start.add(0.5,0,0));f.level.addFreshEntity(boat);
            try {
                helper.assertTrue(!f.spider.canCollideWith(boat)&&!boat.canCollideWith(f.spider),
                        "Spider and boat never physically obstruct each other");
                helper.assertTrue(!f.spider.isPushable()&&!f.spider.canBeCollidedWith(),"Spider remains nonpushable");
                f.spider.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(1,0,0));
                helper.assertTrue(Math.abs(f.spider.getX()-f.start.x-1)<1e-6,"Spider crosses the boat without being stopped");
                f.stone(f.start.add(2,0,0));
                f.spider.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(1,0,0));
                double distance=f.spider.getX()-f.start.x;
                helper.assertTrue(distance>1.3&&distance<1.5,"Solid terrain still blocks the same movement");
                helper.assertTrue(!f.spider.noPhysics&&!f.spider.isNoGravity(),"Terrain physics and gravity remain enabled");
            } finally {boat.discard();}
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void followingBurstStopsAfterSixToNineBlocksEvenBeforeItsGoal(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            f.owner.setPos(f.start.add(14,0,0));((SpiderMotionController.Delay)get(f.spider,"delay")).reset(0,0);f.spider.setOnGround(true);
            f.spider.tickFamiliar();
            Vec3 goal=(Vec3)get(f.spider,"goal");
            helper.assertTrue(goal!=null&&goal.subtract(f.start).horizontalDistance()>9,"Follow goal exceeds burst budget");
            double travelled=f.spider.position().subtract(f.start).horizontalDistance();
            for(int i=0;i<120;i++) {
                if(f.movement.behavior()==SpiderMotionController.Behavior.IDLE)break;
                Vec3 previous=f.spider.position();f.spider.tickFamiliar();travelled+=f.spider.position().subtract(previous).horizontalDistance();
            }
            helper.assertTrue(travelled>=6&&travelled<=9.00001,"Every follow segment uses6-9blocks, actual="+travelled);
            helper.assertTrue(f.movement.behavior()==SpiderMotionController.Behavior.IDLE,"Follow also stops and pauses");
            helper.assertTrue(f.spider.getMovementEmission()==net.minecraft.world.entity.Entity.MovementEmission.NONE,"Movement never emits footsteps");
        }
        helper.succeed();
    }
    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void wallClimbChecksDropOnlyAfterEachCompleteSecond(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            for(int y=0;y<12;y++)for(int z=-1;z<=1;z++)f.stone(f.start.add(1,y,z));
            begin(f,f.start.add(2,0,0),SpiderMotionController.Behavior.ROAM);f.spider.setOnGround(true);
            for(int i=0;i<20&&!f.movement.isClimbing();i++)f.spider.tickFamiliar();
            helper.assertTrue(f.movement.isClimbing(),"Horizontal collision starts climbing");
            Vec3 attached=f.spider.position();
            try {
                var field=net.minecraft.world.entity.Entity.class.getDeclaredField("random");field.setAccessible(true);
                var random=(net.minecraft.util.RandomSource)field.get(f.spider);
                long seed=0;
                while(net.minecraft.util.RandomSource.create(seed).nextFloat()>=0.1F)seed++;
                random.setSeed(seed);
            } catch(ReflectiveOperationException e){throw new AssertionError(e);}
            for(int i=0;i<19;i++) {f.spider.tickFamiliar();helper.assertTrue(f.movement.isClimbing(),"No random drop before20ticks");}
            f.spider.tickFamiliar();
            helper.assertTrue(Math.abs(f.spider.getY()-attached.y-2.0)<1e-5,"A full second climbs exactly2blocks");
            helper.assertTrue(!f.movement.isClimbing(),"Chosen random draw drops at the one-second check");
            double released=f.spider.getY();
            for(int i=0;i<4;i++){f.spider.tickFamiliar();helper.assertTrue(!f.movement.isClimbing(),"Dropping cannot reattach midair");}
            helper.assertTrue(f.spider.getY()<released-0.1,"Released spider falls under gravity");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void climbingReachesASingleBlockLedgeWithoutJumping(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            f.stone(f.start.add(1,0,0));
            begin(f,f.start.add(2,0,0),SpiderMotionController.Behavior.ROAM);f.spider.setOnGround(true);
            for(int i=0;i<32;i++)f.spider.tickFamiliar();
            helper.assertTrue(f.spider.getY()>=f.start.y+0.99,"Climbing actually places the spider over the ledge");
            helper.assertTrue(!f.movement.isClimbing()&&SpiderTerrain.clearSweep(f.spider,f.spider.position(),f.spider.position()),"Ledge ends climbing without clipping");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void climbTopSchedulesVelocityBeforeHorizontalTravel(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            reachClimbTop(helper,f);
            Vec3 start=f.spider.position();
            double total=0;
            int movingTicks=0;
            for(int tick=0;tick<8;tick++) {
                Vec3 before=f.spider.position();f.spider.tickFamiliar();
                double distance=f.spider.position().subtract(before).horizontalDistance();
                helper.assertTrue(distance<=0.100001,"Crest motion integrates the climb-speed velocity every tick");
                if(distance>1e-8)movingTicks++;
                total+=distance;
            }
            helper.assertTrue(movingTicks>=2&&total>0.12,"Entering the ledge takes multiple movement ticks");
            helper.assertTrue(f.spider.getX()>start.x&&f.spider.onGround(),"Velocity carries the feet onto the platform before gravity settles");
            helper.assertTrue(SpiderTerrain.clearSweep(f.spider,f.spider.position(),f.spider.position()),"The body remains outside solid blocks");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void crestVelocityStopsAtANewObstacle(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            reachClimbTop(helper,f);
            Vec3 before=f.spider.position();
            f.stone(f.start.add(1,1,0));
            f.spider.tickFamiliar();
            helper.assertTrue(f.spider.position().subtract(before).horizontalDistance()<1e-6,"A blocked crest does not clip into the new wall");
            helper.assertTrue(f.spider.getDeltaMovement().horizontalDistanceSqr()==0,"A blocked crest releases its horizontal velocity");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void recallClearsAnUnfinishedCrestVelocity(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            reachClimbTop(helper,f);
            f.spider.setPos(f.start.add(17,0,0));f.spider.tick();
            helper.assertTrue(f.spider.isRecallPending(),"Recall interrupts the active crest");
            helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner),"The shared scheduler recalls the spider");
            helper.assertTrue(f.spider.getDeltaMovement().lengthSqr()==0,"Recall clears old platform-entry velocity");
            helper.assertTrue(f.movement.phase()==SpiderMotionController.Phase.WAITING,"Recall restores waiting");
        }
        helper.succeed();
    }

    private static void reachClimbTop(GameTestHelper helper,Fixture f) {
        f.stone(f.start.add(1,0,0));
        begin(f,f.start.add(2,0,0),SpiderMotionController.Behavior.ROAM);
        set(f.spider,"travelMode",SpiderMovement.Gait.FORWARD);
        f.spider.setYRot(-90);f.spider.setOnGround(true);
        for(int tick=0;tick<40;tick++) {
            boolean climbing=f.movement.isClimbing();
            Vec3 before=f.spider.position();f.spider.tickFamiliar();
            if(climbing&&!f.movement.isClimbing()&&f.spider.getY()>=f.start.y+1) {
                helper.assertTrue(f.spider.position().subtract(before).horizontalDistance()<1e-8,
                        "Finishing the climb must schedule a velocity without an immediate horizontal relocation");
                helper.assertTrue(f.spider.getDeltaMovement().x>0&&f.spider.getDeltaMovement().horizontalDistance()<=0.100001,
                        "The pending velocity points into the platform at climb speed");
                return;
            }
        }
        throw new AssertionError("The fixture did not reach the climb top");
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void roamingAndFollowingWalkOffEdgesAndContinueFalling(GameTestHelper helper) {
        for(var state:List.of(SpiderMotionController.Behavior.ROAM,SpiderMotionController.Behavior.FOLLOW)) {
            try(var f=new Fixture(helper)) {
                clearDrop(f);
                begin(f,f.start.add(4,0,0),state);
                set(f.spider,"travelMode",SpiderMovement.Gait.FORWARD);
                f.spider.setYRot(-90);f.spider.setOnGround(true);
                for(int i=0;i<8;i++)f.spider.tickFamiliar();
                helper.assertTrue(f.spider.getX()>f.start.x+0.6,"Movement deliberately leaves the ledge: "+state);
                helper.assertTrue(f.spider.getY()<f.start.y-0.2,"Unsupported movement falls under gravity: "+state);
                helper.assertTrue(!f.movement.isClimbing(),"Walking off a ledge never attaches to a wall");
                helper.assertTrue(f.data.getCount(TYPE)==1,"A fall does not consume the familiar");
            }
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void attackingAlsoWalksOffUnsupportedEdges(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            clearDrop(f);
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.start.add(3,0,0));helper.getLevel().addFreshEntity(target);
            try {
                begin(f,target.position(),SpiderMotionController.Behavior.ATTACK);
                set(f.spider,"activeTarget",target);set(f.spider,"travelMode",SpiderMovement.Gait.FORWARD);
                f.spider.setYRot(-90);f.spider.setOnGround(true);
                for(int i=0;i<6;i++)f.spider.tickFamiliar();
                helper.assertTrue(!f.spider.isRemoved()&&f.spider.getX()>f.start.x+0.6&&f.spider.getY()<f.start.y-0.2,
                        "Attack movement can leave the platform and fall");
            } finally {target.discard();}
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void grassToDirtPathDescendsWithoutClimbingOrTeleporting(GameTestHelper helper) {
        tracePathTransition(helper,false);
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void dirtPathToGrassUsesTheCurrentWallClimb(GameTestHelper helper) {
        tracePathTransition(helper,true);
    }

    private static void tracePathTransition(GameTestHelper helper,boolean ascending) {
        try(var f=new Fixture(helper)) {
            for(int x=0;x<=5;x++) {
                BlockPos at=BlockPos.containing(f.start.add(x,-1,0));
                f.level.setBlock(at,(ascending==(x==0)?Blocks.DIRT_PATH:Blocks.GRASS_BLOCK).defaultBlockState(),2);
            }
            f.spider.setPos(f.start.add(0,ascending?-1.0/16:0,0));
            begin(f,f.start.add(4,0,0),SpiderMotionController.Behavior.ROAM);
            set(f.spider,"travelMode",SpiderMovement.Gait.FORWARD);set(f.spider,"burstSpeed",0.25);
            f.spider.setYRot(-90);f.spider.setOnGround(true);
            var packets=new java.util.ArrayList<net.minecraft.network.protocol.Packet<?>>();
            var tracker=new net.minecraft.server.level.ServerEntity(f.level,f.spider,1,false,packets::add);
            tracker.sendChanges();packets.clear();
            boolean climbed=false;
            var visual=new AbstractSpiderFamiliarEntity.ClientInterpolation();
            Vec3 displayed=f.spider.position();
            for(int tick=0;tick<14;tick++) {
                Vec3 before=f.spider.position();f.spider.tickFamiliar();
                Vec3 delta=f.spider.position().subtract(before);
                int oldPacketCount=packets.size();
                tracker.sendChanges();
                for(int index=oldPacketCount;index<packets.size();index++) {
                    if(packets.get(index) instanceof net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket packet) {
                        helper.assertTrue(!visual.accept(displayed,packet.getX(),packet.getY(),packet.getZ(),3,true),
                                "Actual absolute tracking packets at the step must remain interpolated");
                        helper.assertTrue(new Vec3(visual.x,visual.y,visual.z).distanceToSqr(displayed)<1e-12,
                                "Receiving a ground-state correction never changes displayed position immediately");
                    }
                }
                if(visual.tick())displayed=new Vec3(visual.x,visual.y,visual.z);
                climbed|=f.movement.isClimbing();
                System.out.printf(java.util.Locale.ROOT,"SPIDER_PATH_TRACE direction=%s tick=%d x=%.6f y=%.6f dx=%.6f dy=%.6f phase=%s ground=%s%n",
                        ascending?"up":"down",tick,f.spider.getX()-f.start.x,f.spider.getY()-f.start.y,delta.x,delta.y,
                        f.movement.phase(),f.spider.onGround());
                helper.assertTrue(delta.horizontalDistance()<=0.250001,"Server displacement stays within the movement speed");
                if(!ascending)helper.assertTrue(delta.y<=1e-6&&!f.movement.isClimbing(),"Descending to path never triggers wall climbing");
            }
            helper.assertTrue(f.spider.getX()>f.start.x+0.55,"The spider crosses the grass/path boundary");
            long absolutePackets=packets.stream().filter(packet->packet instanceof net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket).count();
            System.out.println("SPIDER_PATH_PACKETS direction="+(ascending?"up":"down")+" absolute="+absolutePackets);
            helper.assertTrue(absolutePackets>=2,"Ground-state transitions produce absolute position packets even on tiny steps");
            if(ascending)helper.assertTrue(climbed,"Even the 1/16-block upward face currently triggers wall climbing");
            else helper.assertTrue(Math.abs(f.spider.getY()-(f.start.y-1.0/16))<1e-6,"Feet settle onto the path collision top");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void losingSupportStillRestoresNormalGravity(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            ((SpiderMotionController.Delay)get(f.spider,"delay")).reset(10,40);f.spider.setOnGround(true);
            for(int y=-4;y<=-1;y++) {
                BlockPos at=BlockPos.containing(f.start.add(0,y,0));
                f.originalBlocks.putIfAbsent(at,f.level.getBlockState(at));
                f.level.setBlock(at,Blocks.AIR.defaultBlockState(),2);
            }
            for(int i=0;i<8;i++)f.spider.tickFamiliar();
            helper.assertTrue(f.spider.getY()<f.start.y-0.5,"Removed support still makes a spider fall");
            helper.assertTrue(f.data.getCount(TYPE)==1,"A natural fall does not consume the familiar");
        }
        helper.succeed();
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void contactAtAnUnsupportedEdgeStillConsumesImmediately(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            clearDrop(f);
            f.spider.setPos(f.start.add(0.4,0,0));
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.spider.position());helper.getLevel().addFreshEntity(target);
            try {
                begin(f,f.start.add(4,0,0),SpiderMotionController.Behavior.ROAM);
                f.spider.setOnGround(true);f.spider.tickFamiliar();
                helper.assertTrue(f.spider.isRemoved()&&f.data.getCount(TYPE)==0,
                        "An unsupported edge step must still process existing contact in the same tick");
            } finally {target.discard();}
        }
        helper.succeed();
    }

    private static void clearDrop(Fixture f) {
        for(int x=1;x<=5;x++)for(int y=-4;y<=-1;y++)for(int z=-1;z<=1;z++) {
            BlockPos at=BlockPos.containing(f.start.add(x,y,z));
            f.originalBlocks.putIfAbsent(at,f.level.getBlockState(at));
            f.level.setBlock(at,Blocks.AIR.defaultBlockState(),2);
        }
    }

    @GameTest(template="trajectory_empty",batch="spider_controller")
    public static void stationaryWaitStillDealsContactDamage(GameTestHelper helper) {
        try(var f=new Fixture(helper)) {
            var target=EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);target.setNoGravity(true);target.setPos(f.start);helper.getLevel().addFreshEntity(target);
            try {
                ((SpiderMotionController.Delay)get(f.spider,"delay")).reset(10,40);f.spider.setOnGround(true);
                f.spider.tickFamiliar();
                helper.assertTrue(f.spider.isRemoved()&&f.data.getCount(TYPE)==0,"Waiting contact consumes exactly once");
            } finally{target.discard();}
        }
        helper.succeed();
    }

    private static void choose(Fixture f,boolean following) {
        try {
            var method=SpiderMotionController.class.getDeclaredMethod("chooseMovement",ServerPlayer.class,
                    net.minecraft.world.entity.LivingEntity.class,boolean.class);
            method.setAccessible(true);method.invoke(f.movement,f.owner,null,following);
        } catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static void begin(Fixture f,Vec3 target,SpiderMotionController.Behavior behavior) {
        try {
            var method=SpiderMotionController.class.getDeclaredMethod("startBurst",Vec3.class,SpiderMotionController.Behavior.class,ServerPlayer.class);
            method.setAccessible(true);method.invoke(f.movement,target,behavior,f.owner);
        } catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static Object get(Object obj,String name) {
        try {
            var field=AbstractSpiderFamiliarEntity.class.getDeclaredField("movement");field.setAccessible(true);
            Object movement=field.get(obj);
            try {Field f=SpiderMotionController.class.getDeclaredField(name);f.setAccessible(true);return f.get(movement);}
            catch(NoSuchFieldException ignored) {
                Field f=SpiderMotionController.Attempt.class.getDeclaredField(name);f.setAccessible(true);
                Field attempt=SpiderMotionController.class.getDeclaredField("attempt");attempt.setAccessible(true);
                return f.get(attempt.get(movement));
            }
        }
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static void set(Object obj,String name,Object value) {
        try {
            var field=AbstractSpiderFamiliarEntity.class.getDeclaredField("movement");field.setAccessible(true);
            Object movement=field.get(obj);
            var attempt=SpiderMotionController.class.getDeclaredField("attempt");attempt.setAccessible(true);
            Field f=SpiderMotionController.Attempt.class.getDeclaredField(name);f.setAccessible(true);f.set(attempt.get(movement),value);
        }
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static final class Fixture implements AutoCloseable {
        final ServerPlayer owner;final BlueSpiderEntity spider;final SpiderMotionController movement;final PlayerFamiliarData data;final Vec3 start;
        final ServerLevel level;
        final Map<BlockPos,BlockState> originalBlocks=new LinkedHashMap<>();
        Fixture(GameTestHelper helper) {
            level=helper.getLevel();
            start=Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3,16,3)));
            for(int x=-3;x<=17;x++)for(int z=-4;z<=4;z++)for(int y=-1;y<=13;y++) {
                BlockPos pos=BlockPos.containing(start.add(x,y,z));
                originalBlocks.put(pos,level.getBlockState(pos));
                level.setBlock(pos,(y==-1?Blocks.STONE:Blocks.AIR).defaultBlockState(),2);
            }
            owner=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"spider_fixture"));
            owner.setGameMode(GameType.SURVIVAL);owner.setHealth(owner.getMaxHealth());owner.setInvulnerable(false);owner.getAbilities().invulnerable=false;owner.setPos(start);
            data=owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            data.setCount(TYPE,1);FamiliarHelper.spawnNextMissingFamiliar(owner);
            spider=(BlueSpiderEntity)level.getEntity(data.getEntry(TYPE).orElseThrow().getEntityId(0));
            try {var field=AbstractSpiderFamiliarEntity.class.getDeclaredField("movement");field.setAccessible(true);movement=(SpiderMotionController)field.get(spider);}
            catch(ReflectiveOperationException e){throw new AssertionError(e);}
            spider.setPos(start);spider.setDeltaMovement(Vec3.ZERO);
            helper.assertTrue(spider.isValidFamiliar(),"Fixture starts with a valid familiar");
            helper.assertTrue(SpiderTerrain.groundPosition(spider,start,0)!=null,"Fixture starts on supported ground");
        }
        void stone(Vec3 at) {
            BlockPos pos=BlockPos.containing(at);originalBlocks.putIfAbsent(pos,level.getBlockState(pos));
            level.setBlock(pos,Blocks.STONE.defaultBlockState(),2);
        }
        @Override public void close(){
            FamiliarHelper.discardAllRuntimeFamiliars(owner);data.clearRequirements();owner.discard();
            originalBlocks.forEach((pos,state)->level.setBlock(pos,state,2));
        }
    }
}
