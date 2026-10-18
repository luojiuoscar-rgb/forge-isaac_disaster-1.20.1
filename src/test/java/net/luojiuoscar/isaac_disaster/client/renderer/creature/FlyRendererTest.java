package net.luojiuoscar.isaac_disaster.client.renderer.creature;

import net.minecraft.world.scores.Scoreboard;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlyRendererTest {
    @Test
    void selfAndTeammatesKeepTheirOwnColorsEvenWithFriendlyFireEnabled() {
        UUID viewer = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        var scoreboard = new Scoreboard();
        var team = scoreboard.addPlayerTeam("friends");
        team.setAllowFriendlyFire(true);
        assertFalse(FlyRenderer.shouldFlash(viewer, null, UUID.fromString(viewer.toString()), null));
        assertFalse(FlyRenderer.shouldFlash(viewer, team, owner, team));
        for (int color : new int[]{0x4077E0, 0xD04030, 0x308050}) {
            for (double seconds = 0; seconds < 1; seconds += 0.0125) {
                assertEquals(color, FlyRenderer.color(color, false, seconds));
            }
        }
    }

    @Test
    void otherOwnersFlashWhenOnAnotherTeamOrWhenNeitherHasATeam() {
        UUID viewer = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        var scoreboard = new Scoreboard();
        var first = scoreboard.addPlayerTeam("first");
        var second = scoreboard.addPlayerTeam("second");
        assertTrue(FlyRenderer.shouldFlash(viewer, first, owner, second));
        assertTrue(FlyRenderer.shouldFlash(viewer, null, owner, null));
        assertTrue(FlyRenderer.shouldFlash(viewer, first, owner, null));
        assertTrue(FlyRenderer.shouldFlash(viewer, null, owner, second));
    }

    @Test
    void unknownViewerOrOwnerUsesNormalRenderingUntilAvailable() {
        assertFalse(FlyRenderer.shouldFlash(null, null, UUID.randomUUID(), null));
        assertFalse(FlyRenderer.shouldFlash(UUID.randomUUID(), null, null, null));
    }

    @Test
    void eightFullCyclesPerSecondUseEqualHardSwitchedHalvesBetweenGameTicks() {
        for (int original : new int[]{0x4077E0, 0xD04030, 0x308050}) {
            for (int cycle = 0; cycle < 8; cycle++) {
                double start = cycle * 0.125;
                assertEquals(original, FlyRenderer.color(original, true, start));
                assertEquals(original, FlyRenderer.color(original, true, start + 0.06249));
                assertEquals(0xD8D8D8, FlyRenderer.color(original, true, start + 0.0625));
                assertEquals(0xD8D8D8, FlyRenderer.color(original, true, start + 0.12499));
            }
            assertEquals(original, FlyRenderer.color(original, true, 1));
        }
    }
}
