package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.curse.CurseOfTheMaze;
import net.luojiuoscar.isaac_disaster.effect.custom.*;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl.Perfection;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ItemEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerHurtHighest(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // 免爆
        if (source.getMsgId().contains("explosion") &&
                (PlayerHelper.hasItem(ItemId.HOST_HAT.getId(), player) ||
                        PlayerHelper.hasItem(ItemId.PYROMANIAC.getId(), player))){
            // 取消受伤事件
            event.setCanceled(true);
            // 如果是纵火狂  则回血
            if (PlayerHelper.hasItem(ItemId.PYROMANIAC.getId(), player)){
                player.setHealth(player.getHealth() + (float) StatManager.MAX_HEALTH.getBonus() * 0.4f);
            }
            return;
        }

        // 神圣护盾(免疫则取消时间)
        if (player.hasEffect(ModEffects.HOLY_SHIELD.get())){
            HolyShieldEffect.onTriggered(event);
        }
    }


    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        // 检测受伤者是否为玩家
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // 常规效果（无论如何都会触发）
        // 成人套装
        if (PlayerHelper.hasSet(ModSetAbility.ADULT.getId(), player)){
            player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                    ModSounds.STEVE_HURT_OLD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        // 惩罚性效果
        if (PlayerHelper.isSelfDamage(source)) return;

        if (player.hasEffect(ModEffects.NECRONMICON_SHIELD.get())) NecronmiconShieldEffect.onTriggered(event);
        if (player.hasEffect(ModEffects.ETERNAL_HEART.get()))
            if (EternalHeartEffect.onTriggered(event))
                return;
        if (player.hasEffect(ModEffects.GILDING.get())) GildingEffect.onTriggered(event);
        if (player.hasEffect(ModEffects.FRAGILE_HEART.get())) FragileHeartEffect.onTriggered(event);
        if (player.hasEffect(ModEffects.CURSE_OF_THE_MAZE.get())) CurseOfTheMaze.onTriggered(event);
        if (PlayerHelper.hasTrinket(TrinketId.PERFECTION.getId(), player)) Perfection.onTriggered(player);

    }


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
