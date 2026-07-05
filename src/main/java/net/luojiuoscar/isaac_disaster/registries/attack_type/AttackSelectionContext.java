package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Runtime data used while rebuilding the cached attack selection.
 *
 * <p>The player is optional so existing capability-only refresh points can keep working. Future
 * effects that disable attack types can use the player-aware refresh path when their condition
 * depends on player state or inventory.</p>
 */
public record AttackSelectionContext(Map<ResourceLocation, Integer> attackTypes,
                                     @Nullable ServerPlayer player) {
}
