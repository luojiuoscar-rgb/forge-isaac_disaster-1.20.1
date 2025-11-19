package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectories;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RingWorm implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.RING_WORM.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {
        if (!(entity instanceof Player player)) return;

        StatManager.addTrajectory(player, ModAttackTrajectories.RING_WORM.getId(), 1);
        if (isEnchanted){
            StatManager.TEARS.apply(player, 1);
        }else{
            StatManager.TEARS.apply(player, 0.5);
        }
        StatManager.addSpectral(player, 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {
        if (!(entity instanceof Player player)) return;

        StatManager.addTrajectory(player, ModAttackTrajectories.RING_WORM.getId(), -1);
        if (isEnchanted){
            StatManager.TEARS.apply(player, -1);
        }else{
            StatManager.TEARS.apply(player, -0.5);
        }        StatManager.addSpectral(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.ring_worm.lore.1"),
                StatManager.TEARS.description(0.5),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet")
        );
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(StatManager.TEARS.description(0.5, Style.EMPTY.withColor(ColorManager.SYNERGY)));
    }
}
