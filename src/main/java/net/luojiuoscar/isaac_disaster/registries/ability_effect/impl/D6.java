package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class D6 implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Set<BlockPos> posList = BlockData.get(serverLevel).getAllItemBlocks();
        Vec3 playerPos = player.position();

        final double MAX_DISTANCE = 10.0;

        for (BlockPos pos : posList) {
            // distance
            Vec3 blockCenter = Vec3.atCenterOf(pos);
            double distanceSq = playerPos.distanceToSqr(blockCenter);
            if (distanceSq > MAX_DISTANCE * MAX_DISTANCE) continue;

            if (serverLevel.getBlockEntity(pos) instanceof ItemDisplayContainerBlockEntity be) {
                be.itemRollFromPlayer(player);
            }
        }
    }
}