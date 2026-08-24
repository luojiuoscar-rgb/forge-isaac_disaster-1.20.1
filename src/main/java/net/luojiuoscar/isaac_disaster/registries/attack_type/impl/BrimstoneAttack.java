package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
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

public class BrimstoneAttack extends LaserAttack implements IChargeableAttack {
    private static final float DAMAGE_PERCENTAGE = 0.6f;
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
    @Override
    public void shoot(AttackContext ctx) {
        if (ctx.usesExactSpawnPosition()) {
            shootSingleLaser(ctx, ctx.getAttackSequenceIndex());
            return;
        }

        // 玩家域的schedule
        AtomicInteger sequenceIndex = new AtomicInteger();
        ScheduledFuncHelper.scheduleForPlayer(ctx.getOwner().getUUID(),
                SCHEDULE_TYPE, 1,1, 13, false, () -> {

            ctx.setAttackSequenceIndex(sequenceIndex.incrementAndGet());

            Entity s = ctx.getShooter();
            Vec3 eyePos = s.getEyePosition().add(0, s.getBbHeight() * -0.15, 0);
            ctx.setPos(eyePos);

            if (isControllable(ctx.getOwner())){
                ctx.setXRot(s.getXRot());
                ctx.setYRot(s.getYRot());
            }

            shootSingleLaser(ctx, ctx.getAttackSequenceIndex());
        });
    }

    @Override
    protected double getWidth(LivingEntity living, double damage) {
        return getBulletScale(living, damage);
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
    protected boolean makeDamage(LivingEntity source, LivingEntity target, float damage) {
        target.invulnerableTime = 0;
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
                                AttackPipelineMode.GROUP_AND_BULLET, false));
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
