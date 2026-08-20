package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GuppysCollarReviveEffect extends ReviveExecutableEffect {
    @Override
    protected void applyReviveEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        EntityHelper.teleportToRandomLocation(player, StatManager.getNearbyRange());
        StatManager.healHealth(player, 1);
    }

    @Override
    protected ItemStack getDisplayItem() {
        return new ItemStack(ModPassiveItems.GUPPYS_COLLAR.get());
    }
}
