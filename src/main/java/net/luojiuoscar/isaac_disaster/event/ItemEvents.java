package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.effect.*;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IHurtTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.items.TheWafer;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.items.LuckyRock;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ItemEvents {
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
            Map<Integer, Integer> triggerItemMap = passiveItems.getAllTriggerItems(player);
            for (int itemId : triggerItemMap.keySet()) {
                if (triggerItemMap.get(itemId) > 0){
                    if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IDamageTriggerPassiveItem item)) return;
                    item.onAttackEntity(player, event.getEntity());
                }
            }
        });
    }

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
        Entity attacker = event.getSource().getEntity();
        DamageSource source = event.getSource();

        // 检测受伤者是否为玩家
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // 获取玩家的被动物品能力实例
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
            Map<Integer, Integer> triggerItemMap = passiveItems.getAllTriggerItems(player);
            for (int itemId : triggerItemMap.keySet()) {
                if (triggerItemMap.get(itemId) > 0){
                    if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IHurtTriggerPassiveItem item)) return;
                    // 如果是不触发惩罚性效果的类型
                    if (source.is(DamageTypes.GENERIC_KILL) && item.isPunishType()) continue;
                    item.onHurt(player, event.getSource().getEntity());
                }
            }
        });
        // 饰品效果
        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                playerSwallowedTrinkets -> {
                    List<ItemStack> stackList = playerSwallowedTrinkets.getAllTrinkets(player);

                    for (ItemStack stack : stackList) {
                        if (!(stack.getItem() instanceof Trinket item)) continue;
                        if (!(TrinketManager.getInstance().getTrinketFromId(item.getTrinketId()) instanceof IHurtTriggerTrinket trinket)) continue;
                        if (source.is(DamageTypes.GENERIC_KILL) && trinket.isPunishType()) continue; // 惩罚类型

                        trinket.onHurt(player, attacker, playerSwallowedTrinkets.getAllTrinketListFromId(player, item.getTrinketId()), event);
                    }
                });


        // 常规效果（无论如何都会触发）
        // 成人套装
        if (PlayerHelper.hasSet(SetId.ADULT.getId(), player)){
            player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                    ModSounds.STEVE_HURT_OLD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (PlayerHelper.hasItem(ItemId.THE_WAFER.getId(), player)) TheWafer.onTriggered(event);

        // 惩罚性效果
        if (source.is(DamageTypes.GENERIC_KILL)) return;
        if (player.hasEffect(ModEffects.NECRONMICON_SHIELD.get())) NecronmiconShieldEffect.onTriggered(event);
        if (player.hasEffect(ModEffects.ETERNAL_HEART.get())){
            if (EternalHeartEffect.onTriggered(event)){
                return;
            }
        }
        if (player.hasEffect(ModEffects.GILDING.get())) GildingEffect.onTriggered(event);
        if (player.hasEffect(ModEffects.FRAGILE_HEART.get())) FragileHeartEffect.onTriggered(event);
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
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Level level = player.level();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.COBBLESTONE)) {
            LuckyRock.onTriggered(event);
        }
    }

}
