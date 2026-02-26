package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;

public class Magneto implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {

        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        Level level = entity.level();
        double range = StatManager.getNearbyRange() * 0.75;

        BlockPos center = entity.blockPosition();
        int r = (int) range;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r),
                center.offset(r, r, r))) {

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ChestBlockEntity chest)) continue;

            // ---------- 遍历箱子 ----------
            for (int i = 0; i < chest.getContainerSize(); i++) {

                ItemStack stack = chest.getItem(i);
                if (stack.isEmpty()) continue;

                ItemStack dropStack = stack.copy();
                chest.setItem(i, ItemStack.EMPTY);

                Vec3 spawnPos = Vec3.atCenterOf(pos);

                ItemEntity itemEntity = new ItemEntity(
                        level,
                        spawnPos.x,
                        spawnPos.y + 0.5,
                        spawnPos.z,
                        dropStack
                );

                Vec3 direction = entity.position().subtract(itemEntity.position());
                Vec3 motion = direction.normalize().scale(0.2);

                itemEntity.setDeltaMovement(motion);
                itemEntity.hurtMarked = true;

                level.addFreshEntity(itemEntity);

                chest.setChanged();

                break; // 一次只吸一组
            }
        }
    }
}
