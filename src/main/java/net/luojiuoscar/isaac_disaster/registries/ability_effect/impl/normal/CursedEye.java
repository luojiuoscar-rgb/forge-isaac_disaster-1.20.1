package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.server.level.ServerPlayer;

public class CursedEye implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    var attack = playerAbility.getAttackSelection().mainAttack();
                    if (attack != ModAttackTypes.CURSED_EYE.get()
                            || !(attack instanceof IChargeableAttack charge)) return;
                    var barId = charge.getChargeBarId();
                    if (playerAbility.getChargeAmount(barId) == 0) return;

                    playerAbility.setChargeAmount(barId, 0);
                    EntityHelper.teleportToRandomLocation(player, StatManager.getNearbyRange());
                }
        );

        return true;
    }
}
