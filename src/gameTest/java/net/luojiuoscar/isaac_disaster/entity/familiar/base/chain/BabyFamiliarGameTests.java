package net.luojiuoscar.isaac_disaster.entity.familiar.base.chain;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarLimitGroups;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_baby_familiar")
@PrefixGameTestTemplate(false)
public final class BabyFamiliarGameTests {
    private static final ResourceLocation BOBBY = ModEntities.BROTHER_BOBBY.getId();
    private static final ResourceLocation OTHER = ResourceLocation.fromNamespaceAndPath("baby_test", "other_baby");
    private static final ResourceLocation CHAIN_ONLY = ResourceLocation.fromNamespaceAndPath("baby_test", "chain_only");

    @GameTest(template = "trajectory_empty")
    public static void queryingChainLinksNeverAssignsSlotsAndExplicitRefreshAndTickHandleCompaction(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOBBY);
            var second = f.add(OTHER);
            helper.assertTrue(first.getFormationIndex() == 0 && second.getFormationIndex() == 1,
                    "Admission assigns slots through each familiar's formation rule");
            first.discardAndUnlink();
            f.data.addCount(BOBBY, -1);
            helper.assertTrue(FamiliarChain.link(f.owner, second).slot() == 0 && second.getFormationIndex() == 1,
                    "Removal invalidates the snapshot without query-time assignments");
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(second.getFormationIndex() == 0,
                    "The common maintenance entrypoint refreshes the compacted slot");
            var third = f.add(BOBBY);
            helper.assertTrue(third.getFormationIndex() == 1, "A newcomer joins the refreshed chain's end");
            second.discardAndUnlink();
            f.data.addCount(OTHER, -1);
            helper.assertTrue(FamiliarChain.link(f.owner, third).slot() == 0 && third.getFormationIndex() == 1,
                    "A fresh query leaves the surviving familiar's assigned slot unchanged");
            third.tick();
            helper.assertTrue(third.getFormationIndex() == 0, "The chain entity refreshes its compacted slot before moving");
        }
        helper.succeed();
    }

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class BabyFamiliarTestRegistration {
        static EntityType<TestBaby> other;
        static EntityType<TestChain> chainOnly;
        @SubscribeEvent public static void registerBabyFamiliarTestTypes(RegisterEvent event) {
            event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
                other = EntityType.Builder.of(TestBaby::new, MobCategory.MISC).sized(0.5F, 0.5F).build(OTHER.toString());
                helper.register(OTHER, other);
                chainOnly = EntityType.Builder.of(TestChain::new, MobCategory.MISC).sized(0.5F, 0.5F).build(CHAIN_ONLY.toString());
                helper.register(CHAIN_ONLY, chainOnly);
            });
            event.register(ModFamiliarEntities.FAMILIAR_ENTITY_KEY, helper -> {
                    helper.register(OTHER, new FamiliarEntityType(() -> other, TestBaby.class, 64,
                            ModFamiliarLimitGroups.BABIES, 0, false));
                    helper.register(CHAIN_ONLY, new FamiliarEntityType(() -> chainOnly, TestChain.class, 1,
                            null, 0, false));
            });
        }
    }
    public static final class TestChain extends AbstractChainFamiliarEntity {
        TestChain(EntityType<? extends TestChain> type, Level level) { super(type, level); }
    }
    public static final class TestBaby extends AbstractBabyFamiliarEntity {
        TestBaby(EntityType<? extends TestBaby> type, Level level) { super(type, level); }
        int shots;
        int interval = 4;
        boolean reject;
        Vec3 lastDirection;
        @Override protected int attackIntervalTicks() { return interval; }
        @Override protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
            if (reject) return false;
            shots++;
            lastDirection = direction;
            return true;
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void chainFollowingDoesNotImplyBabyCategoryOrConsumeBabyCapacity(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOBBY);
            var follower = f.add(CHAIN_ONLY);
            helper.assertTrue(first instanceof AbstractBabyFamiliarEntity && !(follower instanceof AbstractBabyFamiliarEntity),
                    "Only baby subclasses belong to the baby category; pure chain followers do not");
            helper.assertTrue(!FamiliarHelper.getFamiliarType(CHAIN_ONLY).hasEntityTag(AbstractBabyFamiliarEntity.class),
                    "Descriptor tags do not label a pure chain follower as a baby");
            helper.assertTrue(follower.getFormationIndex() == 1, "A non-baby chain follower still joins the next slot");
            first.setPos(f.hover().add(5, 0, 0));
            follower.setPos(f.hover());
            f.advance();
            follower.tick();
            helper.assertTrue(follower.getDeltaMovement().x > 0,
                    "A pure chain follower inherits movement toward the preceding chain member");
            f.data.setCount(BOBBY, 64);
            helper.assertTrue(f.data.getEffectiveCount(BOBBY) == 64 && f.data.getEffectiveCount(CHAIN_ONLY) == 1,
                    "The baby group retains all sixty-four slots alongside a non-baby chain follower");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void sharedAttackEntrypointsNormalizeAimAndRespectConcreteCadenceAndFailures(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            TestBaby baby = (TestBaby) f.add(OTHER);
            helper.assertTrue(!baby.attackInDirection(Vec3.ZERO)
                    && !baby.attackInDirection(new Vec3(Double.NaN, 0, 0))
                    && !baby.attackTowards(new Vec3(Double.POSITIVE_INFINITY, 0, 0)), "Invalid directions cannot attack");
            baby.reject = true;
            helper.assertTrue(!baby.attackInDirection(new Vec3(1, 0, 0)) && baby.shots == 0,
                    "The concrete attack may fail without consuming the shared cooldown");
            baby.reject = false;
            helper.assertTrue(baby.attackTowards(baby.getBoundingBox().getCenter().add(3, 4, 0)),
                    "A valid world target can immediately retry after a failed attack");
            helper.assertTrue(baby.lastDirection.distanceTo(new Vec3(0.6, 0.8, 0)) < 1e-7,
                    "World target aiming uses the concrete baby's origin and passes a unit direction");
            helper.assertTrue(!baby.attackInDirection(new Vec3(0, 0, 2)) && baby.shots == 1,
                    "Both entrypoints share same-tick deduplication");
            for (int tick = 0; tick < 3; tick++) f.advance();
            helper.assertTrue(!baby.attackInDirection(new Vec3(0, 0, 2)), "Concrete four-tick cooldown still runs");
            f.advance();
            helper.assertTrue(baby.attackInDirection(new Vec3(0, 0, 7)) && baby.lastDirection.equals(new Vec3(0, 0, 1)),
                    "Concrete cadence and direction normalization are independent of Bobby's attack");
            baby.setPos(f.hover().add(0, 40, 0));
            baby.tick();
            helper.assertTrue(baby.isRecallPending() && !baby.attackInDirection(new Vec3(1, 0, 0)),
                    "Public attack requests are suppressed during recall");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void secondBabyInheritsHeldPlayerAttacksUsingItsOwnAttackAndInterval(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            TestBaby baby = (TestBaby) f.add(OTHER);
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            f.owner.setYRot(90);
            f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> ability.setHoldRightClick(true));
            for (int tick = 0; tick <= 12; tick++) {
                if (tick > 0) f.advance();
                baby.tick();
            }
            helper.assertTrue(baby.shots == 4 && baby.lastDirection.distanceTo(new Vec3(-1, 0, 0)) < 1e-6,
                    "The base schedules the second baby's own attack every four ticks along the player's view");
            f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> ability.setHoldRightClick(false));
            for (int tick = 0; tick < 8; tick++) { f.advance(); baby.tick(); }
            helper.assertTrue(baby.shots == 4, "The second baby also stops attacking on release");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void otherBabyInheritsChainFollowingWithoutDefiningItsOwnMotion(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(OTHER);
            baby.setPos(f.hover().add(5, 0, 0));
            baby.tick();
            helper.assertTrue(baby.getX() < f.hover().x + 5 && baby.getDeltaMovement().x < 0,
                    "A second baby implementation inherits the same chain follow behavior");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void babiesRemainVisibleBeyondTheOldThirtyTwoBlockCutoff(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(BOBBY);
            helper.assertTrue(baby.shouldRenderAtSqrDistance(96 * 96), "A normal-length baby chain remains renderable");
            helper.assertTrue(baby.getType().clientTrackingRange() == 8, "Baby tracking covers eight chunks");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void markerAndSharedQuotaApplyAcrossBabyTypesWithoutUsingFlyCapacity(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var descriptor = ModFamiliarEntities.BROTHER_BOBBY.get();
            helper.assertTrue(descriptor.hasEntityTag(AbstractBabyFamiliarEntity.class) && descriptor.getLimit() == 64,
                    "Bobby is a baby familiar with an own limit of 64");
            helper.assertTrue(descriptor.getGroup() == ModFamiliarLimitGroups.BABIES.get()
                    && descriptor.getGroup().getLimit() == 64, "Babies share 64 slots");
            f.data.setCount(BOBBY, 40);
            f.data.setCount(OTHER, 40);
            helper.assertTrue(f.data.getEffectiveCount(BOBBY) == 40 && f.data.getEffectiveCount(OTHER) == 24,
                    "Different baby types compete for the same 64 slots: bobby=" + f.data.getEffectiveCount(BOBBY)
                            + " other=" + f.data.getEffectiveCount(OTHER) + " descriptor=" + FamiliarHelper.getFamiliarType(OTHER)
                            + " entity=" + ForgeRegistries.ENTITY_TYPES.getKey(BabyFamiliarTestRegistration.other)
                            + " types=" + FamiliarHelper.getRegisteredFamiliarTypeIds().toList());
            f.data.setCount(ModEntities.BLUE_FLY.getId(), 64);
            helper.assertTrue(f.data.getEffectiveCount(ModEntities.BLUE_FLY.getId()) == 64
                    && f.data.getEffectiveCount(BOBBY) + f.data.getEffectiveCount(OTHER) == 64, "Flies keep separate capacity");
            f.data.setCount(BOBBY, 0);
            f.data.setCount(OTHER, 100);
            helper.assertTrue(f.data.getCount(OTHER) == 64 && f.data.getEffectiveCount(OTHER) == 64,
                    "Each individual baby type also caps at 64");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void sixtyFourBabiesSpawnOneApartAndTailDoesNotRecallToPlayer(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BOBBY, 128);
            for (int i = 0; i < 64; i++) helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "Admit baby " + i);
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner), "No 65th baby");
            List<UUID> ids = List.copyOf(f.data.getEntityIdsInAdmissionOrder());
            Vec3 previous = f.hover();
            for (int i = 0; i < ids.size(); i++) {
                var baby = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(ids.get(i));
                helper.assertTrue(Math.abs(baby.position().distanceTo(previous) - 1.0) < 1e-7, "Spawn spacing for slot " + i);
                baby.tick();
                helper.assertTrue(baby.getFormationIndex() == i && !baby.isRecallPending(), "Legal slot " + i + " does not recall");
                previous = baby.position();
            }
            var tail = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(ids.get(63));
            helper.assertTrue(tail.distanceToSqr(f.owner) > 16 * 16, "The legitimate tail is well beyond player recall distance");
            var before = (AbstractIsaacFamiliarEntity) f.owner.serverLevel().getEntity(ids.get(62));
            tail.setPos(before.position().add(0, 30, 0));
            tail.tick();
            helper.assertTrue(tail.isRecallPending() && tail.recallToOwner(), "A broken link still recalls");
            helper.assertTrue(Math.abs(tail.position().distanceTo(before.position()) - 1.0) < 1e-7,
                    "Recall restores the slot behind its predecessor");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedTypesFollowAdmissionOrderAndReconnectAfterMiddleRemoval(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOBBY);
            helper.assertTrue(first.getFormationIndex() == 0, "First baby returns in slot zero");
            var middle = f.add(OTHER);
            helper.assertTrue(middle.getFormationIndex() == 1, "A different baby type returns in the next global slot");
            var last = f.add(BOBBY);
            helper.assertTrue(last.getFormationIndex() == 2, "The second Bobby returns in the third global slot");
            FamiliarChain.link(f.owner, last);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(first.getFormationIndex() == 0 && middle.getFormationIndex() == 1 && last.getFormationIndex() == 2,
                    "Slots span types rather than restarting at each type");
            middle.setPos(f.hover().add(6, 0, 0));
            last.setPos(f.hover().add(1, 0, 0));
            f.advance();
            var link = FamiliarChain.link(f.owner, last);
            helper.assertTrue(link.target().equals(middle.position()), "The last baby targets the prior slot, not the player");
            last.tick();
            helper.assertTrue(last.getDeltaMovement().x > 0, "Bobby actually moves toward the preceding other baby");
            middle.discardAndUnlink();
            f.data.addCount(OTHER, -1);
            helper.assertTrue(FamiliarChain.link(f.owner, last).slot() == 1
                    && FamiliarChain.link(f.owner, last).target().equals(first.position()), "Removal closes the gap immediately");
            var appended = f.add(OTHER);
            helper.assertTrue(FamiliarChain.link(f.owner, appended).slot() == 2
                    && FamiliarChain.link(f.owner, appended).target().equals(last.position()), "A new baby joins at the end");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void immediateSpawnsAlsoReturnTheirGlobalSlots(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = FamiliarHelper.spawnFamiliars(f.owner, BOBBY, f.hover()).get(0);
            var second = FamiliarHelper.spawnFamiliars(f.owner, OTHER, f.hover().add(0, 0, -1.5)).get(0);
            var third = FamiliarHelper.spawnFamiliars(f.owner, BOBBY, f.hover().add(0, 0, -3)).get(0);
            helper.assertTrue(first.getFormationIndex() == 0 && second.getFormationIndex() == 1 && third.getFormationIndex() == 2,
                    "Immediate explicit-position admission uses the same cross-type slot order");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void replacingAMemberInTheSameTickInvalidatesTheRosterSnapshot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOBBY);
            var middle = f.add(OTHER);
            var last = f.add(BOBBY);
            FamiliarChain.link(f.owner, last);
            first.discardAndUnlink();
            var replacement = ModEntities.BROTHER_BOBBY.get().create(f.owner.serverLevel());
            replacement.setOwner(f.owner);
            replacement.setPos(f.hover().add(2, 0, 0));
            f.data.registerEntity(BOBBY, replacement.getUUID());
            helper.assertTrue(f.owner.serverLevel().addFreshEntity(replacement), "Replacement enters the level");
            helper.assertTrue(FamiliarChain.link(f.owner, middle).slot() == 0
                    && FamiliarChain.link(f.owner, last).slot() == 1
                    && FamiliarChain.link(f.owner, replacement).slot() == 2, "Same-size roster replacement refreshes slot order");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void unlinkedNonBabyAndOtherOwnerEntitiesNeverBecomePredecessors(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); Fixture other = new Fixture(helper)) {
            f.add(BOBBY);
            f.add(ModEntities.MOM_KNIFE.getId());
            var second = f.add(BOBBY);
            other.owner.setPos(other.owner.position().add(10, 0, 0));
            var isolated = other.add(BOBBY);
            helper.assertTrue(FamiliarChain.link(f.owner, second).slot() == 1, "Non-baby knife does not consume a baby slot");
            helper.assertTrue(FamiliarChain.link(other.owner, isolated).slot() == 0
                    && FamiliarChain.link(other.owner, isolated).target().equals(other.hover()), "Each owner has an independent chain");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final GameTestHelper helper;
        final long originalTime;
        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "baby-chain"));
            BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
            owner.setPos(pos.getX() + 0.5, 220, pos.getZ() + 0.5);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }
        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        AbstractIsaacFamiliarEntity add(ResourceLocation type) {
            data.addCount(type, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Spawn " + type
                    + " count=" + data.getCount(type) + " effective=" + data.getEffectiveCount(type)
                    + " descriptor=" + FamiliarHelper.getFamiliarType(type));
            UUID last = data.getEntityIdsInAdmissionOrder().stream().reduce((a, b) -> b).orElseThrow();
            return (AbstractIsaacFamiliarEntity) owner.serverLevel().getEntity(last);
        }
        void advance() { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + 1); }
        @Override public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            owner.discard();
            helper.getLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
        }
    }
}
