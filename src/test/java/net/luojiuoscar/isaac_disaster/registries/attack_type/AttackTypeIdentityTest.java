package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.TrajectoryAttachment;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AttackTypeIdentityTest {
    @Test void derivedAttacksDeclareConcreteAndRootIds() {
        var types = List.of(new BulletAttack(0), new CSectionAttack(0), new LaserAttack(0),
                new BrimstoneAttack(0), new Technology2Attack(0), new ShoopDaWhoop(0));
        var names = List.of("bullet", "c_section", "laser", "brimstone", "technology2", "shoop_da_whoop");
        for (int i = 0; i < types.size(); i++) {
            assertEquals(ResourceLocation.parse("isaac_disaster:" + names.get(i)), types.get(i).getId());
            assertEquals(ResourceLocation.parse("isaac_disaster:" + (i < 2 ? "bullet" : "laser")), types.get(i).getRootId());
        }
    }

    @Test void externalLaserKeepsConcreteIdentityAndSelectsLaserModule() throws Exception {
        var type = new LaserAttack(0) {
            @Override public ResourceLocation getId() { return ResourceLocation.parse("addon:ice_laser"); }
        };
        var context = PatternTestSupport.context(0, 0).toBuilder().attackType(type).build();
        var attachment = new TrajectoryAttachment(ResourceLocation.parse("test:bullet"), ResourceLocation.parse("test:laser"));
        attachment.attachToBullet(new ExecutableEffectContext(null), context);
        assertEquals(ResourceLocation.parse("test:laser"), context.getTrajectorySpecs().get(0).id());
        context.freeze();
        context.setAttackType(new BulletAttack(0));
        for (var copy : List.of(context, context.copy(), context.copyConfiguration(), context.toBuilder().build())) {
            assertEquals(type.getId(), copy.getTypeId());
            assertEquals(type.getRootId(), copy.getRootTypeId());
        }
    }

    @Test void externalRootDoesNotFallBackToOrdinaryTrajectory() throws Exception {
        var type = new BulletAttack(0) {
            @Override public ResourceLocation getId() { return ResourceLocation.parse("addon:flame_stream"); }
            @Override public ResourceLocation getRootId() { return getId(); }
        };
        var context = PatternTestSupport.context(0, 0).toBuilder().attackType(type).build();
        new TrajectoryAttachment(ResourceLocation.parse("test:bullet"), ResourceLocation.parse("test:laser"))
                .attachToBullet(new ExecutableEffectContext(null), context);
        assertTrue(context.getTrajectorySpecs().isEmpty());
    }
    @Test void retypingPreservesSnapshotButBuilderDerivationRecalculatesSize() throws Exception {
        var original = PatternTestSupport.context(0, 0);
        original.setBulletScale(7, true);
        original.freeze();
        var rebound = original.bindAttackTypeOrCopy(TestAttackTypes.LASER);
        assertNotSame(original, rebound);
        assertEquals(7, rebound.getBulletScale());
        assertEquals(TestAttackTypes.BULLET.getId(), original.getTypeId());
        assertTrue(original.isFrozen());
        assertFalse(rebound.isFrozen());
        assertEquals(7, original.copy().getBulletScale());
        var child = original.toBuilder().damage(2.0).build();
        assertEquals(1, child.getBulletScale(), 1e-12);
    }

    @Test void triggerAccessIsDeliberatelyMutableAndCopiesStayIndependent() throws Exception {
        var context = PatternTestSupport.context(0, 0);
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        var trigger = new net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger(null, null) {
            @Override public void fire(ExecutableEffectContext ignored) { calls.incrementAndGet(); }
        };
        context.freeze();
        var copy = context.copy();
        context.getTrigger().add(trigger);
        copy.getTrigger().fire(new ExecutableEffectContext(null), null);
        assertEquals(0, calls.get());
        context.getTrigger().fire(new ExecutableEffectContext(null), null);
        assertEquals(1, calls.get());
        context.addSimpleTrigger(trigger);
        context.getTrigger().fire(new ExecutableEffectContext(null), null);
        assertEquals(2, calls.get(), "Controlled additions stay frozen; direct collection access remains mutable");
    }

    @Test void contextualProjectileKeepsOneAuthoritativeTypeDeclaration() throws Exception {
        var context = PatternTestSupport.context(0, 0).toBuilder().attackType(TestAttackTypes.C_SECTION).build();
        var bullet = net.luojiuoscar.isaac_disaster.bullet.core.BulletState.builder().attackContext(context).build();
        assertEquals(context.getTypeId(), bullet.getTypeId());
        assertEquals(context.getRootTypeId(), bullet.getRootTypeId());
        assertEquals(bullet.getTypeId(), bullet.getAttackContext().getTypeId());
    }

}
