package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LuckyToe implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.LUCKY_TOE.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(StatManager.LUCK.description(1),
                Component.translatable("item.isaac_disaster.lucky_toe.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(StatManager.LUCK.description(1, Style.EMPTY.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.LUCK.apply(player, 2);
        } else {
            StatManager.LUCK.apply(player, 1);
        }

    };
    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.LUCK.apply(player, -2);
        } else {
            StatManager.LUCK.apply(player, -1);
        }
    };

}
