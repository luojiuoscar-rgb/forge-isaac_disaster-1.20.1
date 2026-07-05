package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.DelegatingAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class CursedEyeAttack extends AttackType implements IChargeableAttack, DelegatingAttackType {
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "cursed_eye_attack");

    public CursedEyeAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public CursedEyeAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackType.CURSED_EYE.getId();
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

    @Override
    public void onTick(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.isHoldingRightClick() &&
                            playerAbility.getChargeAmount() < getTotalCharge(player)){

                        playerAbility.setChargeAmount(playerAbility.getChargeAmount() + 1);
                    }
                }
        );
    }

    @Override
    public void onPressed(ServerPlayer player) {

    }

    @Override
    public void onReleased(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.getChargeAmount() >= getShotDelay(player)
                            && PlayerHelper.isHoldingIsaacHead(player)){

                        AttackType attack = pickLowerAttackType(player, playerAbility.getAttackTypes(), 0);

                        BeforePerformAttackEvent event = new BeforePerformAttackEvent(player, this);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.isCanceled()) return;

                        double shotDelay = getShotDelay(player);

                        int count;
                        if (shotDelay < 1){
                            count = 6;
                        }else {
                            count = (int) (playerAbility.getChargeAmount() / getShotDelay(player));
                        }

                        // attack
                        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(), SCHEDULE_TYPE,
                                1,1, count, false, () -> {

                            attack.performAttack(attack.getAttackContextsWithEvent(player, attack.getBulletCount(player)));
                            attack.makeSound(player);
                        });
                    }
                    playerAbility.setChargeAmount(0);
                }
        );
    }

    @Override
    public int getTotalCharge(Player player) {
        return (int) getShotDelay(player) * 5;
    }
}
