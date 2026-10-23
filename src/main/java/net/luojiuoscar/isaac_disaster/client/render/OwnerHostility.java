package net.luojiuoscar.isaac_disaster.client.render;

import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Shared viewer/owner relation for hostile visual cues, independent of friendly-fire settings. */
public final class OwnerHostility {
    private OwnerHostility() {
    }

    public static boolean isHostile(@Nullable UUID viewer, @Nullable Team viewerTeam,
                                    @Nullable UUID owner, @Nullable Team ownerTeam) {
        return viewer != null && owner != null && !viewer.equals(owner)
                && (viewerTeam == null || !viewerTeam.isAlliedTo(ownerTeam));
    }
}
