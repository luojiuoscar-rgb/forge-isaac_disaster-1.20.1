package net.luojiuoscar.isaac_disaster.event.effect;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.FrozenEffect;
import net.luojiuoscar.isaac_disaster.effect.custom.GoldenEffect;
import net.luojiuoscar.isaac_disaster.effect.custom.PetrifiedEffect;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.networking.EntityVisualStateSync;
import net.luojiuoscar.isaac_disaster.system.freeze.EntityFreezeRules;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.luojiuoscar.isaac_disaster.system.freeze.state.FrozenImpactState;
import net.luojiuoscar.isaac_disaster.system.freeze.state.TimeStopState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;

/** Forge-bus handlers shared by the golden, petrified, frozen, and time-stop pipelines. */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FreezeEffectEvents {
    private static final float FROZEN_HIT_PUSH_SPEED = 0.8F;

    private FreezeEffectEvents() {
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TimeStopState.clearServerLevelState(serverLevel);
            FrozenImpactState.clearLevel(serverLevel.dimension());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        TimeStopState.clearServerState();
        FrozenImpactState.clearAll();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TimeStopState.clearServerPlayerSnapshots(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TimeStopState.clearServerPlayerSnapshots(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        GoldenEffect.reconcileVisualState(target);
        PetrifiedEffect.reconcileVisualState(target);
        FrozenEffect.reconcileVisualState(target);
        EntityVisualStateSync.syncToPlayer(target, player);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TimeStopState.clearServerPlayerSnapshots(player.getUUID());
        }
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Mob mob) {
            FrozenImpactState.clear(mob);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide
                || (!victim.hasEffect(ModEffects.GOLDEN.get())
                && !victim.hasEffect(ModEffects.PETRIFIED.get()))) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            victim.level().playSound(null, victim.blockPosition(), SoundEvents.METAL_PLACE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (event.getSource().getDirectEntity() instanceof Player player
                && player.getMainHandItem().canPerformAction(ToolActions.PICKAXE_DIG)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().is(ModDamageType.FROZEN_SHATTER)) {
            return;
        }
        if (event.getEntity() instanceof Mob mob && EntityFreezeRules.usesLowFriction(mob)) {
            if (!mob.level().isClientSide) {
                FrozenImpactState.recordAttack(mob, event.getSource(), FROZEN_HIT_PUSH_SPEED);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            TimeStopState.clearServerPlayerSnapshots(player.getUUID());
        }
        if (entity.hasEffect(ModEffects.GOLDEN.get())) {
            LootHelper.spawnLootAtPos(entity, entity.position(), ModLootTables.RANDOM_COINS,
                    entity.getRandom().nextInt(0, 3));
        }
        if (!entity.level().isClientSide && entity instanceof Mob mob) {
            FrozenImpactState.clear(mob);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFrozenLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) {
            return;
        }
        if (!EntityVisualState.isFrozen(mob)) {
            if (!mob.level().isClientSide) {
                FrozenImpactState.clear(mob);
            }
            return;
        }

        if (!mob.level().isClientSide) {
            FrozenImpactState.tick(mob);
            if (mob.isRemoved()) {
                return;
            }
        }
        mob.xxa = 0.0F;
        mob.yya = 0.0F;
        mob.zza = 0.0F;
        mob.setJumping(false);

        if (EntityFreezeRules.shouldClearHorizontalMotion(mob)) {
            if (mob.onGround()) {
                mob.setDeltaMovement(0.0, 0.0, 0.0);
            } else {
                mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityKnockback(LivingKnockBackEvent event) {
        if (EntityFreezeRules.shouldCancelKnockback(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity livingEntity) {
            GoldenEffect.reconcileVisualState(livingEntity);
            PetrifiedEffect.reconcileVisualState(livingEntity);
            FrozenEffect.reconcileVisualState(livingEntity);
        }
    }

    @SubscribeEvent
    public static void onServerTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TimeStopState.refreshServer(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.side.isClient() && event.phase == TickEvent.Phase.END
                && event.player instanceof ServerPlayer player) {
            TimeStopState.updatePlayerFreezeState(player);
        }
    }
}
