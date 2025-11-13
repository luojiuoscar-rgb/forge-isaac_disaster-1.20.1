package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AceOfSpadesTrinket implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.ACE_OF_SPADES_TRINKET.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.ace_of_spades_trinket.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.higher_prob")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }
}
