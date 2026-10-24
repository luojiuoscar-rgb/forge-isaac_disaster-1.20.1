package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.CSection;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.CSectionAttack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class FetusMomsKnifeGameTests {
    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void knifeAndCSectionSelectFetusesAtPriorityThreeHundredOne(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.ability.addAttackType(ModAttackTypes.C_SECTION.getId(), 1, f.owner);
            var selected = f.ability.getAttackSelection();
            helper.assertTrue(selected.mainAttack() == ModAttackTypes.C_SECTION.get()
                    && selected.mainCandidate().priorityTier() == 300 && selected.mainCandidate().priority() == 1,
                    "Knife plus C Section selects C Section at (300, 1)");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void theTripleSelectsBrimstoneAtPriorityThreeHundredTwo(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.ability.addAttackType(ModAttackTypes.C_SECTION.getId(), 1, f.owner);
            f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
            var selected = f.ability.getAttackSelection();
            helper.assertTrue(selected.mainAttack() == ModAttackTypes.BRIMSTONE.get()
                    && selected.mainCandidate().priorityTier() == 300 && selected.mainCandidate().priority() == 2,
                    "The triple selects Brimstone at (300, 2)");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void aFetusBladeDealsTwiceTheBulletDamageBeyondItsBody(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            Vec3 position = f.owner.position().add(5, 0, 0);
            BulletState bullet = new FetusProbe().create(AttackContext.builder(f.owner, f.owner)
                    .attackType(ModAttackTypes.C_SECTION.get()).position(position).mainAxis(new Vec3(0, 0, 1))
                    .damage(3.0).speed(0.1).range(24).build());
            bullet.setPosition(position);
            var target = f.cow(position.add(0, 0, 0.9));
            BulletManager manager = new BulletManager();
            manager.spawn(bullet);
            manager.tick(f.level);
            helper.assertTrue(Math.abs(target.getHealth() - 994) < 1e-4,
                    "The front knife deals six damage while the fetus body does not touch the target: " + target.getHealth());
            manager.clear();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void continuousKnifeDamageIgnoresTheBodyCooldownAndNormalInvulnerability(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        Vec3 origin = f.owner.position().add(5, 0, 0);
        BulletManager manager = new BulletManager();
        BulletState bullet = manager.spawn(armed(f, origin, 0.1, 3));
        var target = f.cow(origin.add(0, 0, 0.9));
        manager.tick(f.level);
        helper.assertTrue(Math.abs(target.getHealth() - 994) < 1e-4, "The first blade contact deals six damage");
        helper.runAfterDelay(1, () -> {
            try {
                manager.tick(f.level);
                helper.assertTrue(Math.abs(target.getHealth() - 988) < 1e-4,
                        "A second tick deals another six through normal invulnerability: " + target.getHealth());
                bullet.discard();
                manager.tick(f.level);
                helper.assertTrue(Math.abs(target.getHealth() - 988) < 1e-4, "A removed fetus has no surviving blade");
                helper.succeed();
            } finally { manager.clear(); f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void separateFetusKnivesStackInTheSameTickAndSweepFastMovement(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            Vec3 origin = f.owner.position().add(5, 0, 0);
            var target = f.cow(origin.add(0, 0, 0.9));
            BulletManager manager = new BulletManager();
            manager.spawn(armed(f, origin, 0.1, 3));
            manager.spawn(armed(f, origin, 0.1, 3));
            manager.tick(f.level);
            helper.assertTrue(Math.abs(target.getHealth() - 988) < 1e-4,
                    "Each attached knife applies its own six damage: " + target.getHealth());
            manager.clear();
            target.discard();
            var swept = f.cow(origin.add(0, 0, 5));
            manager.spawn(armed(f, origin, 10, 3));
            manager.tick(f.level);
            helper.assertTrue(Math.abs(swept.getHealth() - 991) < 1e-4,
                    "A fast sweep applies the original three body damage and six knife damage: " + swept.getHealth());
            manager.clear();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void knifeOwnershipIsSnapshottedAndFetusScaleReachesTheModelAndBlade(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            AttackContext context = AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.C_SECTION.get())
                    .damage(2.0).bulletScaleModifier(1.0).speed(1).range(24).build();
            var armed = new FetusProbe().create(context);
            helper.assertTrue(armed.fetusKnife() != null && Math.abs(armed.renderScale() - 2) < 1e-9,
                    "The fetus retains the real doubled model scale and carries a knife");
            var blade = FetusMomsKnifeState.blade(armed.position(), new Vec3(0, 0, 1),
                    armed.renderScale(), armed.collisionWidth());
            helper.assertTrue(Math.abs(blade.tip().distanceTo(blade.anchor()) - 0.7875) < 1e-9,
                    "A doubled fetus has a proportionally doubled blade");
            f.ability.addAttackType(ModAttackTypes.MOMS_KNIFE.getId(), -1, f.owner);
            var plain = new FetusProbe().create(context);
            helper.assertTrue(plain.fetusKnife() == null && armed.fetusKnife() != null,
                    "Removing Knife affects future fetuses while an already fired fetus retains its blade");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void removingCombinationPartnersRestoresTheRemainingAttack(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.ability.addAttackType(ModAttackTypes.C_SECTION.getId(), 1, f.owner);
            f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
            f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), -1, f.owner);
            helper.assertTrue(f.ability.getAttackSelection().mainAttack() == ModAttackTypes.C_SECTION.get(),
                    "Removing Brimstone restores the two-item fetus attack");
            f.ability.addAttackType(ModAttackTypes.C_SECTION.getId(), -1, f.owner);
            helper.assertTrue(f.ability.getAttackSelection().mainAttack() == ModAttackTypes.MOMS_KNIFE.get(),
                    "Removing C Section restores the player's ordinary knife attack");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 30)
    public static void theTripleKeepsBrimstonesFourExtraFetusesAndArmsEachOfThem(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        new CSection(0, 4).handleObtain(f.owner, null);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        List<BulletState> shots = new ArrayList<>();
        Consumer<TearBulletShootEvent> listener = event -> {
            if (event.getBulletObject() instanceof BulletState bullet && bullet.getOwner() == f.owner
                    && bullet.getTypeId().equals(ModAttackTypes.C_SECTION.getId())) shots.add(bullet);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        MinecraftForge.EVENT_BUS.post(new BeforePerformAttackEvent(f.owner, ModAttackTypes.BRIMSTONE.get()));
        helper.runAfterDelay(14, () -> {
            try {
                helper.assertTrue(shots.size() == 4 && shots.stream().allMatch(b -> b.fetusKnife() != null),
                        "The existing Brimstone/C Section effect emits four armed fetuses: " + shots.size());
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
                shots.forEach(BulletState::discard);
                ScheduledFuncHelper.clearByType(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "c_section_attack"),
                        f.owner.getUUID());
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void playerShotSpeedOfOneTenthProducesAVisibleLiveFetusAtSevenHundredths(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(0.1);
        CSectionAttack attack = (CSectionAttack) ModAttackTypes.C_SECTION.get();
        AttackContext context = attack.createAttackContext(f.owner, f.owner);
        helper.assertTrue(context != null && Math.abs(context.getBulletSpeed() - 0.07) < 1e-12,
                "The real player attribute passes through C Section's 0.7 speed multiplier");
        var bullet = new FetusProbe().create(context.toBuilder().position(f.owner.position().add(5, 0, 0)).build());
        var spawned = BulletRuntime.INSTANCE.spawn(f.level, bullet);
        helper.assertTrue(spawned == bullet && bullet.isAlive(), "The real runtime accepts the slow fetus before its spawn snapshot");
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(bullet.isAlive() && bullet.traveled() > 0,
                        "The slow fetus survives subsequent physics and visibility synchronization");
                helper.succeed();
            } finally { bullet.discard(); f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void theServerSpawnGuardAcceptsOneHundredthAndRejectsAnythingSlower(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            var valid = armed(f, f.owner.position().add(5, 0, 0), 0.01, 2);
            var invalid = armed(f, f.owner.position().add(5, 0, 0), 0.009999, 2);
            helper.assertTrue(BulletRuntime.INSTANCE.spawn(f.level, valid) == valid && valid.isAlive(),
                    "The exact 0.01 boundary is a valid spawn");
            helper.assertTrue(BulletRuntime.INSTANCE.spawn(f.level, invalid) == null && !invalid.isAlive(),
                    "A velocity below 0.01 is still rejected");
            valid.discard();
        }
        helper.succeed();
    }

    private static BulletState armed(PlayerMomsKnifeGameTests.Fixture f, Vec3 origin, double speed, double damage) {
        return BulletState.builder().owner(f.owner).position(origin).velocity(new Vec3(0, 0, speed)).baseSpeed(speed)
                .damage(damage).renderScale(1).collisionWidth(0.21).collisionHeight(0.63).lifetime(100)
                .noGravity(true).spectral(true).piercing(true).rememberHitTargets(false).fetusKnife(true)
                .typeIds(ModAttackTypes.C_SECTION.getId(), ModAttackTypes.BULLET.getId()).build();
    }

    private static final class FetusProbe extends CSectionAttack {
        FetusProbe() { super(0); }
        BulletState create(AttackContext context) { return createOptimizedState(context); }
    }
}
