package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BulletBounceOnBlock;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Callus;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.FiringModifierAttackPlan;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.FiringModifierShotDelay;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LaserPlusBrimstone;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.RandomHarmfulPotion;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive.SimpleReviveEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class RequiredTriggerTypeTest {
    @Test
    void executableEffectsDefaultToTheEmptyRequirement() {
        IExecutableEffect effect = context -> {};

        assertSame(ModTriggerTypes.EMTPY, effect.getRequiredTriggerType());
    }

    @Test
    void explicitlyBoundEffectsExposeTheirRequiredTriggerType() {
        assertSame(ModTriggerTypes.BULLET_HIT_BLOCK,
                new BulletBounceOnBlock().getRequiredTriggerType());
        assertSame(ModTriggerTypes.ON_HURT,
                new Callus().getRequiredTriggerType());
        assertSame(ModTriggerTypes.ATTACK_PLAN,
                new FiringModifierAttackPlan().getRequiredTriggerType());
        assertSame(ModTriggerTypes.GET_SHOT_DELAY,
                new FiringModifierShotDelay().getRequiredTriggerType());
        assertSame(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                new LaserPlusBrimstone().getRequiredTriggerType());
        assertSame(ModTriggerTypes.HIT_ENTITY_RESTRICTED,
                new RandomHarmfulPotion().getRequiredTriggerType());
        assertSame(ModTriggerTypes.DEATH,
                new SimpleReviveEffect().getRequiredTriggerType());
    }
}
