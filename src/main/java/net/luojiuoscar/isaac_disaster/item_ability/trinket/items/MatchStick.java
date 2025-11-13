package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MatchStick implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.MATCH_STICK.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.match_stick.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.higher_prob")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }
}
