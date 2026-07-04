package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.effect.custom.SoulStateEffect;
import net.luojiuoscar.isaac_disaster.effect.custom.TheWorldEffect;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ItemEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        SoulStateEffect.onTriggered(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event){
        TheWorldEffect.onTriggered(event);
    }


    @SubscribeEvent
    public static void onGetShotDelay(GetShotDelayEvent event){
        Player player = event.getPlayer();
        double originalDelay = event.getOriginalDelay();

        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                playerPassiveItem -> {
                    Set<Integer> keys = playerPassiveItem.getItemCountMapFromAll(player).keySet();

                    if (keys.contains(ItemId.POLYPHEMUS.getId()) || keys.contains(ItemId.MUTANT_SPIDER.getId())
                            || keys.contains(ItemId.THE_INNER_EYE.getId())){
                        event.setDelay(originalDelay * 2);
                    }

                    if (keys.contains(ItemId.IPECAC.getId())){
                        event.setDelay(originalDelay * 3);
                    }

                    if (keys.contains(ItemId.PERFECT_VISION.getId())){
                        if (event.getDelay() > originalDelay)
                            event.setDelay(originalDelay);
                    }
                }
        );
    }
}
