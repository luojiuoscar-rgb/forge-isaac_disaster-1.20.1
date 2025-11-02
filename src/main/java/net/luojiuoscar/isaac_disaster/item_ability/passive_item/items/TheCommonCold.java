package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.INewBulletTypePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.EffectDescriptionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheCommonCold implements IDamageTriggerPassiveItem, INewBulletTypePassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_COMMON_COLD.getId();
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.THE_COMMON_COLD.get());
    }

    @Override
    public double getTriggerChance(Player player) {
        return 1 / Math.max(1, 4 - (getPlayerLuck(player) / 4));
    }

    @Override
    public void handleAttackEntityEffect(Player player, LivingEntity target){
        // 给目标添加中毒效果：持续5秒（100游戏刻，20刻=1秒），等级1
        MobEffectInstance poisonEffect = new MobEffectInstance(
                ModEffects.ISAAC_POISON.get(),  // 中毒效果的类型
                70,                 // 持续时间（游戏刻）
                0,                  // 效果等级（0=I级，1=II级，以此类推）
                false,              // 是否显示粒子效果
                true,               // 是否显示图标
                true                // 是否可以被移除（如牛奶）
        );

        // 为目标实体应用效果
        target.addEffect(poisonEffect, player);
    };


    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_common_cold.lore.1")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.POISON.getId()));

        return description;
    }

    @Override
    public int getNewColor(){
        return ColorManager.POISON_BULLET_COLOR;
    }

    @Override
    public void onShootEffect(Player player, IsaacBullet bullet) {
        // 如果触发
        if (Math.random() <= getTriggerChance(player)) {
            bullet.setColor(getNewColor());
            bullet.addBulletHitEffect(ItemId.THE_COMMON_COLD.getId());
        }
    }
}
