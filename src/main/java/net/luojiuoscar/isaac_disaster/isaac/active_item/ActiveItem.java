package net.luojiuoscar.isaac_disaster.isaac.active_item;


import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public interface ActiveItem {
    /**
     * 获取物品ID
     */
    int getItemId();

    

    default void onUse(Player player, InteractionHand hand){
        // 判断车载电池
        AtomicInteger carBatteryCount = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    carBatteryCount.set(playerPassiveItem.getItemCount(ItemId.CAR_BATTERY.getId()));
                });
        if (carBatteryCount.get() != 0){
            onTriggerEffectStronger(player);
        }else {
            onTriggeredEffect(player);
        }

        // 修改服务器端的物品耐久度
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !(stack.getItem() instanceof NormalActiveItem item)){
            return;
        }

        // 给予物品的过载情况计算剩余充能
        int damage = stack.getDamageValue();
        if (NormalActiveItem.getOverCharged(stack)){
            damage += NormalActiveItem.getDamagePerUse(player) - stack.getMaxDamage();
            NormalActiveItem.setOverCharged(stack, false);
        }else{
            damage += NormalActiveItem.getDamagePerUse(player);
        }
        damage = Math.max(0, damage);

        // 如果有9伏特，恢复20%的耐久
        AtomicInteger volt_9 = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    volt_9.set(playerPassiveItem.getItemCount(ItemId.VOLT_9.getId()));
                });
        if (volt_9.get() > 0){
            damage -= (int) (NormalActiveItem.getOriginalDamagePerUse() * 0.2);
        }

        stack.setDamageValue(damage);

        // 设置0.5秒的冷却
        player.getCooldowns().addCooldown(item, 10);
    }


    /**
     * 触发效果
     */
    void onTriggeredEffect(Player player);

    /**
     * 触发更强效果时的效果
     */
    void onTriggerEffectStronger(Player player);


    /**
     * 使用时的客户端效果
     */
    default void onUseClientEffect(Player player){
        onTriggerSound(player);
    }

    /**
     * 使用时的音效
     */
    default void onTriggerSound(Player player) {
        SoundEvent sound = getSound();
        player.playSound(sound, 1.0f, 1.0f);
    }

    /**
     * 决定了播放什么声音
     */
    default SoundEvent getSound(){
        return SoundEvents.PLAYER_LEVELUP;
    }

    /**
     * 充能时的音效
     */
    default void onRechargeSound(Player player){

    }

    /**
     * 获取物品实例
     */
    ItemStack getItemStack();

    /**
     * 获取名称
     */
    default Component getDisplayName(){
        return getItemStack().getDisplayName();
    }

    /**
     * 获取描述文本(lore)
     */
    List<Component> getDescription();
}
    