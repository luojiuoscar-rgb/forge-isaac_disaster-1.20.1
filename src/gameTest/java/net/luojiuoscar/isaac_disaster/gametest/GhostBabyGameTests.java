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

@GameTestHolder("isaac_disaster_ghost_baby")
@PrefixGameTestTemplate(false)
public final class GhostBabyGameTests {
    private static final ResourceLocation GHOST = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "ghost_baby");
    private static final ResourceLocation BOBBY = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "brother_bobby");
    private static final ResourceLocation MAGGY = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "sister_maggy");
    private static final ResourceLocation GHOST_TEAR = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "ghost_tear");
    private static final ResourceLocation WEAKENING = ResourceLocation.fromNamespaceAndPath("ghost_baby_test", "weakening");

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class GhostBabyTestRegistration {
        @SubscribeEvent
        public static void registerGhostBabyModifiers(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> registry.register(WEAKENING,
                    new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, -2,
                            AbstractBabyFamiliarEntity.class::isInstance)));
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredBabySharesSixtyFourSlotsAcrossThreeTypes(GameTestHelper helper) {
        helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(GHOST), "Ghost Baby has an entity and familiar descriptor");
        var descriptor = FamiliarHelper.getFamiliarType(GHOST);
        helper.assertTrue(descriptor.getLimit() == 64 && descriptor.hasEntityTag(AbstractBabyFamiliarEntity.class)
                && descriptor.getGroup() == ModFamiliarLimitGroups.BABIES.get(), "Ghost Baby belongs to the shared baby category");
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(GHOST, 100);
            helper.assertTrue(f.data.getEffectiveCount(GHOST) == 64, "Ghost Baby alone has a sixty-four cap");
            f.data.setCount(GHOST, 0);
            f.data.setCount(BOBBY, 20);
            f.data.setCount(MAGGY, 20);
            f.data.setCount(GHOST, 100);
            helper.assertTrue(f.data.getCount(GHOST) == 100 && f.data.getEffectiveCount(GHOST) == 24,
                    "All three types share sixty-four slots while excess requests wait");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedChainFollowsItsPredecessorAndCompactsOnRemoval(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var bobby = f.add(BOBBY);
            var maggy = f.add(MAGGY);
            var ghost = f.add(GHOST);
            helper.assertTrue(bobby.getFormationIndex() == 0 && maggy.getFormationIndex() == 1 && ghost.getFormationIndex() == 2,
                    "Three real types share admission-ordered chain slots");
            maggy.setPos(f.hover().add(5, 0, 0));
            ghost.setPos(f.hover());
            ghost.tick();
            helper.assertTrue(ghost.getDeltaMovement().x > 0, "Ghost Baby follows Maggy rather than the nearby player");
            maggy.discardAndUnlink();
            f.data.addCount(MAGGY, -1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(ghost.getFormationIndex() == 1, "Removing a middle member compacts the ghost's slot");
            var appended = f.add(MAGGY);
            helper.assertTrue(appended.getFormationIndex() == 2, "A new member joins after Ghost Baby");
            bobby.setPos(f.hover());
            ghost.setPos(f.hover().add(0, 40, 0));
            f.advance(1);
            ghost.tick();
            helper.assertTrue(ghost.recallToOwner(), "A real recall clears prior pursuit momentum");
            Vec3 parked = bobby.position().add(0.25, 0, 0);
            ghost.setPos(parked);
            f.advance(1);
            ghost.tick();
            helper.assertTrue(ghost.position().distanceTo(parked) < 1e-7, "Inside one block the baby is not pushed outward");
            helper.assertTrue(ghost.shouldRenderAtSqrDistance(96 * 96) && ghost.getType().clientTrackingRange() == 8,
                    "Ghost Baby has normal baby render and tracking distances");
            FamiliarHelper.discardAllRuntimeFamiliars(f.owner);
            helper.assertTrue(f.data.getEntityIdsInAdmissionOrder().isEmpty() && ghost.isRemoved() && appended.isRemoved(),
                    "Cleanup removes all types and roster links");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldInputFiresEveryFifteenTicksWhileFollowingAndStopsOnRelease(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ghost = f.add(GHOST);
            ghost.setPos(f.hover().add(-5, 0, 0));
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.setHeld(true);
            List<Integer> firedAt = new ArrayList<>();
            for (int tick = 0; tick <= 30; tick++) {
                if (tick > 0) f.advance(1);
                f.owner.setPos(f.owner.position().add(0.03, 0, 0));
                if (tick == 15) f.owner.setYRot(90);
                int before = f.shots(ghost).size();
                ghost.tick();
                ghost.tick();
                if (f.shots(ghost).size() > before) firedAt.add(tick);
            }
            helper.assertTrue(firedAt.equals(List.of(0, 15, 30)), "Held input has its own fifteen-tick cadence and same-tick deduplication");
            helper.assertTrue(f.shots(ghost).get(1).velocity().distanceTo(new Vec3(-1, 0, 0)) < 1e-6,
                    "The baby follows the player's current view without requiring a player projectile");
            f.setHeld(false);
            f.advance(15);
            ghost.tick();
            helper.assertTrue(f.shots(ghost).size() == 3, "Release stops a ready baby");
            f.setHeld(true);
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            ghost.tick();
            helper.assertTrue(f.shots(ghost).size() == 3, "Stale held input cannot fire after switching items");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void spectralTearsCrossWallsHitOnlyTheFirstEnemyAndCreditThePlayer(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ghost = f.add(GHOST);
            var bobby = f.add(BOBBY);
            var maggy = f.add(MAGGY);
            for (var baby : List.of(ghost, bobby, maggy)) baby.setPos(f.hover());
            Vec3 head = f.head(ghost);
            Zombie first = f.enemy(head.add(0, 0, 4));
            Zombie behind = f.enemy(head.add(0, 0, 6));
            BlockPos wall = BlockPos.containing(head.add(0, 0, 2));
            var original = f.owner.level().getBlockState(wall);
            try {
                f.owner.level().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                for (var baby : List.of(ghost, bobby, maggy)) helper.assertTrue(baby.attackInDirection(new Vec3(0, 0, 1)), "Each baby fires");
                BulletState shot = f.shots(ghost).get(0);
                helper.assertTrue(shot.isSpectral() && !shot.isPiercing() && shot.getOwner() == f.owner && shot.getShooter() == ghost,
                        "The spectral tear keeps distinct owner and shooter without entity piercing");
                helper.assertTrue(!f.shots(bobby).get(0).isSpectral() && !f.shots(maggy).get(0).isSpectral(),
                        "Configuring a ghost tear cannot leak into later ordinary baby tears");
                for (int tick = 0; tick < 9; tick++) f.tickBullets();
                helper.assertTrue(Math.abs(first.getHealth() - (100 - 2 * StatManager.DAMAGE.getBonus())) < 0.001,
                        "Only the ghost tear crosses the solid wall and deals its actual damage");
                helper.assertTrue(behind.getHealth() == 100 && f.shots(ghost).isEmpty(), "The first enemy consumes the tear");
                helper.assertTrue(f.shots(bobby).isEmpty() && f.shots(maggy).isEmpty(), "Ordinary baby tears still stop at the wall");
                f.advance(30);
                ghost.tick();
                helper.assertTrue(f.shots(ghost).isEmpty() && ghost.getAttackAge(0) < 0, "Nearby living enemies do not trigger autonomous attacks");
            } finally {
                f.owner.level().setBlockAndUpdate(wall, original);
                first.discard();
                behind.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void ghostStatsSnapshotConfigurationAndIgnorePlayerEffects(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var ghost = f.add(GHOST);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(800);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).addTransientModifier(
                    new AttributeModifier(UUID.randomUUID(), "temporary range", 500, AttributeModifier.Operation.ADDITION));
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(8);
            f.owner.getAttribute(ModAttributes.BULLET_SCALE.get()).setBaseValue(40);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ability.setHoming(1);
            ability.setPiercing(1);
            ability.setControllable(1);
            ability.addBulletColor(ModBulletColors.POISON.getId(), 1);
            for (double configured : new double[]{20, 2, 100}) {
                settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured);
                f.advance(15);
                helper.assertTrue(ghost.attackInDirection(new Vec3(0, 0, 1)), "Ready ghost fires");
                var shots = f.shots(ghost);
                BulletState shot = shots.get(shots.size() - 1);
                double expected = Math.max(12, 1.5 * configured);
                helper.assertTrue(shot.getRange() == expected && shot.velocity().length() == 1 && shot.renderScale() == 1
                                && shot.noGravity() && shot.getDamage() == 2 * StatManager.DAMAGE.getBonus(), "Only configured range and baby stats apply");
                helper.assertTrue(shot.isSpectral() && !shot.isHoming() && !shot.isPiercing() && !shot.isControllable()
                        && shot.getAttackContext() == null && shot.getSplitSequence().isEmpty() && shot.getTrajectorySpecs().isEmpty()
                        && shot.visualIds().isEmpty(), "The tear is independent of player effects and attack modules");
                helper.assertTrue(shot.getColorId().equals(GHOST_TEAR) && shot.color() == 0xFFFFFF && Math.abs(shot.alpha() - 0.6F) < 1e-7,
                        "The tear uses its own white translucent appearance");
                settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, 1.0);
                helper.assertTrue(shot.getRange() == expected, "Existing shots retain the range snapshot");
                for (int tick = 0; tick < expected; tick++) helper.assertTrue(shot.tickPhysics(), "Full configured movement tick");
                helper.assertTrue(Math.abs(shot.traveled() - expected) < 1e-6 && !shot.tickPhysics(), "Tears travel their full range, even above sixty-four blocks");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void damageMatchesBobbyAndClampsEnhancementWithoutChangingExistingShots(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var ghost = f.add(GHOST);
            var bobby = f.add(BOBBY);
            ghost.attackInDirection(new Vec3(1, 0, 0));
            bobby.attackInDirection(new Vec3(1, 0, 0));
            BulletState first = f.shots(ghost).get(0);
            double damage = 2 * StatManager.DAMAGE.getBonus();
            helper.assertTrue(first.getDamage() == damage && first.getDamage() == f.shots(bobby).get(0).getDamage(), "Ghost Baby retains Bobby's damage coefficient");
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            f.advance(15);
            helper.assertTrue(ghost.attackTowards(ghost.position().add(4, 3, 0)), "World-position aiming uses the same attack path");
            BulletState enhanced = f.shots(ghost).get(1);
            helper.assertTrue(enhanced.getDamage() == 2 * damage && first.getDamage() == damage, "Enhancement changes later shots only");
            helper.assertTrue(ghost.position().add(4, 3, 0).subtract(enhanced.position()).normalize().distanceTo(enhanced.velocity()) < 1e-7,
                    "Position aiming uses the actual scaled head origin");
            StatManager.removeFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            helper.assertTrue(ModFamiliarModifiers.get(WEAKENING) != null, "The ghost test's negative modifier is registered independently");
            StatManager.addFamiliarModifier(f.owner, WEAKENING, 1);
            f.advance(15);
            ghost.attackInDirection(new Vec3(1, 0, 0));
            helper.assertTrue(f.shots(ghost).get(2).getDamage() == 0, "Enhancement below minus one clamps to zero");
            StatManager.removeFamiliarModifier(f.owner, WEAKENING, 1);
            settings.set(GameplayConfig.DAMAGE_BONUS, 0.0);
            f.advance(15);
            ghost.attackInDirection(new Vec3(1, 0, 0));
            helper.assertTrue(f.shots(ghost).get(3).getDamage() == 0, "Zero configured damage remains harmless");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void shotsStartAtTheHeadInEveryDirectionWithoutForwardOffset(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ghost = f.add(GHOST);
            for (Vec3 axis : List.of(new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 1, 0),
                    new Vec3(0, -1, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1))) {
                f.advance(15);
                helper.assertTrue(ghost.attackInDirection(axis), "Ready ghost fires along each axis");
                var shots = f.shots(ghost);
                helper.assertTrue(shots.get(shots.size() - 1).position().distanceTo(f.head(ghost)) < 1e-7,
                        "Aiming never moves the tear forward from its head origin");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void failureAnimationTrackingAndRecallPreserveTheSharedCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var ghost = f.add(GHOST);
            helper.assertTrue(!ghost.attackInDirection(Vec3.ZERO) && !ghost.attackInDirection(new Vec3(Double.NaN, 0, 0)), "Invalid aim cannot consume cooldown");
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 1);
            var blocker = BulletState.builder().velocity(new Vec3(1, 0, 0)).lifetime(2).build();
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).spawn(blocker);
            helper.assertTrue(!ghost.attackInDirection(new Vec3(0, 0, 1)) && ghost.getAttackAge(0) < 0, "Failed spawn has no action or cooldown");
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).remove(blocker);
            helper.assertTrue(ghost.attackInDirection(new Vec3(0, 0, 1)) && !ghost.attackInDirection(new Vec3(0, 0, 1)), "A successful retry is deduplicated in the same tick");
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 100);
            f.advance(5);
            var mirror = (AbstractTearBabyFamiliarEntity) ghost.getType().create(helper.getLevel());
            mirror.getEntityData().assignValues(ghost.getEntityData().getNonDefaultValues());
            helper.assertTrue(mirror.getAttackAge(0) == 5, "New tracking resumes the existing animation phase");
            f.advance(5);
            helper.assertTrue(ghost.getAttackAge(0) < 0 && mirror.getAttackAge(0) < 0, "The ten-tick action finishes before the cooldown");
            f.advance(4);
            helper.assertTrue(!ghost.attackInDirection(new Vec3(0, 0, 1)), "Fourteen ticks cannot fire again");
            f.advance(1);
            helper.assertTrue(ghost.attackInDirection(new Vec3(0, 0, 1)), "Fifteen ticks allows the next shot");
            f.advance(3);
            ghost.setPos(f.hover().add(0, 40, 0));
            ghost.tick();
            helper.assertTrue(ghost.isRecallPending() && ghost.getAttackAge(0) < 0 && !ghost.attackInDirection(new Vec3(1, 0, 0)), "Recall suppresses attacks and resets the action");
            helper.assertTrue(ghost.recallToOwner() && ghost.getDeltaMovement().equals(Vec3.ZERO), "Recall clears movement");
            f.advance(11);
            helper.assertTrue(!ghost.attackInDirection(new Vec3(1, 0, 0)), "Recall cannot refresh the remaining cooldown");
            f.advance(1);
            helper.assertTrue(ghost.attackInDirection(new Vec3(1, 0, 0)), "Original cooldown expires normally after recall");
            ghost.discardAndUnlink();
            helper.assertTrue(!f.data.containsEntity(GHOST, ghost.getUUID()) && !ghost.attackInDirection(new Vec3(1, 0, 0)), "Removed babies cannot fire");
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
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ghost-test"));
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

        Zombie enemy(Vec3 center) {
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true);
            target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setHealth(100);
            target.setPos(center.add(0, -target.getBbHeight() / 2, 0));
            helper.getLevel().addFreshEntity(target);
            return target;
        }

        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        Vec3 head(AbstractTearBabyFamiliarEntity baby) { return baby.position().add(0, 5.5 / 16 * 0.6 * baby.getVisualScale(), 0); }
        void advance(int ticks) { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks); }
        void setHeld(boolean held) { owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> ability.setHoldRightClick(held)); }
        void tickBullets() { BulletRuntime.INSTANCE.manager(owner.serverLevel()).tick(owner.serverLevel()); }
        List<BulletState> shots(AbstractBabyFamiliarEntity baby) {
            return BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates().stream().filter(shot -> shot.getShooter() == baby).toList();
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
