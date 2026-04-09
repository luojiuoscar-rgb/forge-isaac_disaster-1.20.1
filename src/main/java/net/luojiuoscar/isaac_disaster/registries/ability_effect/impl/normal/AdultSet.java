package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AdultSet implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        entity.level().playSound(null, BlockPos.containing(pos),
                ModSounds.STEVE_HURT_OLD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }
}
