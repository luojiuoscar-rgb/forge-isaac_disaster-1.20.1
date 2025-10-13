package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FreeLemonade implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.FREE_LEMONADE.getId();
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.LEMON_MISHAP_USE.get();
    }

    @Override
    public void onTriggeredEffect(Player player) {

        // 创建药水云
        LemonEffectCloud cloud = new LemonEffectCloud(player.level(), player.getX(), player.getY(), player.getZ(),
        player, (float)StatManager.getNearbyRange() * 0.8f, 100, 0, 10,
                (float) StatManager.getDamageBonus() * 4f);

        // 生成实体
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.FREE_LEMONADE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.free_lemonade.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
