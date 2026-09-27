package net.luojiuoscar.isaac_disaster.registries.trajectory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.TrajectoryAttachment;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class TrajectoryAttachmentTest {
    @Test
    void attachmentExposesExplicitMappingAndUniversalConstructors() {
        var constructors = TrajectoryAttachment.class.getConstructors();

        assertEquals(2, constructors.length);
        assertTrue(
            java.util.Arrays.stream(constructors)
                .anyMatch(
                    c ->
                        java.util.Arrays.equals(
                            new Class<?>[] {ResourceLocation.class, Map.class},
                            c.getParameterTypes())));
        assertTrue(
            java.util.Arrays.stream(constructors)
                .anyMatch(
                    c ->
                        java.util.Arrays.equals(
                            new Class<?>[] {ResourceLocation.class}, c.getParameterTypes())));
    }

    @Test
    void registeredRootsDispatchAndPreserveConcreteTypes() throws Exception {
        var bullet = ResourceLocation.parse("test:bullet");
        var laser = ResourceLocation.parse("test:laser");
        Map<ResourceLocation, ResourceLocation> modulesByRoot =
            Map.of(
                TestAttackTypes.BULLET.getRootId(), bullet,
                TestAttackTypes.LASER.getRootId(), laser);
        var trigger = new TrajectoryAttachment(null, modulesByRoot);
        for (var variant : TestAttackTypes.PROJECTILES) {
            var context = PatternTestSupport.context(0, 0).toBuilder().attackType(variant).build();
            assertEquals(variant.getRootId(), context.getRootTypeId());
            trigger.attachToBullet(new ExecutableEffectContext(null), context);
            assertEquals(
                List.of(new TrajectorySpec(modulesByRoot.get(variant.getRootId()), 0)),
                context.getTrajectorySpecs());
            assertEquals(variant, context.toBuilder().build().getAttackType());
        }
    }

    @Test
    void allSourceTypesSurviveCopiesAndFreeze() throws Exception {
        for (var type : TestAttackTypes.PROJECTILES) {
            AttackContext original =
                PatternTestSupport.context(0, 0).toBuilder().attackType(type).build();
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
        TrajectoryAttachment trigger =
            new TrajectoryAttachment(
                null,
                Map.of(
                    TestAttackTypes.BULLET.getRootId(), bulletId,
                    TestAttackTypes.LASER.getRootId(), laserId));
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
        TrajectoryAttachment trigger =
            new TrajectoryAttachment(
                null,
                Map.of(
                    TestAttackTypes.BULLET.getRootId(), ID,
                    TestAttackTypes.LASER.getRootId(), ID));
        trigger.fire(context, ModTriggerTypes.ATTACK_PLAN);
        assertTrue(attack.getTrajectorySpecs().isEmpty());
        trigger.fire(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        assertEquals(List.of(new TrajectorySpec(ID, 2)), attack.getTrajectorySpecs());
        attack.freeze();
        trigger.fire(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        assertEquals(List.of(new TrajectorySpec(ID, 2)), attack.getTrajectorySpecs());
    }

    @Test
    void universalAttachmentUsesItsModuleForAnyAttackType() throws Exception {
        TrajectoryAttachment trigger = new TrajectoryAttachment(ID);
        AttackContext attack =
            PatternTestSupport.context(0, 0).toBuilder().attackType(TestAttackTypes.BRIMSTONE).build();

        trigger.attachToBullet(new ExecutableEffectContext(null), attack);

        assertEquals(List.of(new TrajectorySpec(ID, 0)), attack.getTrajectorySpecs());
    }

    @Test
    void typedMappingOverridesDefaultModule() throws Exception {
        ResourceLocation override = ResourceLocation.parse("isaac_disaster:laser_override");
        TrajectoryAttachment trigger =
            new TrajectoryAttachment(ID, Map.of(TestAttackTypes.LASER.getId(), override));
        AttackContext attack =
            PatternTestSupport.context(0, 0).toBuilder().attackType(TestAttackTypes.LASER).build();

        trigger.attachToBullet(new ExecutableEffectContext(null), attack);

        assertEquals(List.of(new TrajectorySpec(override, 0)), attack.getTrajectorySpecs());
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
