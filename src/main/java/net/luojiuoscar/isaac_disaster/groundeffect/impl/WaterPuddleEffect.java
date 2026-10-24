package net.luojiuoscar.isaac_disaster.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** The first concrete ground effect: a short-lived, damaging water puddle. */
public final class WaterPuddleEffect extends AbstractGroundEffect {
    public static final double DEFAULT_RADIUS = 0.5D;
    public static final int DEFAULT_LIFETIME = 100;

    public WaterPuddleEffect(GroundEffectType<WaterPuddleEffect> type, int slot, int generation,
                               @Nullable UUID ownerUuid, Vec3 position, double radius, int lifetime, float damage, long creationOrder, int animationDurationTicks) {
        super(type, slot, generation, ownerUuid, position, radius, lifetime, damage, creationOrder, animationDurationTicks);
    }

    @Override
    public int getColor() {
        return 0x3C9BE8;
    }

    @Override
    public void onContactDamage(GroundEffectContact contact) {
        LivingEntity target = contact.target();
        if (!target.isAlive() || target.isInvulnerable()) return;
        if (damage() <= 0.0F) return;
        target.hurt(ModDamageSources.water(contact.level(), contact.owner()), damage());
    }
}
