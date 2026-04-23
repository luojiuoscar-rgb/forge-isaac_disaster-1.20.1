package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class Volt45 implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        // 检查攻击者是否为玩家
        if (!(context.getEntity() instanceof Player player)) return false;
        if (!context.has(ContextKeys.DOUBLE) || context.get(ContextKeys.DOUBLE).isEmpty()) return false;

        double amount = context.get(ContextKeys.DOUBLE).get(0);

        boolean multiply = false;
        if (context.get(ContextKeys.EVENT) instanceof LivingAttackEvent event
                && event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            multiply = true;
        }

        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                int rate = Config.ACTIVE_ITEM_DURABILITY_RESTORE_RATE.get() / 2;
                // 近战伤害获取额外充能数值
                if (multiply){
                    rate *= 4;
                }
                // 充电（传入蓄电池参数）
                PlayerHelper.chargeItem(stack,
                        Math.max((int) amount * rate, 1),
                        PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), (ServerPlayer) player));
            }
        }

        return true;
    }
}
