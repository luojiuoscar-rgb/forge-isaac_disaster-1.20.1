package net.luojiuoscar.isaac_disaster.entity.familiar.base;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
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
import net.minecraft.world.phys.Vec3;
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
    protected static final double MAX_VALID_DISTANCE = 16.0;
    private static final int MIN_VALIDITY_CHECK_INTERVAL = 20;
    private static final int MAX_VALIDITY_CHECK_INTERVAL = 40;
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(AbstractIsaacFamiliarEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> VISUAL_SCALE =
            SynchedEntityData.defineId(AbstractIsaacFamiliarEntity.class, EntityDataSerializers.FLOAT);

    protected UUID ownerUUID;
    protected Player cachedOwner;
    private int formationIndex;
    private int validityCheckDelay;
    private boolean recallPending;

    protected AbstractIsaacFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        resetValidityCheckDelay();
    }

    @Override
    public void tick() {
        super.tick();
        configurePhysics();

        if (!level().isClientSide) {
            Player owner = getOwner();
            updateVisualScale(owner instanceof ServerPlayer serverPlayer ? serverPlayer : null);
            if (owner instanceof ServerPlayer && owner.isAlive() && !owner.isRemoved()
                    && owner.level() != level()) {
                // Cross-dimension and unavailable entities retain the replacement fallback.
                discardAndUnlink();
                return;
            }
            if (--validityCheckDelay <= 0) {
                if (!isValidFamiliar()) {
                    discardAndUnlink();
                    return;
                }
                resetValidityCheckDelay();
            }
            if (owner instanceof ServerPlayer && owner.isAlive() && !owner.isRemoved()
                    && exceedsRecallDistance(owner)) {
                requestRecall();
                return;
            }
            if (recallPending) {
                setDeltaMovement(Vec3.ZERO);
                return;
            }
        }

        tickFamiliar();
    }

    /** Large formations can account for their legitimate extent without changing other familiars. */
    protected boolean exceedsRecallDistance(Player owner) {
        return distanceToSqr(owner) > MAX_VALID_DISTANCE * MAX_VALID_DISTANCE;
    }

    /**
     * Runs the concrete familiar behavior after base ownership checks pass.
     */
    protected abstract void tickFamiliar();

    /** Airborne familiars retain their defaults; grounded variants opt into world physics. */
    protected void configurePhysics() {
        noPhysics = true;
        setNoGravity(true);
    }

    protected Vec3 ownerPlacement(Player owner) {
        return owner.position().add(0, owner.getBbHeight() * 0.55, 0);
    }
    /** Shared by first admission and recall so ground variants use the same placement rules. */
    public final void placeAtOwner() {
        Player owner = getOwner();
        if (owner == null) return;
        setPos(ownerPlacement(owner));
        setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Checks whether this familiar is still accepted by its owner capability.
     */
    public boolean isValidFamiliar() {
        Player owner = getOwner();
        ResourceLocation familiarType = getFamiliarType();
        if (isRemoved() || !(owner instanceof ServerPlayer) || !owner.isAlive() || owner.isRemoved()
                || owner.level() != level() || familiarType == null) {
            return false;
        }
        final boolean[] valid = {false};
        owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> valid[0] = data.containsEntity(familiarType, getUUID()));
        return valid[0];
    }

    public final boolean isRecallPending() {
        return recallPending;
    }

    /** Queues an existing entity without freeing its roster slot or persistent requirement. */
    private void requestRecall() {
        if (recallPending) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (!isValidFamiliar()) {
            discardAndUnlink();
            return;
        }
        recallPending = true;
        setDeltaMovement(Vec3.ZERO);
        getOwner().getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .ifPresent(data -> data.queueRecall(getUUID()));
    }

    /** Called only by the owner's shared admission scheduler. */
    public final boolean recallToOwner() {
        if (!recallPending || !isValidFamiliar()) return false;
        Vec3 position = ownerPlacement(getOwner());
        teleportTo(position.x, position.y, position.z);
        setDeltaMovement(Vec3.ZERO);
        recallPending = false;
        resetValidityCheckDelay();
        onRecalled();
        return true;
    }

    /** Subclasses clear attack/navigation state after an actual teleport, before behavior resumes. */
    protected void onRecalled() { }

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
        if (!level().isClientSide) updateVisualScale(owner);
    }

    public final float getVisualScale() {
        return entityData.get(VISUAL_SCALE);
    }

    /** Starts with 0 */
    public final double getEnhancement() {
        if (!(getOwner() instanceof ServerPlayer owner)) return 0;
        return owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getFamiliarModifiers().enhancement(this,
                        GameplayConfig.ALLOW_SAME_FAMILIAR_MODIFIER_ENTRY_STACKING.get())).orElse(0.0);
    }

    private void updateVisualScale(@Nullable ServerPlayer owner) {
        float scale = owner == null ? 1 : owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getFamiliarModifiers().visualScale(this,
                        GameplayConfig.ALLOW_SAME_FAMILIAR_MODIFIER_ENTRY_STACKING.get())).orElse(1.0F);
        entityData.set(VISUAL_SCALE, scale);
    }

    /** Assigns a slot using this familiar's formation rule instead of caller-side type checks. */
    public final void refreshFormationIndex(ServerPlayer owner, int typeLocalIndex) {
        formationIndex = Math.max(0, resolveFormationIndex(owner, Math.max(0, typeLocalIndex)));
    }

    protected int resolveFormationIndex(ServerPlayer owner, int typeLocalIndex) {
        return typeLocalIndex;
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

    private void resetValidityCheckDelay() {
        validityCheckDelay = MIN_VALIDITY_CHECK_INTERVAL
                + random.nextInt(MAX_VALIDITY_CHECK_INTERVAL - MIN_VALIDITY_CHECK_INTERVAL + 1);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER_UUID, Optional.empty());
        entityData.define(VISUAL_SCALE, 1.0F);
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
