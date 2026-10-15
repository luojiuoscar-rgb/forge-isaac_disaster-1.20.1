package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class SoulLocket implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var amounts = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (amounts.isEmpty() || !Double.isFinite(amounts.get(0)) || amounts.get(0) < 0) return false;
        int halfHearts = (int) Math.floor(amounts.get(0) + 1.0e-6);
        int copies = player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS)
                .map(items -> items.getItemCountFromAll(ModPassiveItems.SOUL_LOCKET.getId())).orElse(0);
        for (int i = 0; i < halfHearts; i++) {
            for (int copy = 0; copy < copies; copy++) {
                switch (player.getRandom().nextInt(6)) {
                    case 0 -> StatManager.MOVEMENT_SPEED.apply(player, 0.2);
                    case 1 -> StatManager.TEARS.apply(player, 0.15);
                    case 2 -> StatManager.DAMAGE.apply(player, 0.2);
                    case 3 -> StatManager.RANGE.apply(player, 0.35);
                    case 4 -> StatManager.BULLET_SPEED.apply(player, 0.2);
                    case 5 -> StatManager.LUCK.apply(player, 0.2);
                }
            }
        }
        return true;
    }
}
