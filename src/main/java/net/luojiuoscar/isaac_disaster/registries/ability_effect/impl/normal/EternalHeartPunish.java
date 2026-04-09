package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EternalHeartPunish implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;

        if (!(event.getEntity() instanceof Player player) || player.getAbsorptionAmount() != 0) return true;

        player.removeEffect(ModEffects.ETERNAL_HEART.get());
        event.setAmount(0.0f);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        return true;
    }
}
