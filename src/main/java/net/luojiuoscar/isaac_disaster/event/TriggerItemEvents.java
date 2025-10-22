package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IHurtTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
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
            for (int itemId : triggerItemMap.keySet()) {
                if (triggerItemMap.get(itemId) > 0){
                    if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IDamageTriggerPassiveItem item)) return;
                    item.onAttackEntity(player, event.getEntity());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        DamageSource source = event.getSource();
        double damage = event.getAmount();

        // 检测受伤者是否为玩家
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
                player.setHealth(player.getHealth() + StatManager.getHealthBonus() * 0.4f);
            }
            return;
        }

        // 神圣护盾(免疫则取消时间)
        if (player.hasEffect(ModEffects.HOLY_SHIELD.get())){
            int amplifier = player.getEffect(ModEffects.HOLY_SHIELD.get()).getAmplifier();

            if (damage > (amplifier + 1) * StatManager.getHolyShieldStrength()){
                // 只有伤害足够高的时候才移除护盾
                EntityHelper.removeAmplifier(player, ModEffects.HOLY_SHIELD.get());
                LevelHelper.spawnParticle((ServerLevel) player.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()),
                        player.position(), 0.5, 0.5, 0.5, 0.05, 20, false, null);

                // sounds
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.HOLY_SHIELD_BROKE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            event.setAmount(0.0f);
            event.setCanceled(true);
            return;
        }

        // 获取玩家的被动物品能力实例
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
            Map<Integer, Integer> triggerItemMap = passiveItems.getPlayerTriggerItemMap();
            for (int itemId : triggerItemMap.keySet()) {
                if (triggerItemMap.get(itemId) > 0){
                    if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IHurtTriggerPassiveItem item)) return;
                    // 如果是不触发惩罚性效果的类型
                    if (source.getMsgId().equals("genericKill") && item.isPunishType()) continue;
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
                        if (source.getMsgId().equals("genericKill") && trinket.isPunishType()) continue; // 惩罚类型

                        trinket.onHurt(player, attacker, playerSwallowedTrinkets.getAllTrinketListFromId(player, item.getTrinketId()), event);
                    }
                });

        // 常规效果（无论如何都会触发）
        // 成人套装
        if (PlayerHelper.hasSet(SetId.ADULT.getId(), player)){
            player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                    ModSounds.STEVE_HURT_OLD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        // 圣饼
        if (PlayerHelper.hasItem(ItemId.THE_WAFER.getId(), player)){
            event.setAmount(event.getAmount() * 0.5f);
        }

        // 惩罚性效果
        if (source.getMsgId().equals("genericKill")) return;
        // 死灵护盾
        if (player.hasEffect(ModEffects.NECRONMICON_SHIELD.get()) && damage > Math.max(1.0f, StatManager.getHealthBonus() * 0.25f)){
            // 伤害来源不能是拥有死灵庇护的玩家；否则不生效
            if (!(attacker instanceof Player attackerplayer &&
                    attackerplayer.hasEffect(ModEffects.NECRONMICON_SHIELD.get()))){
                // effect
                ActiveItemManager.getInstance().getItemFromId(ItemId.THE_NECRONMICON.getId()).onTriggeredEffect(player);
                // remove 1 amplifier
                EntityHelper.removeAmplifier(player, ModEffects.NECRONMICON_SHIELD.get());
                // sounds
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
        // 脆弱的心
        if (player.hasEffect(ModEffects.FRAGILE_HEART.get()) && damage > Math.max(1.0f, StatManager.getHealthBonus() * 0.25f)){
            double emptyHealth = player.getMaxHealth() - player.getHealth();
            // 当前骨心中有生命值时不消耗
            if (emptyHealth < StatManager.getHealthBonus()) return;

            EntityHelper.removeAmplifier(player, ModEffects.FRAGILE_HEART.get());
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.BONE_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            event.setAmount(0.0f); // 骨心破碎时不额外扣除生命值
        }
    }
}
