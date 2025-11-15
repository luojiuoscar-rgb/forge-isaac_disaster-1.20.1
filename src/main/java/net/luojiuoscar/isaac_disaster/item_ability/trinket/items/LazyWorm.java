package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LazyWorm implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.LAZY_WORM.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {
        if (!(entity instanceof Player player)) return;

        if (isEnchanted){
            StatManager.BULLET_SPEED.apply(player, -4);
        }else{
            StatManager.BULLET_SPEED.apply(player, -2);
        }
    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {
        if (!(entity instanceof Player player)) return;

        if (isEnchanted){
            StatManager.BULLET_SPEED.apply(player, 4);
        }else{
            StatManager.BULLET_SPEED.apply(player, 2);
        }
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.BULLET_SPEED.description(2)
        );
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(StatManager.BULLET_SPEED.description(2, Style.EMPTY.withColor(ColorManager.SYNERGY)));
    }
}
