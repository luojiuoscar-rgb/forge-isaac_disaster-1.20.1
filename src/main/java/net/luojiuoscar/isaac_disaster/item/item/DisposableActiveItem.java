package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class DisposableActiveItem extends ActiveItem{

    public DisposableActiveItem(Properties properties, int chargePerUse, int maxCharge, RegistryObject<ActiveAbility> ability) {
        super(properties, chargePerUse, maxCharge, ability);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        InteractionResultHolder<ItemStack> r = super.use(level, player, hand);
        player.getItemInHand(hand).shrink(1); // shrink 1
        return r;
    }
}
