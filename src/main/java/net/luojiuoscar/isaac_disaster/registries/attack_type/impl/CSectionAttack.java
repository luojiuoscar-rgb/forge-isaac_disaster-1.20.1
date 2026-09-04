package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.bullet.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CSectionAttack extends BulletAttack implements IChargeableAttack {
    private static final float DAMAGE_PERCENTAGE = 0.75f;

    public CSectionAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public CSectionAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.C_SECTION.getId();
    }

    @Override
    public void makeSound(LivingEntity entity){
        entity.level().playSound(
                null,
                entity.blockPosition(),
                ModSounds.C_SECTION_SHOT.get(),
                SoundSource.PLAYERS,
                0.6f,
                1.0f
        );
    }

    @Override
    protected BulletState createOptimizedState(AttackContext context) {
        BulletState state = super.createOptimizedState(context);
        double collisionHeight = context.getBulletScale() * 0.35 * 1.8;
        return BulletState.from(context)
                .position(state.position().add(0.0D, (collisionHeight - state.collisionHeight()) * 0.5D, 0.0D))
                .lifetime(state.lifetime())
                .collisionWidth(context.getBulletScale() * 0.35 * 0.6)
                .collisionHeight(collisionHeight)
                .sourceType(BulletSourceType.FETUS_BULLET).homingRange(6.0).spectral(true).homing(true).piercing(true)
                .controlRange(64.0D).controlSteer(0.8D)
                .rememberHitTargets(false).steeringMode(net.luojiuoscar.isaac_disaster.bullet.BulletSteeringMode.DIRECT)
                .maxSpeedChange(0.25D)
                .build();
    }

    @Override
    public void shoot(AttackContext ctx) {
        super.shoot(ctx);
        if (ctx.getOwner() == null || !(ctx.getOwner().level() instanceof ServerLevel level)) return;
        spawnBloodParticles(level, ctx.getPos(), ctx.getOwner().getLookAngle(), 10, 0.35, 0.35);
    }

    private void spawnBloodParticles(ServerLevel level, Vec3 pos, Vec3 direction, int count, double spread, double verticalOffset) {
        Vec3 flatDir = new Vec3(direction.x, 0, direction.z).normalize();

        for (int i = 0; i < count; i++) {
            double angleOffset = (level.random.nextDouble() - 0.5) * spread;
            double cos = Math.cos(angleOffset);
            double sin = Math.sin(angleOffset);

            double vx = flatDir.x * cos - flatDir.z * sin;
            double vz = flatDir.x * sin + flatDir.z * cos;

            double vy = (level.random.nextDouble() - 0.5) * verticalOffset;

            DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f);
            level.sendParticles(dust, pos.x, pos.y, pos.z, 1, vx, vy, vz, 0.1);
        }
    }


    @Override
    protected boolean isSpectral(LivingEntity entity){
        return true;
    }

    @Override
    protected boolean isHoming(LivingEntity entity){
        return true;
    }

    @Override
    protected boolean isPiercing(LivingEntity entity){
        return true;
    }

    /** 胎儿子弹从腹部发射 */
    @Override
    public @Nullable AttackContext createAttackContext(ServerPlayer player, Entity shooter) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(playerAbility -> {
                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();
                    Vec3 eyePos = player.position().add(0, player.getBbHeight() * 0.5, 0);

                    return AttackContext.builder(player, shooter)
                            .color(colorRl).visuals(playerAbility.getBulletVisuals())
                            .trigger(new CompositeTrigger()).trajectories(trajectories)
                            .position(eyePos).mainAxis(GeometryHelper.mainAxisFromRotation(player.getXRot(), player.getYRot()))
                            .damage((double) getDamage(player) * DAMAGE_PERCENTAGE)
                            .range(getRange(player)).speed(getBulletSpeed(player)).build();
                })
                .orElse(null);
    }

    @Override
    public void onTick(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    int charge = playerAbility.getChargeAmount();

                    if (playerAbility.isHoldingRightClick() && charge < getTotalCharge(player)
                            && PlayerHelper.isHoldingIsaacHead(player)){

                        if (charge + 1 >= getTotalCharge(player)){
                            playerAbility.setChargeAmount(0);

                            BeforePerformAttackEvent event = new BeforePerformAttackEvent(player, this);
                            MinecraftForge.EVENT_BUS.post(event);
                            if (event.isCanceled()) return;

                            AttackExecutor.perform(AttackRequest.generated(
                                    player, this, AttackOrigin.PLAYER_PRIMARY,
                                    AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false));
                            makeSound(player);

                        }else{
                            playerAbility.setChargeAmount(charge + 1);
                        }
                    }
                }
        );
    }
    // =================== Chargeable ===================
    @Override
    public void onPressed(ServerPlayer player) {

    }

    @Override
    public void onReleased(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setChargeAmount(0)
        );
    }

    @Override
    public int getTotalCharge(Player player) {
        return (int) (6 * getShotDelay(player) + 4) / 3;
    }

    @Override
    public double getBulletSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_SPEED.get());
        return attr != null ? Math.max(attr.getValue(), 0.1) * 0.7 : 0.7;
    }

}
