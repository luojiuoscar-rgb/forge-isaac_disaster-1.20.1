package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.event.FamiliarEvents;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("isaac_disaster_harlequin_baby")
@PrefixGameTestTemplate(false)
public final class HarlequinBabyGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    private static final AtomicInteger FIXTURE = new AtomicInteger();
    private static final ResourceLocation HARLEQUIN = id("harlequin_baby");
    private static final ResourceLocation BOBBY = id("brother_bobby");
    private static final ResourceLocation MAGGY = id("sister_maggy");
    private static final ResourceLocation GHOST = id("ghost_baby");

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name); }

    @GameTest(template = "trajectory_empty")
    public static void allFourBabyItemsAndTheNewFamiliarAreRegistered(GameTestHelper helper) {
        for (String name : List.of("brother_bobby", "sister_maggy", "ghost_baby", "harlequin_baby")) {
            var id = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name);
            helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id), "Missing baby passive item: " + name);
            helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(id), "Missing baby familiar: " + name);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void babyRequirementsWaitAboveTheSharedCapAndRefillFreedSlots(GameTestHelper helper) {
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "baby-overflow"));
        var data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        var bobby = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "brother_bobby");
        var ghost = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "ghost_baby");
        try {
            data.setCount(bobby, FAMILIAR_SOURCE, 64);
            data.setCount(ghost, FAMILIAR_SOURCE, 100);
            helper.assertTrue(data.getCount(ghost) == 100 && data.getEffectiveCount(ghost) == 0,
                    "Full capacity must preserve waiting baby requests");
            data.addCount(bobby, FAMILIAR_SOURCE, -1);
            helper.assertTrue(data.getEffectiveCount(bobby) == 63 && data.getEffectiveCount(ghost) == 1,
                    "Freeing one slot activates a waiting ghost");
            data.addCount(ghost, FAMILIAR_SOURCE, -1);
            helper.assertTrue(data.getCount(ghost) == 99 && data.getEffectiveCount(ghost) == 1,
                    "Removing a waiting copy cannot remove an admitted baby");
        } finally { data.clearRequirements(); owner.discard(); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void volleysKeepSymmetricFifteenDegreeSpreadAtEveryPitchAndSnapshotStats(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(HARLEQUIN);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(9);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ability.setPiercing(1); ability.setSpectral(1); ability.setHoming(1);
            int index = 0;
            for (Vec3 aim : List.of(new Vec3(0, 0, 1), new Vec3(1, 2, 3).normalize(), new Vec3(0, 1, 0), new Vec3(0, -1, 0))) {
                double configured = new double[]{20, 2, 100, 20}[index++];
                f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured);
                f.advance(15);
                helper.assertTrue(baby.attackInDirection(aim), "A complete volley fires at every pitch");
                var all = f.shots(baby);
                var pair = all.subList(all.size() - 2, all.size());
                helper.assertTrue(pair.get(0).position().distanceTo(f.head(baby)) < 1e-7
                                && pair.get(0).position().equals(pair.get(1).position()), "Both tears start at the neutral head without forward offsets");
                helper.assertTrue(pair.get(0).velocity().add(pair.get(1).velocity()).normalize().distanceTo(aim) < 1e-7,
                        "The two directions remain centered on the aim, including vertical aim");
                for (BulletState shot : pair) {
                    helper.assertTrue(Math.abs(shot.velocity().length() - 1) < 1e-7
                            && Math.abs(shot.velocity().dot(aim) - Math.cos(Math.PI / 12)) < 1e-7,
                            "Each tear travels at unit speed fifteen degrees from the aim");
                    helper.assertTrue(shot.getDamage() == 3 * StatManager.DAMAGE.getBonus()
                            && shot.getRange() == Math.max(12, 1.5 * configured) && shot.noGravity(), "The volley snapshots configured baby stats only");
                    helper.assertTrue(!shot.isHoming() && !shot.isPiercing() && !shot.isSpectral()
                            && shot.getAttackContext() == null && shot.getSplitSequence().isEmpty(), "Player effects cannot leak into the volley");
                }
            }
            BulletState old = f.shots(baby).get(0);
            double damage = old.getDamage();
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            f.advance(15);
            baby.attackInDirection(new Vec3(0, 0, 1));
            helper.assertTrue(f.shots(baby).get(8).getDamage() == 2 * damage && f.shots(baby).get(9).getDamage() == 2 * damage
                    && old.getDamage() == damage, "Both later tears receive enhancement without modifying earlier shots");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void wholeVolleyCapacityFailureAnimationAndRecallKeepOneCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(HARLEQUIN);
            Vec3 aim = new Vec3(0, 0, 1);
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 1);
            helper.assertTrue(!baby.attackInDirection(aim) && f.shots(baby).isEmpty() && baby.getAttackAge(0) < 0,
                    "One free slot cannot produce half a volley or a false animation");
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 2);
            helper.assertTrue(baby.attackInDirection(aim) && f.shots(baby).size() == 2 && baby.getAttackAge(0) == 0,
                    "Failure consumes no cooldown and both tears start one action");
            helper.assertTrue(!baby.attackInDirection(aim), "The successful volley is deduplicated in the same tick");
            f.advance(5);
            var mirror = (AbstractTearBabyFamiliarEntity) baby.getType().create(helper.getLevel());
            mirror.getEntityData().assignValues(baby.getEntityData().getNonDefaultValues());
            helper.assertTrue(mirror.getAttackAge(0) == 5, "New tracking resumes the existing action phase");
            f.advance(5);
            helper.assertTrue(baby.getAttackAge(0) < 0 && mirror.getAttackAge(0) < 0, "The action ends after ten ticks");
            f.advance(5);
            helper.assertTrue(!baby.attackInDirection(aim) && f.shots(baby).size() == 2, "An exhausted manager rejects the entire next volley");
            f.clearShots();
            helper.assertTrue(baby.attackInDirection(aim), "A failed ready volley can retry immediately once capacity is freed");
            f.advance(3);
            baby.setPos(f.hover().add(0, 40, 0)); baby.tick();
            helper.assertTrue(baby.isRecallPending() && baby.getAttackAge(0) < 0 && !baby.attackInDirection(aim), "Recall clears action and suppresses attacks");
            helper.assertTrue(baby.recallToOwner(), "The existing entity recalls");
            f.clearShots(); f.advance(11);
            helper.assertTrue(!baby.attackInDirection(aim), "Recall does not refresh the remaining fifteen-tick cooldown");
            f.advance(1);
            helper.assertTrue(baby.attackInDirection(aim), "The original cooldown expires normally");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldInputFiresPairsEveryFifteenTicksWhileMovingAndStopsOnRelease(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(HARLEQUIN);
            baby.setPos(f.hover().add(-5, 0, 0));
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.held(true);
            List<Integer> fired = new ArrayList<>();
            for (int tick = 0; tick <= 30; tick++) {
                if (tick > 0) f.advance(1);
                f.owner.setPos(f.owner.position().add(0.03, 0, 0));
                int before = f.shots(baby).size(); baby.tick(); baby.tick();
                if (f.shots(baby).size() > before) {
                    helper.assertTrue(f.shots(baby).size() == before + 2, "Every successful action has exactly two tears");
                    fired.add(tick);
                }
            }
            helper.assertTrue(fired.equals(List.of(0, 15, 30)), "Independent held cadence is fifteen ticks per pair");
            f.held(false); f.advance(15); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 6, "Release stops ready fire while following");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bothTearsActuallyHitAlongTheirSeparateDirections(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(HARLEQUIN);
            baby.attackInDirection(new Vec3(0, 0, 1));
            var pair = f.shots(baby);
            Zombie left = f.enemy(pair.get(0).position().add(pair.get(0).velocity().scale(5)));
            Zombie right = f.enemy(pair.get(1).position().add(pair.get(1).velocity().scale(5)));
            try {
                helper.assertTrue(pair.stream().allMatch(s -> s.getOwner() == f.owner && s.getShooter() == baby), "Each tear credits the player and records the baby shooter");
                for (int tick = 0; tick < 7; tick++) BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel());
                double expected = 100 - 3 * StatManager.DAMAGE.getBonus();
                helper.assertTrue(Math.abs(left.getHealth() - expected) < 0.001 && Math.abs(right.getHealth() - expected) < 0.001,
                        "Both targets take the full per-tear damage");
                helper.assertTrue(f.shots(baby).isEmpty(), "Each non-piercing tear is consumed on its hit");
            } finally { left.discard(); right.discard(); }
        }
        helper.succeed();
    }

    private static void tickCurios(ServerPlayer player) {
        CurioSlotManager.tick(player);
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    @GameTest(template = "trajectory_empty")
    public static void duplicateCopiesOfEveryBabyItemCountAndRemoveIndependently(GameTestHelper helper) {
        for (var type : List.of(BOBBY, MAGGY, GHOST, HARLEQUIN)) {
            try (Fixture f = new Fixture(helper)) {
                f.absorb(type); f.absorb(type); f.absorb(type);
                helper.assertTrue(f.data.getCount(type) == 3
                                && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3
                                && f.items.isObtainedSet(ModSetAbilities.CONJOINED.get().getId()),
                        "同一种宝宝的三份道具各计一次套装进度：" + type);
                for (int remaining = 2; remaining >= 0; remaining--) {
                    f.items.removeFromId(f.owner, type);
                    helper.assertTrue(f.data.getCount(type) == remaining
                                    && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == remaining,
                            "每移除一份道具都扣除一个跟班需求和一点套装进度：" + type);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void absorbedCopiesGrantBabiesAndEachContributesToConjoined(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.absorb(BOBBY); f.absorb(BOBBY); f.absorb(BOBBY);
            helper.assertTrue(f.data.getCount(BOBBY) == 3 && f.items.getItemCountFromAll(BOBBY) == 3
                    && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3
                    && f.items.isObtainedSet(ModSetAbilities.CONJOINED.get().getId()),
                    "三份相同道具生成三个宝宝，并达到连体婴的三份要求");
            f.absorb(MAGGY); f.absorb(GHOST); f.absorb(HARLEQUIN);
            helper.assertTrue(f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 6,
                    "不同宝宝道具的每份副本也各增加一次套装计数");
            for (int i = 0; i < 6; i++) helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "The scheduler restores an item-granted baby");
            f.items.removeFromId(f.owner, BOBBY); f.items.removeFromId(f.owner, BOBBY);
            helper.assertTrue(f.data.getCount(BOBBY) == 1 && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 4,
                    "移除两份重复道具后，套装计数从六降到四，波比保留一个");
            f.items.removeFromId(f.owner, BOBBY);
            helper.assertTrue(f.data.getCount(BOBBY) == 0 && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3,
                    "移除第三份波比后，套装计数继续减少到三");
            f.data.addCount(BOBBY, FAMILIAR_SOURCE, 4);
            helper.assertTrue(f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "Command-granted babies do not count as collectibles");
            f.items.clearPlayerPassiveItems(f.owner);
            helper.assertTrue(f.data.getCount(BOBBY) == 4 && f.data.getCount(MAGGY) == 0 && f.data.getCount(GHOST) == 0
                    && f.data.getCount(HARLEQUIN) == 0 && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 0,
                    "清空被动道具移除各份需求和套装计数，保留命令添加的跟班");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void removingBabyItemsLeavesEntityCleanupToThePlayerTickScheduler(GameTestHelper helper) {
        for (var type : List.of(BOBBY, MAGGY, GHOST, HARLEQUIN)) {
            try (Fixture f = new Fixture(helper)) {
                f.absorb(type); f.absorb(type);
                f.owner.tickCount = 1;
                FamiliarEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
                f.advance(1); f.owner.tickCount++;
                FamiliarEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
                var entry = f.data.getEntry(type).orElseThrow();
                helper.assertTrue(entry.getEntityCount() == 2, "正常玩家 tick 按需求补出两个宝宝：" + type);
                var first = f.owner.serverLevel().getEntity(entry.getEntityId(0));
                var last = f.owner.serverLevel().getEntity(entry.getEntityId(1));

                f.items.removeFromId(f.owner, type);
                helper.assertTrue(f.data.getCount(type) == 1 && entry.getEntityCount() == 2 && !last.isRemoved(),
                        "道具移除只减少需求，不直接销毁实体：" + type);
                f.advance(1); f.owner.tickCount++;
                FamiliarEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
                helper.assertTrue(last.isRemoved() && !first.isRemoved() && entry.getEntityCount() == 1,
                        "下一玩家 tick 清理超额宝宝并保留较早加入的实体：" + type);

                f.items.clearPlayerPassiveItems(f.owner);
                helper.assertTrue(f.data.getCount(type) == 0 && !first.isRemoved(),
                        "清空道具也只撤销需求：" + type);
                f.advance(1); f.owner.tickCount++;
                FamiliarEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
                helper.assertTrue(first.isRemoved() && f.data.getEntry(type).isEmpty(),
                        "调度器移除最后一个宝宝并清理空记录：" + type);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void removingAnUnallocatedItemPreservesOtherBabiesAndFreedCapacityRefills(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BOBBY, FAMILIAR_SOURCE, 64);
            f.absorb(GHOST); f.absorb(GHOST);
            helper.assertTrue(f.data.getCount(GHOST) == 2 && f.data.getEffectiveCount(GHOST) == 0, "Full capacity retains both item requests");
            f.items.removeFromId(f.owner, GHOST);
            helper.assertTrue(f.data.getCount(GHOST) == 1 && f.data.getEffectiveCount(BOBBY) == 64,
                    "Removing a waiting copy cannot subtract an unrelated granted baby");
            f.data.addCount(BOBBY, FAMILIAR_SOURCE, -1);
            helper.assertTrue(f.data.getEffectiveCount(GHOST) == 1 && f.data.getEffectiveCount(BOBBY) == 63,
                    "A released slot immediately grants the waiting type quota");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner) && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "The normal scheduler fills missing requests");
            var ghostEntry = f.data.getEntry(GHOST).orElseThrow();
            helper.assertTrue(ghostEntry.getEntityCount() == 1, "A waiting ghost is actually spawned");
            var ghost = f.owner.serverLevel().getEntity(ghostEntry.getEntityId(0));
            f.items.removeFromId(f.owner, GHOST);
            helper.assertTrue(!ghost.isRemoved(), "撤销最后一份需求后，实体等待调度器清理");
            f.advance(1); f.owner.tickCount = 1;
            FamiliarEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, f.owner));
            helper.assertTrue(ghost.isRemoved() && f.data.getCount(GHOST) == 0 && f.data.getCount(BOBBY) == 63,
                    "调度器清理已撤销需求的实体，保留其他类型的需求");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosAndFullPlayerSaveReloadPreserveBabyAndSetCounts(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(8);
            tickCurios(original.owner);
            for (var type : List.of(BOBBY, MAGGY, GHOST, HARLEQUIN)) {
                helper.assertTrue(original.slots().isItemValid(0, new ItemStack(ForgeRegistries.ITEMS.getValue(type))),
                        "Generated passive tags admit the baby item into a real Curios slot: " + type);
            }
            original.slots().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(BOBBY)));
            original.slots().setStackInSlot(1, new ItemStack(ForgeRegistries.ITEMS.getValue(BOBBY)));
            original.slots().setStackInSlot(2, new ItemStack(ForgeRegistries.ITEMS.getValue(MAGGY)));
            original.slots().setStackInSlot(3, new ItemStack(ForgeRegistries.ITEMS.getValue(HARLEQUIN)));
            tickCurios(original.owner); tickCurios(original.owner);
            helper.assertTrue(original.data.getCount(BOBBY) == 2 && original.data.getCount(MAGGY) == 1
                    && original.data.getCount(HARLEQUIN) == 1 && original.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 4,
                    "实际装备的四份道具各增加一次跟班需求和套装计数");
            original.data.setCount(GHOST, FAMILIAR_SOURCE, 100);
            CompoundTag saved = original.owner.saveWithoutId(new CompoundTag());
            restored.owner.load(saved);
            DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner); tickCurios(restored.owner); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(BOBBY) == 2 && restored.data.getCount(MAGGY) == 1
                    && restored.data.getCount(HARLEQUIN) == 1 && restored.data.getCount(GHOST) == 100
                    && restored.data.getEffectiveCount(GHOST) == 60 && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 4,
                    "玩家重载保留需求和套装计数：bobby=" + restored.data.getCount(BOBBY)
                            + " maggy=" + restored.data.getCount(MAGGY) + " harlequin=" + restored.data.getCount(HARLEQUIN)
                            + " ghost=" + restored.data.getCount(GHOST) + "/" + restored.data.getEffectiveCount(GHOST)
                            + " set=" + restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()));
            restored.slots().setStackInSlot(0, ItemStack.EMPTY); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(BOBBY) == 1 && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3,
                    "卸下一份重载后的波比，套装计数从四降到三");
            restored.slots().setStackInSlot(1, ItemStack.EMPTY); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(BOBBY) == 0 && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2,
                    "再卸下一份波比，套装计数从三降到二");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void clearingAbsorbedItemsPreservesPhysicallyEquippedBabyCounts(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(4);
            tickCurios(f.owner);
            f.slots().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(BOBBY)));
            f.slots().setStackInSlot(1, new ItemStack(ForgeRegistries.ITEMS.getValue(MAGGY)));
            tickCurios(f.owner); tickCurios(f.owner);
            f.absorb(BOBBY); f.absorb(GHOST);
            f.items.clearPlayerPassiveItems(f.owner);
            helper.assertTrue(f.data.getCount(BOBBY) == 1 && f.data.getCount(MAGGY) == 1 && f.data.getCount(GHOST) == 0
                            && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2,
                    "清空吸收道具保留实际装备的两份宝宝需求和套装计数");
            f.slots().setStackInSlot(0, ItemStack.EMPTY); tickCurios(f.owner);
            f.slots().setStackInSlot(1, ItemStack.EMPTY); tickCurios(f.owner);
            helper.assertTrue(f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 0,
                    "卸下剩余的实际装备后，套装计数归零且不出现负数");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void removingDuplicatesImmediatelyAfterReloadSubtractsEachCopy(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            original.absorb(BOBBY); original.absorb(BOBBY); original.absorb(MAGGY); original.absorb(HARLEQUIN);
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
            helper.assertTrue(restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 4,
                    "首次库存 tick 之前，存档已恢复四份套装计数");
            restored.items.removeFromId(restored.owner, BOBBY);
            helper.assertTrue(restored.data.getCount(BOBBY) == 1
                            && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3,
                    "首次库存 tick 之前移除一份重复道具，套装计数减少一");
            restored.items.removeFromId(restored.owner, BOBBY);
            helper.assertTrue(restored.data.getCount(BOBBY) == 0
                            && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2,
                    "再移除一份重复道具，套装计数继续减少一");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualDeathCopyPreservesAbsorbedRequestsAndIndependentRemoval(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture clone = new Fixture(helper)) {
            original.absorb(BOBBY); original.absorb(BOBBY); original.absorb(GHOST); original.absorb(HARLEQUIN);
            original.data.setCount(MAGGY, FAMILIAR_SOURCE, 100);
            ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, original.owner, true));
            original.owner.reviveCaps();
            helper.assertTrue(clone.data.getCount(BOBBY) == 2 && clone.data.getCount(GHOST) == 1 && clone.data.getCount(HARLEQUIN) == 1
                    && clone.data.getCount(MAGGY) == 100 && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 4,
                    "死亡复制保留四份道具的套装计数及所有跟班需求");
            clone.items.removeFromId(clone.owner, BOBBY);
            helper.assertTrue(clone.data.getCount(BOBBY) == 1 && original.data.getCount(BOBBY) == 2
                            && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3
                            && original.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 4,
                    "从复制玩家移除一份道具只扣除其自身的需求和套装计数");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(clone.owner), "The clone's scheduler recreates entities from copied requests");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final PlayerIsaacItems items;
        final GameTestDatapackSettings.Scope settings;
        final long originalTime;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            settings = GameTestDatapackSettings.override(helper);
            settings.set(GameplayConfig.EXTRA_PASSIVE_ITEM_BACKPACK, true);
            settings.set(GameplayConfig.PASSIVE_ITEM_LIMIT, 256);
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "harlequin-test"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(2, 220 + 3 * FIXTURE.getAndIncrement(), 2));
            DefaultAttributeManager.apply(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
        }

        AbstractTearBabyFamiliarEntity add(ResourceLocation type) {
            data.addCount(type, FAMILIAR_SOURCE, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "The normal scheduler admits " + type);
            var entry = data.getEntry(type).orElseThrow();
            return (AbstractTearBabyFamiliarEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        void absorb(ResourceLocation type) {
            Item item = ForgeRegistries.ITEMS.getValue(type);
            helper.assertTrue(item instanceof PassiveItem, "Registered passive item " + type);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            ((PassiveItem) item).use(owner.level(), owner, InteractionHand.MAIN_HAND);
        }

        IDynamicStackHandler slots() {
            return CuriosApi.getCuriosInventory(owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
        }

        Zombie enemy(Vec3 center) {
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            target.setNoAi(true); target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setHealth(100); target.setPos(center.add(0, -target.getBbHeight() / 2, 0));
            helper.getLevel().addFreshEntity(target); return target;
        }

        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        Vec3 head(AbstractTearBabyFamiliarEntity baby) { return baby.position().add(0, 5.5 / 16 * 0.6 * baby.getVisualScale(), 0); }
        void advance(int ticks) { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks); }
        void held(boolean held) { owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(a -> a.setHoldRightClick(held)); }
        List<BulletState> shots(AbstractTearBabyFamiliarEntity baby) {
            return BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates().stream().filter(s -> s.getShooter() == baby).toList();
        }
        void clearShots() {
            for (var shot : List.copyOf(BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates()))
                if (shot.getOwner() == owner) BulletRuntime.INSTANCE.manager(owner.serverLevel()).remove(shot);
        }

        @Override public void close() {
            var slots = CuriosApi.getCuriosInventory(owner).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM);
            if (slots.isPresent()) {
                var stacks = slots.get().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) stacks.setStackInSlot(i, ItemStack.EMPTY);
                tickCurios(owner);
            }
            items.clearPlayerPassiveItems(owner);
            FamiliarHelper.discardAllRuntimeFamiliars(owner); data.clearRequirements(); clearShots(); owner.discard();
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
            settings.close();
        }
    }
}
