package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.UUID;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** Immutable attack identity captured when a bullet is created. */
public final class BulletAttackIdentity {
    private final LivingEntity owner;
    private final Object shooter;
    private final UUID ownerUuid;
    private final AttackContext attackContext;
    private final ResourceLocation typeId;
    private final ResourceLocation rootTypeId;

    public BulletAttackIdentity(
        @Nullable LivingEntity owner,
        @Nullable Object shooter,
        @Nullable UUID ownerUuid,
        @Nullable AttackContext attackContext,
        ResourceLocation typeId,
        ResourceLocation rootTypeId) {
        this.owner = owner;
        this.shooter = shooter;
        this.ownerUuid = ownerUuid;
        this.attackContext = attackContext;
        this.typeId = typeId;
        this.rootTypeId = rootTypeId;
    }

    @Nullable
    public LivingEntity owner() {
        return owner;
    }

    @Nullable
    public Object shooter() {
        return shooter;
    }

    @Nullable
    public UUID ownerUuid() {
        return ownerUuid;
    }

    @Nullable
    public AttackContext attackContext() {
        return attackContext;
    }

    public ResourceLocation typeId() {
        return typeId;
    }

    public ResourceLocation rootTypeId() {
        return rootTypeId;
    }
}
