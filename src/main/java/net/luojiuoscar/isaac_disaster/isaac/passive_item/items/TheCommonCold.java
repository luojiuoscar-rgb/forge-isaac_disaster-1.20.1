package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.InteractivePassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TheCommonCold implements InteractivePassiveItem {
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
                MobEffects.POISON,  // 中毒效果的类型
                100,                // 持续时间（游戏刻）
                0,                  // 效果等级（0=I级，1=II级，以此类推）
                false,              // 是否显示粒子效果
                true,               // 是否显示图标
                false               // 是否可以被移除（如牛奶）
        );

        // 为目标实体应用效果
        target.addEffect(poisonEffect);
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
                //移动速度以百分比显示
                Component.translatable("item.isaac_disaster.the_common_cold.lore.1")
        );
    }
}
