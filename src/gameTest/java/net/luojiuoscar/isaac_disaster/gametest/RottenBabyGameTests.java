package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.rotten_baby.RottenBabyEntity;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_rotten_baby")
@PrefixGameTestTemplate(false)
public final class RottenBabyGameTests {
    private static final ResourceLocation ROTTEN = id("rotten_baby");
    private static final ResourceLocation FLY = id("blue_fly");
    private static final ResourceLocation COMMAND = id("command");
    private static final Vec3 AIM = new Vec3(0, 0, 1);

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("isaac_disaster", path); }

    @GameTest(template = "trajectory_empty")
    public static void parentsShareTheirEffectiveQuotaAsAGenerationGate(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.addBaby();
            var second = f.addBaby();
            helper.assertTrue(first.attackInDirection(AIM) && second.attackInDirection(AIM), "两个宝宝各成功生成一只");
            helper.assertTrue(f.data.getCount(FLY, ROTTEN) == 2 && f.data.getEntityCount(FLY, ROTTEN) == 2, "共享同一来源计数");
            f.advance(20);
            helper.assertTrue(!first.attackInDirection(AIM) && !second.attackInDirection(AIM), "来源满额时不再生成");
            var consumed = f.flies().get(0);
            f.consume(consumed);
            helper.assertTrue(first.attackInDirection(AIM), "释放来源数量后立即可重试，满额失败不消耗冷却");
            helper.assertTrue(!second.attackInDirection(AIM), "后一宝宝不能超出共享生成门槛");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldInputRunsEveryTwentyTicksAndReleaseStopsNewGeneration(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.addBaby();
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.held(true);
            List<Integer> fired = new ArrayList<>();
            for (int tick = 0; tick <= 40; tick++) {
                if (tick > 0) f.advance(1);
                baby.tick(); baby.tick();
                if (!f.flies().isEmpty()) {
                    fired.add(tick);
                    var fly = f.flies().get(0);
                    helper.assertTrue(fly.position().distanceTo(f.head(baby)) < 1e-7, "苍蝇从中立头部位置生成，没有前向偏移");
                    helper.assertTrue(baby.getAttackAge(0) == 0, "成功生成才启动动作");
                    f.consume(fly);
                }
            }
            helper.assertTrue(fired.equals(List.of(0, 20, 40)), "独立二十 tick 节奏且同 tick 去重：" + fired);
            f.held(false); f.advance(20); baby.tick();
            helper.assertTrue(f.flies().isEmpty() && baby.getAttackAge(0) < 0, "松开后不生成并回待机");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void rejectedWorldJoinLeavesNoContributionActionOrCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.addBaby();
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof BlueFlyEntity fly && fly.getOwner() == f.owner) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(reject);
            try {
                helper.assertTrue(!baby.attackInDirection(AIM) && f.data.getCount(FLY, ROTTEN) == 0
                        && f.flies().isEmpty() && baby.getAttackAge(0) < 0, "世界拒绝完整回滚");
            } finally { MinecraftForge.EVENT_BUS.unregister(reject); }
            helper.assertTrue(baby.attackInDirection(AIM), "失败后同 tick 可重试");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void fullTypeCapacityDoesNotConsumeCooldownOrAnotherSourcesContribution(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.addBaby();
            f.data.setCount(FLY, COMMAND, 64);
            helper.assertTrue(!baby.attackInDirection(AIM) && baby.getAttackAge(0) < 0, "类型满额时没有虚假动作");
            helper.assertTrue(f.data.getCount(FLY, COMMAND) == 64 && f.data.getCount(FLY, ROTTEN) == 0, "不挤掉其他来源");
            f.data.addCount(FLY, COMMAND, -1);
            helper.assertTrue(baby.attackInDirection(AIM) && f.data.getCount(FLY, COMMAND) == 63, "腾位后立即可生成");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void reducingAndRemovingParentsPreservesExistingIndependentFlies(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.addBaby(); var second = f.addBaby();
            first.attackInDirection(AIM); second.attackInDirection(AIM);
            var original = f.flies();
            f.data.setCount(ROTTEN, COMMAND, 1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            f.advance(20);
            helper.assertTrue(!first.attackInDirection(AIM) && f.data.getCount(FLY, ROTTEN) == 2, "缩小门槛只停止新增");
            f.data.setCount(ROTTEN, COMMAND, 0);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(original.stream().noneMatch(Entity::isRemoved)
                    && original.stream().allMatch(entity -> f.data.containsEntity(FLY, entity.getUUID()))
                    && f.data.getCount(FLY, ROTTEN) == 2, "移除全部宝宝仍保留来源苍蝇及其记录");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void waitingParentsUseEffectiveQuotaAndQuotaLossDoesNotRemoveChildren(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(id("brother_bobby"), COMMAND, 63);
            f.data.setCount(ROTTEN, COMMAND, 100);
            helper.assertTrue(f.data.getEffectiveCount(ROTTEN) == 1, "一百个记录只有一个有效配额");
            RottenBabyEntity baby = (RottenBabyEntity) FamiliarHelper.spawnFamiliars(f.owner,
                    ROTTEN, COMMAND, f.hover()).stream().findFirst().orElse(null);
            helper.assertTrue(baby == null, "即时生成不能越过已满宝宝配额");
            for (int i = 0; i < 2; i++) FamiliarHelper.spawnNextMissingFamiliar(f.owner);
            baby = (RottenBabyEntity) f.owner.serverLevel().getEntity(f.data.getEntry(ROTTEN).orElseThrow().getEntityId(0));
            helper.assertTrue(baby.attackInDirection(AIM), "有效宝宝可生成一只");
            f.advance(20);
            helper.assertTrue(!baby.attackInDirection(AIM), "按有效配额而非一百个等待需求限制生成");
            f.data.setCount(ROTTEN, COMMAND, 0);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.data.getCount(FLY, ROTTEN) == 1 && !f.flies().get(0).isRemoved(), "有效配额归零不撤销苍蝇");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void sourceReplacementWorksEvenWhenTotalCountDoesNotChange(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var old = FamiliarHelper.spawnFamiliars(f.owner, FLY, COMMAND, f.hover()).get(0);
            f.data.setCount(FLY, COMMAND, 0);
            f.data.setCount(FLY, ROTTEN, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "同总数也替换来源实体");
            helper.assertTrue(old.isRemoved() && f.data.getEntityCount(FLY, COMMAND) == 0
                    && f.data.getEntityCount(FLY, ROTTEN) == 1, "旧来源清理且新来源补出");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallClearsActionButKeepsRemainingCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.addBaby();
            helper.assertTrue(baby.attackInDirection(AIM), "首次生成成功");
            f.consume(f.flies().get(0));
            f.advance(3); baby.setPos(f.hover().add(0, 40, 0)); baby.tick();
            helper.assertTrue(baby.isRecallPending() && baby.getAttackAge(0) < 0 && !baby.attackInDirection(AIM), "召回期间停止动作与生成");
            helper.assertTrue(baby.recallToOwner(), "召回成功");
            f.advance(16);
            helper.assertTrue(!baby.attackInDirection(AIM), "原冷却仍剩一 tick");
            f.advance(1);
            helper.assertTrue(baby.attackInDirection(AIM), "原二十 tick 冷却结束");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void fliesUseLiveOwnerDamageAndConsumeOnlyTheirSourceOnRealContact(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.addBaby(); baby.attackInDirection(AIM);
            var fly = f.flies().get(0);
            f.data.setCount(FLY, COMMAND, 2);
            var target = EntityType.ZOMBIE.create(f.owner.serverLevel());
            target.setNoAi(true); target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0); target.setHealth(100);
            target.setPos(fly.position().add(0, -target.getBbHeight() / 2, 0));
            f.owner.serverLevel().addFreshEntity(target);
            try {
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
                fly.tick();
                helper.assertTrue(fly.isRemoved() && Math.abs(target.getHealth() - 86) < 0.001, "真实接触按命中时玩家伤害两倍计算");
                helper.assertTrue(f.data.getCount(FLY, ROTTEN) == 0 && f.data.getCount(FLY, COMMAND) == 2, "只消费腐烂来源数量");
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosDuplicatesAndAbsorbedRemovalLeaveSpawnedFliesAlive(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(4); f.tickCurios();
            var item = ModPassiveItems.ROTTEN_BABY.get();
            helper.assertTrue(ItemId.ROTTEN_BABY.getLevel() == 3 && f.slots().isItemValid(0, new ItemStack(item)), "品质三且生成标签支持 Curios");
            f.slots().setStackInSlot(0, new ItemStack(item)); f.slots().setStackInSlot(1, new ItemStack(item));
            f.tickCurios(); f.tickCurios();
            helper.assertTrue(f.data.getCount(ROTTEN, ROTTEN) == 2 && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "两份装备独立计数");
            FamiliarHelper.spawnNextMissingFamiliar(f.owner);
            var baby = (RottenBabyEntity) f.owner.serverLevel().getEntity(f.data.getEntry(ROTTEN).orElseThrow().getEntityId(0));
            baby.attackInDirection(AIM); var fly = f.flies().get(0);
            f.slots().setStackInSlot(0, ItemStack.EMPTY); f.slots().setStackInSlot(1, ItemStack.EMPTY); f.tickCurios();
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(f.data.getCount(ROTTEN) == 0 && f.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 0
                    && !fly.isRemoved() && f.data.getCount(FLY, ROTTEN) == 1, "卸下只撤销宝宝及套装计数");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            ((PassiveItem) item).use(f.owner.level(), f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(f.data.getCount(ROTTEN) == 1, "吸收也增加一份宝宝");
            f.items.clearPlayerPassiveItems(f.owner); FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(!fly.isRemoved() && f.data.getCount(FLY, ROTTEN) == 1, "清空吸收道具仍保留苍蝇");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void saveReloadAndDeathCopyKeepIndependentFlySourcesWithoutParents(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper); Fixture clone = new Fixture(helper)) {
            original.data.setCount(FLY, ROTTEN, 2);
            original.data.setCount(FLY, COMMAND, 1);
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
            ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, original.owner, true)); original.owner.reviveCaps();
            for (var f : List.of(restored, clone)) {
                helper.assertTrue(f.data.getCount(ROTTEN) == 0 && f.data.getCount(FLY, ROTTEN) == 2
                        && f.data.getCount(FLY, COMMAND) == 1, "无宝宝也保存并恢复独立来源贡献");
                for (int i = 0; i < 3; i++) FamiliarHelper.spawnNextMissingFamiliar(f.owner);
                helper.assertTrue(f.data.getEntityCount(FLY, ROTTEN) == 2 && f.data.getEntityCount(FLY, COMMAND) == 1, "重建正确实体来源");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsScalesTheBabyAndHiveMindEnhancesOnlyTheGeneratedFly(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (var item : List.of(ModPassiveItems.BFFS.get(), ModPassiveItems.HIVE_MIND.get())) {
                f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
                ((PassiveItem) item).use(f.owner.level(), f.owner, InteractionHand.MAIN_HAND);
            }
            var baby = f.addBaby();
            helper.assertTrue(baby.attackInDirection(AIM), "增强后的宝宝仍按原配额生成");
            var fly = f.flies().get(0);
            helper.assertTrue(Math.abs(baby.getVisualScale() - 1.25F) < 1e-7
                    && Math.abs(fly.getVisualScale() - 1.5F) < 1e-7
                    && baby.getEnhancement() == 1 && fly.getEnhancement() == 1,
                    "BFFS 放大宝宝；苍蝇仅获得 Hive Mind，不叠加父宝宝增强");
            helper.assertTrue(fly.position().distanceTo(f.head(baby)) < 1e-7, "放大后的中立头部位置用于生成");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualDimensionTransferRestoresFlySourcesWithoutRequiringTheParent(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.addBaby();
            helper.assertTrue(baby.attackInDirection(AIM) && !f.flies().isEmpty(), "跨维度前成功生成可查询的苍蝇");
            var fly = f.flies().get(0);
            f.data.setCount(ROTTEN, COMMAND, 0);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            var home = f.owner.serverLevel();
            var destination = home.getServer().getLevel(Level.NETHER);
            Vec3 original = f.owner.position();
            List<BlueFlyEntity> entered = new ArrayList<>();
            Consumer<EntityJoinLevelEvent> capture = event -> {
                if (event.getLevel() == destination && event.getEntity() instanceof BlueFlyEntity candidate
                        && candidate.getOwner() == f.owner) entered.add(candidate);
            };
            MinecraftForge.EVENT_BUS.addListener(capture);
            try {
                f.owner.teleportTo(destination, original.x, original.y, original.z, 0, 0);
                FamiliarHelper.maintainExistingFamiliars(f.owner);
                if (!destination.hasChunkAt(f.owner.blockPosition())) {
                    helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner)
                            && f.data.getEntityCount(FLY, ROTTEN) == 0,
                            "目标区块未加载时推迟补出，不登记不可见的实体");
                }
                destination.getChunkAt(f.owner.blockPosition());
                helper.assertTrue(f.data.getCount(FLY, ROTTEN) == 1
                        && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "跨维度重建来源苍蝇，不依赖父宝宝");
                helper.assertTrue(entered.size() == 1, "目标维度收到一次真实实体加入事件");
                var replacement = entered.get(0);
                helper.assertTrue(replacement.level() == destination && !replacement.getUUID().equals(fly.getUUID())
                        && f.data.containsEntity(FLY, replacement.getUUID()), "新维度重新建立实体名册");
                replacement.discardAndUnlink();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(capture);
                entered.forEach(AbstractIsaacFamiliarEntity::discardAndUnlink);
                f.owner.teleportTo(home, original.x, original.y, original.z, 0, 0);
                fly.discardSilently();
            }
            helper.assertTrue(f.data.getCount(FLY, ROTTEN) == 1
                    && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "返回原维度仍保留独立来源");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final PlayerIsaacItems items;
        final long originalTime;
        final GameTestDatapackSettings.Scope settings;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            settings = GameTestDatapackSettings.override(helper);
            settings.set(GameplayConfig.EXTRA_PASSIVE_ITEM_BACKPACK, true);
            settings.set(GameplayConfig.PASSIVE_ITEM_LIMIT, 256);
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "rotten-test"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(2, 220, 2));
            DefaultAttributeManager.apply(owner);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
        }

        RottenBabyEntity addBaby() {
            data.addCount(ROTTEN, COMMAND, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "调度器生成腐烂宝宝");
            var entry = data.getEntry(ROTTEN).orElseThrow();
            return (RottenBabyEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        List<AbstractIsaacFamiliarEntity> flies() {
            var entry = data.getEntry(FLY).orElse(null);
            if (entry == null) return List.of();
            List<AbstractIsaacFamiliarEntity> result = new ArrayList<>();
            for (UUID uuid : entry.getEntityIds()) {
                Entity entity = owner.serverLevel().getEntity(uuid);
                if (entity instanceof AbstractIsaacFamiliarEntity fly) result.add(fly);
            }
            return result;
        }

        void consume(AbstractIsaacFamiliarEntity fly) { data.consumeEntity(FLY, fly.getUUID()); fly.discardSilently(); }
        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        Vec3 head(RottenBabyEntity baby) { return baby.position().add(0, 5.5 / 16 * baby.getBaseModelScale() * baby.getVisualScale(), 0); }
        void advance(int ticks) { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks); }
        void held(boolean held) { owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(a -> a.setHoldRightClick(held)); }
        void tickCurios() { CurioSlotManager.tick(owner); new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(owner)); CurioSlotManager.tick(owner); }
        IDynamicStackHandler slots() { return CuriosApi.getCuriosInventory(owner).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks(); }

        @Override public void close() {
            var slot = CuriosApi.getCuriosInventory(owner).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM);
            if (slot.isPresent()) { for (int i = 0; i < slot.get().getStacks().getSlots(); i++) slot.get().getStacks().setStackInSlot(i, ItemStack.EMPTY); tickCurios(); }
            items.clearPlayerPassiveItems(owner);
            FamiliarHelper.discardAllRuntimeFamiliars(owner); data.clearRequirements(); owner.discard();
            helper.getLevel().getServer().getWorldData().overworldData().setGameTime(originalTime); settings.close();
        }
    }
}
