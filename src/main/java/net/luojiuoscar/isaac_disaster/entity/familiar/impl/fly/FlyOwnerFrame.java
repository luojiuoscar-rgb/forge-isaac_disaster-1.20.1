package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.*;

/** One owner snapshot per tick; target qualification and the attack queue are shared. */
final class FlyOwnerFrame {
    private static final Map<ServerLevel, Map<UUID, WeakReference<FlyOwnerFrame>>> FRAMES = new WeakHashMap<>();
    private final List<AbstractFlyFamiliarEntity> flies = new ArrayList<>();
    private final Map<UUID, Integer> slots = new HashMap<>();
    private final Map<LivingEntity, Boolean> targetValidity = new IdentityHashMap<>();
    private final FlyAttackQueue attack = new FlyAttackQueue(this::validForTick);
    private List<LivingEntity> candidates = List.of();
    private long frameTick = Long.MIN_VALUE;
    private long scanTick = Long.MIN_VALUE;
    private double phase;
    private double radius;
    private Vec3 center = Vec3.ZERO;
    private int scanCount;

    static FlyOwnerFrame get(ServerPlayer owner) {
        Map<UUID, WeakReference<FlyOwnerFrame>> frames = FRAMES.computeIfAbsent(owner.serverLevel(), ignored -> new HashMap<>());
        WeakReference<FlyOwnerFrame> reference = frames.get(owner.getUUID());
        FlyOwnerFrame frame = reference == null ? null : reference.get();
        if (frame == null) {
            frames.entrySet().removeIf(entry -> entry.getValue().get() == null);
            frame = new FlyOwnerFrame();
            frames.put(owner.getUUID(), new WeakReference<>(frame));
        }
        return frame;
    }

    void refresh(ServerPlayer owner, long tick) {
        if (frameTick == tick) return;
        boolean initial = frameTick == Long.MIN_VALUE;
        frameTick = tick;
        targetValidity.clear();
        flies.clear();
        slots.clear();
        owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
            for (UUID id : data.getEntityIdsInAdmissionOrder()) {
                if (owner.serverLevel().getEntity(id) instanceof AbstractFlyFamiliarEntity fly
                        && !fly.isRemoved() && fly.getOwner() == owner) {
                    slots.put(id, flies.size());
                    flies.add(fly);
                }
            }
        });
        center = owner.getBoundingBox().getCenter();
        radius = FlyOrbitLayout.radius(owner.getBbWidth(), flies.size());
        if (!initial) phase = FlyOrbitLayout.advance(phase, radius);
        if (scanTick == Long.MIN_VALUE || tick - scanTick >= 4) {
            scanTick = tick;
            scanCount++;
            candidates = owner.serverLevel().getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(8), candidate -> validForTick(owner, candidate));
        }
        attack.refresh(owner, flies, slots::containsKey, tick);
    }

    static boolean validTarget(ServerPlayer owner, LivingEntity target) {
        return target.level() == owner.level() && !target.isRemoved()
                && target.getBoundingBox().getCenter().distanceToSqr(owner.getBoundingBox().getCenter()) <= 64
                && TrackingTargetRules.eligibleFlyOrSpider(owner.serverLevel(), owner, target);
    }

    /** Decisions share expensive qualification; damage attempts still perform a fresh check. */
    boolean validForTick(ServerPlayer owner, LivingEntity target) {
        return target.isAlive() && !target.isRemoved() && target.level() == owner.level()
                && targetValidity.computeIfAbsent(target, candidate -> validTarget(owner, candidate));
    }

    boolean contains(AbstractFlyFamiliarEntity fly) { return slots.containsKey(fly.getUUID()); }
    List<LivingEntity> candidates() { return candidates; }
    int scans() { return scanCount; }
    FlyAttackQueue attack() { return attack; }

    LivingEntity attackTarget(ServerPlayer owner, AbstractFlyFamiliarEntity fly, long tick) {
        return attack.target(owner, fly, flies, candidates, tick);
    }

    Vec3 position(AbstractFlyFamiliarEntity fly) {
        return FlyOrbitLayout.position(center, radius, fly.getBbHeight(),
                slots.getOrDefault(fly.getUUID(), 0), flies.size(), phase);
    }

}
