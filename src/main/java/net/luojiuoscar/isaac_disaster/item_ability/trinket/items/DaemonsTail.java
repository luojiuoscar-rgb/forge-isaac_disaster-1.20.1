package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DaemonsTail implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.DAEMONS_TAIL.getId();
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
