package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Heart extends Pickup{
    public Heart(Properties pProperties, int itemId, Rarity rarity) {
        this(pProperties, itemId, rarity, 64);
    }

    public Heart(Properties pProperties, int itemId, Rarity rarity, int stackTo) {
        super(pProperties.food(new FoodProperties.Builder()
                .nutrition(0).saturationMod(0).alwaysEat().build()).rarity(rarity).stacksTo(stackTo),
                itemId);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        // 自定义食用速度
        return 10;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack itemstack = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof Player player)) return itemstack;

        PickupManager.getInstance().getItemFromId(getPickupId()).onUse(player, itemstack, null);

        if (!level.isClientSide()) {
            // 播放自定义的音效
            PickupManager.getInstance().getItemFromId(getPickupId()).onUseClient(player);
        }

        return itemstack;

    }
}
