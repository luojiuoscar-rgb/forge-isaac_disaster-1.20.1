package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.event.custom.*;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item.pickup.Card;
import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.ISpecialTypeBulletPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.items.*;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.SetId;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IsaacDisasterEvents {

    @SubscribeEvent
    public static void onPlayerPerformAttack(PlayerPerformAttackEvent event) {
        Player player = event.getPlayer();

        // 按概率、顺序修改可能的子弹类型
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
            Map<Integer, Integer> bulletTypeMap = passiveItems.getAllNewBulletTypeItems(player);

            for (int itemId : bulletTypeMap.keySet()) {

                if (bulletTypeMap.get(itemId) > 0){
                    ISpecialTypeBulletPassiveItem item = (ISpecialTypeBulletPassiveItem) PassiveItemManager.getInstance().getItemFromId(itemId);
                    item.onShoot(event);
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
            TinyPlanet.onTriggered(event);
        }
    }



    @SubscribeEvent
    public static void onBulletHitBlock(IsaacBulletHitBlockEvent event) {
        IsaacBullet bullet = event.getBullet();

        if (!(bullet.getOwner() instanceof ServerPlayer player)) return;

        if (PlayerHelper.hasItem(ItemId.RUBBER_CEMENT.getId(), player)){
            RubberCement.bounceOnBlock(event);
        }
    }



    @SubscribeEvent
    public static void beforeBulletHitEntity(IsaacBulletBeforeHitEvent event) {
        IsaacBullet bullet = event.getBullet();
        Set<Integer> effects = bullet.getBulletHitEffects();

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
        IsaacBullet bullet = event.getBullet();
        EntityHitResult hit = event.getHitResult();

        if (!(bullet.getOwner() instanceof ServerPlayer player)) return;

        if (PlayerHelper.hasItem(ItemId.POLYPHEMUS.getId(), player)){
            if (hit.getEntity() instanceof LivingEntity living && living.isDeadOrDying()){
                Polyphemus.onTriggered(event);
            }
        }

        if (PlayerHelper.hasItem(ItemId.RUBBER_CEMENT.getId(), player) && !bullet.isPiercing){
            RubberCement.bounceOnEntity(event);
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

    @SubscribeEvent
    public static void onItemDisplayAdd(ItemDisplayAddEvent event){
        if (event.getLevel().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.getPlayer();

        if (PlayerHelper.hasItem(ItemId.GLITCHED_CROWN.getId(), player)){
            GlitchedCrown.addDisplayItem(event);
        }
        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), player)){
            BingeEater.addDisplayItem(event);
        }

    }

    @SubscribeEvent
    public static void onPickupUse(PickupUseEvent event){
        ItemStack stack = event.getStack();
        Player player = event.getPlayer();
        if (stack == null || player == null || player.level().isClientSide) return;

        if ((stack.getItem() instanceof Pill || stack.getItem() instanceof Card) &&
                PlayerHelper.hasItem(ItemId.ECHO_CHAMBER.getId(), (ServerPlayer) player)){
            EchoChamber.onTriggered(player);
        }
    }

    @SubscribeEvent
    public static void onGetBulletCount(IsaacGetBulletCountEvent event){
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        RandomSource rand = player.getRandom();
        int count = event.getCount();

        if (PlayerHelper.hasSet(SetId.BOOK.getId(), player) && rand.nextDouble() < 0.25){
            count++;
        }

        event.setCount(count);
    }


}
