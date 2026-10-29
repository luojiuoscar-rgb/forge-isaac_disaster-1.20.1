package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.SinusInfectionDamage;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.CricketsBodySplitModule;
import net.luojiuoscar.isaac_disaster.system.damage.HurtKnockback;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_pisces")
@PrefixGameTestTemplate(false)
public final class PiscesGameTests {
    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void defaultsAndCopiesStackAndRemoveSymmetrically(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.2);
            double melee = f.owner.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            ModPassiveAbilities.PISCES.get().handleObtain(f.owner, null);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.5);
            close(helper, f.owner.getAttributeValue(Attributes.ATTACK_KNOCKBACK), melee + 0.5);
            ModPassiveAbilities.PISCES.get().handleObtain(f.owner, null);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.8);
            ModPassiveAbilities.PISCES.get().handleRemove(f.owner, null);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.5);
            ModPassiveAbilities.PISCES.get().handleRemove(f.owner, null);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.2);
            close(helper, f.owner.getAttributeValue(Attributes.ATTACK_KNOCKBACK), melee);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void configuredDefaultsAndUnitBonusDoNotCancelEachOther(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.DEFAULT_BULLET_KNOCKBACK, 0.4);
            settings.set(GameplayConfig.BULLET_KNOCKBACK_BONUS, 0.2);
            DefaultAttributeManager.apply(f.owner);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.4);
            ModPassiveAbilities.PISCES.get().handleObtain(f.owner, null);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 1.0);
            DefaultAttributeManager.apply(f.owner);
            DefaultAttributeManager.apply(f.owner);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 1.0);
            helper.assertTrue(ModPassiveAbilities.PISCES.get().getDesc(null, f.owner).get(4).getString().contains("0.6"),
                    "The fifth lore line uses the configured three-unit value");
            ModPassiveAbilities.PISCES.get().handleRemove(f.owner, null);
            close(helper, f.owner.getAttributeValue(ModAttributes.BULLET_KNOCKBACK.get()), 0.4);
            close(helper, HurtKnockback.captureMultiplier(f.cow(f.origin.add(4, 0, 0))), 0.4);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void shotsAndSplitChildrenKeepTheirLaunchSnapshot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var original = f.attack();
            ModPassiveAbilities.PISCES.get().handleObtain(f.owner, null);
            var parent = f.bullet(original);
            close(helper, parent.getBulletKnockback(), 0.2);
            close(helper, f.bullet(f.attack()).getBulletKnockback(), 0.5);
            var module = new CricketsBodySplitModule();
            var event = new BulletSplitEvent(parent, new SplitSequence(), parent.getAttackContext(), SplitTriggerType.ENTITY);
            var split = new SplitContext(event, ModSplitModules.CRICKETS_BODY.getId(), module, 1, 1);
            var children = module.generate(split);
            helper.assertTrue(children.size() == 4, "The real Cricket's Body generator creates four children");
            for (var child : children) close(helper, f.bullet(child).getBulletKnockback(), 0.2);
            var strengthened = f.attack();
            ModPassiveAbilities.PISCES.get().handleRemove(f.owner, null);
            close(helper, f.bullet(strengthened).getBulletKnockback(), 0.5);
            close(helper, f.bullet(f.attack()).getBulletKnockback(), 0.2);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void piercingBulletUsesSnapshotForEverySuccessfulHit(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.cow(f.origin.add(0.8, 0, 0));
            var second = f.cow(f.origin.add(1.8, 0, 0));
            f.owner.getAttribute(ModAttributes.BULLET_KNOCKBACK.get()).setBaseValue(0.5);
            var bullet = f.bullet(f.attack());
            f.owner.getAttribute(ModAttributes.BULLET_KNOCKBACK.get()).setBaseValue(0.8);
            f.bullets.spawn(bullet);
            f.bullets.tick(f.level);
            for (var target : List.of(first, second)) {
                helper.assertTrue(target.getHealth() < 200, "Both targets receive the piercing hit");
                close(helper, target.getDeltaMovement().x, 0.4 * 0.5);
                close(helper, target.getDeltaMovement().y, 0);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void nativeStrengthAndResistanceAreAppliedOnceWithoutVerticalMotion(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var target = f.cow(f.origin.add(2, 0, 0));
            for (double multiplier : new double[] {0, 0.2, 0.5, 0.8}) {
                target.setDeltaMovement(0.1, 0.37, -0.1);
                target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25);
                helper.assertTrue(target.hurt(ModDamageSources.tear(f.level, f.owner, new Vec3(3, 9, 4), multiplier), 1),
                        "Each same-tick tear is accepted");
                close(helper, target.getDeltaMovement().x, 0.1 + 0.4 * multiplier * 0.75 * 0.6);
                close(helper, target.getDeltaMovement().y, 0.37);
                close(helper, target.getDeltaMovement().z, -0.1 + 0.4 * multiplier * 0.75 * 0.8);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void sinusInfectionDamageDoesNotUseTheStoredTearKnockback(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var target = f.cow(f.origin.add(2, 0, 0));
            var bullet = f.bullet(f.attack().toBuilder().bulletKnockback(1024).build());
            var stick = new BulletStickContext(bullet, target, target.getUUID(), target.position(), 20, 100, null);
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.BULLET, bullet);
            context.set(ContextKeys.BULLET_STICK, stick);
            Vec3 initial = new Vec3(0.1, 0.37, -0.1);
            target.setDeltaMovement(initial);
            helper.assertTrue(new SinusInfectionDamage().applyEffect(context), "The real DOT effect accepts its attachment context");
            close(helper, target.getHealth(), 199);
            helper.assertTrue(target.getDeltaMovement().equals(initial), "DOT deals damage without knockback");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "pisces")
    public static void zeroDirectionCannotProduceKnockbackEvenWithALargeMultiplier(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var target = f.cow(f.origin.add(2, 0, 0));
            Vec3 initial = new Vec3(0.1, 0.37, -0.1);
            target.setDeltaMovement(initial);
            target.hurt(ModDamageSources.tear(f.level, f.owner, Vec3.ZERO, 1024), 1);
            helper.assertTrue(target.getDeltaMovement().equals(initial), "No source direction means no added impulse");
            target.hurt(ModDamageSources.tear(f.level, f.owner, new Vec3(1, 0, 0), 0), 1);
            helper.assertTrue(target.getDeltaMovement().equals(initial), "DOT-style zero multiplier preserves velocity");
        }
        helper.succeed();
    }

    private static void close(GameTestHelper helper, double actual, double expected) {
        helper.assertTrue(Math.abs(actual - expected) < 1e-6, "Expected " + expected + ", got " + actual);
    }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 origin;
        final BulletManager bullets = new BulletManager();
        final List<Cow> targets = new ArrayList<>();

        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            origin = helper.absoluteVec(new Vec3(1.5, 3, 1.5));
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "p_" + id.toString().substring(0, 8)));
            owner.setPos(origin.add(0, 5, 0));
            level.addNewPlayer(owner);
        }

        Cow cow(Vec3 position) {
            var cow = new Cow(EntityType.COW, level);
            cow.setNoAi(true);
            cow.setNoGravity(true);
            cow.setOnGround(true);
            cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            cow.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0);
            cow.getAttribute(Attributes.ARMOR).setBaseValue(0);
            cow.setHealth(200);
            cow.setPos(position);
            level.addFreshEntity(cow);
            targets.add(cow);
            return cow;
        }

        AttackContext attack() {
            return AttackContext.builder(owner, owner).attackType(ModAttackTypes.BULLET.get()).damage(1.0)
                    .bulletScaleModifier(0.0).range(10).speed(2.5).position(origin).mainAxis(new Vec3(1, 0, 0)).build();
        }

        BulletState bullet(AttackContext attack) {
            return BulletState.from(attack).position(origin).velocity(new Vec3(2.5, 0, 0)).baseSpeed(2.5)
                    .lifetime(20).piercing(true).collisionWidth(0.2).collisionHeight(0.2).build();
        }

        @Override
        public void close() {
            bullets.clear();
            targets.forEach(Entity::discard);
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
