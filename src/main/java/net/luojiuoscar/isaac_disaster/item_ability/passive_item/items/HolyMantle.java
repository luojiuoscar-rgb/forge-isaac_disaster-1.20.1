package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HolyMantle implements IRecursiveItem {
    @Override
    public int getItemId() {
        return ItemId.HOLY_MANTLE.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        recursiveEffect(player);
    }

    @Override
    public void onRemove(Player player) {
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.HOLY_MANTLE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.holy_mantle.lore.1"),
                Component.translatable("item.isaac_disaster.special.stackable")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("effect.isaac_disaster.holy_shield").append(": ")
                .append(Component.translatable("effect.isaac_disaster.holy_shield.explain.1"))
                .append(Component.translatable("effect.isaac_disaster.holy_shield.explain.2"))
                .append(Component.translatable("effect.isaac_disaster.holy_shield.explain.3")));

        return description;
    }

    @Override
    public int getTickInterval() {
        return 400; // 20s
    }

    @Override
    public void recursiveEffect(Player player) {
        PlayerHelper.addAmplifier(player, ModEffects.HOLY_SHIELD.get());
    }
}
