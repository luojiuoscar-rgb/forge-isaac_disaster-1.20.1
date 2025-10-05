package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.network.chat.Component;

import java.util.List;


public class PassiveItem extends IsaacItem {

    public PassiveItem(Properties properties, int itemLevel, int itemId ) {
        this(properties, itemLevel, itemId, false, false);
    }
    public PassiveItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffect) {
        this(properties, itemLevel, itemId, hasSpecialEffect, false);
    }

    public PassiveItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffect, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(IsaacItem.getRarity(itemLevel)), itemLevel, itemId, hasSpecialEffect, useOriginalColor);
    }

    @Override
    public void addDescription(List<Component> tooltipComponents){
        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getDescription()
        );
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents){
        tooltipComponents.add(Component.literal(""));
    }

    @Override
    public void addExplainInfo(List<Component> tooltipComponents) {
        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getExplain()
        );
    }
}
