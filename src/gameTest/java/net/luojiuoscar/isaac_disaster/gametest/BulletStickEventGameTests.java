package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickEndReason;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("isaac_disaster_stick")
@PrefixGameTestTemplate(false)
public final class BulletStickEventGameTests {
    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void lifecycleDispatchPreservesSnapshotAndFiltersUnavailableTargets(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "stick_events"));
        var target = EntityType.PIG.create(helper.getLevel());
        var received = new ArrayList<ExecutableEffectContext>();
        var triggers = new CompositeTrigger();
        for (TriggerType type : List.of(ModTriggerTypes.BULLET_STICK_START, ModTriggerTypes.BULLET_STICK_TICK,
                ModTriggerTypes.BULLET_STICK_END)) {
            triggers.add(new SimpleTrigger(type, () -> received::add));
        }
        BulletState bullet = bullet(owner, triggers);
        Vec3 position = new Vec3(2, 3, 4);
        var start = new BulletStickContext(bullet, target, target.getUUID(), position, 0, 100, null);
        var startEvent = new BulletStickStartEvent(start);
        TriggerModuleEvents.onBulletStickStart(startEvent);
        helper.assertTrue(received.size() == 1, "Start maps to exactly its bullet trigger");
        var effectContext = received.get(0);
        helper.assertTrue(effectContext.getEntity() == owner && effectContext.get(ContextKeys.EVENT) == startEvent
                && effectContext.get(ContextKeys.BULLET) == bullet && effectContext.get(ContextKeys.BULLET_STICK) == start
                && effectContext.get(ContextKeys.TARGET_POSITION) == position, "All snapshot data reaches effects");
        helper.assertTrue(effectContext.get(ContextKeys.SECONDARY_LIVING_ENTITIES).equals(List.of(target)),
                "A living same-level target is exposed");
        target.setHealth(0);
        var tick = new BulletStickContext(bullet, target, target.getUUID(), position, 1, 100, null);
        TriggerModuleEvents.onBulletStickTick(new BulletStickTickEvent(tick));
        helper.assertTrue(received.size() == 2 && !received.get(1).has(ContextKeys.SECONDARY_LIVING_ENTITIES),
                "Dead targets are omitted without dropping the tick event");
        var removed = EntityType.PIG.create(helper.getLevel());
        removed.discard();
        var end = new BulletStickContext(bullet, removed, removed.getUUID(), position, 2, 100,
                BulletStickEndReason.TARGET_REMOVED);
        TriggerModuleEvents.onBulletStickEnd(new BulletStickEndEvent(end));
        helper.assertTrue(received.size() == 3 && received.get(2).get(ContextKeys.BULLET_STICK) == end
                && !received.get(2).has(ContextKeys.SECONDARY_LIVING_ENTITIES),
                "Removed target end retains the reason and position without secondary entities");
        var otherLevel = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(otherLevel != null, "Nether is available for the dimension boundary check");
        var otherTarget = EntityType.PIG.create(otherLevel);
        var changed = new BulletStickContext(bullet, otherTarget, otherTarget.getUUID(), position, 3, 100,
                BulletStickEndReason.TARGET_CHANGED_DIMENSION);
        TriggerModuleEvents.onBulletStickEnd(new BulletStickEndEvent(changed));
        helper.assertTrue(received.size() == 4 && !received.get(3).has(ContextKeys.SECONDARY_LIVING_ENTITIES),
                "Targets in another dimension are omitted");
        var ownerless = new BulletStickContext(bullet(null, triggers), null, target.getUUID(), position, 4, 100,
                BulletStickEndReason.REMOVED_BY_EFFECT);
        TriggerModuleEvents.onBulletStickEnd(new BulletStickEndEvent(ownerless));
        helper.assertTrue(received.size() == 4, "Ownerless notifications skip trigger dispatch");
        helper.succeed();
    }

    private static BulletState bullet(LivingEntity owner, CompositeTrigger triggers) {
        return BulletState.builder().owner(owner).triggers(triggers).build();
    }
}
