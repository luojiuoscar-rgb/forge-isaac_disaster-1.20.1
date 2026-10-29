package net.luojiuoscar.isaac_disaster.system.damage;

import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PiscesKnockbackTest {
    @Test
    void defaultsAreExposedThroughTheSharedGameplaySettings() {
        assertTrue(GameplayConfig.keys().containsKey("default_bullet_knockback"));
        assertTrue(GameplayConfig.keys().containsKey("bullet_knockback_bonus"));
        assertEquals(0.2, (Double) GameplayConfig.keys().get("default_bullet_knockback").get(), 1e-12);
        assertEquals(0.1, (Double) GameplayConfig.keys().get("bullet_knockback_bonus").get(), 1e-12);
    }

    @Test
    void derivedAndCopiedAttacksRetainTheirKnockbackSnapshot() throws Exception {
        var attack = PatternTestSupport.context(0, 0).toBuilder().bulletKnockback(0.8).build();
        attack.freeze();
        assertEquals(0.8, attack.getBulletKnockback());
        assertEquals(0.8, attack.copy().getBulletKnockback());
        assertEquals(0.8, attack.copyConfiguration().getBulletKnockback());
        assertEquals(0.8, attack.toBuilder().damage(2.0).build().getBulletKnockback());
        assertEquals(0.8, attack.bindAttackTypeOrCopy(TestAttackTypes.LASER).getBulletKnockback());
        assertEquals(0.5, attack.toBuilder().bulletKnockback(0.5).build().getBulletKnockback());
        assertEquals(0.8, attack.getBulletKnockback());
    }

    @Test
    void standaloneBulletsNormalizeSnapshotsAndUseConfiguredDefaults() {
        assertEquals(0.2, BulletState.builder().build().getBulletKnockback());
        assertEquals(0.5, BulletState.builder().bulletKnockback(0.5).build().getBulletKnockback());
        for (double value : new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, HurtKnockback.normalizeMultiplier(value));
            assertEquals(0, BulletState.builder().bulletKnockback(value).build().getBulletKnockback());
        }
        assertEquals(1024, HurtKnockback.normalizeMultiplier(2048));
    }
}
