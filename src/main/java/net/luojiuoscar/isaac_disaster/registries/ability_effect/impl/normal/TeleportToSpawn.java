package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class TeleportToSpawn implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (context.getEntity() instanceof ServerPlayer player){
            PlayerHelper.teleportPlayerToSpawn(player);

            // 在传送后获取位置并且播放声音
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f ,1.0f);
            return true;
        }
        return false;
    }
}
