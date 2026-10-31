package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.AbstractLaserAttack;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_robo_baby")
@PrefixGameTestTemplate(false)
public final class RoboBabyGameTests {
    private static final ResourceLocation ROBO = id("robo_baby");
    private static final ResourceLocation COMMAND = id("command");
    private static final Vec3 AIM = new Vec3(0, 0, 1);
    private static final AtomicInteger FIXTURE = new AtomicInteger();
    private static final ResourceLocation WEAKENING = ResourceLocation.fromNamespaceAndPath("robo_test", "weakening");

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, path); }

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class RoboBabyTestRegistration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> registry.register(WEAKENING,
                    new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, -2,
                            AbstractBabyFamiliarEntity.class::isInstance)));
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void ownLaserSnapshotsConfigAndIgnoresPlayerProjectileProperties(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            helper.assertTrue(ForgeRegistries.ITEMS.getValue(ROBO) instanceof PassiveItem && ItemId.ROBO_BABY.getLevel() == 1
                    && FamiliarHelper.getFamiliarType(ROBO).getLimit() == 64, "品质1被动道具与64上限跟班正确注册");
            var baby = f.add(ROBO);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(9);
            f.owner.getAttribute(ModAttributes.BULLET_SCALE.get()).setBaseValue(40);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ability.setHoming(1); ability.setControllable(1);
            ability.addBulletColor(ModBulletColors.POISON.getId(), 1);
            f.enemy(f.head(baby).add(0, 0, 5));
            double[] configured = {2, 20, 50};
            double[] expected = {24, 40, 64};
            IBulletObject original = null;
            for (int i = 0; i < configured.length; i++) {
                f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured[i]);
                f.advance(15); f.lasers.clear();
                helper.assertTrue(baby.attackInDirection(AIM), "独立激光正常发射");
                helper.assertTrue(f.lasers.size() == 1, "一轮只有一条激光");
                var laser = f.lasers.iterator().next();
                var context = laser.getAttackContext();
                helper.assertTrue(laser.getRange() == expected[i] && context.getDamage() == 2 * StatManager.DAMAGE.getBonus(),
                        "配置射程使用24下限和64上限，伤害不读取玩家面板");
                helper.assertTrue(laser.getOwner() == f.owner && laser.getShooter() == baby
                        && context.getPos().distanceTo(f.head(baby)) < 1e-7 && context.getMainAxis().equals(AIM),
                        "玩家归属、宝宝射手、中立头部起点且无前移");
                helper.assertTrue(laser.isSpectral() && laser.isPiercing() && !laser.isHoming() && !laser.isControllable()
                        && laser.getColorId().equals(ModBulletColors.BASE.getId())
                        && ((AbstractLaserAttack.LaserProjectile) laser).width == 0.25
                        && context.getVisualIds().isEmpty() && context.getTrajectorySpecs().isEmpty()
                        && context.getSplitSequence().isEmpty() && context.usesFixedLaunchTransform()
                        && context.getRootTypeId().equals(ModAttackTypes.LASER.getId()),
                        "穿墙穿怪且隔离玩家颜色、追踪、大小、弹种及附加模块");
                if (i == 0) original = laser;
            }
            helper.assertTrue(original.getRange() == 24, "后续配置变化不修改已执行的快照");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void laserHitsThroughWallsAndSeveralEnemiesOnceButNeverFriendlyPets(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            Vec3 origin = f.head(baby);
            var near = f.enemy(origin.add(0, 0, 0.3));
            var far = f.enemy(origin.add(0, 0, 7));
            // FakePlayer is not registered in the level's player list, so resolve this pet's owner directly.
            Wolf pet = new Wolf(EntityType.WOLF, helper.getLevel()) {
                @Override public LivingEntity getOwner() { return f.owner; }
            };
            pet.tame(f.owner); pet.setNoAi(true); pet.setNoGravity(true);
            pet.setPos(origin.add(0, -pet.getBbHeight() / 2, 4));
            helper.getLevel().addFreshEntity(pet); f.targets.add(pet);
            float petHealth = pet.getHealth();
            BlockPos wall = BlockPos.containing(origin.add(0, 0, 3));
            var old = helper.getLevel().getBlockState(wall);
            try {
                helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                helper.assertTrue(baby.attackInDirection(AIM), "激光从头部立即执行");
                double expected = 100 - 2 * StatManager.DAMAGE.getBonus();
                helper.assertTrue(Math.abs(near.getHealth() - expected) < 0.001 && Math.abs(far.getHealth() - expected) < 0.001,
                        "近距离没有判定缺口，隔墙目标同样命中且每只只伤害一次");
                helper.assertTrue(pet.getHealth() == petHealth, "友方宠物不受伤");
            } finally { helper.getLevel().setBlockAndUpdate(wall, old); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void rangeBoundaryAndVerticalAimUseTheActualLaserSegment(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, 100.0);
            Vec3 origin = f.head(baby);
            var inside = f.enemy(origin.add(0, 0, 63));
            var outside = f.enemy(origin.add(0, 0, 65));
            helper.assertTrue(baby.attackInDirection(AIM), "高配置仍正常发射");
            helper.assertTrue(inside.getHealth() < 100 && outside.getHealth() == 100, "实际命中范围不超过64格");
            f.advance(15);
            var above = f.enemy(origin.add(0, 5, 0));
            helper.assertTrue(baby.attackInDirection(new Vec3(0, 1, 0)) && above.getHealth() < 100, "激光支持纯垂直瞄准");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldInputUsesFifteenTickCadenceAndReleaseDoesNotQueueExtraAttacks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.held(true);
            List<Integer> fired = new ArrayList<>();
            for (int tick = 0; tick <= 45; tick++) {
                if (tick > 0) f.advance(1);
                int before = f.sounds;
                baby.tick(); baby.tick();
                if (f.sounds > before) {
                    helper.assertTrue(f.sounds == before + 1, "同tick不重复播放声音或发射");
                    fired.add(tick);
                }
            }
            helper.assertTrue(fired.equals(List.of(0, 15, 30, 45)), "没有敌人也按宝宝自己的15tick节奏发射");
            f.held(false); f.advance(1); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 1, "松开保留本轮表现状态");
            f.advance(40); baby.tick();
            helper.assertTrue(f.sounds == 4 && baby.getAttackAge(0) < 0, "松开后不新增攻击或排队补发");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void invalidDirectionsRecallAndRemovalNeverGrantFreeShots(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            helper.assertTrue(!baby.attackInDirection(Vec3.ZERO) && !baby.attackInDirection(new Vec3(Double.NaN, 0, 0))
                    && f.sounds == 0 && baby.getAttackAge(0) < 0, "无效输入不消耗冷却或播放动作");
            helper.assertTrue(baby.attackInDirection(AIM) && !baby.attackInDirection(AIM), "失败后同tick可成功，成功后去重");
            f.advance(3); baby.setPos(f.hover().add(0, 40, 0)); baby.tick();
            helper.assertTrue(baby.isRecallPending() && !baby.attackInDirection(AIM) && baby.getAttackAge(0) < 0, "召回期间暂停攻击");
            helper.assertTrue(baby.recallToOwner(), "执行召回");
            f.advance(11); helper.assertTrue(!baby.attackInDirection(AIM), "召回保留原有冷却");
            f.advance(1); helper.assertTrue(baby.attackInDirection(AIM) && f.sounds == 2, "原15tick冷却到期可再次发射");
            baby.discardAndUnlink();
            helper.assertTrue(!baby.attackInDirection(AIM), "被移除实体不能发射");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsChangesOnlyBabyDamageAndVisualScale(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            f.enemy(f.head(baby).add(0, 0, 5));
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId(), 1);
            helper.assertTrue(baby.getEnhancement() == 0, "Hive Mind不影响机器宝宝");
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_VISUAL_SCALE.getId(), 1);
            baby.tick();
            helper.assertTrue(baby.attackInDirection(AIM), "BFFS后正常发射");
            var laser = f.lasers.iterator().next();
            helper.assertTrue(laser.getDamage() == 4 * StatManager.DAMAGE.getBonus()
                    && ((AbstractLaserAttack.LaserProjectile) laser).width == 0.25
                    && Math.abs(baby.getVisualScale() - 1.25F) < 1e-7, "增强伤害和体型，但不继承玩家激光尺寸");
            f.settings.set(GameplayConfig.DAMAGE_BONUS, 0.0);
            f.advance(15); f.lasers.clear(); baby.attackInDirection(AIM);
            helper.assertTrue(f.lasers.iterator().next().getDamage() == 0, "零伤害配置规范化为零");
            f.settings.set(GameplayConfig.DAMAGE_BONUS, 1.0);
            StatManager.removeFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, WEAKENING, 1);
            f.advance(15); f.lasers.clear(); baby.attackInDirection(AIM);
            helper.assertTrue(f.lasers.iterator().next().getDamage() == 0, "负增强不会产生负伤害或回退到玩家伤害");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedChainCompactsAndOverflowRequestsWaitForFreedCapacity(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(id("brother_bobby")); var middle = f.add(ROBO); var last = f.add(id("ghost_baby"));
            helper.assertTrue(first.getFormationIndex() == 0 && middle.getFormationIndex() == 1 && last.getFormationIndex() == 2,
                    "机器宝宝加入跨类型链槽");
            middle.setPos(f.hover().add(5, 0, 0)); last.setPos(f.hover()); last.tick();
            helper.assertTrue(last.getDeltaMovement().x > 0, "后继跟随机器宝宝而非玩家");
            middle.discardAndUnlink(); f.data.addCount(ROBO, COMMAND, -1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(last.getFormationIndex() == 1, "移除中间成员后重排");
            f.data.setCount(id("brother_bobby"), COMMAND, 63);
            f.data.setCount(ROBO, COMMAND, 100);
            helper.assertTrue(f.data.getCount(ROBO) == 100 && f.data.getEffectiveCount(ROBO) == 0, "满额时保留100份等待需求");
            f.data.addCount(id("brother_bobby"), COMMAND, -1);
            helper.assertTrue(f.data.getEffectiveCount(ROBO) == 1, "腾位后补出等待类型");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosAbsorbedCopiesSaveReloadAndDeathCopyKeepSourceCounts(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper); Fixture clone = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(4);
            tickCurios(original.owner);
            helper.assertTrue(original.slots().isItemValid(0, new ItemStack(ForgeRegistries.ITEMS.getValue(ROBO))), "生成标签允许真实装备");
            original.slots().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(ROBO)));
            tickCurios(original.owner); tickCurios(original.owner);
            original.absorb(ROBO); original.absorb(ROBO);
            helper.assertTrue(original.data.getCount(ROBO, ROBO) == 3
                    && original.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "三份同类道具各贡献跟班和套装进度");
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
            DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner); tickCurios(restored.owner); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(ROBO, ROBO) == 3
                    && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "重载不重复授予");
            restored.slots().setStackInSlot(0, ItemStack.EMPTY); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(ROBO, ROBO) == 2
                    && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "卸下只减少一份");
            ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, restored.owner, true));
            restored.owner.reviveCaps();
            helper.assertTrue(clone.data.getCount(ROBO, ROBO) == 2
                    && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "死亡复制保留吸收需求与套装计数");
            clone.items.removeFromId(clone.owner, ROBO);
            helper.assertTrue(clone.data.getCount(ROBO, ROBO) == 1, "移除一份按来源扣减");
            clone.items.clearPlayerPassiveItems(clone.owner);
            FamiliarHelper.maintainExistingFamiliars(clone.owner);
            helper.assertTrue(clone.data.getCount(ROBO) == 0 && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 0,
                    "清空道具由调度器收敛并清理");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualDimensionTransferRebuildsTheRoboRosterFromItsSource(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.absorb(ROBO);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "初始生成机器宝宝");
            var home = f.owner.serverLevel();
            var destination = home.getServer().getLevel(Level.NETHER);
            Vec3 original = f.owner.position();
            List<AbstractBabyFamiliarEntity> entered = new ArrayList<>();
            Consumer<EntityJoinLevelEvent> capture = event -> {
                if (event.getLevel() == destination && event.getEntity() instanceof AbstractBabyFamiliarEntity baby && baby.getOwner() == f.owner)
                    entered.add(baby);
            };
            MinecraftForge.EVENT_BUS.addListener(capture);
            try {
                f.owner.teleportTo(destination, original.x, original.y, original.z, 0, 0);
                FamiliarHelper.maintainExistingFamiliars(f.owner);
                destination.getChunkAt(f.owner.blockPosition());
                helper.assertTrue(f.data.getCount(ROBO, ROBO) == 1 && FamiliarHelper.spawnNextMissingFamiliar(f.owner)
                                && entered.size() == 1 && f.data.containsEntity(ROBO, entered.get(0).getUUID()), "实际跨维度恢复来源及运行名册");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(capture);
                entered.forEach(AbstractIsaacFamiliarEntity::discardAndUnlink);
                f.owner.teleportTo(home, original.x, original.y, original.z, 0, 0);
            }
            helper.assertTrue(f.data.getCount(ROBO, ROBO) == 1 && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "返回原维度正常补出");
        }
        helper.succeed();
    }

    private static void tickCurios(ServerPlayer player) {
        CurioSlotManager.tick(player);
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final PlayerIsaacItems items;
        final GameTestDatapackSettings.Scope settings;
        final long originalTime;
        final Set<IBulletObject> lasers = Collections.newSetFromMap(new IdentityHashMap<>());
        final List<LivingEntity> targets = new ArrayList<>();
        final Consumer<IsaacAttackBeforeHitEntityEvent> hits;
        final Consumer<PlayLevelSoundEvent.AtPosition> soundCapture;
        int sounds;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            settings = GameTestDatapackSettings.override(helper);
            settings.set(GameplayConfig.EXTRA_PASSIVE_ITEM_BACKPACK, true);
            settings.set(GameplayConfig.PASSIVE_ITEM_LIMIT, 256);
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "robo-test"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(2, 220 + 4 * FIXTURE.getAndIncrement(), 2));
            DefaultAttributeManager.apply(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            hits = event -> { if (event.getBulletObject().getOwner() == owner) lasers.add(event.getBulletObject()); };
            soundCapture = event -> {
                if (event.getLevel() == owner.level() && event.getSound() != null
                        && event.getSound().value() == ModSounds.LASER_SHOT.get()) sounds++;
            };
            MinecraftForge.EVENT_BUS.addListener(hits);
            MinecraftForge.EVENT_BUS.addListener(soundCapture);
        }

        AbstractBabyFamiliarEntity add(ResourceLocation type) {
            data.addCount(type, COMMAND, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "正常调度器生成 " + type);
            var entry = data.getEntry(type).orElseThrow();
            return (AbstractBabyFamiliarEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        void absorb(ResourceLocation type) {
            var item = ForgeRegistries.ITEMS.getValue(type);
            helper.assertTrue(item instanceof PassiveItem, "正常注册被动道具 " + type);
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
            helper.getLevel().addFreshEntity(target); targets.add(target); return target;
        }

        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        Vec3 head(AbstractBabyFamiliarEntity baby) { return baby.position().add(0, 6.5 / 16 * 0.6 * baby.getVisualScale(), 0); }
        void advance(int ticks) { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks); }
        void held(boolean held) { owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(a -> a.setHoldRightClick(held)); }

        @Override public void close() {
            MinecraftForge.EVENT_BUS.unregister(hits);
            MinecraftForge.EVENT_BUS.unregister(soundCapture);
            targets.forEach(LivingEntity::discard);
            var slots = CuriosApi.getCuriosInventory(owner).resolve().orElseThrow().getStacksHandler(CurioSlotManager.PASSIVE_ITEM);
            if (slots.isPresent()) {
                var stacks = slots.get().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) stacks.setStackInSlot(i, ItemStack.EMPTY);
                tickCurios(owner);
            }
            items.clearPlayerPassiveItems(owner);
            FamiliarHelper.discardAllRuntimeFamiliars(owner); data.clearRequirements(); owner.discard();
            owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(originalTime);
            settings.close();
        }
    }
}
