package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class PassiveItem extends IsaacItem implements IIsaacCuriosItem {
    public PassiveItem(Properties properties, RegistryObject<PassiveAbility> ability) {
        super(properties.stacksTo(1), ability);
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents, @Nullable ItemStack stack){
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return Config.ALLOW_CURIO_UNEQUIP.get();
    }

    @Override
    public void tryEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;
        if (!(getAbility() instanceof PassiveAbility passiveAbility)) return;

        passiveAbility.onObtain(player, stack);
        setHasBeenUsed(stack, true);
    }

    @Override
    public void tryUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;
        if (!(getAbility() instanceof PassiveAbility passiveAbility)) return;

        passiveAbility.onRemove(player, stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!Config.USABLE_PASSIVE_ITEM.get() || player.level().isClientSide) return InteractionResultHolder.fail(stack);

        PassiveAbility ability = (PassiveAbility) this.getAbility();

        if (player instanceof ServerPlayer serverPlayer){
            // 触发效果 (先触发效果以适配会对stack本身产生变化的道具；确保后续正确存入)
            ability.onObtain(serverPlayer, stack);
            ability.makeSound(serverPlayer);
            PassiveItem.setHasBeenUsed(stack, true);

            // 记录
            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                    playerPassiveItem -> playerPassiveItem.addItem(serverPlayer, stack, hand));
        }

        // shrink 1
        if (!player.isCreative()){
            stack.shrink(1);
        }

        return InteractionResultHolder.pass(stack);
    }
}