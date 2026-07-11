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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Base entity for non-persistent Isaac familiars owned by a server player.
 */
public abstract class AbstractIsaacFamiliarEntity extends Entity {
    private static final double MAX_VALID_DISTANCE = 64.0;
    private static final int MIN_VALIDITY_CHECK_INTERVAL = 20;
    private static final int MAX_VALIDITY_CHECK_INTERVAL = 40;
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(AbstractIsaacFamiliarEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected UUID ownerUUID;
    protected Player cachedOwner;
    private int formationIndex;
    private int validityCheckDelay;

    protected AbstractIsaacFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        resetValidityCheckDelay();
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);

        if (!level().isClientSide && --validityCheckDelay <= 0) {
            if (!isValidFamiliar()) {
                discardAndUnlink();
                return;
            }
            resetValidityCheckDelay();
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
        Player owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (!(owner instanceof ServerPlayer) || !owner.isAlive() || owner.isRemoved() || familiarType == null) {
            return false;
        }
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
        Player owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (owner instanceof ServerPlayer serverPlayer && familiarType != null) {
            serverPlayer.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                    data -> data.removeEntity(familiarType, getUUID()));
        }
        discardSilently();
    }

    public void setOwner(@Nullable ServerPlayer owner) {
        this.cachedOwner = owner;
        this.ownerUUID = owner == null ? null : owner.getUUID();
        entityData.set(OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    /**
     * Sets the runtime formation slot assigned by the server.
     */
    public void setFormationIndex(int formationIndex) {
        this.formationIndex = Math.max(0, formationIndex);
    }

    /**
     * Returns the runtime formation slot used by server-side familiar behavior.
     */
    public int getFormationIndex() {
        return formationIndex;
    }

    @Nullable
    public Player getOwner() {
        if (ownerUUID == null) {
            ownerUUID = entityData.get(OWNER_UUID).orElse(null);
        }

        if (cachedOwner != null && (cachedOwner.isRemoved() ||
                (ownerUUID != null && !ownerUUID.equals(cachedOwner.getUUID())))) {
            cachedOwner = null;
        }

        if (cachedOwner == null && ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof ServerPlayer serverPlayer) {
                cachedOwner = serverPlayer;
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
        Player owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (owner == null || familiarType == null) return -1;

        final int[] index = {-1};
        owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> index[0] = data.getIndex(familiarType, getUUID()));
        return index[0];
    }

    private void resetValidityCheckDelay() {
        validityCheckDelay = MIN_VALIDITY_CHECK_INTERVAL
                + random.nextInt(MAX_VALIDITY_CHECK_INTERVAL - MIN_VALIDITY_CHECK_INTERVAL + 1);
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
