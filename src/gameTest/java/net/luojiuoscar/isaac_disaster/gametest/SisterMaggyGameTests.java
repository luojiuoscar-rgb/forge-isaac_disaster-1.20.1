package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarLimitGroups;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_sister_maggy")
@PrefixGameTestTemplate(false)
public final class SisterMaggyGameTests {
    private static final ResourceLocation MAGGY = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "sister_maggy");
    private static final ResourceLocation BOBBY = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "brother_bobby");
    private static final ResourceLocation WEAKENING = ResourceLocation.fromNamespaceAndPath("sister_maggy_test", "weakening");

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class TestRegistration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> registry.register(WEAKENING,
                    new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, -2,
                            AbstractBabyFamiliarEntity.class::isInstance)));
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredBabySharesSixtyFourSlotsWithBobby(GameTestHelper helper) {
        helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(MAGGY), "Sister Maggy has an entity and familiar descriptor");
        var descriptor = FamiliarHelper.getFamiliarType(MAGGY);
        helper.assertTrue(descriptor.getLimit() == 64 && descriptor.hasEntityTag(AbstractBabyFamiliarEntity.class)
                        && descriptor.getGroup() == ModFamiliarLimitGroups.BABIES.get(),
                "Maggy is a baby with its own and shared cap of sixty-four");
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(MAGGY, 100);
            helper.assertTrue(f.data.getCount(MAGGY) == 64 && f.data.getEffectiveCount(MAGGY) == 64,
                    "Maggy alone cannot exceed its own sixty-four slots");
            f.data.setCount(MAGGY, 0);
            f.data.setCount(BOBBY, 40);
            f.data.setCount(MAGGY, 100);
            helper.assertTrue(f.data.getCount(MAGGY) == 24 && f.data.getEffectiveCount(MAGGY) == 24,
                    "Existing Bobby allocation leaves twenty-four slots; babies do not retain overflow requests");
            f.data.setCount(BOBBY, 0);
            helper.assertTrue(f.data.getEffectiveCount(MAGGY) == 24, "Releasing capacity cannot restore discarded overflow");
            f.data.setCount(MAGGY, 100);
            helper.assertTrue(f.data.getEffectiveCount(MAGGY) == 64, "A new request can use the released capacity");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void tenTickAnimationEndsBeforeFifteenTickCooldownAndTrackingResumesItsPhase(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var maggy = f.add(MAGGY);
            f.setHeld(true);
            maggy.tick();
            helper.assertTrue(maggy.getAttackAge(0) == 0, "Successful fire starts the action immediately");
            f.advance(5);
            var mirror = (AbstractTearBabyFamiliarEntity) maggy.getType().create(helper.getLevel());
            mirror.getEntityData().assignValues(maggy.getEntityData().getNonDefaultValues());
            helper.assertTrue(mirror.getAttackAge(0) == 5, "New tracking resumes the existing action phase");
            f.advance(5);
            maggy.tick();
            helper.assertTrue(maggy.getAttackAge(0) < 0 && mirror.getAttackAge(0) < 0 && f.shots(maggy).size() == 1,
                    "Animation completes after ten ticks without replaying or firing early");
            f.advance(4);
            maggy.tick();
            helper.assertTrue(f.shots(maggy).size() == 1, "The five-tick gap remains idle");
            f.advance(1);
            maggy.tick();
            helper.assertTrue(maggy.getAttackAge(0) == 0 && f.shots(maggy).size() == 2, "The next action starts on tick fifteen");
            helper.assertTrue(maggy.shouldRenderAtSqrDistance(96 * 96) && maggy.getType().clientTrackingRange() == 8,
                    "Maggy uses normal baby visibility and tracking");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedChainFollowsAdmissionOrderAndReconnectsAfterRemoval(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOBBY);
            var maggy = f.add(MAGGY);
            var last = f.add(BOBBY);
            helper.assertTrue(first.getFormationIndex() == 0 && maggy.getFormationIndex() == 1 && last.getFormationIndex() == 2,
                    "Real familiar types share one admission-ordered chain");
            first.setPos(f.hover().add(5, 0, 0));
            maggy.setPos(f.hover());
            f.advance(1);
            maggy.tick();
            helper.assertTrue(maggy.getDeltaMovement().x > 0, "Maggy approaches Bobby rather than its nearby player");
            first.discardAndUnlink();
            f.data.addCount(BOBBY, -1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(maggy.getFormationIndex() == 0 && last.getFormationIndex() == 1,
                    "Removing a predecessor compacts both types' slots");
            var appended = f.add(MAGGY);
            helper.assertTrue(appended.getFormationIndex() == 2, "New Maggy joins the end");
            FamiliarHelper.discardAllRuntimeFamiliars(f.owner);
            helper.assertTrue(f.data.getEntityIdsInAdmissionOrder().isEmpty() && maggy.isRemoved() && appended.isRemoved(),
                    "Owner cleanup removes mixed members and runtime registrations");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldInputRunsIndependentTenAndFifteenTickCadencesAndStopsOnRelease(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var bobby = f.add(BOBBY);
            var maggy = f.add(MAGGY);
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.setHeld(true);
            List<Integer> bobbyTicks = new ArrayList<>();
            List<Integer> maggyTicks = new ArrayList<>();
            for (int tick = 0; tick <= 30; tick++) {
                if (tick > 0) f.advance(1);
                if (tick == 15) f.owner.setYRot(90);
                int bobbyBefore = f.shots(bobby).size();
                int maggyBefore = f.shots(maggy).size();
                bobby.tick();
                maggy.tick();
                maggy.tick();
                if (f.shots(bobby).size() > bobbyBefore) bobbyTicks.add(tick);
                if (f.shots(maggy).size() > maggyBefore) maggyTicks.add(tick);
            }
            helper.assertTrue(bobbyTicks.equals(List.of(0, 10, 20, 30)) && maggyTicks.equals(List.of(0, 15, 30)),
                    "Each baby owns its cadence and same-tick deduplication: " + bobbyTicks + " / " + maggyTicks);
            helper.assertTrue(f.shots(maggy).get(1).velocity().distanceTo(f.owner.getLookAngle()) < 1e-6,
                    "Maggy samples the current player view without waiting for a player shot");
            f.setHeld(false);
            f.advance(15);
            maggy.tick();
            helper.assertTrue(f.shots(maggy).size() == 3, "Releasing input stops ready Maggy");
            f.setHeld(true);
            maggy.tick();
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            f.advance(15);
            maggy.tick();
            helper.assertTrue(f.shots(maggy).size() == 4, "Stale held input cannot shoot after switching items");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void redTearHasOneAndAHalfBobbyDamageAndSnapshotsEnhancement(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var bobby = f.add(BOBBY);
            var maggy = f.add(MAGGY);
            helper.assertTrue(!maggy.attackInDirection(Vec3.ZERO) && !maggy.attackInDirection(new Vec3(Double.NaN, 0, 0)),
                    "Invalid attacks do not consume cooldown");
            helper.assertTrue(bobby.attackInDirection(new Vec3(1, 0, 0)) && maggy.attackInDirection(new Vec3(7, 0, 0)),
                    "Both real baby types expose independent directional attacks");
            BulletState bobbyShot = f.shots(bobby).get(0);
            BulletState first = f.shots(maggy).get(0);
            double damage = 3 * StatManager.DAMAGE.getBonus();
            helper.assertTrue(Math.abs(first.getDamage() - damage) < 1e-6 && Math.abs(first.getDamage() - 1.5 * bobbyShot.getDamage()) < 1e-6,
                    "Maggy's coefficient is three while Bobby retains two");
            helper.assertTrue(first.getColorId().equals(ModBulletColors.BLOOD_TEAR.getId())
                            && first.color() == ModBulletColors.BLOOD_TEAR.get().color()
                            && bobbyShot.getColorId().equals(ModBulletColors.BASE.getId()), "Sibling tear colors stay independent");
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            f.advance(15);
            helper.assertTrue(maggy.attackTowards(maggy.position().add(4, 3, 0)), "Position aiming shares the same attack implementation");
            BulletState enhanced = f.shots(maggy).get(1);
            helper.assertTrue(Math.abs(enhanced.getDamage() - 2 * damage) < 1e-6 && first.getDamage() == damage,
                    "Only later shots receive enhancement; existing tears retain their snapshot");
            helper.assertTrue(maggy.position().add(4, 3, 0).subtract(enhanced.position()).normalize().distanceTo(enhanced.velocity()) < 1e-6,
                    "World-position requests aim from the scaled head position");
            StatManager.removeFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, WEAKENING, 1);
            f.advance(15);
            helper.assertTrue(maggy.attackInDirection(new Vec3(1, 0, 0)) && f.shots(maggy).get(2).getDamage() == 0,
                    "Enhancement below minus one safely clamps damage to zero");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void configuredRangeAndOrdinaryTearIgnorePlayerProperties(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var maggy = f.add(MAGGY);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(800);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).addTransientModifier(
                    new AttributeModifier(UUID.randomUUID(), "temporary range", 500, AttributeModifier.Operation.ADDITION));
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(8);
            f.owner.getAttribute(ModAttributes.BULLET_SCALE.get()).setBaseValue(40);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ability.setHoming(1);
            ability.setPiercing(1);
            ability.setSpectral(1);
            ability.setControllable(1);
            ability.addBulletColor(ModBulletColors.POISON.getId(), 1);
            int index = 0;
            for (double configured : new double[]{20, 2, 100}) {
                settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured);
                f.advance(15);
                helper.assertTrue(maggy.attackInDirection(new Vec3(0, 0, 1)), "Ready attack creates a tear");
                BulletState shot = f.shots(maggy).get(index++);
                double expected = Math.max(12, 1.5 * configured);
                helper.assertTrue(shot.getRange() == expected && shot.velocity().length() == 1 && shot.renderScale() == 1
                        && shot.noGravity() && shot.getDamage() == 3 * StatManager.DAMAGE.getBonus(), "Only configured range and Maggy stats apply");
                helper.assertTrue(!shot.isHoming() && !shot.isPiercing() && !shot.isSpectral() && !shot.isControllable()
                        && shot.getAttackContext() == null && shot.getSplitSequence().isEmpty() && shot.getTrajectorySpecs().isEmpty()
                        && shot.visualIds().isEmpty() && shot.getColorId().equals(ModBulletColors.BLOOD_TEAR.getId()), "Player effects cannot leak into red tears");
                settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, 1.0);
                helper.assertTrue(shot.getRange() == expected, "Changing configuration cannot modify an existing tear");
                for (int tick = 0; tick < expected; tick++) helper.assertTrue(shot.tickPhysics(), "Full configured travel tick");
                helper.assertTrue(Math.abs(shot.traveled() - expected) < 1e-6 && !shot.tickPhysics(), "Lifetime allows full range, including more than sixty-four blocks");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void tearsStartAtTheNeutralHeadWithoutSkippingTheInitialCollisionSegment(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (ResourceLocation type : List.of(BOBBY, MAGGY)) {
                var baby = f.add(type);
                Vec3 head = baby.position().add(0, 5.5 / 16 * 0.6 * baby.getVisualScale(), 0);
                for (Vec3 direction : List.of(new Vec3(1, 0, 0), new Vec3(-1, 0, 0),
                        new Vec3(0, 1, 0), new Vec3(0, -1, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1))) {
                    f.advance(15);
                    helper.assertTrue(baby.attackInDirection(direction), "Ready baby fires in every direction");
                    var shots = f.shots(baby);
                    helper.assertTrue(shots.get(shots.size() - 1).position().distanceTo(head) < 1e-7,
                            "Tears start at the neutral head without an aim-dependent forward offset: " + type);
                }
                BlockPos wall = BlockPos.containing(head);
                var original = f.owner.level().getBlockState(wall);
                try {
                    f.owner.level().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                    baby.setPos(wall.getX() + 0.99, baby.getY(), wall.getZ() + 0.5);
                    f.advance(15);
                    helper.assertTrue(baby.attackInDirection(new Vec3(1, 0, 0)), "A flying baby may shoot while overlapping a wall");
                    var shots = f.shots(baby);
                    BulletState shot = shots.get(shots.size() - 1);
                    BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel());
                    helper.assertTrue(!f.shots(baby).contains(shot),
                            "The initial wall overlap must collide instead of being skipped by a forward spawn offset");
                } finally { f.owner.level().setBlockAndUpdate(wall, original); }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void zeroDamageBonusCreatesAHarmlessTear(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var maggy = f.add(MAGGY);
            settings.set(GameplayConfig.DAMAGE_BONUS, 0.0);
            helper.assertTrue(maggy.attackInDirection(new Vec3(1, 0, 0)) && f.shots(maggy).get(0).getDamage() == 0,
                    "A zero configured damage bonus never creates negative damage or fails the attack");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void redTearActuallyHitsAndCreditsItsOwner(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var maggy = f.add(MAGGY);
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);
            target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setHealth(100);
            target.setPos(f.hover().add(0, -target.getBbHeight() / 2, 7));
            helper.getLevel().addFreshEntity(target);
            try {
                maggy.attackTowards(target.getBoundingBox().getCenter());
                var shot = f.shots(maggy).get(0);
                helper.assertTrue(shot.getOwner() == f.owner && shot.getShooter() == maggy, "Player credit and Maggy shooter are preserved");
                for (int tick = 0; tick < 10; tick++) BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel());
                helper.assertTrue(Math.abs(target.getHealth() - (100 - 3 * StatManager.DAMAGE.getBonus())) < 0.001,
                        "The red tear applies actual collision damage");
                f.advance(30);
                maggy.tick();
                helper.assertTrue(f.shots(maggy).isEmpty(), "Surviving enemies never activate autonomous fire");
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallPreservesFifteenTickCooldownAndUnlinksOnRemoval(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var maggy = f.add(MAGGY);
            helper.assertTrue(maggy.attackInDirection(new Vec3(0, 0, 1)), "Initial attack succeeds");
            f.advance(3);
            maggy.setPos(f.hover().add(0, 40, 0));
            maggy.tick();
            helper.assertTrue(maggy.isRecallPending() && maggy.getAttackAge(0) < 0
                    && !maggy.attackInDirection(new Vec3(1, 0, 0)), "Recall suspends attacks and resets the action");
            helper.assertTrue(maggy.recallToOwner() && maggy.getDeltaMovement().equals(Vec3.ZERO), "Recall resets movement");
            helper.assertTrue(!maggy.attackInDirection(new Vec3(1, 0, 0)), "Recall grants no free shot");
            f.advance(11);
            helper.assertTrue(!maggy.attackInDirection(new Vec3(1, 0, 0)), "Fourteen ticks is still cooling down");
            f.advance(1);
            helper.assertTrue(maggy.attackInDirection(new Vec3(1, 0, 0)), "The original fifteen-tick deadline remains in force");
            maggy.discardAndUnlink();
            helper.assertTrue(!f.data.containsEntity(MAGGY, maggy.getUUID()) && !maggy.attackInDirection(new Vec3(1, 0, 0)), "Removed members cannot attack or remain linked");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void rejectedSpawnCanRetryInTheSameTick(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var maggy = f.add(MAGGY);
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 1);
            var blocker = BulletState.builder().velocity(new Vec3(1, 0, 0)).lifetime(2).build();
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).spawn(blocker);
            helper.assertTrue(!maggy.attackInDirection(new Vec3(1, 0, 0)) && maggy.getAttackAge(0) < 0,
                    "A full bullet manager rejects the attack without a false animation");
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).remove(blocker);
            helper.assertTrue(maggy.attackInDirection(new Vec3(1, 0, 0)) && f.shots(maggy).size() == 1,
                    "Failure consumes neither cooldown nor same-tick allowance");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final long originalTime;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "maggy-test"));
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 1, 1));
            owner.setPos(anchor.getX() + 0.5, 220, anchor.getZ() + 0.5);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        AbstractTearBabyFamiliarEntity add(ResourceLocation type) {
            helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(type), "Familiar must be registered: " + type);
            data.addCount(type, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Normal admission creates " + type);
            var entry = data.getEntry(type).orElseThrow();
            return (AbstractTearBabyFamiliarEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        void advance(int ticks) { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks); }
        void setHeld(boolean held) { owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> ability.setHoldRightClick(held)); }
        List<BulletState> shots(AbstractBabyFamiliarEntity familiar) {
            return BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates().stream().filter(shot -> shot.getShooter() == familiar).toList();
        }

        @Override public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            for (var shot : List.copyOf(BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates()))
                if (shot.getOwner() == owner) BulletRuntime.INSTANCE.manager(owner.serverLevel()).remove(shot);
            owner.discard();
            helper.getLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
        }
    }
}
