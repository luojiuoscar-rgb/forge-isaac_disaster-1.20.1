package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class Heart extends FoodPickup implements ICommonPickup {
    public Heart(Properties pProperties, RegistryObject<PickupAbility> ability, Rarity rarity, int stackTo) {
        super(pProperties.food(new FoodProperties.Builder()
                .nutrition(0).saturationMod(0).alwaysEat().build()).rarity(rarity).stacksTo(stackTo),
                ability);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 10;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack before = stack.copy();
        ItemStack itemstack = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return itemstack;

        // server side
        getAbility().onUse(player, before, player.getUsedItemHand());
        getAbility().makeSound(player);

        return itemstack;

    }
}
