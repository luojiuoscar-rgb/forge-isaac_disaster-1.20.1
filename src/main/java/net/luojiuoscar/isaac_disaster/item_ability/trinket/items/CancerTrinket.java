package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class CancerTrinket implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.CANCER_TRINKET.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(StatManager.TEARS_CORRECTION.description(1));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.double")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.TEARS_CORRECTION.apply(player, 2);
        } else {
            StatManager.TEARS_CORRECTION.apply(player, 1);
        }

    };
    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.TEARS_CORRECTION.apply(player, -2);
        } else {
            StatManager.TEARS_CORRECTION.apply(player, -1);
        }
    };

}
