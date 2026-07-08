package net.luojiuoscar.isaac_disaster.entity.familiar;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Base entity for Isaac familiars that are owned by a living entity but do not behave as mobs.
 */
public abstract class AbstractIsaacFamiliarEntity extends Entity {
    private static final double MAX_VALID_DISTANCE = 64.0;
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(AbstractIsaacFamiliarEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected UUID ownerUUID;
    protected LivingEntity cachedOwner;

    protected AbstractIsaacFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);

        if (!level().isClientSide && !isValidFamiliar()) {
            discardAndUnlink();
            return;
        }

        tickFamiliar();
    }

    /**
     * Runs the concrete familiar behavior after base ownership checks pass.
     */
    protected abstract void tickFamiliar();

    /**
     * Checks whether this familiar is still accepted by its owner capability.
     */
    public boolean isValidFamiliar() {
        LivingEntity owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (owner == null || !owner.isAlive() || owner.isRemoved() || familiarType == null) return false;
        if (distanceToSqr(owner) > MAX_VALID_DISTANCE * MAX_VALID_DISTANCE) return false;

        final boolean[] valid = {false};
        owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> valid[0] = data.containsEntity(familiarType, getUUID()));
        return valid[0];
    }

    /**
     * Discards the entity without editing the owner capability.
     */
    public void discardSilently() {
        super.discard();
    }

    /**
     * Removes this entity UUID from the owner capability before discarding the entity.
     */
    public void discardAndUnlink() {
        LivingEntity owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (owner != null && familiarType != null) {
            owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                    data -> data.removeEntity(familiarType, getUUID()));
        }
        discardSilently();
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.cachedOwner = owner;
        this.ownerUUID = owner == null ? null : owner.getUUID();
        entityData.set(OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUUID == null) {
            ownerUUID = entityData.get(OWNER_UUID).orElse(null);
        }

        if (cachedOwner != null && (cachedOwner.isRemoved() ||
                (ownerUUID != null && !ownerUUID.equals(cachedOwner.getUUID())))) {
            cachedOwner = null;
        }

        if (cachedOwner == null && ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity livingEntity) {
                cachedOwner = livingEntity;
            }
        }

        if (cachedOwner == null && ownerUUID != null && level().isClientSide) {
            Player player = level().getPlayerByUUID(ownerUUID);
            if (player != null) {
                cachedOwner = player;
            }
        }

        return cachedOwner;
    }

    @Nullable
    public ResourceLocation getFamiliarType() {
        return ForgeRegistries.ENTITY_TYPES.getKey(getType());
    }

    /**
     * Returns this familiar's current order in the owner capability for server-side positioning logic.
     */
    public int getFamiliarIndex() {
        LivingEntity owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (owner == null || familiarType == null) return -1;

        final int[] index = {-1};
        owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> index[0] = data.getIndex(familiarType, getUUID()));
        return index[0];
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
