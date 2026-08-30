package net.luojiuoscar.isaac_disaster.event;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeEventsProjectileKnockbackTest {
    private static final double EPSILON = 1.0E-9;

    @Test
    void tearImpulseIsHorizontalAndRespectsKnockbackResistance() {
        Vec3 currentVelocity = new Vec3(0.2D, 0.37D, -0.1D);
        Vec3 updatedVelocity = ForgeEvents.applyTearImpulse(currentVelocity, new Vec3(3.0D, 0.0D, 4.0D),
                0.10D, 0.25D);

        assertEquals(0.245D, updatedVelocity.x, EPSILON);
        assertEquals(0.37D, updatedVelocity.y, EPSILON);
        assertEquals(-0.04D, updatedVelocity.z, EPSILON);
    }

    @Test
    void projectileDamageTypesBypassOnlyTheDamageCooldown() throws IOException {
        String cooldownTag = resource("data/minecraft/tags/damage_type/bypasses_cooldown.json");
        String invulnerabilityTag = resource("data/minecraft/tags/damage_type/bypasses_invulnerability.json");

        assertTrue(cooldownTag.contains("isaac_disaster:tear"));
        assertTrue(cooldownTag.contains("isaac_disaster:laser"));
        assertFalse(invulnerabilityTag.contains("isaac_disaster:tear"));
        assertFalse(invulnerabilityTag.contains("isaac_disaster:laser"));
    }

    private static String resource(String path) throws IOException {
        try (InputStream input = ForgeEventsProjectileKnockbackTest.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing test resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
