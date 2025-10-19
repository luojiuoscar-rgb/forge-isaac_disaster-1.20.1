package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlackCandle implements IRecursivePassiveItem {

    @Override
    public int getItemId() {
        return ItemId.BLACK_CANDLE.getId();
    }

    @Override
    public void onObtain(Player player) {
        PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
    }

    @Override
    public void onDirectObtain(Player player) {
        recursiveEffect(player);
    }

    @Override
    public void onRemove(Player player) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.BLACK_CANDLE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.black_candle.lore.1"),
                Component.translatable("item.isaac_disaster.action.give_black_heart", 1)
        );
    }

    @Override
    public int getTickInterval() {
        return 4;
    }

    @Override
    public void recursiveEffect(Player player) {
        List<MobEffectInstance> toRemove = new ArrayList<>();
        // 获取所有需要被移除的效果
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL
                    && !effect.getEffect().getCurativeItems().isEmpty()) {
                toRemove.add(effect);
            }
        }

        // 移除效果
        for (MobEffectInstance effect : toRemove) {
            player.removeEffect(effect.getEffect());
        }
    }
}
