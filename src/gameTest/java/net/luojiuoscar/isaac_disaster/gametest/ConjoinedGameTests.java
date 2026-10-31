package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipeline;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

@GameTestHolder("isaac_disaster_conjoined")
@PrefixGameTestTemplate(false)
public final class ConjoinedGameTests {
    @GameTest(template = "trajectory_empty")
    public static void thresholdCrossingsApplyAndRestoreBothStatsOnce(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            double damage = f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double tears = f.owner.getAttributeValue(ModAttributes.TEARS.get());
            f.changeSet(2);
            f.assertStats(damage, tears);
            f.changeSet(1);
            f.assertStats(damage - 0.3 * StatManager.DAMAGE.getBonus(), tears - 0.3 * StatManager.TEARS.getBonus());
            helper.assertTrue(f.fire(ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY).size() == 3, "三份进度激活两颗额外泪弹");
            f.changeSet(5);
            f.changeSet(-5);
            f.assertStats(damage - 0.3 * StatManager.DAMAGE.getBonus(), tears - 0.3 * StatManager.TEARS.getBonus());
            helper.assertTrue(f.fire(ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY).size() == 3, "超过门槛不重复添加模块");
            f.changeSet(-1);
            f.assertStats(damage, tears);
            helper.assertTrue(f.fire(ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY).size() == 1, "跌破门槛移除额外发射");
            f.changeSet(1);
            f.assertStats(damage - 0.3 * StatManager.DAMAGE.getBonus(), tears - 0.3 * StatManager.TEARS.getBonus());
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void multishotAlwaysAddsOnlyTwoAtSymmetricFortyFiveDegrees(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.changeSet(3);
            for (int count : List.of(1, 5, 17)) {
                f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(count);
                for (float pitch : new float[]{0, 35, -90, 90}) {
                    f.owner.setXRot(pitch);
                    f.owner.setYRot(37);
                    var contexts = f.fire(ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY);
                    helper.assertTrue(contexts.size() == count + 2, "每轮固定额外两发，不按主弹数量复制");
                    Vec3 aim = Vec3.directionFromRotation(pitch, 37).normalize();
                    Vec3 left = contexts.get(count).getMainAxis();
                    Vec3 right = contexts.get(count + 1).getMainAxis();
                    near(helper, left.length(), 1, "左弹单位方向");
                    near(helper, right.length(), 1, "右弹单位方向");
                    near(helper, aim.dot(left), Math.sqrt(0.5), "左弹相对目视方向45度");
                    near(helper, aim.dot(right), Math.sqrt(0.5), "右弹相对目视方向45度");
                    near(helper, left.dot(right), 0, "两弹夹角90度，包括垂直瞄准");
                    near(helper, left.add(right).normalize().distanceTo(aim), 0, "额外方向围绕目视方向对称");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void extraContextsInheritAttackTypeColorAndPreparationModules(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.changeSet(3);
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
            ability.addBulletColor(ModBulletColors.BLOOD_TEAR.getId(), 1);
            ability.setSpectral(1);
            ability.setHoming(1);
            StatManager.addTriggerModule(f.owner, ModTriggerModules.CRICKETS_BODY.getId(), 1);
            StatManager.addTriggerModule(f.owner, ModTriggerModules.TINY_PLANET.getId(), 1);
            for (var type : List.of(ModAttackTypes.BULLET.get(), ModAttackTypes.LASER.get(), ModAttackTypes.BRIMSTONE.get())) {
                var contexts = f.fire(type, AttackOrigin.PLAYER_PRIMARY);
                helper.assertTrue(contexts.size() == 3, "普通泪弹、激光和蓄力硫磺火各增加两发");
                for (var context : contexts) {
                    helper.assertTrue(type.getId().equals(context.getTypeId()), "额外攻击保留当前攻击类型");
                    helper.assertTrue(ModBulletColors.BLOOD_TEAR.getId().equals(context.getColorRl()), "额外攻击继承弹色");
                    helper.assertTrue(!context.getSplitSequence().isEmpty(), "额外攻击也经过分裂模块附加流程");
                    helper.assertTrue(!context.getTrajectorySpecs().isEmpty(), "额外攻击也经过轨迹附加流程");
                    near(helper, context.getDamage(), f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE), "伤害包含套装扣减");
                }
            }
            var bullets = BulletRuntime.INSTANCE.manager(f.owner.serverLevel()).activeStates().stream()
                    .filter(state -> f.owner.getUUID().equals(state.ownerUuid())).toList();
            helper.assertTrue(bullets.size() == 3, "实际生成三颗普通泪弹");
            helper.assertTrue(bullets.stream().allMatch(state -> state.isSpectral() && state.isHoming()), "侧弹继承穿墙和追踪");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void derivedRequestsDoNotRecursivelyAddConjoinedShots(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.changeSet(3);
            for (var origin : List.of(AttackOrigin.ABILITY_EXTRA, AttackOrigin.BULLET_SECONDARY, AttackOrigin.SPLIT_CHILD, AttackOrigin.SYSTEM)) {
                helper.assertTrue(f.fire(ModAttackTypes.BULLET.get(), origin).size() == 1, "额外攻击和分裂不再触发连体婴");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void reloadKeepsOnePenaltyAndOneModuleAndRemovalRestoresStats(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            double damage = original.owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double tears = original.owner.getAttributeValue(ModAttributes.TEARS.get());
            original.changeSet(3);
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
            DefaultAttributeManager.apply(restored.owner);
            restored.assertStats(damage - 0.3 * StatManager.DAMAGE.getBonus(), tears - 0.3 * StatManager.TEARS.getBonus());
            helper.assertTrue(restored.fire(ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY).size() == 3, "重载保留且不重复触发额外攻击");
            restored.changeSet(-1);
            restored.assertStats(damage, tears);
            helper.assertTrue(restored.fire(ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY).size() == 1, "重载后移除套装正常清理模块");
        }
        helper.succeed();
    }

    private static void near(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-5, message + ": " + actual + " / " + expected);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "conjoined-test"));
            DefaultAttributeManager.apply(owner);
            owner.setPos(helper.absoluteVec(new Vec3(4, 3, 4)));
        }

        private void changeSet(int delta) {
            StatManager.modifySetWithId(owner, ModSetAbilities.CONJOINED.getId(), delta);
        }

        private void assertStats(double damage, double tears) {
            near(helper, owner.getAttributeValue(Attributes.ATTACK_DAMAGE), damage, "套装伤害只改变一次");
            near(helper, owner.getAttributeValue(ModAttributes.TEARS.get()), tears, "套装射速只改变一次");
        }

        private List<AttackContext> fire(AttackType type, AttackOrigin origin) {
            List<AttackContext> contexts = new ArrayList<>();
            Consumer<AttackContextPrepareEvent> capture = event -> {
                if (event.getOwner() == owner) contexts.add(event.getAttackContext());
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, capture);
            try {
                helper.assertTrue(AttackPipeline.executeRequest(AttackRequest.generated(owner, type, origin,
                        AttackPipelineMode.FULL, false)), "攻击成功执行");
                return contexts;
            } finally {
                MinecraftForge.EVENT_BUS.unregister(capture);
            }
        }

        @Override
        public void close() {
            ScheduledFuncHelper.clearPlayerTasks(owner.getUUID());
            var manager = BulletRuntime.INSTANCE.manager(owner.serverLevel());
            manager.activeStates().stream().filter(state -> owner.getUUID().equals(state.ownerUuid()))
                    .toList().forEach(manager::remove);
        }
    }
}
