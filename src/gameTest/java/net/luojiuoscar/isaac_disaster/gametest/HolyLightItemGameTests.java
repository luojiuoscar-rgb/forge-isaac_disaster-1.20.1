package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.HolyLightAttachment;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.HolyLightImpact;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@GameTestHolder("isaac_disaster_holy_light_item")
@PrefixGameTestTemplate(false)
public final class HolyLightItemGameTests {
    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void restrictedMeleeAddsHolyDamageWithoutSuppressingTheOriginalHit(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.HOLY_LIGHT.get().handleObtain(f.owner, null);
            Cow target = f.cow(f.center);
            f.owner.getRandom().setSeed(4096);
            helper.assertTrue(target.hurt(f.level.damageSources().playerAttack(f.owner), 2), "The original melee attack still lands");
            health(helper, target, 198);
            ScheduledFuncHelper.tick(f.level.getServer());
            helper.assertTrue(f.strikes().size() == 1, "A successful restricted-hit roll creates one holy strike");
            health(helper, target, 192);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void restrictedArrowUsesTheSavedIncomingDamageAndHasNoWarning(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.HOLY_LIGHT.get().handleObtain(f.owner, null);
            Cow target = f.cow(f.center);
            Arrow arrow = new Arrow(f.level, f.owner);
            f.owner.getRandom().setSeed(4096);
            helper.assertTrue(target.hurt(f.level.damageSources().arrow(arrow, f.owner), 3), "Vanilla arrow damage is an eligible restricted hit");
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
            ScheduledFuncHelper.tick(f.level.getServer());
            helper.assertTrue(f.strikes().size() == 1, "A vanilla projectile can trigger the holy effect");
            helper.assertTrue(f.strikes().get(0).warningTicks() == 0, "The deferred payoff still skips all warning visuals");
            health(helper, target, 188);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void owningHolyLightDoesNotDoubleProcModTearsOrHolyDamage(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.HOLY_LIGHT.get().handleObtain(f.owner, null);
            Cow target = f.cow(f.center);
            f.owner.getRandom().setSeed(4096);
            f.bullets.spawn(f.bullet(f.holyContext(), f.center.add(-1.5, .8, 0), new Vec3(2, 0, 0), false));
            f.bullets.tick(f.level);
            ScheduledFuncHelper.tick(f.level.getServer());
            helper.assertTrue(f.strikes().size() == 1, "Custom tear and holy damage must not re-enter the restricted proc");
            health(helper, target, 192);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void impactUsesSavedBulletDamageAndDoublesOnlyUndead(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow normal = f.cow(f.center.add(.3, 0, 0));
            Zombie undead = f.add(new Zombie(EntityType.ZOMBIE, f.level), f.center.add(-.3, 0, 0));
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
            BulletState bullet = f.bullet(f.context(ModAttackTypes.BULLET.get()), f.center, Vec3.ZERO, false);
            helper.assertTrue(f.impact(bullet, normal, 999), "A valid collision creates a strike");
            health(helper, normal, 194);
            health(helper, undead, 188);
            helper.assertTrue(f.strikes().size() == 1, "Holy damage does not recursively create another strike");
            helper.assertTrue(f.strikes().get(0).warningTicks() == 0, "The beam fires immediately");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void blockImpactUsesTheContactPointOutsideTheWall(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            BlockPos wall = BlockPos.containing(f.center).east();
            f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
            Vec3 contact = new Vec3(wall.getX(), f.center.y + .3, f.center.z);
            BulletState bullet = f.bullet(f.context(ModAttackTypes.BULLET.get()), contact, Vec3.ZERO, false);
            var event = new IsaacAttackHitBlockEvent(bullet, f.owner, bullet.getTypeId(), bullet.getTriggers(),
                    new BlockHitResult(contact, Direction.WEST, wall, false));
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.EVENT, event);
            context.set(ContextKeys.TARGET_POSITION, wall.getCenter());
            helper.assertTrue(new HolyLightImpact().applyEffect(context), "The wall contact is a valid strike position");
            HolyLightSnapshot strike = f.strikes().get(0);
            helper.assertTrue(Math.abs(strike.position().x - (contact.x - .01)) < .0001,
                    "The beam starts just outside the exact contact, not at the block center");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void airborneTargetTakesImmediateDamageWithoutGroundScorch(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Vec3 position = f.center.add(0, 4, 0);
            Cow target = f.cow(position);
            var bullet = f.bullet(f.context(ModAttackTypes.BULLET.get()), position, Vec3.ZERO, false);
            helper.assertTrue(f.impact(bullet, target, 2), "Airborne impact remains valid");
            health(helper, target, 194);
            helper.assertTrue(!f.strikes().get(0).grounded(), "Airborne beams do not create a scorch decal");
            helper.assertTrue(Math.abs(f.strikes().get(0).position().y - position.y) < .0001, "The beam stays at the target's feet");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void piercingTearTriggersOnceForEachSuccessfulTarget(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow first = f.cow(f.center.add(-.75, 0, 0));
            Cow second = f.cow(f.center.add(.75, 0, 0));
            AttackContext context = f.holyContext();
            f.bullets.spawn(f.bullet(context, f.center.add(-2, .8, 0), new Vec3(4.5, 0, 0), true));
            f.bullets.tick(f.level);
            helper.assertTrue(f.strikes().size() == 2, "A piercing holy tear triggers for both successful collisions");
            health(helper, first, 192);
            health(helper, second, 192);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void rejectedTearDamageAndNaturalExpiryDoNotTrigger(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            Consumer<LivingAttackEvent> cancel = event -> {
                if (event.getEntity() == target) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingAttackEvent.class, cancel);
            try {
                f.bullets.spawn(f.bullet(f.holyContext(), f.center.add(-1.5, .8, 0), new Vec3(2, 0, 0), false));
                f.bullets.tick(f.level);
                helper.assertTrue(f.strikes().isEmpty(), "Failed hurt has no after-hit proc");
                health(helper, target, 200);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
            f.bullets.clear();
            f.bullets.spawn(BulletState.from(f.holyContext()).position(f.center.add(0, 5, 0))
                    .velocity(Vec3.ZERO).lifetime(1).range(8).build());
            f.bullets.tick(f.level);
            helper.assertTrue(f.strikes().isEmpty(), "Natural expiry has no holy trigger");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void appleHitMultiplierDoesNotMultiplyTheHolyDamageAgain(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            AttackContext context = f.holyContext();
            context.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.APPLE_DAMAGE));
            f.bullets.spawn(f.bullet(context, f.center.add(-1.5, .8, 0), new Vec3(2, 0, 0), false));
            f.bullets.tick(f.level);
            helper.assertTrue(f.strikes().size() == 1, "The normal tear hit triggers one holy strike");
            health(helper, target, 186); // Eight tear damage plus six holy damage.
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void laserProcKeepsSnapshotDamageDespiteTechnologyTwoDiscount(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            AttackContext laser = f.context(ModAttackTypes.TECHNOLOGY2.get()).toBuilder()
                    .position(f.center.add(-2, .8, 0)).mainAxis(new Vec3(1, 0, 0)).build();
            var preparation = new ExecutableEffectContext(f.owner);
            preparation.set(ContextKeys.ATTACK_CONTEXT, laser);
            preparation.set(ContextKeys.EFFECT_ATTACHMENT_NODE, laser);
            new HolyLightAttachment().apply(preparation);
            f.owner.getRandom().setSeed(4096);
            ModAttackTypes.TECHNOLOGY2.get().performAttack(List.of(laser));
            helper.assertTrue(f.strikes().size() == 1, "A successful laser hurt independently rolls a holy strike");
            health(helper, target, 193.8F); // 0.2 laser damage plus six holy damage.
            helper.assertTrue(laser.getColorRl().equals(ModBulletColors.BASE.getId()), "The laser retains its original color");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void copiesShareOneModuleAndRemovalDoesNotEraseFiredTearEffects(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ability = ModPassiveAbilities.HOLY_LIGHT.get();
            ability.handleObtain(f.owner, null);
            ability.handleObtain(f.owner, null);
            var modules = f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new).getTriggerModules();
            helper.assertTrue(modules.get(ModTriggerModules.HOLY_LIGHT.getId()).stacks() == 2, "Copies contribute ownership counts to one module");
            AttackContext saved = f.holyContext();
            ability.handleRemove(f.owner, null);
            helper.assertTrue(modules.contains(ModTriggerModules.HOLY_LIGHT.getId()), "One remaining copy retains the module");
            ability.handleRemove(f.owner, null);
            helper.assertTrue(!modules.contains(ModTriggerModules.HOLY_LIGHT.getId()), "Last removal removes the module");
            AttackContext fresh = f.context(ModAttackTypes.BULLET.get());
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.ATTACK_CONTEXT, fresh);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, fresh);
            TriggerModuleEvents.dispatch(context, ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE);
            helper.assertTrue(!fresh.copyTrigger().getView().contains(ModExecutableEffects.HOLY_LIGHT_IMPACT.get()), "New tears no longer gain the effect");
            Cow target = f.cow(f.center);
            f.bullets.spawn(f.bullet(saved, f.center.add(-1.5, .8, 0), new Vec3(2, 0, 0), false));
            f.bullets.tick(f.level);
            helper.assertTrue(f.strikes().size() == 1, "An already fired tear retains its trigger after removal");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light_item")
    public static void missingOrBeforeHitEventDoesNotCreateAFreeStrike(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var effect = new HolyLightImpact();
            var context = new ExecutableEffectContext(f.owner);
            helper.assertTrue(!effect.applyEffect(context), "A real collision event is required");
            Cow target = f.cow(f.center);
            BulletState bullet = f.bullet(f.context(ModAttackTypes.BULLET.get()), f.center, Vec3.ZERO, false);
            context.set(ContextKeys.EVENT, new IsaacAttackBeforeHitEntityEvent(bullet, f.owner,
                    bullet.getTypeId(), bullet.getTriggers(), new EntityHitResult(target), 2));
            helper.assertTrue(!effect.applyEffect(context), "A before-hit event cannot bypass successful hurt");
            helper.assertTrue(f.strikes().isEmpty(), "Invalid contexts do not create beams");
        }
        helper.succeed();
    }

    private static void health(GameTestHelper helper, LivingEntity target, float expected) {
        helper.assertTrue(Math.abs(target.getHealth() - expected) < .001,
                "Expected health " + expected + ", got " + target.getHealth());
    }

    private static final class Fixture implements AutoCloseable {
        private final ServerLevel level;
        private final ServerPlayer owner;
        private final Vec3 center;
        private final BulletManager bullets = new BulletManager();
        private final List<Mob> targets = new ArrayList<>();
        private final Set<Long> existing;

        private Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            center = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
            for (int x = -2; x <= 2; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos base = BlockPos.containing(center).below().offset(x, 0, z);
                    level.setBlockAndUpdate(base, Blocks.STONE.defaultBlockState());
                    for (int y = 1; y <= 17; y++) level.setBlockAndUpdate(base.above(y), Blocks.AIR.defaultBlockState());
                }
            }
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "holy_" + id.toString().substring(0, 8)));
            owner.setPos(center.add(5, 0, 0));
            level.addNewPlayer(owner);
            existing = HolyLightRuntime.INSTANCE.manager(level).snapshots().stream().map(HolyLightSnapshot::id).collect(Collectors.toSet());
        }

        private <T extends Mob> T add(T target, Vec3 position) {
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            target.setHealth(200);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(position);
            level.addFreshEntity(target);
            targets.add(target);
            return target;
        }

        private Cow cow(Vec3 position) {
            return add(new Cow(EntityType.COW, level), position);
        }

        private AttackContext context(AttackType type) {
            return AttackContext.builder(owner, owner).attackType(type).damage(2.0).bulletScaleModifier(0.0)
                    .range(8).speed(2).position(center).mainAxis(new Vec3(1, 0, 0)).color(ModBulletColors.BASE.getId()).build();
        }

        private AttackContext holyContext() {
            AttackContext context = context(ModAttackTypes.BULLET.get());
            context.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModExecutableEffects.HOLY_LIGHT_IMPACT));
            context.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.HOLY_LIGHT_IMPACT));
            return context;
        }

        private BulletState bullet(AttackContext context, Vec3 position, Vec3 velocity, boolean piercing) {
            return BulletState.from(context).position(position).velocity(velocity).baseSpeed(velocity.length())
                    .lifetime(8).collisionWidth(.2).collisionHeight(.2).piercing(piercing).build();
        }

        private boolean impact(BulletState bullet, LivingEntity target, double eventDamage) {
            var context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.EVENT, new IsaacAttackAfterHitEvent(bullet, owner, bullet.getTypeId(),
                    bullet.getTriggers(), new EntityHitResult(target), eventDamage, target.getHealth()));
            return new HolyLightImpact().applyEffect(context);
        }

        private List<HolyLightSnapshot> strikes() {
            return HolyLightRuntime.INSTANCE.manager(level).snapshots().stream().filter(s -> !existing.contains(s.id())).toList();
        }

        @Override
        public void close() {
            bullets.clear();
            for (Mob target : targets) target.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
