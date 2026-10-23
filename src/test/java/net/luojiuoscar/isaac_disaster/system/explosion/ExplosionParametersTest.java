package net.luojiuoscar.isaac_disaster.system.explosion;

import net.minecraft.world.level.Explosion;
import org.junit.jupiter.api.Test;

import static net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters.*;
import static org.junit.jupiter.api.Assertions.*;

class ExplosionParametersTest {
    @Test
    void ordinaryPowerUsesAnActualDoubleRadiusWithoutCouplingDamage() {
        var parameters = ExplosionParameters.standard(4, 17);
        assertEquals(4, parameters.power());
        assertEquals(8, parameters.entityRadius());
        assertEquals(4, parameters.blockPower());
        assertEquals(17, parameters.centerDamage());
        assertEquals(EntityImpactMode.STANDARD, parameters.entityImpactMode());
    }

    @Test
    void gigaRetainsItsIndependentRadiusAndDamagePolicy() {
        var parameters = ExplosionParameters.giga(10);
        assertEquals(300, parameters.centerDamage());
        assertEquals(10, parameters.entityRadius());
        assertEquals(BlockDestructionMode.IGNORE_RESISTANCE, parameters.blockDestructionMode());
        assertEquals(EntityImpactMode.GIGA, parameters.entityImpactMode());
        assertEquals(FeedbackMode.GIGA, parameters.feedbackMode());
        assertTrue(parameters.ignoreFriendly());
    }

    @Test
    void changingFlagsDoesNotMutateTheOriginalSnapshot() {
        var original = ExplosionParameters.standard(4, 40);
        var copy = original.withIgnoreFriendly(true).withCausesFire(true)
                .withBlockInteraction(Explosion.BlockInteraction.KEEP);
        assertFalse(original.ignoreFriendly());
        assertFalse(original.causesFire());
        assertTrue(copy.ignoreFriendly());
        assertTrue(copy.causesFire());
        assertEquals(40, copy.centerDamage());
        assertEquals(8, copy.entityRadius());
        assertEquals(Explosion.BlockInteraction.KEEP, copy.blockInteraction());
    }

    @Test
    void invalidRecoverableNumbersAreNormalized() {
        var parameters = new ExplosionParameters(Float.NaN, -1, Float.POSITIVE_INFINITY,
                -2, Float.NEGATIVE_INFINITY, BlockDestructionMode.RESISTANCE,
                EntityImpactMode.STANDARD, false, false, Explosion.BlockInteraction.KEEP,
                FeedbackMode.STANDARD);
        assertEquals(0, parameters.power());
        assertEquals(0, parameters.centerDamage());
        assertEquals(0, parameters.entityRadius());
        assertEquals(0, parameters.blockPower());
        assertEquals(0, parameters.blockResistanceMultiplier());
    }

    @Test
    void requiredPoliciesCannotSilentlyDefault() {
        assertThrows(NullPointerException.class, () -> new ExplosionParameters(4, 40, 8, 4, 1,
                null, EntityImpactMode.STANDARD, false, false,
                Explosion.BlockInteraction.DESTROY, FeedbackMode.STANDARD));
    }

    @Test
    void extremeFiniteGeometryIsBoundedWithoutCappingDamage() {
        var parameters = ExplosionParameters.standard(Float.MAX_VALUE, 2_000_000);
        assertEquals(128, parameters.power());
        assertEquals(128, parameters.blockPower());
        assertEquals(256, parameters.entityRadius());
        assertEquals(2_000_000, parameters.centerDamage());
    }
}
