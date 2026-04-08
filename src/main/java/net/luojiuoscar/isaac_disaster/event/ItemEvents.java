package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.effect.custom.SoulStateEffect;
import net.luojiuoscar.isaac_disaster.effect.custom.TheWorldEffect;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
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
    public static void onExplosion(ExplosionEvent.Detonate event) {
        // 获取爆炸源实体
        Entity source = event.getExplosion().getDirectSourceEntity();
        Level level = event.getLevel();

        // 获取到tnt的owner属性；触发后续逻辑
        if (source instanceof IsaacBomb tnt) {
            LivingEntity owner = tnt.getOwner();
            if (owner instanceof ServerPlayer player){
                // 获取pos
                Vec3 pos = new Vec3(tnt.getX(), tnt.getY(), tnt.getZ());
                // bomber boy
                if(PlayerHelper.hasItem(ItemId.BOMBER_BOY.getId(), player)){
                    EntityHelper.bomberBoy(player, tnt, pos, level);
                }

                // scatter bomb
                if(PlayerHelper.hasItem(ItemId.SCATTER_BOMB.getId(), player)){
                    EntityHelper.scatterBomb(player, tnt, pos, level);
                }

                // hot bomb
                if(PlayerHelper.hasItem(ItemId.HOT_BOMB.getId(), player)){
                    EntityHelper.HotBomb(player, tnt, pos, level);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGetShotDelay(GetShotDelayEvent event){
        Player player = event.getPlayer();
        double originalDelay = event.getOriginalDelay();

        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
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
