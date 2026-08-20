package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class DeadCatReviveEffect extends ReviveExecutableEffect {
    @Override
    protected void applyReviveEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        StatManager.MAX_HEALTH.set(player, 1);
        EntityHelper.teleportToRandomLocation(player, StatManager.getNearbyRange());
        player.setHealth(player.getMaxHealth());
    }

    @Override
    protected ItemStack getDisplayItem() {
        return new ItemStack(ModPassiveItems.DEAD_CAT.get());
    }
}
