package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.event.custom.*;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.INewBulletTypePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IsaacDisasterEvents {

    @SubscribeEvent
    public static void onBulletShoot(IsaacBulletShootEvent event) {
        // 检查owner是否为玩家
        if (!(event.getShooter() instanceof Player player)) return;

        // 按概率、顺序修改可能的子弹类型
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
            Map<Integer, Integer> bulletTypeMap = passiveItems.getAllNewBulletTypeItems(player);
            for (int itemId : bulletTypeMap.keySet())
            {
                if (bulletTypeMap.get(itemId) > 0){
                    INewBulletTypePassiveItem item = (INewBulletTypePassiveItem) PassiveItemManager.getInstance().getItemFromId(itemId);
                    item.onShootEffect(player, event.getBullet());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onBulletTick(IsaacBulletTickEvent event){
        IsaacBullet bullet = event.getBullet();
        LivingEntity owner = bullet.getOwner();

        if (!(owner instanceof ServerPlayer player)) return;

        if (PlayerHelper.hasItem(ItemId.TINY_PLANET.getId(), player)){
            bullet.steerHorizontalOrbit(player, 0.3, 2.0, 0.1); // 尝试朝玩家飞行
        }
    }



    @SubscribeEvent
    public static void onBulletHitBlock(IsaacBulletHitBlockEvent event) {
        IsaacBullet bullet = event.getBullet();
        BlockHitResult hit = event.getHitResult();

        // 仅玩家
        if (!(bullet.getOwner() instanceof ServerPlayer player)) return;
        if (PlayerHelper.hasItem(ItemId.RUBBER_CEMENT.getId(), player)){
            Vec3 motion = bullet.getDeltaMovement();
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).normalize();

            // 反射方向
            Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));
            bullet.setDeltaMovement(reflected);

            // 防止立即再次撞到同一个方块
            bullet.move(MoverType.SELF, reflected.scale(0.1));

            // 不要销毁子弹
            event.setCanceled(true);
        }
    }



    @SubscribeEvent
    public static void beforeBulletHitEntity(IsaacBulletBeforeHitEvent event) {
        IsaacBullet bullet = event.getBullet();
        List<Integer> effects = bullet.getBulletHitEffects();

        if (event.getHit().getEntity() instanceof LivingEntity living &&
        living.hasEffect(ModEffects.SOUL_STATE.get())) {
            event.setCanceled(true);
            return;
        }

        // 检测是否为玩家触发的效果
        if (!(bullet.getOwner() instanceof Player player && event.getHit().getEntity() instanceof LivingEntity living)) return;

        // 遍历并触发对应效果
        for (int itemId : effects){
            IDamageTriggerPassiveItem item =(IDamageTriggerPassiveItem)PassiveItemManager.getInstance().getItemFromId(itemId);
            item.handleAttackEntityEffect(player, living);
        }
    }

    @SubscribeEvent
    public static void afterBulletHitEntity(IsaacBulletAfterHitEvent event) {
        double damage = event.getDamage();
        IsaacBullet bullet = event.getBullet();
        EntityHitResult hit = event.getHitResult();

        if (bullet.getOwner() instanceof ServerPlayer player){

            if (PlayerHelper.hasItem(ItemId.POLYPHEMUS.getId(), player)){
                // 若生物死亡，则减少对应伤害并接着飞行
                if (hit.getEntity() instanceof LivingEntity living && living.isDeadOrDying()){
                    double effectiveDamage = event.getTargetHealth();
                    double newDamage = damage - effectiveDamage;
                    if (newDamage > 0){  // 如果还有伤害剩余
                        event.setDiscardAfterHit(false);
                        event.setDamage(newDamage);
                        // 计算新体型
                        event.getBullet().setScale(PlayerHelper.getBulletScale(newDamage, PlayerHelper.getExtraBulletScale(player)));
                    }
                }
            }
            if (PlayerHelper.hasItem(ItemId.RUBBER_CEMENT.getId(), player)){
                if (bullet.isPiercing()) return;

                Vec3 motion = bullet.getDeltaMovement();
                double speed = motion.length();

                Vec3 hitPos = hit.getLocation();
                Vec3 targetCenter = hit.getEntity().position().add(0, hit.getEntity().getBbHeight() * 0.5, 0);

                Vec3 diff = targetCenter.subtract(hitPos);
                Vec3 normal = new Vec3(diff.x, 0, diff.z).normalize();

                if (normal.lengthSqr() < 1e-6)
                    normal = motion.reverse().normalize();

                Vec3 reflected = motion.normalize().subtract(normal.scale(2 * motion.normalize().dot(normal))).normalize().scale(speed);

                if (reflected.dot(motion.normalize()) > 0.9)
                    reflected = reflected.add((Math.random()-0.5)*0.2, (Math.random()-0.5)*0.05, (Math.random()-0.5)*0.2).normalize().scale(speed);

                bullet.setDeltaMovement(reflected);

                // 阻止子弹被销毁
                event.setDiscardAfterHit(false);
            }
        }
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

    @SubscribeEvent
    public static void onActiveItemUse(ActiveItemUseEvent event){
        Player player = event.getPlayer();

        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                playerSwallowedTrinkets -> {
                    List<ItemStack> stackList = playerSwallowedTrinkets.getAllTrinkets(player);

                    // 损坏的遥控器
                    if (stackList.stream().anyMatch(stack -> stack.getItem() instanceof Trinket trinket &&
                            trinket.getTrinketId() == TrinketId.BROKEN_REMOTE.getId())){
                        List<ItemStack> s = playerSwallowedTrinkets.getAllTrinketListFromId(player, TrinketId.BROKEN_REMOTE.getId());
                        if (s.stream().anyMatch(Trinket::isEnchanted)){
                            PlayerHelper.teleportToRandomLocation(player, StatManager.getNearbyRange() * 6);
                        }else{
                            PlayerHelper.teleportToRandomLocation(player, StatManager.getNearbyRange() * 3);
                        }
                    }
                });


    }
}
