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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.npc.Villager;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_auto_baby")
@PrefixGameTestTemplate(false)
public final class AutoAttackBabyGameTests {
    private static final ResourceLocation ROBO = id("robo_baby_2");
    private static final ResourceLocation DEMON = id("demon_baby");
    private static final ResourceLocation COMMAND = id("command");
    private static final Vec3 AIM = new Vec3(0, 0, 1);
    private static final ResourceLocation WEAKENING = ResourceLocation.fromNamespaceAndPath("auto_baby_test", "weakening");

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, path); }

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class AutoBabyTestRegistration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> registry.register(WEAKENING,
                    new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, -2,
                            AbstractBabyFamiliarEntity.class::isInstance)));
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredBabiesFireEverySevenTicksWithoutAnyPlayerInput(GameTestHelper helper) {
        helper.assertTrue(ItemId.ROBO_BABY_2.getLevel() == 3 && ItemId.DEMON_BABY.getLevel() == 2, "两件品质按模组方案注册");
        for (var type : List.of(ROBO, DEMON)) try (Fixture f = new Fixture(helper)) {
            var baby = f.add(type);
            var enemy = f.enemy(f.head(baby).add(0, 0, 5));
            f.owner.setYRot(180); f.owner.setXRot(-70);
            List<Integer> fired = new ArrayList<>();
            for (int tick = 0; tick <= 28; tick++) {
                if (tick > 0) f.advance(1);
                int before = f.sounds;
                baby.tick(); baby.tick();
                if (f.sounds > before) {
                    helper.assertTrue(f.sounds == before + 1, "同tick不重复发射或播放声音");
                    fired.add(tick);
                }
            }
            helper.assertTrue(fired.equals(List.of(0, 7, 14, 21, 28)), "空手也以独立7tick节奏攻击 " + type + fired);
            Vec3 axis = type.equals(ROBO) ? f.lasers.iterator().next().getAttackContext().getMainAxis() : f.shots(baby).get(0).getVelocity();
            helper.assertTrue(axis.distanceTo(AIM) < 1e-6, "自动瞄准目标，不采用玩家朝向");
            if (type.equals(ROBO)) helper.assertTrue(enemy.getHealth() < 100, "机器宝宝2的自动激光真实命中");
            helper.assertTrue(FamiliarHelper.getFamiliarType(type).getLimit() == 64, "同类型有效上限为64");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void noTargetsMeansNoPlayerTriggeredFallbackAndFourTickScans(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) try (Fixture f = new Fixture(helper)) {
            var baby = f.add(type);
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(a -> a.setHoldRightClick(true));
            baby.tick();
            f.enemy(f.head(baby).add(0, 0, 5));
            for (int tick = 1; tick <= 3; tick++) { f.advance(1); baby.tick(); }
            helper.assertTrue(f.sounds == 0 && baby.getAttackAge(0) < 0, "无锁定时按住以撒头也不发射，扫描间隔为4tick");
            f.advance(1); baby.tick();
            helper.assertTrue(f.sounds == 1, "第4tick的正常扫描发现新目标");
            f.owner.setGameMode(GameType.SPECTATOR);
            f.advance(7); baby.tick();
            helper.assertTrue(f.sounds == 1, "旁观者的宝宝暂停自动攻击");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void sphericalRangeIncludesBoundaryAndChecksPureHeight(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) try (Fixture f = new Fixture(helper)) {
            var baby = f.add(type);
            double range = type.equals(ROBO) ? 12 : 8;
            Vec3 origin = f.head(baby);
            var target = f.enemy(origin.add(0, 0, range));
            baby.tick();
            helper.assertTrue(f.sounds == 1, "恰好位于索敌边界仍可攻击 " + type);
            target.setPos(origin.add(0, -target.getBbHeight() / 2, range + 0.01));
            f.advance(7); baby.tick();
            helper.assertTrue(f.sounds == 1, "超过范围立即解除锁定");
            target.setPos(origin.add(0, range - target.getBbHeight() / 2, 0));
            for (int i = 0; i < 4; i++) { f.advance(1); baby.tick(); }
            helper.assertTrue(f.sounds == 2, "纯高度差也按三维距离纳入索敌");
            if (type.equals(DEMON)) helper.assertTrue(f.shots(baby).get(1).getVelocity().distanceTo(new Vec3(0, 1, 0)) < 1e-6,
                    "垂直目标使用真实向上的单位方向");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void priorityStableLocksAndImmediateReplacementUseTheExistingHostilityRules(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            Vec3 origin = f.head(baby);
            var close = f.enemy(origin.add(4, 0, 0));
            var aggro = f.enemy(origin.add(0, 0, 7)); aggro.setTarget(f.owner);
            baby.tick();
            helper.assertTrue(f.lasers.iterator().next().getAttackContext().getMainAxis().distanceTo(AIM) < 1e-6,
                    "正在攻击主人的敌对目标优先于更近的普通怪物");
            close.setTarget(f.owner);
            f.advance(7); f.lasers.clear(); baby.tick();
            helper.assertTrue(f.lasers.iterator().next().getAttackContext().getMainAxis().distanceTo(AIM) < 1e-6,
                    "更近目标不抢占有效锁定");
            aggro.discard();
            f.advance(1); baby.tick();
            f.advance(6); f.lasers.clear(); baby.tick();
            helper.assertTrue(f.lasers.iterator().next().getAttackContext().getMainAxis().distanceTo(new Vec3(1, 0, 0)) < 1e-6,
                    "原目标移除后立即重选，冷却仍按上次成功发射计算");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void equidistantCandidatesUseEntityIdAndIgnoreFriendlyOrPeacefulCandidates(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            Vec3 origin = f.head(baby);
            Cow cow = EntityType.COW.create(helper.getLevel()); cow.setNoAi(true); cow.setNoGravity(true);
            cow.setPos(origin.add(0, -cow.getBbHeight() / 2, 1)); helper.getLevel().addFreshEntity(cow); f.targets.add(cow);
            Villager villager = EntityType.VILLAGER.create(helper.getLevel()); villager.setNoAi(true); villager.setNoGravity(true);
            villager.setPos(origin.add(0, -villager.getBbHeight() / 2, 2)); helper.getLevel().addFreshEntity(villager); f.targets.add(villager);
            Wolf pet = new Wolf(EntityType.WOLF, helper.getLevel()) {
                @Override public LivingEntity getOwner() { return f.owner; }
            };
            pet.tame(f.owner); pet.setNoAi(true); pet.setNoGravity(true); pet.setTarget(f.owner);
            pet.setPos(origin.add(0, -pet.getBbHeight() / 2, 3)); helper.getLevel().addFreshEntity(pet); f.targets.add(pet);
            var first = f.enemy(origin.add(5, 0, 0));
            var second = f.enemy(origin.add(0, 0, 5));
            helper.assertTrue(first.getId() < second.getId(), "场景按实体ID顺序生成两个等距敌人");
            baby.tick();
            helper.assertTrue(f.lasers.iterator().next().getAttackContext().getMainAxis().distanceTo(new Vec3(1, 0, 0)) < 1e-6,
                    "等优先级、等距离时按实体ID选择，跳过动物、村民和友方宠物");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void robotTargetsThroughWallsButDemonRequiresVisibilityAndLosesBlockedLocks(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) try (Fixture f = new Fixture(helper)) {
            var baby = f.add(type);
            Vec3 origin = f.head(baby);
            var target = f.enemy(origin.add(0, 0, 6));
            BlockPos wall = BlockPos.containing(origin.add(0, 0, 3));
            f.block(wall, Blocks.STONE.defaultBlockState());
            baby.tick();
            helper.assertTrue(f.sounds == (type.equals(ROBO) ? 1 : 0), "只允许机器宝宝2隔墙索敌");
            if (type.equals(ROBO)) helper.assertTrue(target.getHealth() < 100, "隔墙锁定后激光真实伤害目标");
            f.block(wall, Blocks.AIR.defaultBlockState());
            f.advance(7); baby.tick();
            helper.assertTrue(f.sounds == (type.equals(ROBO) ? 2 : 1), "可见后按原冷却或扫描节奏开火");
            f.block(wall, Blocks.STONE.defaultBlockState());
            f.advance(7); baby.tick();
            helper.assertTrue(f.sounds == (type.equals(ROBO) ? 3 : 1), "恶魔锁定被方块挡住后立即解除");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void failedTearAdmissionCanRetrySameTickWithoutFakeActionOrSound(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(DEMON);
            f.enemy(f.head(baby).add(0, 0, 5));
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 1);
            var blocker = BulletState.builder().owner(f.owner).shooter(f.owner).position(f.owner.position().add(30, 0, 0))
                    .velocity(AIM).baseSpeed(1).range(20).lifetime(100).damage(0).noGravity(true).build();
            helper.assertTrue(BulletRuntime.INSTANCE.spawn(f.owner.serverLevel(), blocker) != null, "正常占满弹体容量");
            baby.tick();
            helper.assertTrue(f.shots(baby).isEmpty() && f.sounds == 0 && baby.getAttackAge(0) < 0,
                    "容量拒绝不消耗冷却、不播放虚假动作或声音");
            BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).remove(blocker);
            baby.tick(); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 1 && f.sounds == 1 && baby.getAttackAge(0) == 0,
                    "释放容量后同tick可重试，成功后同tick去重");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void demonShotsSnapshotTheirOwnRangeDamageAndOrdinaryRedTearProperties(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(DEMON);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(9);
            f.owner.getAttribute(ModAttributes.BULLET_SCALE.get()).setBaseValue(40);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ability.setHoming(1); ability.setControllable(1); ability.addBulletColor(ModBulletColors.POISON.getId(), 1);
            double[] configured = {2, 20, 100};
            double[] expected = {8, 20, 100};
            for (int i = 0; i < configured.length; i++) {
                f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured[i]);
                if (i > 0) f.advance(7);
                helper.assertTrue(baby.attackInDirection(AIM), "普通方向攻击接口仍可正常发射");
                var shot = f.shots(baby).get(i);
                helper.assertTrue(shot.getRange() == expected[i] && shot.getDamage() == 1.5 * StatManager.DAMAGE.getBonus(),
                        "射程下限8，支持超过64，伤害不读取玩家面板");
                helper.assertTrue(shot.getVelocity().distanceTo(AIM) < 1e-7 && shot.position().distanceTo(f.head(baby)) < 1e-7
                        && shot.getOwner() == f.owner && shot.getShooter() == baby, "独立弹速、玩家归属和头部起点，无前移");
                helper.assertTrue(shot.getColorId().equals(ModBulletColors.BLOOD_TEAR.getId()) && !shot.isSpectral()
                        && !shot.isPiercing() && !shot.isHoming() && !shot.isControllable() && shot.getSplitSequence().isEmpty(),
                        "红色普通泪弹不继承玩家颜色、追踪、控制和分裂");
            }
            helper.assertTrue(f.shots(baby).get(0).getRange() == 8, "发射后修改配置不影响已创建快照");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void robotLaserKeepsIndependentContextAndConfiguredRangeCap(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO);
            f.enemy(f.head(baby).add(0, 0, 5));
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SCALE.get()).setBaseValue(99);
            f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(a -> { a.setHoming(1); a.setControllable(1); });
            double[] configured = {2, 20, 50};
            double[] expected = {24, 40, 64};
            for (int i = 0; i < configured.length; i++) {
                f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured[i]);
                if (i > 0) f.advance(7);
                f.lasers.clear();
                helper.assertTrue(baby.attackInDirection(AIM), "独立激光执行成功");
                var laser = f.lasers.iterator().next(); var context = laser.getAttackContext();
                helper.assertTrue(laser.getRange() == expected[i] && laser.getDamage() == 2 * StatManager.DAMAGE.getBonus()
                        && laser.getOwner() == f.owner && laser.getShooter() == baby, "激光配置范围24到64，伤害独立且归属正确");
                helper.assertTrue(laser.isSpectral() && laser.isPiercing() && !laser.isHoming() && !laser.isControllable()
                        && context.usesFixedLaunchTransform() && context.getPos().distanceTo(f.head(baby)) < 1e-7
                        && context.getColorRl().equals(ModBulletColors.BASE.getId()) && context.getSplitSequence().isEmpty()
                        && context.getVisualIds().isEmpty() && context.getTrajectorySpecs().isEmpty()
                        && ((AbstractLaserAttack.LaserProjectile) laser).width == 0.25, "固定中立发射点、普通红色和独立尺寸，不继承玩家模块");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualTearsHitFirstCreatureAndRemainBlockedByStone(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(DEMON);
            Vec3 origin = f.head(baby);
            var near = f.enemy(origin.add(0, 0, 3));
            var far = f.enemy(origin.add(0, 0, 6));
            baby.tick();
            for (int i = 0; i < 7; i++) { f.advance(1); BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel()); }
            helper.assertTrue(near.getHealth() < 100 && far.getHealth() == 100 && f.shots(baby).isEmpty(),
                    "真实命中首个生物后消失，不穿透后方生物");
            near.discard();
            helper.assertTrue(baby.attackInDirection(AIM), "下一轮正常直接发射");
            f.block(BlockPos.containing(origin.add(0, 0, 2)), Blocks.STONE.defaultBlockState());
            for (int i = 0; i < 7; i++) { f.advance(1); BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel()); }
            helper.assertTrue(far.getHealth() == 100 && f.shots(baby).isEmpty(), "普通泪弹仍被石块碰撞销毁");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void robotPiercesWallsAndEachCreatureOnceWithoutLeavingAnInitialGap(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(ROBO); Vec3 origin = f.head(baby);
            var near = f.enemy(origin.add(0, 0, 0.3)); var far = f.enemy(origin.add(0, 0, 7));
            f.block(BlockPos.containing(origin.add(0, 0, 3)), Blocks.STONE.defaultBlockState());
            helper.assertTrue(baby.attackInDirection(AIM), "机器宝宝2的单次独立激光正常发射");
            double expected = 100 - 2 * StatManager.DAMAGE.getBonus();
            helper.assertTrue(Math.abs(near.getHealth() - expected) < 1e-3 && Math.abs(far.getHealth() - expected) < 1e-3,
                    "极近目标与隔墙后方目标都恰好命中一次");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void losingTargetFinishesTheActionAndRecallPreservesCooldownButClearsTheLock(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) try (Fixture f = new Fixture(helper)) {
            var baby = f.add(type); var target = f.enemy(f.head(baby).add(0, 0, 6)); baby.tick();
            target.discard(); f.advance(2); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 2 && f.sounds == 1, "失去目标不打断正在播放的动作");
            baby.setPos(f.hover().add(0, 40, 0)); baby.tick();
            helper.assertTrue(baby.isRecallPending() && baby.getAttackAge(0) < 0 && f.sounds == 1, "召回期间清除动作与目标并停止攻击");
            helper.assertTrue(baby.recallToOwner(), "执行召回");
            baby.setPos(f.hover());
            f.enemy(f.head(baby).add(5, 0, 0));
            f.advance(4); baby.tick();
            helper.assertTrue(f.sounds == 1, "召回不刷新剩余冷却");
            f.advance(1); f.lasers.clear(); baby.tick();
            helper.assertTrue(f.sounds == 2, "原7tick冷却到期后可重新选敌并发射");
            baby.discardAndUnlink(); f.advance(7); baby.tick();
            helper.assertTrue(f.sounds == 2 && !baby.attackInDirection(AIM), "清理后的跟班不再攻击");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsAndEnhancementBoundariesDoNotAffectHiveMindOrPlayerStats(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) try (Fixture f = new Fixture(helper)) {
            var baby = f.add(type); f.enemy(f.head(baby).add(0, 0, 5));
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId(), 1);
            helper.assertTrue(baby.getEnhancement() == 0, "Hive Mind不影响自动宝宝");
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_VISUAL_SCALE.getId(), 1);
            baby.tick();
            IBulletObject shot = type.equals(ROBO) ? f.lasers.iterator().next() : f.shots(baby).get(0);
            helper.assertTrue(shot.getDamage() == (type.equals(ROBO) ? 4 : 3) * StatManager.DAMAGE.getBonus() && Math.abs(baby.getVisualScale() - 1.25F) < 1e-7,
                    "BFFS自然增强两种宝宝的伤害与体型");
            StatManager.removeFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, WEAKENING, 1);
            f.advance(7); f.lasers.clear(); baby.attackInDirection(AIM);
            shot = type.equals(ROBO) ? f.lasers.iterator().next() : f.shots(baby).get(1);
            helper.assertTrue(shot.getDamage() == 0, "低于负一的增强钳制为零伤害");
            StatManager.removeFamiliarModifier(f.owner, WEAKENING, 1);
            f.settings.set(GameplayConfig.DAMAGE_BONUS, 0.0);
            f.advance(7); f.lasers.clear(); baby.attackInDirection(AIM);
            shot = type.equals(ROBO) ? f.lasers.iterator().next() : f.shots(baby).get(2);
            helper.assertTrue(shot.getDamage() == 0, "零配置伤害保持为零");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedChainSharedQuotaWaitingDemandAndRemovalContinueToUseTheScheduler(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(id("brother_bobby")); var middle = f.add(DEMON); var last = f.add(ROBO);
            helper.assertTrue(first.getFormationIndex() == 0 && middle.getFormationIndex() == 1 && last.getFormationIndex() == 2,
                    "自动宝宝与玩家跟射宝宝共用链槽");
            middle.setPos(f.hover().add(5, 0, 0)); last.setPos(f.hover()); last.tick();
            helper.assertTrue(last.getDeltaMovement().x > 0, "自动宝宝仍跟随前一链槽，未脱队追敌");
            f.data.addCount(DEMON, COMMAND, -1); FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(middle.isRemoved() && last.getFormationIndex() == 1, "需求减少由调度器移除实体并重排");
        }
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(id("brother_bobby"), COMMAND, 64); f.data.setCount(ROBO, COMMAND, 100);
            helper.assertTrue(f.data.getCount(ROBO) == 100 && f.data.getEffectiveCount(ROBO) == 0, "共享64上限保留超额等待需求");
            f.data.addCount(id("brother_bobby"), COMMAND, -1);
            helper.assertTrue(f.data.getEffectiveCount(ROBO) == 1, "释放配额后等待需求自动获得有效配额");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosAbsorbedCopiesSaveReloadAndDeathCopyKeepSourceCounts(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) {
            try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper); Fixture clone = new Fixture(helper)) {
                original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(4);
                tickCurios(original.owner);
                helper.assertTrue(original.slots().isItemValid(0, new ItemStack(ForgeRegistries.ITEMS.getValue(type))), "生成标签允许真实装备");
                original.slots().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(type)));
                tickCurios(original.owner); tickCurios(original.owner);
                original.absorb(type); original.absorb(type);
                helper.assertTrue(original.data.getCount(type, type) == 3
                        && original.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "三份同类道具各贡献跟班和套装进度");
                restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
                DefaultAttributeManager.apply(restored.owner);
                CurioSlotManager.requestReconcile(restored.owner); tickCurios(restored.owner); tickCurios(restored.owner);
                helper.assertTrue(restored.data.getCount(type, type) == 3
                        && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "重载不重复授予");
                restored.slots().setStackInSlot(0, ItemStack.EMPTY); tickCurios(restored.owner);
                helper.assertTrue(restored.data.getCount(type, type) == 2
                        && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "卸下只减少一份");
                ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, restored.owner, true));
                restored.owner.reviveCaps();
                helper.assertTrue(clone.data.getCount(type, type) == 2
                        && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "死亡复制保留吸收需求与套装计数");
                clone.items.removeFromId(clone.owner, type);
                helper.assertTrue(clone.data.getCount(type, type) == 1, "移除一份按来源扣减");
                clone.items.clearPlayerPassiveItems(clone.owner);
                FamiliarHelper.maintainExistingFamiliars(clone.owner);
                helper.assertTrue(clone.data.getCount(type) == 0 && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 0,
                        "清空道具由调度器收敛并清理");
            }

            }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualDimensionTransferRebuildsTheAutoBabyRosterFromItsSource(GameTestHelper helper) {
        for (var type : List.of(ROBO, DEMON)) {
            try (Fixture f = new Fixture(helper)) {
                f.absorb(type);
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
                    helper.assertTrue(f.data.getCount(type, type) == 1 && FamiliarHelper.spawnNextMissingFamiliar(f.owner)
                                    && entered.size() == 1 && f.data.containsEntity(type, entered.get(0).getUUID()), "实际跨维度恢复来源及运行名册");
                } finally {
                    MinecraftForge.EVENT_BUS.unregister(capture);
                    entered.forEach(AbstractIsaacFamiliarEntity::discardAndUnlink);
                    f.owner.teleportTo(home, original.x, original.y, original.z, 0, 0);
                }
                helper.assertTrue(f.data.getCount(type, type) == 1 && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "返回原维度正常补出");
            }

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
        final Map<BlockPos, BlockState> oldBlocks = new HashMap<>();
        final Consumer<IsaacAttackBeforeHitEntityEvent> hits;
        final Consumer<PlayLevelSoundEvent.AtPosition> soundCapture;
        int sounds;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            originalTime = helper.getLevel().getGameTime();
            settings = GameTestDatapackSettings.override(helper);
            settings.set(GameplayConfig.EXTRA_PASSIVE_ITEM_BACKPACK, true);
            settings.set(GameplayConfig.PASSIVE_ITEM_LIMIT, 256);
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "auto-baby-test"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(2, 240, 2));
            DefaultAttributeManager.apply(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            hits = event -> { if (event.getBulletObject().getOwner() == owner
                    && event.getBulletObject() instanceof AbstractLaserAttack.LaserProjectile) lasers.add(event.getBulletObject()); };
            soundCapture = event -> {
                if (event.getLevel() == owner.level() && event.getSound() != null
                        && (event.getSound().value() == ModSounds.LASER_SHOT.get()
                        || event.getSound().value() == ModSounds.TEAR_BULLET_SHOT.get())) sounds++;
            };
            MinecraftForge.EVENT_BUS.addListener(hits);
            MinecraftForge.EVENT_BUS.addListener(soundCapture);
        }

        AbstractBabyFamiliarEntity add(ResourceLocation type) {
            data.addCount(type, COMMAND, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "正常调度器生成 " + type);
            var entry = data.getEntry(type).orElseThrow();
            var baby = (AbstractBabyFamiliarEntity) owner.serverLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
            baby.setPos(hover());
            return baby;
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
            helper.getLevel().getChunkAt(target.blockPosition());
            helper.getLevel().addFreshEntity(target); targets.add(target); return target;
        }

        Vec3 hover() { return owner.position().add(0, owner.getBbHeight() * 0.55, 0); }
        Vec3 head(AbstractBabyFamiliarEntity baby) {
            double pixels = ForgeRegistries.ENTITY_TYPES.getKey(baby.getType()).equals(ROBO) ? 3.5 : 5.5;
            return baby.position().add(0, pixels / 16 * baby.getBaseModelScale() * baby.getVisualScale(), 0);
        }
        List<BulletState> shots(AbstractBabyFamiliarEntity baby) {
            return BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates().stream().filter(s -> s.getShooter() == baby).toList();
        }
        void block(BlockPos pos, BlockState state) {
            oldBlocks.putIfAbsent(pos, helper.getLevel().getBlockState(pos));
            helper.assertTrue(helper.getLevel().setBlockAndUpdate(pos, state) || helper.getLevel().getBlockState(pos).equals(state),
                    "测试场景必须成功放置方块 " + pos);
        }
        void advance(int ticks) { owner.serverLevel().getServer().getWorldData().overworldData().setGameTime(owner.level().getGameTime() + ticks); }

        @Override public void close() {
            MinecraftForge.EVENT_BUS.unregister(hits);
            MinecraftForge.EVENT_BUS.unregister(soundCapture);
            targets.forEach(LivingEntity::discard);
            oldBlocks.forEach((pos, state) -> helper.getLevel().setBlockAndUpdate(pos, state));
            for (var shot : List.copyOf(BulletRuntime.INSTANCE.manager(owner.serverLevel()).activeStates()))
                if (shot.getOwner() == owner) BulletRuntime.INSTANCE.manager(owner.serverLevel()).remove(shot);
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
