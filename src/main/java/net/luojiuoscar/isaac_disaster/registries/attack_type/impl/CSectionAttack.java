package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CSectionAttack extends BulletAttack implements IChargeableAttack {
    private static final float DAMAGE_PERCENTAGE = 0.75f;

    public CSectionAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackType.C_SECTION.getId();
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
    public TearBullet getBulletObject(LivingEntity shooter, float xRot, float yRot){
        return new FetusBullet(
                shooter.level(),
                shooter,
                getBulletLiftTime(shooter),
                getBulletSpeed(shooter),
                getBulletScale(shooter),
                getDamage(shooter) * DAMAGE_PERCENTAGE,
                xRot,
                yRot
        );
    }

    @Override
    public void handleShoot(LivingEntity shooter, AttackContext context, Vec3 offset, float xRot, float yRot) {
        if (shooter.level().isClientSide) return;

        Vec3 bodyPos = shooter.position().add(0, shooter.getBbHeight() * 0.5, 0);
        Vec3 spawnPos = bodyPos.add(offset);

        TearBullet bullet = createBullet(shooter, spawnPos, xRot, yRot, context);
        shooter.level().addFreshEntity(bullet);

        spawnBloodParticles((ServerLevel) shooter.level(), bodyPos,
                shooter.getLookAngle(), 10, 0.35, 0.35);
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

    // =================== Chargeable ===================
    @Override
    public void onTick(Player player, AttackContext context) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    int charge = playerAbility.getChargeAmount();

                    if (playerAbility.isHoldingRightClick() && charge < getTargetValue(player)){

                        if (charge + 1 >= getTargetValue(player)){
                            playerAbility.setChargeAmount(0);

                            performAttack(player, context);
                            makeSound(player);

                        }else{
                            playerAbility.setChargeAmount(charge + 1);
                        }
                    }
                }
        );
    }

    @Override
    public void onPressed(Player player, AttackContext context) {

    }

    @Override
    public void onReleased(Player player, AttackContext context) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setChargeAmount(0)
        );
    }

    @Override
    public int getTargetValue(Player player) {
        return (int) (6 * getShotDelay(player) + 4) / 3;
    }

    @Override
    protected double getBulletSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_SPEED.get());
        return attr != null ? Math.max(attr.getValue(), 0.1) * 0.7 : 0.7;
    }
}
