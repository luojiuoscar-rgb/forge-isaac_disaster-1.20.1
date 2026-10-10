package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.RevelationBeamS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.AdditionalAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ModChargeBars;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class RevelationAttack extends AbstractLaserAttack implements IChargeableAttack, AdditionalAttackType {
    private static final int CHARGE_TICKS = 47;
    private static final int HIT_INTERVAL = 2;
    private static final int HIT_COUNT = 15;
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "revelation_attack");
    private final ThreadLocal<ServerLevel> beamLevel = new ThreadLocal<>();

    public RevelationAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.REVELATION.getId();
    }

    private static final class Beam {
        private final UUID visualId = UUID.randomUUID();
        private final ServerPlayer player;
        private final ServerLevel level;
        private final AttackContext snapshot;
        private final boolean controllable;
        private Vec3 position;
        private Vec3 direction;
        private boolean followingPlayer = true;
        private int pulses;

        private Beam(ServerPlayer player, AttackContext snapshot, boolean controllable) {
            this.player = player;
            this.level = player.serverLevel();
            this.snapshot = snapshot;
            this.position = snapshot.getPos();
            this.direction = snapshot.getMainAxis();
            this.controllable = controllable;
        }

        private void updateAnchor() {
            if (!followingPlayer) return;
            if (player.isRemoved() || !player.isAlive() || player.level() != level
                    || level.getServer().getPlayerList().getPlayer(player.getUUID()) != player) {
                followingPlayer = false;
                return;
            }
            position = getBeamOrigin(player, 1.0F);
            if (controllable) {
                direction = GeometryHelper.mainAxisFromRotation(player.getXRot(), player.getYRot());
            }
        }
    }

    @Override
    public ResourceLocation getChargeBarId() {
        return ModChargeBars.REVELATION.getId();
    }

    @Override
    public int getTotalCharge(Player player) {
        return CHARGE_TICKS;
    }

    @Override
    public void onReleased(ServerPlayer player) {
        try {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
                if (ability.getChargeAmount(getChargeBarId()) < CHARGE_TICKS) return;
                AttackExecutor.perform(AttackRequest.withContexts(player, this, AttackOrigin.ABILITY_EXTRA,
                        AttackPipelineMode.EXECUTE_ONLY, List.of(createAttackContext(player, player)), true));
            });
        } finally {
            IChargeableAttack.super.onReleased(player);
        }
    }

    @Override
    public void onTick(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            if (ability.isHoldingRightClick()) addCharge(player, 1);
        });
    }

    @Override
    public void performAttack(List<AttackContext> contexts) {
        if (contexts == null) return;
        for (AttackContext context : contexts) {
            if (context == null || !(context.getOwner() instanceof ServerPlayer player)) continue;
            context.freeze();
            scheduleBeam(new Beam(player, context, isControllable(player)));
        }
    }

    private void scheduleBeam(Beam beam) {
        // A global finite task survives player cleanup and always completes its 15 pulses.
        ScheduledFuncHelper.schedule(SCHEDULE_TYPE,
                HIT_INTERVAL, HIT_INTERVAL - 1, HIT_COUNT, false, () -> {
                    beam.updateAnchor();
                    AttackContext shot = beam.snapshot.toBuilder()
                            .position(beam.position).mainAxis(beam.direction).build();
                    shot.setBulletScale(beam.snapshot.getBulletScale(), true);
                    performBeamPulse(beam.level, shot);
                    syncBeam(beam);
                    beam.pulses++;
                });
    }

    private void syncBeam(Beam beam) {
        Vec3 end = beam.position.add(beam.direction.normalize().scale(beam.snapshot.getBulletRange()));
        RevelationBeamS2CPacket packet = new RevelationBeamS2CPacket(
                beam.visualId, beam.player.getUUID(), beam.player.getId(), beam.level.dimension().location(),
                beam.position, beam.direction, (float) beam.snapshot.getBulletRange(),
                (float) getWidth(beam.snapshot), beam.pulses + 1 == HIT_COUNT,
                beam.followingPlayer, beam.controllable);
        Vec3 delta = end.subtract(beam.position);
        double lengthSqr = delta.lengthSqr();
        for (ServerPlayer viewer : beam.level.players()) {
            double fraction = lengthSqr == 0 ? 0 : Math.max(0, Math.min(1,
                    viewer.position().subtract(beam.position).dot(delta) / lengthSqr));
            // Close visuals for previous viewers even if they have since left the viewing radius.
            if (packet.finished()
                    || viewer.position().distanceToSqr(beam.position.add(delta.scale(fraction))) <= 32 * 32) {
                ModMessages.sentToPlayer(packet, viewer);
            }
        }
    }

    @Override
    public AttackContext createAttackContext(ServerPlayer player, Entity shooter) {
        // A fresh base context deliberately carries no player projectile modules or visuals.
        return AttackContext.builder(player, shooter).attackType(this)
                .color(ModBulletColors.REVELATION.getId())
                .position(getBeamOrigin(player, 1.0F))
                .mainAxis(GeometryHelper.mainAxisFromRotation(player.getXRot(), player.getYRot()))
                .range(getRange(player)).build();
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        return List.of(createAttackContext(player, player));
    }

    @Override
    public double getRange(LivingEntity entity) {
        return AttackContext.MAX_RANGE;
    }

    /** Shared collision and render origin, kept below the eyes for the entity's current pose. */
    public static Vec3 getBeamOrigin(Entity entity, float partialTick) {
        return entity.getEyePosition(partialTick).add(0, entity.getBbHeight() * -0.45D, 0);
    }

    private void performBeamPulse(ServerLevel level, AttackContext context) {
        ServerLevel previousLevel = beamLevel.get();
        beamLevel.set(level);
        try {
            context.freeze();
            performLaserBatch(List.of(context), ignored -> 0);
        } finally {
            if (previousLevel == null) beamLevel.remove();
            else beamLevel.set(previousLevel);
        }
    }

    @Override
    protected @Nullable ServerLevel getLaserLevel(@Nullable LivingEntity owner) {
        ServerLevel level = beamLevel.get();
        return level == null ? super.getLaserLevel(owner) : level;
    }

    @Override
    protected double getWidth(AttackContext context) {
        return laserWidth(context.getBulletScale(), 1.0D);
    }

    @Override
    protected Vector3f getDefaultLaserColor() {
        return new Vector3f(1.0F, 1.0F, 1.0F);
    }

    @Override
    protected boolean usesCollisionBatch() {
        // Single-beam pulses need one world query, not a shared spatial index.
        return false;
    }

    @Override
    protected boolean usesParticleVisuals() {
        return false;
    }

    @Override
    protected boolean isHoming(LivingEntity entity) {
        return false;
    }

    @Override
    protected boolean isSpectral(LivingEntity entity) {
        return true;
    }

    @Override
    protected boolean isPiercing(LivingEntity entity) {
        return true;
    }

    @Override
    public void makeSound(LivingEntity entity) {
        entity.level().playSound(null, entity.blockPosition(), ModSounds.REVELATION_SHOT.get(),
                SoundSource.PLAYERS, 0.8f, 1.0f);
    }
}
