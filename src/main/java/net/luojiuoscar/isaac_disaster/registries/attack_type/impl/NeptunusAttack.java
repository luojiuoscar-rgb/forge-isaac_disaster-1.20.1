package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.Arrays;
import java.util.List;

public class NeptunusAttack extends AttackType implements IChargeableAttack {
    public NeptunusAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackType.NEPTUNUS.getId();
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        return List.of();
    }

    @Override
    public void performAttack(List<AttackContext> ctxList) {

    }

    @Override
    public void makeSound(LivingEntity entity) {

    }

    @Override
    public void shoot(AttackContext ctx) {

    }

    private static final double[] COEFFS =
            {1.0,0.9,0.8,0.7,0.6,0.55,0.5,0.45,0.4,0.35,0.3,0.25,0.25,0.25,0.25};

    @Override
    public void onTick(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    int totalCharge = getTotalCharge(player);
                    int chargeAmount = playerAbility.getChargeAmount();

                    // 按下右键，有充能，不在冷却->发射子弹
                    if (playerAbility.isHoldingRightClick()
                            && chargeAmount > 2
                            && !player.getCooldowns().isOnCooldown(ModItems.ISAAC_HEAD.get())){

                        AttackType attack = pickLowerAttackType(playerAbility.getAttackTypes(), 0);

                        BeforePerformAttackEvent event = new BeforePerformAttackEvent(player, this);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.isCanceled()) return;

                        // attack
                        attack.performAttack(attack.getAttackContextsWithEvent(player, attack.getBulletCount(player)));
                        attack.makeSound(player);

                        int coolDownTick = getCoolDownTicks(getShotDelay(player), chargeAmount);
                        player.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(),coolDownTick);
                        playerAbility.setChargeAmount(chargeAmount - coolDownTick);
                    }
                    // 没有按下右键，充能未满->充能
                    else if (chargeAmount < totalCharge
                            && !playerAbility.isHoldingRightClick()){
                        // auto charge
                        playerAbility.setChargeAmount(chargeAmount + 1);

                    }else if (chargeAmount > totalCharge){
                        playerAbility.setChargeAmount(totalCharge);
                    }
                }
        );
    }

    private int getCoolDownTicks(double shotDelay, int chargeAmount){
        double accumulated = 0;
        for (int i = 0; i < COEFFS.length; i++) {
            accumulated += shotDelay * COEFFS[i];
            if (chargeAmount <= accumulated) {
                return (int)(shotDelay * COEFFS[i]);
            }
        }
        // charge 超过总量，返回最后一个子弹的 shotDelay
        return (int)(shotDelay * COEFFS[COEFFS.length - 1]);
    }

    @Override
    public void onPressed(ServerPlayer player) {

    }

    @Override
    public void onReleased(ServerPlayer player) {

    }

    @Override
    public int getTotalCharge(Player player) {
        return (int) (getShotDelay(player) * Arrays.stream(COEFFS).sum());
    }

}
