package net.luojiuoscar.isaac_disaster.system.flight;

import net.minecraft.world.entity.player.Player;

/** Tracks the client jump edge needed to distinguish a normal jump from airborne thrust. */
public final class IsaacFlightInput {
    private boolean previousJumpDown;
    private boolean previousGrounded;

    public boolean update(Player player, boolean jumpDown) {
        boolean airbornePress = jumpDown && !previousJumpDown && !player.onGround() && !previousGrounded;
        previousJumpDown = jumpDown;
        previousGrounded = player.onGround();
        return airbornePress;
    }

    public boolean isJumpDown() {
        return previousJumpDown;
    }

    public void reset() {
        previousJumpDown = false;
        previousGrounded = false;
    }
}
