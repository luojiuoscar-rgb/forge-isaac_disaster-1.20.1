package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.event.custom.IsaacBulletTickEvent;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TinyPlanet implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TINY_PLANET.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 2.5);
        StatManager.modifySpectral(player, 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -2.5);
        StatManager.modifySpectral(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.RANGE.description(2.5),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet"),
                Component.translatable("item.isaac_disaster.tiny_planet.lore.1")
                );
    }

    public static void onTriggered(IsaacBulletTickEvent event){
        IsaacBullet bullet = event.getBullet();
        LivingEntity owner = bullet.getOwner();

        if (owner instanceof Player player)
            bullet.steerHorizontalOrbit(player, 0.3, 2.0, 0.1); // 尝试朝玩家飞行
    }

}
