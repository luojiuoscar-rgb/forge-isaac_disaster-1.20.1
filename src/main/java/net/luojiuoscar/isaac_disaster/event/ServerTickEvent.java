package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.RightClickTickEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.RefreshScaleS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.data.AbilityEffectTokenBucket;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ServerTickEvent {
    private static final int TICK_FREQUENCY = 4;
    private static final float SCALE_REFRESH_EPSILON = 0.0001F;
    private static final Map<UUID, Float> PLAYER_SCALE_CACHE = new HashMap<>();

    private static int tickCounter;

    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        // 只在服务器端处理，避免客户端和服务器重复执行
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        tickCounter++;
        // 重置计数器，防止整数溢出
        if (tickCounter >= Integer.MAX_VALUE - 10) {
            tickCounter = 0;
        }
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        // 获取服务器实例
        if (server == null) {
            server = event.player.getServer();
        }
        if (server == null) return;

        // 每tickCounter执行一次
        if (tickCounter % TICK_FREQUENCY == 0){
            for (ServerPlayer player : server.getPlayerList().getPlayers()){

                chargeActiveItem(player);
                onPlayerSprint(player);
                updateFly(player);
            }
        }

        // 每秒执行
        if (tickCounter % 20 == 0){
            for (ServerPlayer player : server.getPlayerList().getPlayers()){

                bugsFix(player);
                ForgeEvents.syncAllDataToClient(player);
            }
        }

        // 每tick执行一次
        for (ServerPlayer player : server.getPlayerList().getPlayers()){

            IsaacHeadAttack(player);
            recursiveModuleTick(player);
            refreshScaleIfChanged(player);

            if (tickCounter % 3 == 0){
                updateClientCharge(player);
            }
        }

        // 更新所有scheduled function
        ScheduledFuncHelper.tick(server);
        AbilityEffectTokenBucket.getInstance().tick();

    }

    private static void refreshScaleIfChanged(ServerPlayer player) {
        float scale = ScaleUtils.getScale(player);
        Float oldScale = PLAYER_SCALE_CACHE.put(player.getUUID(), scale);
        if (oldScale == null || Math.abs(scale - oldScale) > SCALE_REFRESH_EPSILON) {
            player.refreshDimensions();
            ModMessages.sentToPlayer(new RefreshScaleS2CPacket(), player);
        }
    }


    private static void bugsFix(ServerPlayer player){
        // 防止无限无敌
        if (player.isInvulnerable() && player.getEffect(ModEffects.INVINCIBLE.get()) == null){
            player.setInvulnerable(false);
        }
    }
    private static void chargeActiveItem(ServerPlayer player){
        // 是否启用自动回复
        if (!Config.ACTIVE_ITEM_AUTO_RESTORE.get()) return;

        // 有4.5伏特时不执行充能
        if (PlayerHelper.hasItem(ItemId.VOLT_4P5.getId(), player)) return;

        // 恢复耐久
        Inventory inv = player.getInventory();
        List<ItemStack> items = new ArrayList<>();
        // 仅在主副手
        if (Config.LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE.get()){
            items.add(player.getMainHandItem());
            items.add(player.getOffhandItem());
        }else{
            items.addAll(inv.items);
            items.addAll(inv.offhand);
        }

        for (ItemStack stack : items) {

            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                PlayerHelper.chargeItem(stack, Config.ACTIVE_ITEM_DURABILITY_RESTORE_RATE.get(), PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), player));
            }
        }
    }
    private static void updateFly(ServerPlayer player){
        if (player.isCreative() || player.isSpectator() || !PlayerHelper.canFly(player)) return;

        // 飞行故障修复（防止玩家可以飞但飞不起来）
        if (!player.getAbilities().mayfly){
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        // 正在飞
        if (player.getAbilities().flying && player.getEffect(ModEffects.TRANSCENDENCE.get()) == null){
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                    playerStatModifier -> playerStatModifier.addCurrentFlyTime(player, TICK_FREQUENCY)
            );
        }else{
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                    playerStatModifier -> playerStatModifier.addCurrentFlyTime(player, -(int)TICK_FREQUENCY/4)
            );
        }
    }

    private static void updateClientCharge(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    float maxCharge = 1f;
                    if (playerAbility.getCachedAttackType() instanceof IChargeableAttack attack) {
                        maxCharge = attack.getTotalCharge(player);
                        if (maxCharge <= 0) maxCharge = 1f;
                    }

                    float progress = playerAbility.getChargeAmount() / maxCharge;
                    progress = Math.min(progress, 1f);

                    // if current charge amount is not equal to pre-charge amount
                    if (playerAbility.getChargeAmount() != playerAbility.getPreChargeAmount()){
                        playerAbility.setPreChargeAmount(playerAbility.getChargeAmount());
                        ModMessages.sentToPlayer(new ChargeBarUpdateS2CPacket(progress), player);
                    }
                }
        );
    }

    private static void recursiveModuleTick(ServerPlayer player){
        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                playerPassiveItem -> playerPassiveItem.getRecursiveModuleQueue().tickAll(player)
        );
    }

    private static void onPlayerSprint(ServerPlayer player){
        // 仅限疾跑状态
        if (!player.isSprinting() || player.isCreative() || player.isSpectator()) return;

        AttributeInstance instance = player.getAttribute(ModAttributes.COLLISION_DAMAGE.get());
        if (instance == null) return;

        float damage = (float) instance.getValue();
        if (damage == 0) return; // 无碰撞伤害属性则取消判定

        double radius = player.getBbWidth() + 1; // 略大于玩家碰撞盒范围

        List<LivingEntity> entities =
                LevelHelper.selectBySquare(player.level(), player.position(), radius);

        for (LivingEntity living : entities){
            if (EntityHelper.isFriendly(living, player)) return; // 跳过友方

            living.hurt(player.damageSources().playerAttack(player), damage);
        }
    }

    private static void IsaacHeadAttack(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    ItemStack stack = null;
                    if (player.getMainHandItem().getItem() instanceof IsaacHead){
                        stack = player.getMainHandItem();

                    }else if(player.getOffhandItem().getItem() instanceof IsaacHead){
                        stack = player.getOffhandItem();
                    }
                    if (stack == null) return;

                    AttackType attack = playerAbility.getCachedAttackType();
                    attack.onTick(player);

                    if (playerAbility.isHoldingRightClick()){
                        RightClickTickEvent rcEvent = new RightClickTickEvent(player);
                        MinecraftForge.EVENT_BUS.post(rcEvent);
                    }

                    // 没有按下右键 or 若在冷却 or 有无泪症
                    if (!playerAbility.isHoldingRightClick()
                            || player.getCooldowns().isOnCooldown(stack.getItem())
                            || player.hasEffect(ModEffects.LACRIMAL_HYPOSECRETION.get())) return;

                    // perform attack
                    if (!(attack instanceof IChargeableAttack)){

                        BeforePerformAttackEvent event = new BeforePerformAttackEvent(player, attack);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.isCanceled()) return;

                        attack.performAttack(attack.getAttackContextsWithEvent(player, attack.getBulletCount(player)));
                        attack.makeSound(player);

                        // 射击延迟
                        player.getCooldowns().addCooldown(stack.getItem(), (int) PlayerHelper.getShotDelay(player));

                        // 重置charge bar
                        if (playerAbility.getChargeAmount() > 0) playerAbility.setChargeAmount(0);
                    }
                });
    }
}
