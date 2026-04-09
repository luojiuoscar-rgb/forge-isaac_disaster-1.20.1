package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class TeleportToIdentifier implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var rls = context.get(ContextKeys.RESOURCE_LOCATIONS);
        if (rls == null || rls.isEmpty()) return false;

        boolean s = false;
        int index = 0;
        while (!s){
            if (index >= rls.size()) return false;

            s = PlayerHelper.teleportToNearestIdentifier(
                    player, rls.get(index));
            index++;
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f ,1.0f);

        return true;
    }
}
