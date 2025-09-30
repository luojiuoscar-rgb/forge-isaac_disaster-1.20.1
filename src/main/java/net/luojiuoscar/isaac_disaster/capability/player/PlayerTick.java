package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerTick implements INBTSerializable<CompoundTag> {

    // 每tick调用，让玩家背包内物品恢复耐久
    public void tick(Player player) {
        for (ItemStack stack : player.getInventory().items) restore(stack, player);
        for (ItemStack stack : player.getInventory().offhand) restore(stack, player);
    }

    private void restore(ItemStack stack, Player player) {
        if (player.isUsingItem() && player.getUseItem() == stack)
            return;
        if (stack.getItem() instanceof NormalActiveItem) {
            if (stack.getDamageValue() > 0) {
                stack.setDamageValue(stack.getDamageValue() - 1); // 不触发抬手动画
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag(); // 无需存数据
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }

    public interface IMyPlayerRecoveryCap {
        void tick(Player player);
    }
}
