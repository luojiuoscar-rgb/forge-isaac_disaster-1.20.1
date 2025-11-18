package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IRecursiveTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheLeftHand implements IRecursiveTrinket {
    @Override
    public int getId() {
        return TrinketId.THE_LEFT_HAND.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.the_left_hand.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.no_effect")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public int getTickInterval() {
        return 20;
    }

    @Override
    public void recursiveEffect(Player player, boolean isEnchanted) {
        List<ItemStack> items = new ArrayList<>();
        Inventory inv = player.getInventory();
        items.addAll(inv.items);
        items.addAll(inv.offhand);

        for (int i = 0; i < items.size(); i++){
            ItemStack oldStack = items.get(i);
            if (oldStack.getItem() instanceof IsaacChestBlockItem item && !(item == ModItems.RED_CHEST_ITEM.get())){
                ItemStack newStack = new ItemStack(ModItems.RED_CHEST_ITEM.get());
                newStack.setCount(oldStack.getCount());
                newStack.setTag(oldStack.getTag());

                player.getInventory().setItem(i, newStack);
            }
        }

        player.getInventory().setChanged();
    }
}
