package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DaemonsTail implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.DAEMONS_TAIL.getId();
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.DAEMONS_TAIL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.daemons_tail.lore.1"),
                Component.translatable("item.isaac_disaster.daemons_tail.lore.2"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.daemons_tail.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }
}
