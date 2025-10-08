package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
        // 添加协同效果（套装效果也属于协同）
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getSynergyDescription()
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
