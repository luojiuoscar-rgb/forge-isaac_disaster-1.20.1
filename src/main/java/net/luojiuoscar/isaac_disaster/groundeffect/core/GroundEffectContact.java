package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** A server-side contact used by the independent damage and status-effect callbacks. */
public record GroundEffectContact(
        ServerLevel level,
        LivingEntity target,
        @Nullable LivingEntity owner) { }
