package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BrimstoneAttack extends LaserAttack implements IChargeableAttack {
    private static final float DAMAGE_PERCENTAGE = 0.6f;
    private static final String SCHEDULE_NAME = "brimstone_shoot";

    public BrimstoneAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackType.BRIMSTONE.getId();
    }

    // ================== handleAttack ==================
    @Override
    public void handleShoot(LivingEntity entity, AttackContext context, Vec3 offset, float xRot, float yRot) {
        ScheduledFuncHelper.schedule(SCHEDULE_NAME, 1, 13, false,
                () -> super.handleShoot(entity, context, offset, xRot, yRot));
    }

    @Override
    protected double getWidth(LivingEntity living) {
        return getBulletScale(living);
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
    protected LivingEntity makeDamage(LivingEntity source, LivingEntity target, float damage) {
        target.invulnerableTime = 0;
        target.hurt(getDamageSource(source), damage * DAMAGE_PERCENTAGE);
        return target;
    }

    @Override
    protected boolean isSpectral(LivingEntity entity){
        return true;
    }

    @Override
    protected boolean isPiercing(LivingEntity entity){
        return true;
    }

    @Override
    protected boolean isControllable(LivingEntity entity){
        return false;
    }

    // =================== Chargeable ===================
    @Override
    public void onTick(Player player, AttackContext context) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.isHoldingRightClick() &&
                            playerAbility.getChargeAmount() < getTargetValue(player)){
                        playerAbility.setChargeAmount(playerAbility.getChargeAmount() + 1);
                    }
                }
        );
    }

    @Override
    public void onPressed(Player player, AttackContext context) {
        ScheduledFuncHelper.clear(SCHEDULE_NAME);
    }

    @Override
    public void onReleased(Player player, AttackContext context) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.getChargeAmount() >= getTargetValue(player)){
                        // attack
                        performAttack(player, context);
                        makeSound(player);
                    }
                    playerAbility.setChargeAmount(0);
                }
        );
    }

    @Override
    public int getTargetValue(Player player) {
        return (int) getShotDelay(player) * 3;
    }

    @Override
    protected double getTears(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS.get());
        if (instance == null) return StatManager.TEARS.getBonus() * -2;

        return  Math.max(instance.getValue() + (StatManager.TEARS.getBonus() * -2),-7);
    }
}
