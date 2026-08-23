package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class AttackContextIsolationTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.bootStrap();
    }

    @Test
    void constructorTakesOwnershipOfATriggerSnapshot() {
        CompositeTrigger parentTriggers = new CompositeTrigger();

        AttackContext context = new AttackContext(
                new ArmorStand(null, 0.0, 0.0, 0.0),
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
