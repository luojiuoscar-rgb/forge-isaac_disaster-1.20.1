package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HolyLightManagerTest {
    @Test
    void missingWorldOrCasterCannotCreateAnUnattributedStrike() {
        HolyLightManager manager = new HolyLightManager();
        assertFalse(manager.spawn(null, null, Vec3.ZERO, 4, 0.8, false));
        assertTrue(manager.snapshots().isEmpty());
        assertEquals(0, manager.snapshots().size());
    }
}
