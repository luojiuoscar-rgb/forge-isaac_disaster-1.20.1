package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class AttackContextIsolationTest {

    @Test
    void constructorTakesOwnershipOfATriggerSnapshot() {
        CompositeTrigger parentTriggers = new CompositeTrigger();

        AttackContext context = new AttackContext(
                null,
                null,
                ResourceLocation.fromNamespaceAndPath("isaac_disaster", "test"),
                parentTriggers,
                Map.of(),
                Vec3.ZERO,
                0.0f,
                0.0f
        );

        assertNotSame(parentTriggers, context.getTrigger());
    }
}
