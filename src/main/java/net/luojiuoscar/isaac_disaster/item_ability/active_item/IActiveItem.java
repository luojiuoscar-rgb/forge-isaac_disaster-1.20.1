package net.luojiuoscar.isaac_disaster.item_ability.active_item;


import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.DisposableActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.List;


public interface IActiveItem {
    /**
     * 获取物品ID
     */
    int getItemId();

    default void onUse(Player player, @Nullable InteractionHand hand){
        // 如果hand为null则视为不由activeItem触发
        if (hand != null){
            // 修改服务器端的物品耐久度
            ItemStack stack = player.getItemInHand(hand);
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
                onFirstUse(player);
                ActiveItem.setHasBeenUsed(stack, true);
            }
            // 触发音效（由于主动道具为范围音效，因此需要在服务端触发）
            onTriggerSound(player);
        }

        // 判断车载电池
        if (PlayerHelper.hasItem(ItemId.CAR_BATTERY.getId(), (ServerPlayer) player)){
            onTriggeredEffectStronger(player);
        }else {
            onTriggeredEffect(player);
        }
    }

    default void onFirstUse(Player player){
    }

    /**
     * 触发效果
     */
    void onTriggeredEffect(Player player);

    /**
     * 触发更强效果时的效果
     */
    void onTriggeredEffectStronger(Player player);

    /**
     * 使用时的客户端效果
     */
    default void onUseClientEffect(Player player){
    }

    /**
     * 使用时的音效
     */
    default void onTriggerSound(Player player) {
        SoundEvent sound = getSound();
        if (sound == null) return;

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                getSound(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * 决定了播放什么声音
     */
    default @Nullable SoundEvent getSound(){
        return ModSounds.BATTERY_SMALL.get();
    }

    /**
     * 充能时的音效
     */
    default void onRechargeSound(Player player){

    }

    default int getItemLevel(){
        if (ItemId.getItemById(getItemId()).get() instanceof IsaacItem item){
            return item.getItemLevel();
        }
        return 0;
    }


    /**
     * 获取描述文本(lore)
     */
     List<Component> getDescription();

    /**
     * 解释性文本
     */
     default List<Component> getExplain(){
         return new ArrayList<>();
     };

     List<Component> getSynergyDescription();
}
    