package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Pyromaniac implements IRecursivePassiveItem {

    @Override
    public int getItemId() {
        return ItemId.PYROMANIAC.getId();
    }

    @Override
    public void onFirstObtain(Player player) {}

    @Override
    public void onObtain(Player player) {
        recursiveEffect(player); // 获取时触发一次
    }

    @Override
    public void onRemove(Player player) {}

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.PYROMANIAC.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.pyromaniac.lore.1")
        );
    }

    @Override
    public int getTickInterval() {
        return 100; // 每5秒一次
    }

    @Override
    public void recursiveEffect(Player player) {
        // 防火
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false, true));
    }
}
