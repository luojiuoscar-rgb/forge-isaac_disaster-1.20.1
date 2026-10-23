package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectSessions;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectSessionsTest {
    @Test
    void dimensionRoundTripStartsANewerSessionAndAnEmptyVisibilitySet() {
        var sessions = new GroundEffectSessions();
        UUID player = new UUID(1, 2);
        var overworld = ResourceLocation.parse("minecraft:overworld");
        var nether = ResourceLocation.parse("minecraft:the_nether");
        var first = sessions.get(player, overworld);
        first.visible().put(12L, 1);
        assertSame(first, sessions.get(player, overworld));
        var second = sessions.get(player, nether);
        var third = sessions.get(player, overworld);
        assertTrue(first.epoch() < second.epoch() && second.epoch() < third.epoch());
        assertTrue(third.visible().isEmpty());
        sessions.remove(player);
        assertTrue(sessions.get(player, overworld).epoch() > third.epoch());
    }
}
