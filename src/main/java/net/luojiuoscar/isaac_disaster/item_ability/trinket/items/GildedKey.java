package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IRecursiveTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GildedKey implements IRecursiveTrinket {
    @Override
    public int getId() {
        return TrinketId.GILDED_KEY.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        PlayerHelper.giveItem(player, ModItems.KEY.get(), 1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.gilded_key.lore.1"),
                Component.translatable("item.isaac_disaster.gilded_key.lore.2"),
                Component.translatable("item.isaac_disaster.gilded_key.lore.3"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.gilded_key.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }


    @Override
    public int getTickInterval() {
        return 20;
    }

    @Override
    public void recursiveEffect(Player player, boolean isEnchanted) {
        if (PlayerHelper.hasTrinket(TrinketId.THE_LEFT_HAND.getId(), (ServerPlayer) player)) return;

        List<ItemStack> items = new ArrayList<>();
        Inventory inv = player.getInventory();
        items.addAll(inv.items);
        items.addAll(inv.offhand);

        for (int i = 0; i < items.size(); i++){
            ItemStack oldStack = items.get(i);
            if (oldStack.getItem() instanceof IsaacChestBlockItem item && !(item == ModItems.LOCKED_CHEST_ITEM.get())){
                ItemStack newStack = new ItemStack(ModItems.LOCKED_CHEST_ITEM.get());
                newStack.setCount(oldStack.getCount());
                newStack.setTag(oldStack.getTag());

                player.getInventory().setItem(i, newStack);
            }
        }

        player.getInventory().setChanged();
    }
}
