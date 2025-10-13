package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTrigger;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class TriggerItemEvents {
    public static final Set<String> ALLOWED_DAMAGE_TYPES = Set.of(
            "player",              // 玩家直接攻击
            "arrow",               // 弓箭
            "trident",             // 三叉戟
            "mob_projectile",      // 投射物
            "indirect_magic",      // 例如药水、魔法
            "magic",               // 魔法类
            "generic"              // 普通伤害
    );

    public static boolean isAllowed(DamageSource source) {
        return ALLOWED_DAMAGE_TYPES.contains(source.getMsgId());
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event) {
        // 检查攻击者是否为玩家
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        // 是否为可触发效果的伤害
        DamageSource source = event.getSource();
        if (!isAllowed(source)) return;

        // 获取玩家的被动物品能力实例
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
            Map<Integer, Integer> triggerItemMap = passiveItems.getPlayerTriggerItemMap();
            for (int itemId : triggerItemMap.keySet())
            {
                if (triggerItemMap.get(itemId) > 0){
                    IDamageTrigger item = (IDamageTrigger) PassiveItemManager.getInstance().getItemFromId(itemId);
                    item.onAttackEntity(player, event.getEntity());
                }
            }
        });
    }
}
