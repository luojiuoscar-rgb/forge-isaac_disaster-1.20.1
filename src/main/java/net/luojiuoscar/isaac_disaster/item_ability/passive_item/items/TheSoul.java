package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.EffectDescriptionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheSoul implements IRecursivePassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_SOUL.getId();
    }

    @Override
    public void onObtain(Player player) {
        PlayerHelper.giveItem(player, ModItems.SOUL_HEART.get(), 2);
    }

    @Override
    public void onDirectObtain(Player player) {
    }

    @Override
    public void onRemove(Player player) {
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_SOUL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_soul_heart", 2),
                Component.translatable("item.isaac_disaster.the_soul.lore.1")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.REPULSION_AURA.getId()));

        return description;
    }

    @Override
    public int getTickInterval() {
        return 20;
    }

    @Override
    public void recursiveEffect(Player player) {
        player.addEffect(new MobEffectInstance(
                ModEffects.SOUL_STATE.get(), -1, 0, false, false, true));
    }
}
