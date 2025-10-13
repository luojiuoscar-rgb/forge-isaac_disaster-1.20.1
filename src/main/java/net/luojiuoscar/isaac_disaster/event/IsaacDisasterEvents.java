package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.event.custom.*;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTrigger;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.INewBulletType;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class IsaacDisasterEvents {


    @SubscribeEvent
    public static void onBulletShoot(IsaacBulletShootEvent event) {
        // 检查owner是否为玩家
        if (!(event.getShooter() instanceof Player player)) return;

        // 按概率、顺序修改可能的子弹类型
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
            Map<Integer, Integer> bulletTypeMap = passiveItems.getHasNewBulletTypeMap();
            for (int itemId : bulletTypeMap.keySet())
            {
                if (bulletTypeMap.get(itemId) > 0){
                    INewBulletType item = (INewBulletType) PassiveItemManager.getInstance().getItemFromId(itemId);
                    item.onShootEffect(player, event.getBullet());
                }
            }
        });

    }

    @SubscribeEvent
    public static void onBulletTick(IsaacBulletTickEvent event) {
    }

    @SubscribeEvent
    public static void onBulletHitEntity(IsaacBulletHitEntityEvent event) {
        IsaacBullet bullet = event.getBullet();
        List<Integer> effects = bullet.getBulletHitEffects();
        // 检测是否为玩家触发的效果
        if (!(bullet.getOwner() instanceof Player player && event.getHit().getEntity() instanceof LivingEntity living)) return;
        // 遍历并触发对应效果
        for (int itemId : effects){
            IDamageTrigger item =(IDamageTrigger)PassiveItemManager.getInstance().getItemFromId(itemId);
            item.handleAttackEntityEffect(player, living);
        }

    }

    @SubscribeEvent
    public static void onBulletDiscard(IsaacBulletDiscardEvent event) {

    }

    @SubscribeEvent
    public static void onBulletHitBlock(IsaacBulletHitBlockEvent event) {

    }

    @SubscribeEvent
    public static void onPacManEat(PacManEatEvent event) {
        LivingEntity eatenEntity = event.getEatenEntity();
        Player player = event.getPlayer();
        Level level = player.level();

        if (eatenEntity instanceof Creeper creeper){
            creeper.explodeCreeper();
        }
        if (eatenEntity instanceof EnderMan || eatenEntity instanceof Endermite){
            PlayerHelper.teleportToRandomLocation(player, 10);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}
