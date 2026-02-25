package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SackOfPennies implements IRecursiveModule {
    @Override
    public int getInitialTick() {
        return StatManager.getTimeInterval(6);
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return StatManager.getTimeInterval(entity.getRandom().nextInt(6,9));
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        if (!(entity instanceof Player player)) return;

        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_COINS, stacks);
    }
}
