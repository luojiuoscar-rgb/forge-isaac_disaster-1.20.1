package net.luojiuoscar.isaac_disaster.client.render;

import net.minecraft.world.scores.Scoreboard;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerHostilityTest {
    @Test
    void ownEffectsAndSameTeamRemainFriendlyEvenWhenFriendlyFireIsEnabled() {
        UUID viewer = new UUID(1, 1);
        UUID owner = new UUID(2, 2);
        var team = new Scoreboard().addPlayerTeam("friends");
        team.setAllowFriendlyFire(true);
        assertFalse(OwnerHostility.isHostile(viewer, null, viewer, null));
        assertFalse(OwnerHostility.isHostile(viewer, team, owner, team));
    }

    @Test
    void otherOwnersAreHostileWithoutASharedTeam() {
        UUID viewer = new UUID(1, 1);
        UUID owner = new UUID(2, 2);
        var scoreboard = new Scoreboard();
        var first = scoreboard.addPlayerTeam("first");
        var second = scoreboard.addPlayerTeam("second");
        assertTrue(OwnerHostility.isHostile(viewer, first, owner, second));
        assertTrue(OwnerHostility.isHostile(viewer, null, owner, null));
        assertTrue(OwnerHostility.isHostile(viewer, first, owner, null));
        assertTrue(OwnerHostility.isHostile(viewer, null, owner, second));
    }

    @Test
    void missingViewerOrOwnerDoesNotFlash() {
        assertFalse(OwnerHostility.isHostile(null, null, new UUID(1, 1), null));
        assertFalse(OwnerHostility.isHostile(new UUID(1, 1), null, null, null));
    }
}
