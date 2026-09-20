package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.TrajectoryAttachment;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrajectoryAttachmentTest {
    @Test void registeredRootsDispatchAndPreserveConcreteTypes() throws Exception {
        var bullet = ResourceLocation.parse("test:bullet");
        var laser = ResourceLocation.parse("test:laser");
        var trigger = new TrajectoryAttachment(bullet, laser);
        for (var variant : TestAttackTypes.PROJECTILES) {
            var context = PatternTestSupport.context(0, 0).toBuilder().attackType(variant).build();
            assertEquals(variant.getRootId(), context.getRootTypeId());
            trigger.attachToBullet(new ExecutableEffectContext(null), context);
            assertEquals(List.of(new TrajectorySpec(context.isLaserAttack() ? laser : bullet, 0)), context.getTrajectorySpecs());
            assertEquals(variant, context.toBuilder().build().getAttackType());
        }
    }
    @Test void allSourceTypesSurviveCopiesAndFreeze() throws Exception {
        for (var type : TestAttackTypes.PROJECTILES) {
            AttackContext original = PatternTestSupport.context(0, 0).toBuilder().attackType(type).build();
            assertEquals(type, original.copy().getAttackType());
            assertEquals(type, original.toBuilder().build().getAttackType());
            original.freeze();
            original.setAttackType(null);
            assertEquals(type, original.getAttackType());
        }
        assertNull(PatternTestSupport.context(0, 0).toBuilder().attackType(null).build().getTypeId());
    }
    @Test
    void laserFamilySurvivesCopiesAndSelectsOnlyLaserModule() throws Exception {
        ResourceLocation bulletId = ResourceLocation.parse("isaac_disaster:tiny_planet_bullet");
        ResourceLocation laserId = ResourceLocation.parse("isaac_disaster:tiny_planet_laser");
        AttackContext original = PatternTestSupport.context(0, 0);
        original.setAttackType(TestAttackTypes.LASER);
        TrajectoryAttachment trigger = new TrajectoryAttachment(bulletId, laserId);
        for (AttackContext copy : List.of(original.copy(), original.toBuilder().build())) {
            assertTrue(copy.isLaserAttack());
            trigger.attachToBullet(new ExecutableEffectContext(null), copy);
            assertEquals(List.of(new TrajectorySpec(laserId, 0)), copy.getTrajectorySpecs());
        }
        AttackContext ordinary = PatternTestSupport.context(0, 0);
        trigger.attachToBullet(new ExecutableEffectContext(null), ordinary);
        assertEquals(List.of(new TrajectorySpec(bulletId, 0)), ordinary.getTrajectorySpecs());
    }

    private static final ResourceLocation ID = ResourceLocation.parse("isaac_disaster:wiggle_worm");

    @Test
    void onlyPrepareTriggerAttachesAndUsesDispatchedStackCount() throws Exception {
        AttackContext attack = PatternTestSupport.context(0, 0);
        ExecutableEffectContext context = new ExecutableEffectContext(null);
        context.set(ContextKeys.ATTACK_CONTEXT, attack);
        context.set(ContextKeys.AMPLIFIER, 3.0D);
        TrajectoryAttachment trigger = new TrajectoryAttachment(ID);
        trigger.fire(context, ModTriggerTypes.ATTACK_PLAN);
        assertTrue(attack.getTrajectorySpecs().isEmpty());
        trigger.fire(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        assertEquals(List.of(new TrajectorySpec(ID, 2)), attack.getTrajectorySpecs());
        attack.freeze();
        trigger.fire(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        assertEquals(List.of(new TrajectorySpec(ID, 2)), attack.getTrajectorySpecs());
    }

    @Test
    void attachmentCopiesAndReadsDoNotShareMutableModuleStacks() throws Exception {
        AttackContext original = PatternTestSupport.context(0, 0);
        original.addTrajectoryModule(ID, 2);
        AttackContext copy = original.copy();
        copy.addTrajectoryModule(ID, 1);
        original.copyTrajectorySequence().add(ID, 5);

        assertEquals(List.of(new TrajectorySpec(ID, 1)), original.getTrajectorySpecs());
        assertEquals(List.of(new TrajectorySpec(ID, 2)), copy.getTrajectorySpecs());
        assertThrows(UnsupportedOperationException.class, () -> original.getTrajectorySpecs().clear());
    }

    @Test
    void frozenContextCannotAttachMoreModules() throws Exception {
        AttackContext context = PatternTestSupport.context(0, 0);
        context.addTrajectoryModule(ID, 1);
        context.freeze();
        context.addTrajectoryModule(ID, 3);

        assertEquals(List.of(new TrajectorySpec(ID, 0)), context.getTrajectorySpecs());
        assertTrue(context.copy().isFrozen());
    }
}
