package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BulletBounceOnBlock;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Callus;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.JumperCables;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.JumperCablesKillCounter;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LaserPlusBrimstone;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LeadPencilShotCounter;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BookPlusMomsKnife;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MonstrosLungPlusMomsKnife;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.RandomHarmfulPotion;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive.SimpleReviveEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequiredTriggerTypeTest {
    @Test
    void shotCountersRequireAnActualFiredAttack() {
        assertEquals("attack_fired", new LeadPencilShotCounter().getRequiredTriggerType().getId().getPath());
    }

    @Test
    void executableEffectsDefaultToTheEmptyRequirement() {
        IExecutableEffect effect = context -> {};

        assertSame(ModTriggerTypes.EMTPY, effect.getRequiredTriggerType());
        assertSame(ModTriggerTypes.EMTPY, new JumperCables().getRequiredTriggerType());
    }

    @Test
    void explicitlyBoundEffectsExposeTheirRequiredTriggerType() {
        assertSame(ModTriggerTypes.ATTACK_FIRED, new BookPlusMomsKnife().getRequiredTriggerType());
        assertSame(ModTriggerTypes.ATTACK_FIRED, new MonstrosLungPlusMomsKnife().getRequiredTriggerType());
        assertSame(ModTriggerTypes.KILL_ENTITY, new JumperCablesKillCounter().getRequiredTriggerType());
        assertSame(ModTriggerTypes.BULLET_HIT_BLOCK,
                new BulletBounceOnBlock().getRequiredTriggerType());
        assertSame(ModTriggerTypes.ON_HURT,
                new Callus().getRequiredTriggerType());
        assertSame(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                new LaserPlusBrimstone().getRequiredTriggerType());
        assertSame(ModTriggerTypes.HIT_ENTITY_RESTRICTED,
                new RandomHarmfulPotion().getRequiredTriggerType());
        assertSame(ModTriggerTypes.DEATH,
                new SimpleReviveEffect().getRequiredTriggerType());
    }
}
