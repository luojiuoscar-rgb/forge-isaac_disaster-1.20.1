package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby.BrotherBobbyEntity;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_brother_bobby")
@PrefixGameTestTemplate(false)
public final class BrotherBobbyAttackGameTests {
    private static final ResourceLocation BOBBY = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "brother_bobby");

    @GameTest(template = "trajectory_empty")
    public static void positionAndDirectionRequestsUseBobbysOwnTearAndShareHeldCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Vec3 target = f.bobby.position().add(6, 3, 0);
            helper.assertTrue(f.bobby.attackTowards(target), "World-position attack creates Bobby's own tear");
            BulletState shot = f.shots().get(0);
            helper.assertTrue(target.subtract(shot.position()).normalize().distanceTo(shot.velocity()) < 1e-7,
                    "The neutral head/muzzle position aims precisely toward the requested world point");
            f.setHeld(true);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 1, "Held fire shares cooldown with public requests");
            f.advance(10);
            helper.assertTrue(f.bobby.attackInDirection(new Vec3(0, 0, 7)), "Direction requests use the same cadence");
            helper.assertTrue(f.shots().get(1).velocity().distanceTo(new Vec3(0, 0, 1)) < 1e-7,
                    "Direction magnitude does not change Bobby's one-block tear speed");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void nearbyThreatDoesNotCauseAutonomousMovementAimOrFire(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.enemy(new Vec3(0, 0, 6)).setTarget(f.owner);
            Vec3 parked = f.hover().add(0.5, 0, 0);
            f.bobby.setPos(parked);
            float yaw = f.bobby.getYRot();
            for (int tick = 0; tick < 40; tick++) {
                f.advance(1);
                f.bobby.tick();
                helper.assertTrue(f.shots().isEmpty() && f.bobby.getAttackAge(0) < 0,
                        "Nearby threats must not trigger autonomous shots or attack animations");
                helper.assertTrue(f.bobby.position().equals(parked) && f.bobby.getYRot() == yaw,
                        "An idle baby inside the follow area must not approach, retreat from or turn toward enemies");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldHeadFiresAtOwnCadenceWithoutAnyPlayerShotAndStopsOnRelease(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.setHeld(true);
            List<Integer> firedAt = new ArrayList<>();
            for (int tick = 0; tick <= 35; tick++) {
                if (tick > 0) f.advance(1);
                if (tick == 10) f.owner.setYRot(90);
                int before = f.shots().size();
                f.bobby.tick();
                if (f.shots().size() > before) firedAt.add(tick);
            }
            helper.assertTrue(firedAt.equals(List.of(0, 10, 20, 30)),
                    "Holding fires every ten ticks independently of player cooldown: " + firedAt);
            helper.assertTrue(f.shots().get(0).velocity().z > 0.99 && f.shots().get(1).velocity().x < -0.99,
                    "Each held shot uses the player's current look direction");
            f.setHeld(false);
            f.advance(5);
            f.primary(3);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 4, "Release stops fire even when a primary attack executes");
            f.setHeld(true);
            f.bobby.tick();
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 5, "Ready cooldown fires on re-press, only once in the tick");
            f.setHeld(false);
            f.advance(3);
            f.setHeld(true);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 5, "Release/re-press does not reset remaining cooldown");
            f.advance(7);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 6, "The original ten-tick cooldown still governs the next shot");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldChargeFiresBeforePlayerReleasesAndMainHandGuardsStaleInput(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
            ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
            var charge = (IChargeableAttack) ability.getAttackSelection().mainAttack();
            charge.onPressed(f.owner);
            f.setHeld(true);
            for (int tick = 0; tick <= 12; tick++) {
                if (tick > 0) f.advance(1);
                ability.tickAttacks(f.owner);
                f.bobby.tick();
            }
            helper.assertTrue(f.shots().size() == 2, "Bobby fires while the player's charged attack is still held");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            f.advance(10);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 2, "A stale held flag cannot fire after switching away from Isaac Head");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldTearHitsForConfiguredDamageWithoutAutonomousRepeats(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Zombie target = f.enemy(new Vec3(0, 0, 7));
            f.owner.setYRot(0);
            f.fireWhileHeld();
            helper.assertTrue(f.shots().size() == 1, "Held input fires toward the player's view");
            BulletState shot = f.shots().get(0);
            helper.assertTrue(shot.getOwner() == f.owner && shot.getShooter() == f.bobby, "Owner/shooter preserve player credit");
            helper.assertTrue(Math.abs(shot.velocity().length() - 1) < 1e-8, "Speed is one block per tick");
            helper.assertTrue(!shot.isHoming() && !shot.isControllable() && !shot.isSpectral() && !shot.isPiercing()
                    && shot.noGravity() && shot.getTrajectoryRuntime().specs().isEmpty()
                    && shot.getAttackContext() == null && shot.renderScale() == 1, "Independent ordinary tear has no player context");
            float health = target.getHealth();
            for (int i = 0; i < 10; i++) BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel());
            helper.assertTrue(Math.abs(target.getHealth() - (health - 2 * StatManager.DAMAGE.getBonus())) < 0.001,
                    "Real collision applies the configured damage: before=" + health + " after=" + target.getHealth()
                            + " expected=" + (2 * StatManager.DAMAGE.getBonus()) + " shotDamage=" + shot.getDamage()
                            + " start=" + shot.previousPosition() + " end=" + shot.position()
                            + " target=" + target.getBoundingBox() + " alive=" + shot.isAlive()
                            + " block=" + shot.getLastBlockHit());
            f.advance(10);
            f.bobby.tick();
            helper.assertTrue(f.shots().isEmpty(), "A nearby surviving enemy cannot restart fire after release");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void nearbyThreatsNeverRedirectHeldShotsAndReleaseStopsFire(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.enemy(new Vec3(0, 0, 6)).setTarget(f.owner);
            f.enemy(new Vec3(0, 0, -2)).setTarget(f.owner);
            f.owner.setYRot(90);
            f.setHeld(true);
            for (int tick = 0; tick <= 20; tick++) {
                if (tick > 0) f.advance(1);
                f.bobby.tick();
            }
            helper.assertTrue(f.shots().size() == 3, "Enemies do not alter the baby's ten-tick cadence");
            helper.assertTrue(f.shots().stream().allMatch(shot -> shot.velocity().x < -0.99),
                    "Threats never replace the player's westward view direction");
            f.setHeld(false);
            f.advance(10);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 3, "Release stops shooting even with threats in range");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void movingPlayerHeldShotsFollowParallelLookWithoutEnemyInterference(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.enemy(new Vec3(0, 0, 6));
            f.bobby.setPos(f.hover().add(0, 5, 0));
            f.owner.setYRot(90);
            f.owner.setXRot(-30);
            f.owner.setPos(f.owner.position().add(1, 0, 0));
            Vec3 start = f.bobby.position();
            f.fireWhileHeld();
            helper.assertTrue(f.bobby.position().distanceTo(start) > 0 && f.shots().size() == 1,
                    "Following movement and held fire happen in the same tick");
            helper.assertTrue(f.shots().get(0).velocity().distanceTo(f.owner.getLookAngle().normalize()) < 1e-6,
                    "Held fire is parallel to the current look direction");
            f.primary(3);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 1, "Same tick does not fire again");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void configuredRangeIgnoresPlayerBaseAndModifiersHasFloorAndExceedsSixtyFour(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            var range = f.owner.getAttribute(ModAttributes.BULLET_RANGE.get());
            range.addTransientModifier(new AttributeModifier(UUID.randomUUID(), "item range", 500, AttributeModifier.Operation.ADDITION));
            range.addTransientModifier(new AttributeModifier(UUID.randomUUID(), "temporary range", 2, AttributeModifier.Operation.MULTIPLY_TOTAL));
            range.setBaseValue(range.getBaseValue() + 3);
            int index = 0;
            for (double configured : new double[]{20, 2, 100}) {
                settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured);
                DefaultAttributeManager.apply(f.owner);
                f.advance(10);
                f.fireWhileHeld();
                BulletState shot = f.shots().get(index++);
                double expected = Math.max(12, 1.5 * configured);
                helper.assertTrue(shot.getRange() == expected, "Range snapshot uses only configuration, excluding raw base changes and item/temporary bonuses");
                for (int i = 0; i < expected; i++) helper.assertTrue(shot.tickPhysics(), "Full range movement tick");
                helper.assertTrue(Math.abs(shot.traveled() - expected) < 1e-5 && !shot.tickPhysics(), "Expires after full range");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void canceledOrFullyFilteredRequestsAndAdditionalAttacksDoNotFollow(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Consumer<BeforePerformAttackEvent> cancel = event -> { if (event.getEntity() == f.owner) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BeforePerformAttackEvent.class, cancel);
            try {
                helper.assertTrue(!AttackPipeline.executeRequest(AttackRequest.generated(f.owner, ModAttackTypes.BULLET.get(),
                        AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.FULL, false)), "Cancellation retains false return");
            } finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            Consumer<AttackContextPrepareEvent> filter = event -> { if (event.getOwner() == f.owner) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, AttackContextPrepareEvent.class, filter);
            try { helper.assertTrue(f.primary(3), "Filtered execution retains existing true return"); }
            finally { MinecraftForge.EVENT_BUS.unregister(filter); }
            for (AttackOrigin origin : new AttackOrigin[]{AttackOrigin.ABILITY_EXTRA, AttackOrigin.BULLET_SECONDARY, AttackOrigin.SPLIT_CHILD}) {
                AttackPipeline.executeRequest(AttackRequest.withContexts(f.owner, ModAttackTypes.BULLET.get(), origin,
                        AttackPipelineMode.PREPARE_AND_EXECUTE, f.contexts(1), false));
            }
            helper.assertTrue(f.shots().isEmpty(), "Canceled, filtered and derived requests never fire Bobby");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void generatedOrdinaryAndChargedReleaseDoNotFireBobbyWithoutHeldInput(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            AttackPipeline.executeRequest(AttackRequest.generated(f.owner, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.FULL, false));
            helper.assertTrue(f.shots().isEmpty(), "Generated primary does not drive Bobby's cadence");
            f.advance(10);
            var brimstone = ModAttackTypes.BRIMSTONE.get();
            ((IChargeableAttack) brimstone).addCharge(f.owner, ((IChargeableAttack) brimstone).getTotalCharge(f.owner));
            ((IChargeableAttack) brimstone).onReleased(f.owner);
            f.bobby.tick();
            helper.assertTrue(f.shots().isEmpty(), "Charged release does not fire Bobby after input has ended");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldFireRecallRetainsCooldownAndRemovalUnlinks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.setYRot(180);
            f.enemy(new Vec3(0, 0, 6));
            f.setHeld(true);
            f.primary(3);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 1 && f.shots().get(0).velocity().z < -0.9,
                    "Held fire follows the player despite nearby enemies; no duplicate");
            f.bobby.setPos(f.owner.position().add(0, 17, 0));
            f.bobby.tick();
            f.advance(1);
            f.primary(1);
            helper.assertTrue(f.shots().size() == 1 && f.bobby.getAttackAge(0) < 0, "Pending recall suppresses fire and action");
            helper.assertTrue(f.bobby.recallToOwner(), "Queued entity recalls");
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 1, "Recall does not refresh cooldown");
            f.advance(9);
            f.bobby.tick();
            helper.assertTrue(f.shots().size() == 2, "Original cooldown expires normally");
            f.bobby.discardAndUnlink();
            helper.assertTrue(!f.data.containsEntity(BOBBY, f.bobby.getUUID()), "Removal unlinks roster");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void ordinaryTearIgnoresPlayerEffectsAndStopsAtBlocks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
            ability.setHoming(1);
            ability.setSpectral(1);
            ability.setPiercing(1);
            ability.setControllable(1);
            ability.addBulletColor(ModBulletColors.POISON.getId(), 1);
            ability.addBulletVisual(ResourceLocation.fromNamespaceAndPath("test", "player_only"), 1);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(8);
            f.owner.getAttribute(ModAttributes.BULLET_SCALE.get()).setBaseValue(40);
            f.owner.setYRot(-90);
            f.fireWhileHeld();
            BulletState shot = f.shots().get(0);
            helper.assertTrue(!shot.isHoming() && !shot.isSpectral() && !shot.isPiercing() && !shot.isControllable()
                    && shot.getTrajectorySpecs().isEmpty() && shot.getSplitSequence().isEmpty()
                    && shot.visualIds().isEmpty() && shot.getAttackContext() == null, "Player effects are isolated");
            helper.assertTrue(shot.getColorId().equals(ModBulletColors.BASE.getId())
                    && shot.color() == ModBulletColors.BASE.get().color(), "Ordinary tear uses the base color rather than player poison");
            helper.assertTrue(Math.abs(shot.getDamage() - 2 * StatManager.DAMAGE.getBonus()) < 1e-5
                    && Math.abs(shot.velocity().length() - 1) < 1e-8 && shot.renderScale() == 1,
                    "Player damage/speed/scale do not leak into the familiar shot");
            BlockPos wall = BlockPos.containing(shot.position().add(3, 0, 0));
            var original = f.owner.level().getBlockState(wall);
            try {
                f.owner.level().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                for (int i = 0; i < 5; i++) BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel());
                helper.assertTrue(!f.shots().contains(shot), "Non-spectral familiar tear is blocked by stone");
            } finally { f.owner.level().setBlockAndUpdate(wall, original); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void enemiesEnteringAndLeavingRangeDoNotStartShooting(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.bobby.tick();
            Zombie enemy = f.enemy(new Vec3(0, 0, 6));
            for (int tick = 0; tick < 24; tick++) {
                if (tick == 8) enemy.setPos(f.hover().add(0, 8.1, 0));
                if (tick == 16) enemy.setPos(f.hover().add(0, -enemy.getBbHeight() / 2, 2));
                f.advance(1);
                f.bobby.tick();
                helper.assertTrue(f.shots().isEmpty() && f.bobby.getAttackAge(0) < 0,
                        "Crossing the former detection sphere cannot activate autonomous attacks");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void rejectedSpawnConsumesNeitherCooldownNorAnimation(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 1);
            var blocker = BulletState.builder().velocity(new Vec3(1, 0, 0)).lifetime(2).build();
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).spawn(blocker);
            f.fireWhileHeld();
            helper.assertTrue(f.shots().isEmpty() && f.bobby.getAttackAge(0) < 0, "Capacity failure has no action");
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).remove(blocker);
            f.owner.setYRot(-90);
            f.fireWhileHeld();
            helper.assertTrue(f.shots().size() == 1 && f.bobby.getAttackAge(0) == 0, "Same tick retry succeeds without consumed cooldown");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final BrotherBobbyEntity bobby;
        final long originalTime;
        final List<LivingEntity> enemies = new ArrayList<>();
        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "bobby-attack"));
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 1, 1));
            // Keep projectile flight paths above structures and markers from other GameTest batches.
            owner.setPos(anchor.getX() + 0.5, 220, anchor.getZ() + 0.5);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            data.addCount(BOBBY, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Bobby generated by normal admission");
            bobby = (BrotherBobbyEntity) helper.getLevel().getEntity(data.getEntry(BOBBY).orElseThrow().getEntityId(0));
        }
        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        Zombie enemy(Vec3 offset) {
            Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
            zombie.setNoAi(true);
            zombie.setNoGravity(true);
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
            zombie.setHealth(100);
            zombie.setPos(hover().add(offset).add(0, -zombie.getBbHeight() / 2, 0));
            helper.getLevel().addFreshEntity(zombie);
            enemies.add(zombie);
            return zombie;
        }
        List<BulletState> shots() { return BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates().stream()
                .filter(shot -> shot.getShooter() == bobby).toList(); }
        void advance(int ticks) { helper.getLevel().getServer().getWorldData().overworldData().setGameTime(helper.getLevel().getGameTime() + ticks); }
        List<AttackContext> contexts(int count) {
            List<AttackContext> contexts = new ArrayList<>();
            for (int i = 0; i < count; i++) contexts.add(AttackContext.builder(owner, owner).attackType(ModAttackTypes.BULLET.get())
                    .position(owner.getEyePosition()).mainAxis(owner.getLookAngle()).range(12).speed(1).build());
            return contexts;
        }
        boolean primary(int count) {
            return AttackPipeline.executeRequest(AttackRequest.withContexts(owner, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.PREPARE_AND_EXECUTE, contexts(count), false));
        }
        void setHeld(boolean held) {
            owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> ability.setHoldRightClick(held));
        }
        void fireWhileHeld() {
            setHeld(true);
            bobby.tick();
            setHeld(false);
        }
        @Override public void close() {
            enemies.forEach(LivingEntity::discard);
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            for (BulletState shot : List.copyOf(BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates()))
                if (shot.getOwner() == owner || shot.getShooter() == bobby) BulletRuntime.INSTANCE.manager(owner.serverLevel()).remove(shot);
            owner.discard();
            helper.getLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
        }
    }
}
