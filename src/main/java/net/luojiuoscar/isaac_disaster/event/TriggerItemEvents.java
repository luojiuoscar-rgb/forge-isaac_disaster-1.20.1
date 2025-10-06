package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTrigger;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class TriggerItemEvents {

    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event) {
        // 检查攻击者是否为玩家
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

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
