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
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.loki_baby.LokiBabyEntity;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import java.util.function.Consumer;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;
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

@GameTestHolder("isaac_disaster_loki_baby")
@PrefixGameTestTemplate(false)
public final class LokiBabyGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = id("command");
    private static final AtomicInteger FIXTURE = new AtomicInteger();
    private static final ResourceLocation LOKI = id("loki_baby");
    private static final ResourceLocation BOBBY = id("brother_bobby");
    private static final ResourceLocation GHOST = id("ghost_baby");
    private static final ResourceLocation WEAKENING = ResourceLocation.fromNamespaceAndPath("loki_test", "weakening");
    private static final Vec3 AIM = new Vec3(0, 0, 1);

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name); }

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class LokiBabyTestRegistration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> registry.register(WEAKENING,
                    new FamiliarModifierEntry(FamiliarModifierEntry.Parameter.ENHANCEMENT, -2,
                            AbstractBabyFamiliarEntity.class::isInstance)));
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredLokiUsesFourIndependentAxesAndSnapshotsOnlyBabyStats(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            helper.assertTrue(ForgeRegistries.ITEMS.getValue(LOKI) instanceof PassiveItem
                    && ModFamiliarEntities.LOKI_BABY.get().getLimit() == 64 && ItemId.LOKI_BABY.getLevel() == 1,
                    "品质1道具与64上限跟班正常注册");
            var baby = f.add(LOKI);
            f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(999);
            f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(9);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ability.setPiercing(1); ability.setSpectral(1); ability.setHoming(1);
            List<Vec3> axes = List.of(AIM, new Vec3(1, 2, 3).normalize(), new Vec3(0, 1, 0), new Vec3(0, -1, 0));
            int index = 0;
            for (Vec3 aim : axes) {
                double configured = new double[]{20, 2, 100, 20}[index++];
                f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, configured);
                f.advance(15);
                helper.assertTrue(baby.attackInDirection(aim), "各种俯仰均可整轮发射");
                var all = f.shots(baby);
                var volley = all.subList(all.size() - 4, all.size());
                helper.assertTrue(volley.get(0).velocity().distanceTo(aim) < 1e-7
                                && volley.get(1).velocity().add(aim).length() < 1e-7
                                && volley.get(2).velocity().add(volley.get(3).velocity()).length() < 1e-7
                                && Math.abs(volley.get(0).velocity().dot(volley.get(2).velocity())) < 1e-7,
                        "前后左右四向在同一平面内正交，垂直瞄准不退化");
                for (BulletState shot : volley) {
                    helper.assertTrue(shot.position().distanceTo(f.head(baby)) < 1e-7
                            && Math.abs(shot.velocity().length() - 1) < 1e-7, "从共同中立头部出射，无前移且弹速为1");
                    helper.assertTrue(shot.getDamage() == 2 * StatManager.DAMAGE.getBonus()
                            && shot.getRange() == Math.max(12, 1.5 * configured)
                            && shot.getColorId().equals(ModBulletColors.BASE.getId()) && shot.noGravity(), "快照宝宝伤害、配置射程及普通弹色");
                    helper.assertTrue(!shot.isHoming() && !shot.isPiercing() && !shot.isSpectral()
                            && shot.getAttackContext() == null && shot.getSplitSequence().isEmpty(), "隔离玩家子弹效果");
                }
            }
            var old = f.shots(baby).get(0);
            double snapshot = old.getDamage();
            f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, 2.0);
            helper.assertTrue(old.getDamage() == snapshot && old.getRange() == 30, "后续配置变化不修改已生成的子弹");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void capacityFailuresDeduplicationTwelveTickActionAndRecallUseOneCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            helper.assertTrue(!baby.attackInDirection(Vec3.ZERO) && !baby.attackInDirection(new Vec3(Double.NaN, 0, 0)), "拒绝无效方向");
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 3);
            helper.assertTrue(!baby.attackInDirection(AIM) && f.shots(baby).isEmpty() && baby.getAttackAge(0) < 0,
                    "不足四个名额时整轮失败，不消耗冷却或播放动作");
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 4);
            helper.assertTrue(baby.attackInDirection(AIM) && f.shots(baby).size() == 4 && !baby.attackInDirection(AIM),
                    "失败后同tick可重试，成功后同tick只允许一轮");
            var mirror = (AbstractTearBabyFamiliarEntity) baby.getType().create(helper.getLevel());
            f.advance(11);
            mirror.getEntityData().assignValues(baby.getEntityData().getNonDefaultValues());
            helper.assertTrue(baby.getAttackAge(0) == 11 && mirror.getAttackAge(0) == 11, "新客户端续播第11tick");
            f.advance(1);
            helper.assertTrue(baby.getAttackAge(0) < 0 && mirror.getAttackAge(0) < 0, "12tick后动作结束");
            f.advance(3);
            helper.assertTrue(!baby.attackInDirection(AIM) && f.shots(baby).size() == 4, "整轮容量仍不足时保持可重试");
            f.clearShots();
            helper.assertTrue(baby.attackInDirection(AIM), "释放容量后立即重试成功");
            f.advance(3); baby.setPos(f.hover().add(0, 40, 0)); baby.tick();
            helper.assertTrue(baby.isRecallPending() && baby.getAttackAge(0) < 0 && !baby.attackInDirection(AIM), "召回清除动作并暂停攻击");
            helper.assertTrue(baby.recallToOwner() && baby.getDeltaMovement().equals(Vec3.ZERO), "召回清除动量");
            f.clearShots(); f.advance(11);
            helper.assertTrue(!baby.attackInDirection(AIM), "召回保留剩余冷却");
            f.advance(1);
            helper.assertTrue(baby.attackInDirection(AIM), "原15tick冷却正常结束");
            mirror.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void heldFireKeepsItsOwnCadenceWhileMovingAndStopsOnRelease(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            var bobby = f.add(BOBBY);
            baby.setPos(f.hover().add(-5, 0, 0));
            f.owner.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), 200);
            f.held(true);
            List<Integer> fired = new ArrayList<>();
            List<Integer> bobbyFired = new ArrayList<>();
            for (int tick = 0; tick <= 33; tick++) {
                if (tick > 0) f.advance(1);
                f.owner.setPos(f.owner.position().add(0.03, 0, 0));
                int before = f.shots(baby).size(); baby.tick(); baby.tick();
                if (f.shots(baby).size() > before) {
                    helper.assertTrue(f.shots(baby).size() == before + 4, "每轮固定四弹");
                    fired.add(tick);
                }
                before = f.shots(bobby).size(); bobby.tick();
                if (f.shots(bobby).size() > before) bobbyFired.add(tick);
            }
            helper.assertTrue(fired.equals(List.of(3, 18, 33)) && bobbyFired.equals(List.of(0, 10, 20, 30)),
                    "洛基起手3tick后仍每15tick出弹，波比继续即时发射");
            f.held(false); f.advance(15); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 12, "松开立即停止提出新攻击");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void windupKeepsItsTriggeredAimAndSnapshotsStatsAndSoundAtEmission(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            AtomicInteger sounds = new AtomicInteger();
            Consumer<PlayLevelSoundEvent.AtPosition> capture = event -> {
                if (event.getLevel() == f.owner.level() && event.getSound() != null
                        && event.getSound().value() == ModSounds.TEAR_BULLET_SHOT.get()) sounds.incrementAndGet();
            };
            MinecraftForge.EVENT_BUS.addListener(capture);
            try {
                Vec3 triggeredAim = f.owner.getLookAngle().normalize();
                f.held(true); baby.tick(); baby.tick();
                helper.assertTrue(f.shots(baby).isEmpty() && baby.getAttackAge(0) == 0 && sounds.get() == 0,
                        "先开始动画，起手时不出弹、不播放发射音效");
                var mirror = (AbstractTearBabyFamiliarEntity) baby.getType().create(helper.getLevel());
                mirror.getEntityData().assignValues(baby.getEntityData().getNonDefaultValues());
                f.advance(2); baby.tick();
                helper.assertTrue(f.shots(baby).isEmpty() && mirror.getAttackAge(0) == 2, "新进入视野可续播起手阶段");
                f.owner.setYRot(-90); f.owner.setXRot(-30);
                f.settings.set(GameplayConfig.DEFAULT_BULLET_RANGE, 100.0);
                f.settings.set(GameplayConfig.DAMAGE_BONUS, 2.0);
                f.advance(1); baby.tick(); baby.tick();
                var shots = f.shots(baby);
                helper.assertTrue(shots.size() == 4 && sounds.get() == 1
                                && baby.getAttackAge(0) == 3 && mirror.getAttackAge(0) == 3,
                        "第3tick四弹和音效同时产生，动画不从头重播");
                helper.assertTrue(shots.get(0).velocity().distanceTo(triggeredAim) < 1e-7,
                        "蓄力后玩家转头不会改变已触发攻击的方向");
                helper.assertTrue(shots.stream().allMatch(s -> s.getRange() == 150 && s.getDamage() == 4),
                        "出弹瞬间读取新伤害与射程：" + shots.stream().map(s -> s.getDamage() + "/" + s.getRange()).toList());
                f.held(false); f.advance(1); baby.tick();
                helper.assertTrue(baby.getAttackAge(0) == 4, "出弹后松开仍完整播放已开始的旋转动作");
                mirror.discard();
            } finally { MinecraftForge.EVENT_BUS.unregister(capture); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void releaseAndItemSwitchDoNotInterruptTriggeredAttack(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            f.held(true); baby.tick();
            f.advance(2); f.held(false); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 2 && f.shots(baby).isEmpty(), "松开后继续蓄力，不提前发射或取消动画");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            f.advance(1); baby.tick(); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 4 && baby.getAttackAge(0) == 3,
                    "短按后切换手持物仍按时发射且同tick不重复");
            f.advance(8); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 11, "松开后仍完整播放旋转收尾");
            f.advance(1); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) < 0, "第12tick正常回到待机");
            f.advance(5); baby.tick();
            helper.assertTrue(!baby.attackInDirection(AIM), "延迟发射成功后才开始15tick冷却");
            f.advance(20); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 4 && baby.getAttackAge(0) < 0, "松开后只完成已触发的一轮，不产生下一轮");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void cooldownWindupFinishesAfterReleaseWithoutRetriggering(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            f.held(true); baby.tick();
            f.advance(1); f.held(false); baby.tick();
            f.advance(1); f.held(true); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 2, "蓄力期间重复输入不会重启动作");
            f.advance(1); baby.tick();
            f.advance(12); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 0, "剩余3tick冷却时开始下一轮动作");
            f.advance(1); f.held(false); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 1, "冷却末尾开始的动作也不受松开打断");
            f.advance(2); baby.tick(); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 8 && baby.getAttackAge(0) == 3, "两轮仍相隔15tick成功发射");
            f.advance(30); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 8 && baby.getAttackAge(0) < 0, "松开不再触发额外轮次");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallStillCancelsPendingShotsWithoutRefreshingCooldown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            f.held(true); baby.tick();
            f.advance(3); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 4 && baby.getAttackAge(0) == 3, "首轮正常出弹");
            f.advance(12); baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 0, "冷却末尾提前起手以保持15tick节奏");
            f.advance(1); baby.setPos(f.hover().add(0, 40, 0)); baby.tick();
            helper.assertTrue(baby.isRecallPending() && baby.getAttackAge(0) < 0, "召回清除尚未出弹的起手");
            helper.assertTrue(baby.recallToOwner(), "实际执行召回");
            f.held(false); f.advance(1); baby.tick();
            helper.assertTrue(!baby.attackInDirection(AIM) && f.shots(baby).size() == 4, "召回不刷新已有发射冷却");
            f.advance(1); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 4, "取消的起手不会在原定时刻补发");
            helper.assertTrue(baby.attackInDirection(AIM), "原冷却到期，直接方向接口仍按实际发射返回结果");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void failedDelayedVolleyDoesNotConsumeCooldownOrLeaveAPendingShot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            f.held(true); baby.tick();
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 3);
            f.advance(3); baby.tick();
            helper.assertTrue(f.shots(baby).isEmpty() && baby.getAttackAge(0) < 0,
                    "起手后容量不足时整轮失败并清除动作，不留下半轮或已排队攻击");
            f.settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 4);
            baby.tick();
            helper.assertTrue(baby.getAttackAge(0) == 0 && f.shots(baby).isEmpty(), "失败未计冷却，可以重新起手");
            f.advance(3); baby.tick();
            helper.assertTrue(f.shots(baby).size() == 4 && baby.getAttackAge(0) == 3, "新一轮只在自己的第3tick出弹");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void allFourTearsActuallyHitTheirOwnTargetsAndAreConsumed(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            baby.attackInDirection(AIM);
            var volley = f.shots(baby);
            List<Zombie> targets = volley.stream().map(s -> f.enemy(s.position().add(s.velocity().scale(5)))).toList();
            try {
                helper.assertTrue(volley.size() == 4 && volley.stream().allMatch(s -> s.getOwner() == f.owner && s.getShooter() == baby),
                        "四颗均归玩家伤害，shooter为洛基宝宝");
                for (int tick = 0; tick < 7; tick++) BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).tick(f.owner.serverLevel());
                double expected = 100 - 2 * StatManager.DAMAGE.getBonus();
                helper.assertTrue(targets.stream().allMatch(target -> Math.abs(target.getHealth() - expected) < 0.001)
                        && f.shots(baby).isEmpty(), "四向真实命中，并在首个生物处消失");
            } finally { targets.forEach(Zombie::discard); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void bffsEnhancementAndWeakeningAreSnapshottedWithoutHiveMind(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var baby = f.add(LOKI);
            baby.attackInDirection(AIM);
            var original = f.shots(baby).get(0);
            double base = original.getDamage();
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId(), 1);
            helper.assertTrue(baby.getEnhancement() == 0, "Hive Mind不增强洛基宝宝");
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_VISUAL_SCALE.getId(), 1);
            baby.tick(); f.advance(15); baby.attackInDirection(AIM);
            helper.assertTrue(f.shots(baby).subList(4, 8).stream().allMatch(s -> s.getDamage() == 2 * base)
                    && original.getDamage() == base && Math.abs(baby.getVisualScale() - 1.25F) < 1e-7,
                    "BFFS作用于四颗新子弹和视觉缩放，不改变旧快照");
            StatManager.removeFamiliarModifier(f.owner, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
            StatManager.addFamiliarModifier(f.owner, WEAKENING, 1);
            f.advance(15); baby.attackInDirection(AIM);
            helper.assertTrue(f.shots(baby).subList(8, 12).stream().allMatch(s -> s.getDamage() == 0), "负增强安全归零");
            StatManager.removeFamiliarModifier(f.owner, WEAKENING, 1);
            f.settings.set(GameplayConfig.DAMAGE_BONUS, 0.0);
            f.advance(15); baby.attackInDirection(AIM);
            helper.assertTrue(f.shots(baby).subList(12, 16).stream().allMatch(s -> s.getDamage() == 0), "零伤害配置不产生负伤害或失败");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mixedChainCompactsAndOverflowRequestsWaitForFreedCapacity(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(BOBBY); var middle = f.add(LOKI); var last = f.add(GHOST);
            helper.assertTrue(first.getFormationIndex() == 0 && middle.getFormationIndex() == 1 && last.getFormationIndex() == 2,
                    "洛基加入跨类型链槽");
            middle.setPos(f.hover().add(5, 0, 0)); last.setPos(f.hover()); last.tick();
            helper.assertTrue(last.getDeltaMovement().x > 0, "后继跟随洛基而非玩家");
            middle.discardAndUnlink(); f.data.addCount(LOKI, FAMILIAR_SOURCE, -1);
            FamiliarHelper.maintainExistingFamiliars(f.owner);
            helper.assertTrue(last.getFormationIndex() == 1, "移除中间成员后重排");
            f.data.setCount(BOBBY, FAMILIAR_SOURCE, 63);
            f.data.setCount(LOKI, FAMILIAR_SOURCE, 100);
            helper.assertTrue(f.data.getCount(LOKI) == 100 && f.data.getEffectiveCount(LOKI) == 0, "满额时保留100份等待需求");
            f.data.addCount(BOBBY, FAMILIAR_SOURCE, -1);
            helper.assertTrue(f.data.getEffectiveCount(LOKI) == 1, "腾位后补出等待类型");
        }
        helper.succeed();
    }

    private static void tickCurios(ServerPlayer player) {
        CurioSlotManager.tick(player);
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosAbsorbedCopiesSaveReloadAndDeathCopyKeepSourceCounts(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper); Fixture clone = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(4);
            tickCurios(original.owner);
            helper.assertTrue(original.slots().isItemValid(0, new ItemStack(ForgeRegistries.ITEMS.getValue(LOKI))), "生成标签允许真实装备");
            original.slots().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(LOKI)));
            tickCurios(original.owner); tickCurios(original.owner);
            original.absorb(LOKI); original.absorb(LOKI);
            helper.assertTrue(original.data.getCount(LOKI, LOKI) == 3
                    && original.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "三份同类道具各贡献跟班和套装进度");
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
            DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner); tickCurios(restored.owner); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(LOKI, LOKI) == 3
                    && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 3, "重载不重复授予");
            restored.slots().setStackInSlot(0, ItemStack.EMPTY); tickCurios(restored.owner);
            helper.assertTrue(restored.data.getCount(LOKI, LOKI) == 2
                    && restored.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "卸下只减少一份");
            ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, restored.owner, true));
            restored.owner.reviveCaps();
            helper.assertTrue(clone.data.getCount(LOKI, LOKI) == 2
                    && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 2, "死亡复制保留吸收需求与套装计数");
            clone.items.removeFromId(clone.owner, LOKI);
            helper.assertTrue(clone.data.getCount(LOKI, LOKI) == 1, "移除一份按来源扣减");
            clone.items.clearPlayerPassiveItems(clone.owner);
            FamiliarHelper.maintainExistingFamiliars(clone.owner);
            helper.assertTrue(clone.data.getCount(LOKI) == 0 && clone.items.getSetCountFromId(ModSetAbilities.CONJOINED.getId()) == 0,
                    "清空道具由调度器收敛并清理");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void actualDimensionTransferRebuildsTheLokiRosterFromItsSource(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.absorb(LOKI);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "初始生成洛基");
            var home = f.owner.serverLevel();
            var destination = home.getServer().getLevel(Level.NETHER);
            Vec3 original = f.owner.position();
            List<LokiBabyEntity> entered = new ArrayList<>();
            Consumer<EntityJoinLevelEvent> capture = event -> {
                if (event.getLevel() == destination && event.getEntity() instanceof LokiBabyEntity baby && baby.getOwner() == f.owner)
                    entered.add(baby);
            };
            MinecraftForge.EVENT_BUS.addListener(capture);
            try {
                f.owner.teleportTo(destination, original.x, original.y, original.z, 0, 0);
                FamiliarHelper.maintainExistingFamiliars(f.owner);
                destination.getChunkAt(f.owner.blockPosition());
                helper.assertTrue(f.data.getCount(LOKI, LOKI) == 1 && FamiliarHelper.spawnNextMissingFamiliar(f.owner)
                                && entered.size() == 1 && f.data.containsEntity(LOKI, entered.get(0).getUUID()), "实际跨维度恢复来源及运行名册");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(capture);
                entered.forEach(AbstractIsaacFamiliarEntity::discardAndUnlink);
                f.owner.teleportTo(home, original.x, original.y, original.z, 0, 0);
            }
            helper.assertTrue(f.data.getCount(LOKI, LOKI) == 1 && FamiliarHelper.spawnNextMissingFamiliar(f.owner), "返回原维度正常补出");
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
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "loki-test"));
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
