package net.luojiuoscar.isaac_disaster.system.explosion;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeExecutableEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** A real entity that captures bomb item effects on admission and carries them into an explosion. */
public interface BombEffectSource {
    Entity getBombEffectEntity();

    @Nullable
    LivingEntity getOwner();

    int getPower();

    float getScale();

    boolean isOriginal();

    CompositeExecutableEffect getCachedEffect();

    /** Returns true only for the first initialization attempt on this entity instance. */
    boolean markEffectsInitialized();
}
