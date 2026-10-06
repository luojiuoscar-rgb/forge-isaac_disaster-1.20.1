package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrimstoneAttack extends LaserAttack implements IChargeableAttack {
    private static final float DAMAGE_PERCENTAGE = 0.6f;
    static final int SHOT_COUNT = 13;
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "brimstone_attack");

    public BrimstoneAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public BrimstoneAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.BRIMSTONE.getId();
    }

    // ================== handleAttack ==================
    /** Brimstone schedules its 13 shots, grouping contexts that share an owner. */
    @Override
    public void performAttack(List<AttackContext> contexts) {
        if (contexts == null || contexts.isEmpty()) return;

        Map<UUID, List<AttackContext>> contextsByOwner = new LinkedHashMap<>();
        for (AttackContext context : contexts) {
            if (context == null) continue;
            contextsByOwner.computeIfAbsent(context.getOwner().getUUID(), ignored -> new ArrayList<>())
                    .add(context);
        }
        for (List<AttackContext> ownerContexts : contextsByOwner.values()) {
            scheduleShots(ownerContexts);
        }
    }

    @Override
    public void shoot(AttackContext baseContext) {
        if (baseContext == null) return;
        scheduleShots(List.of(baseContext));
    }

    private void scheduleShots(List<AttackContext> baseContexts) {
        if (baseContexts.isEmpty()) return;
        AttackContext firstContext = baseContexts.get(0);
        List<ShotPlan> shotPlans = baseContexts.stream()
                .map(this::createShotPlan)
                .toList();
        AtomicInteger sequenceIndex = new AtomicInteger();
        ScheduledFuncHelper.scheduleForPlayer(firstContext.getOwner().getUUID(),
                SCHEDULE_TYPE, 1, 1, SHOT_COUNT, false, () -> {
            int currentSequenceIndex = sequenceIndex.incrementAndGet();
            List<AttackContext> shotContexts = new ArrayList<>(shotPlans.size());
            for (ShotPlan shotPlan : shotPlans) {
                shotContexts.add(shotPlan.createContext());
            }
            performLaserBatch(shotContexts, ignored -> currentSequenceIndex);
        });
    }

    private ShotPlan createShotPlan(AttackContext baseContext) {
        boolean fixedLaunchTransform = baseContext.usesFixedLaunchTransform();
        boolean controllable = isControllable(baseContext.getOwner());
        Vec3 fixedSpawnPosition = fixedLaunchTransform ? baseContext.getPos() : null;
        Vec3 fixedMainAxis = fixedLaunchTransform ? baseContext.getMainAxis() : null;
        return new ShotPlan(baseContext, fixedLaunchTransform, controllable,
                fixedSpawnPosition, fixedMainAxis);
    }

    private static final class ShotPlan {
        private final AttackContext baseContext;
        private final boolean fixedLaunchTransform;
        private final boolean controllable;
        private final Vec3 fixedSpawnPosition;
        private final Vec3 fixedMainAxis;

        private ShotPlan(AttackContext baseContext, boolean fixedLaunchTransform, boolean controllable,
                         Vec3 fixedSpawnPosition, Vec3 fixedMainAxis) {
            this.baseContext = baseContext;
            this.fixedLaunchTransform = fixedLaunchTransform;
            this.controllable = controllable;
            this.fixedSpawnPosition = fixedSpawnPosition;
            this.fixedMainAxis = fixedMainAxis;
        }

        private AttackContext createContext() {
            AttackContext shotContext = baseContext.toBuilder().build();
            Entity shooter = shotContext.getShooter();
            if (fixedLaunchTransform) {
                refreshBrimstoneShotContext(shotContext, fixedSpawnPosition, fixedMainAxis, true);
            } else {
                Vec3 spawnPosition = resolveSpawnPosition(shooter);
                refreshBrimstoneShotContext(shotContext, spawnPosition,
                        GeometryHelper.mainAxisFromRotation(shooter.getXRot(), shooter.getYRot()), controllable);
            }
            shotContext.freeze();
            return shotContext;
        }
    }

    /** Fires one Brimstone laser using the caller-provided runtime sequence identity. */
    public void shootSingle(AttackContext context, int sequenceIndex) {
        shootSingleLaser(context, sequenceIndex);
    }

    static AttackContext refreshBrimstoneShotContext(AttackContext shotContext, Vec3 spawnPosition,
                                                     Vec3 mainAxis, boolean controllable) {
        shotContext.setPos(spawnPosition);
        if (controllable) {
            shotContext.setMainAxis(mainAxis);
        }
        return shotContext;
    }

    private static Vec3 resolveSpawnPosition(Entity shooter) {
        return shooter.getEyePosition().add(0, shooter.getBbHeight() * -0.15, 0);
    }

    @Override
    protected double getWidth(AttackContext context) {
        return 1.0D;
    }

    @Override
    public void makeSound(LivingEntity entity) {
        entity.level().playSound(
                null,
                entity.blockPosition(),
                ModSounds.BRIMSTONE_SHOT_NORMAL.get(),
                SoundSource.PLAYERS,
                0.8f,
                1.0f
        );
    }

    @Override
    protected boolean applyDamage(LivingEntity source, LivingEntity target, float damage) {
        return target.hurt(getDamageSource(source), damage * DAMAGE_PERCENTAGE);
    }

    @Override
    protected boolean isSpectral(LivingEntity entity){
        return true;
    }

    @Override
    protected boolean isPiercing(LivingEntity entity){
        return true;
    }

    // =================== Chargeable ===================
    @Override
    public void onTick(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.isHoldingRightClick()
                            && playerAbility.getChargeAmount() < getTotalCharge(player)){
                        playerAbility.setChargeAmount(playerAbility.getChargeAmount() + 1);
                    }
                }
        );
    }

        @Override
        public void onPressed(ServerPlayer player) {
            // 清除当前玩家域的schedule
            ScheduledFuncHelper.clearByType(SCHEDULE_TYPE, player.getUUID());
        }

        @Override
        public void onReleased(ServerPlayer player) {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.getChargeAmount() >= getTotalCharge(player)
                            && PlayerHelper.isHoldingIsaacHead(player)){

                        BeforePerformAttackEvent event = new BeforePerformAttackEvent(player, this);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.isCanceled()) return;

                        // attack
                        AttackExecutor.perform(AttackRequest.generated(
                                player, this, AttackOrigin.PLAYER_PRIMARY,
                                AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false));
                        makeSound(player);
                    }
                    playerAbility.setChargeAmount(0);
                }
        );
    }

    @Override
    public int getTotalCharge(Player player) {
        return (int) getShotDelay(player) * 3;
    }

    @Override
    protected double getTears(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS.get());
        if (instance == null) return StatManager.TEARS.getBonus() * -2;

        return  Math.max(instance.getValue() + (StatManager.TEARS.getBonus() * -2),-7);
    }
}
