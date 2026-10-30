package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackSelection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** The held item selects attacks without changing the player's owned attack types. */
public interface IAttackLauncher {
    default boolean usesVanillaItemUse() { return false; }

    @Nullable
    default AttackSelection getAttackSelection(ServerPlayer player) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(PlayerAbility::getAttackSelection).orElse(null);
    }

    static boolean isHolding(Player player) {
        return player.getMainHandItem().getItem() instanceof IAttackLauncher;
    }

    @Nullable
    static AttackSelection selection(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof IAttackLauncher launcher
                ? launcher.getAttackSelection(player) : null;
    }
}
