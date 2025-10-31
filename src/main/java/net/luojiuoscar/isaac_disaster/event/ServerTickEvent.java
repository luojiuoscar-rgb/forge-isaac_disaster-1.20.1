package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.IsaacHead;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ServerTickEvent {
    private static final int TICK_FREQUENCY = 4;

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
        if (tickCounter % ServerTickEvent.TICK_FREQUENCY == 0){
            for (ServerPlayer player : server.getPlayerList().getPlayers()){
                // main
                chargeActiveItem(player);
                updateFly(player);
                bugsFix(player);
                recursiveItemTick(player);
                onPlayerSprint(player);
            }
        }

        // 每tick执行一次
        for (ServerPlayer player : server.getPlayerList().getPlayers()){
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                    playerAbility -> {
                        if (playerAbility.isHoldRightClick()){
                            // right click effect
                            if (player.getMainHandItem().getItem() instanceof IsaacHead){
                                holdRightClick(player, player.getMainHandItem());
                            }else if(player.getOffhandItem().getItem() instanceof IsaacHead){
                                holdRightClick(player, player.getOffhandItem());
                            }
                        }
                    });
        }

        // 更新所有scheduled function
        ScheduledFuncHelper.tick(server);
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


        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                ActiveItem.modifyCharge(stack, Config.ACTIVE_ITEM_DURABILITY_RESTORE_RATE.get(),
                        PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), player));
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
                    playerStatModifier -> playerStatModifier.addCurrentFlyTime(player, 4)
            );
        }else{
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                    playerStatModifier -> playerStatModifier.addCurrentFlyTime(player, -1)
            );
        }
    }
    private static void recursiveItemTick(ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.recursiveItemTick(player, TICK_FREQUENCY)
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
                LevelHelper.selectBySquare(player.level(), player.getX(), player.getY(), player.getZ(), radius);

        for (LivingEntity living : entities){
            if (EntityHelper.isFriendlyToPlayer(living, player)) return; // 跳过友方

            living.hurt(player.damageSources().playerAttack(player), damage);
        }
    }


    private static void holdRightClick(ServerPlayer player, ItemStack stack){
        // 若在冷却
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        // 若有无泪症
        if (player.hasEffect(ModEffects.LACRIMAL_HYPOSECRETION.get())) return;

        // 发射子弹
        PlayerHelper.shotBulletFromPlayer(player);

        // 射击延迟
        player.getCooldowns().addCooldown(stack.getItem(), (int) PlayerHelper.getShotDelay(player));

        // SFX
        player.level().playSound(
                null,
                player.blockPosition(),
                ModSounds.ISAAC_HEAD_SHOOT.get(),
                SoundSource.PLAYERS,
                0.6f,
                1.0f
        );
    }

}
