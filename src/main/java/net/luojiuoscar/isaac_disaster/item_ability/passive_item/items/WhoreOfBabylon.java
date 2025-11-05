package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.EffectDescriptionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WhoreOfBabylon implements IRecursivePassiveItem {

    @Override
    public int getItemId() {
        return ItemId.WHORE_OF_BABYLON.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        recursiveEffect(player);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.whore_of_babylon.lore.1"),
                Component.translatable("item.isaac_disaster.whore_of_babylon.lore.2")
        );
    }

    @Override
    public int getTickInterval() {
        return 10;
    }

    @Override
    public void recursiveEffect(Player player) {
        if (player.getHealth() > player.getMaxHealth() * 0.1) return;
        MobEffectInstance instance = player.getEffect(ModEffects.BABYLON.get());
        if (instance != null){
            double duration = instance.getDuration();
            if (duration > 60){
                return; // 小于3秒的时候再添加新的效果
            }
        }

        player.addEffect(new MobEffectInstance(ModEffects.BABYLON.get(), 240, 0));
    }

    @Override
    public List<Component> getExplain(){
        return EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.BABYLON.getId());
    }
}
