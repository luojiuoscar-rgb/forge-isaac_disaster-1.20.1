package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTrigger;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.INewBulletType;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TheCommonCold implements IDamageTrigger, INewBulletType {
    @Override
    public int getItemId() {
        return ItemId.THE_COMMON_COLD.getId();
    }

    @Override
    public double getTriggerChance(Player player) {
        return 1 / Math.max(1, 4 - (getPlayerLuck(player) / 4));
    }

    @Override
    public void handleAttackEntityEffect(Player player, LivingEntity target){
        // 给目标添加中毒效果：持续5秒（100游戏刻，20刻=1秒），等级1（ amplifier=0 对应等级1）
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
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {

    }

    @Override
    public void onRemove(Player player) {

    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_COMMON_COLD.get());
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

        description.add(Component.translatable("effect.isaac_disaster.isaac_poison").append(": ")
                .append(Component.translatable("effect.isaac_disaster.poison.explain.1")));
        description.add(Component.translatable("effect.isaac_disaster.poison.explain.2"));

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
            LOGGER.info("TRIGGERED EFFECT");
        }
    }
}
