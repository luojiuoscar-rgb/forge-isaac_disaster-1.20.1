package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PlayerMomsKnifeEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RANGE =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> LAUNCHED_AT =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.LONG);

    @Nullable private Frame previous;

    public PlayerMomsKnifeEntity(EntityType<? extends PlayerMomsKnifeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void initialize(ServerPlayer owner) {
        entityData.set(OWNER, owner.getId());
        setPos(PlayerMomsKnifeMotion.idlePosition(owner, 1));
        setOldPosAndRot();
    }

    @Nullable
    public Player getOwner() {
        return level().getEntity(entityData.get(OWNER)) instanceof Player player ? player : null;
    }

    public boolean isInFlight() {
        return entityData.get(RANGE) > 0;
    }

    public void launch(double range, double speed) {
        if (isInFlight() || !Double.isFinite(range) || !Double.isFinite(speed) || range <= 0 || speed <= 0) return;
        entityData.set(RANGE, (float) Math.min(8, range));
        entityData.set(SPEED, (float) speed);
        entityData.set(LAUNCHED_AT, level().getGameTime());
        Player owner = getOwner();
        previous = owner == null ? null : frame(owner);
    }

    public double getFlightDistance(float partialTick) {
        return PlayerMomsKnifeMotion.distance(entityData.get(RANGE), entityData.get(SPEED),
                level().getGameTime() + partialTick - entityData.get(LAUNCHED_AT));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER, -1);
        entityData.define(RANGE, 0.0F);
        entityData.define(SPEED, 1.0F);
        entityData.define(LAUNCHED_AT, 0L);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        Player candidate = getOwner();
        if (!(candidate instanceof ServerPlayer owner) || !MomsKnifeAttack.isSelectedAndReady(owner)
                || owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> !ability.getAttackTypes().containsKey(ModAttackTypes.MOMS_KNIFE.getId())
                        || !getUUID().equals(ability.getRuntimeEntities().getId(ModAttackTypes.MOMS_KNIFE.getId())))
                .orElse(true)) {
            discard();
            return;
        }

        Frame current = frame(owner);
        Frame from = previous;
        // A teleport or a chunk reload must not turn the previous blade into a cross-world damage trail.
        if (from == null || current.time() - from.time() > 1
                || current.idle().distanceToSqr(from.idle()) > 32 * 32) from = current;
        List<PlayerMomsKnifeMotion.Blade> path = sweep(from, current);
        setPos(path.get(path.size() - 1).anchor());
        previous = current;
        damage(owner, path);

        if (isInFlight() && current.time() - entityData.get(LAUNCHED_AT)
                >= PlayerMomsKnifeMotion.duration(entityData.get(RANGE), entityData.get(SPEED))) {
            entityData.set(RANGE, 0.0F);
        }
    }

    private Frame frame(Player owner) {
        return new Frame(PlayerMomsKnifeMotion.idlePosition(owner, 1), owner.getYRot(), owner.getXRot(),
                level().getGameTime());
    }

    private List<PlayerMomsKnifeMotion.Blade> sweep(Frame from, Frame to) {
        double turn = Math.max(Math.abs(Mth.wrapDegrees(to.yaw() - from.yaw())),
                Math.abs(to.pitch() - from.pitch()));
        int steps = Math.max(1, (int) Math.ceil(turn / 5));
        List<Double> fractions = new ArrayList<>();
        for (int i = 0; i <= steps; i++) fractions.add((double) i / steps);
        double duration = PlayerMomsKnifeMotion.duration(entityData.get(RANGE), entityData.get(SPEED));
        // Include the exact apex even when a short throw completes entirely between two server ticks.
        double apex = entityData.get(LAUNCHED_AT) + duration / 2;
        if (isInFlight() && to.time() > from.time() && apex > from.time() && apex < to.time()) {
            fractions.add((apex - from.time()) / (to.time() - from.time()));
            fractions.sort(Double::compare);
        }
        List<PlayerMomsKnifeMotion.Blade> path = new ArrayList<>(fractions.size());
        for (double fraction : fractions) {
            double time = Mth.lerp(fraction, from.time(), to.time());
            double distance = PlayerMomsKnifeMotion.distance(entityData.get(RANGE), entityData.get(SPEED),
                    time - entityData.get(LAUNCHED_AT));
            path.add(PlayerMomsKnifeMotion.blade(from.idle().lerp(to.idle(), fraction),
                    Mth.rotLerp((float) fraction, from.yaw(), to.yaw()),
                    Mth.lerp((float) fraction, from.pitch(), to.pitch()), distance));
        }
        return path;
    }

    private void damage(ServerPlayer owner, List<PlayerMomsKnifeMotion.Blade> path) {
        AABB bounds = path.get(0).bounds();
        for (PlayerMomsKnifeMotion.Blade blade : path) bounds = bounds.minmax(blade.bounds());
        float damage = (float) (2 * owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (!Float.isFinite(damage) || damage <= 0) return;
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, bounds,
                target -> target.isAlive() && !target.isRemoved() && !target.isSpectator()
                        && !EntityHelper.isFriendly(target, owner))) {
            for (int i = 1; i < path.size(); i++) {
                if (PlayerMomsKnifeMotion.contacts(path.get(i - 1), path.get(i), target.getBoundingBox())) {
                    target.hurt(ModDamageSources.momsKnife(level(), this, owner), damage);
                    break;
                }
            }
        }
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        Player owner = getOwner();
        if (!level().isClientSide && owner != null) {
            owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability ->
                    ability.getRuntimeEntities().unregister(ModAttackTypes.MOMS_KNIFE.getId(), getUUID()));
        }
        super.remove(reason);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public boolean isAttackable() { return false; }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        Player owner = getOwner();
        if (owner == null) return getBoundingBox().inflate(1);
        double reach = Math.max(0.6, owner.getBbWidth()) + entityData.get(RANGE)
                + MomKnifePose.BLADE_TIP_DISTANCE + MomKnifePose.BLADE_RADIUS;
        AABB ownerBounds = owner.getBoundingBox();
        AABB movingOwner = ownerBounds.minmax(ownerBounds.move(owner.getPosition(0).subtract(owner.position())));
        return getBoundingBox().minmax(movingOwner.inflate(reach));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private record Frame(Vec3 idle, float yaw, float pitch, long time) {}
}
