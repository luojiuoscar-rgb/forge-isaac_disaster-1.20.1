package net.luojiuoscar.isaac_disaster.registries.ability.active;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.DisposableActiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class ActiveAbility extends IsaacItemAbility {
    public ActiveAbility(int id, int level) {
        super(id, level);
    }

    public void onUse(ServerPlayer player, @Nullable InteractionHand hand){
        ItemStack stack = null;

        // 如果hand为null则视为不由activeItem触发
        if (hand != null){
            // 修改服务器端的物品耐久度
            stack = player.getItemInHand(hand);
            if (stack.isEmpty() || !(stack.getItem() instanceof ActiveItem item)) return;
            // 消耗性主动
            if (item instanceof DisposableActiveItem) player.getItemInHand(hand).shrink(1);


            // 基于物品的过载情况计算剩余充能
            int damage = stack.getDamageValue();
            if (ActiveItem.getOverCharged(stack)){
                damage += item.getDamagePerUse(player) - stack.getMaxDamage();
                ActiveItem.setOverCharged(stack, false);
            }else{
                damage += item.getDamagePerUse(player);
            }
            damage = Math.max(0, damage);

            // 如果有9伏特，恢复20%的耐久
            if (PlayerHelper.hasItem(ItemId.VOLT_9.getId(), (ServerPlayer) player)){
                damage -= (int) (item.getOriginalDamagePerUse() * 0.2);
            }

            // 如果不是创造模式则消耗耐久
            if (!player.isCreative()) stack.setDamageValue(damage);

            // 设置0.25秒的冷却
            player.getCooldowns().addCooldown(item, 5);


            // 首次使用效果
            if (!ActiveItem.hasBeenUsed(stack)){
                onFirstUse(player, stack, hand);
                ActiveItem.setHasBeenUsed(stack, true);
            }
            // 触发音效（由于主动道具为范围音效，因此需要在服务端触发）
            triggerSFX(player);
        }

        // 判断车载电池
        if (PlayerHelper.hasItem(ItemId.CAR_BATTERY.getId(), player)){
            onTriggerStronger(player, stack, hand);
        }else {
            onTrigger(player, stack, hand);
        }
    }

    abstract public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);

    abstract public void onTrigger(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);

    abstract public void onTriggerStronger(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);

    public void rechargeSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), ModSounds.BALLS_OF_STEEL.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    abstract public void triggerSFX(ServerPlayer player);

}
