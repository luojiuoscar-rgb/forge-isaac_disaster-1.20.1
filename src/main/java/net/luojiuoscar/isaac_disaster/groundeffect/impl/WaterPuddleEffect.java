package net.luojiuoscar.isaac_disaster.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** The first concrete ground effect: a short-lived, damaging water puddle. */
public final class WaterPuddleEffect extends AbstractGroundEffect {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "water");
    /** The same immutable factory is registered as ModGroundEffects.WATER. */
    public static final GroundEffectType<WaterPuddleEffect> DEFINITION = new GroundEffectType<>(TYPE, WaterPuddleEffect::new);
    public static final double DEFAULT_RADIUS = 0.5D;
    public static final int DEFAULT_LIFETIME = 100;

    public WaterPuddleEffect(int slot, int generation, @Nullable UUID ownerUuid, Vec3 position,
                             long creationOrder) {
        this(slot, generation, ownerUuid, position, DEFAULT_RADIUS, DEFAULT_LIFETIME,
                creationOrder);
    }

    public WaterPuddleEffect(int slot, int generation, @Nullable UUID ownerUuid, Vec3 position,
                             double radius, int lifetime, long creationOrder) {
        this(DEFINITION, slot, generation, ownerUuid, position, radius, lifetime, creationOrder);
    }

    private WaterPuddleEffect(GroundEffectType<WaterPuddleEffect> type, int slot, int generation,
                               @Nullable UUID ownerUuid, Vec3 position, double radius, int lifetime, long creationOrder) {
        super(type, slot, generation, ownerUuid, position, radius, lifetime, creationOrder);
    }

    @Override
    public int getColor() {
        return 0x3C9BE8;
    }

    @Override
    public void onContactEffect(GroundEffectContact contact) {
        LivingEntity target = contact.target();
        if (!target.isAlive() || target.isInvulnerable()) return;
        float damage = contact.ownerAttackDamage();
        if (damage <= 0.0F) return;
        target.hurt(ModDamageSources.water(contact.level(), contact.owner()), damage);
    }
}
