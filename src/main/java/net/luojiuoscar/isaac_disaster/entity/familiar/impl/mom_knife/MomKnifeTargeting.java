package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** One rear-area scan per owner every five ticks, with independent knife target choices. */
final class MomKnifeTargeting {
    static final int SCAN_INTERVAL = 5;
    private static final Map<ServerLevel, Map<UUID, WeakReference<MomKnifeTargeting>>> POOLS = new WeakHashMap<>();
    private final List<LivingEntity> candidates = new ArrayList<>();
    private final List<LivingEntity> available = new ArrayList<>();
    private long scanTick = Long.MIN_VALUE;
    private long validationTick = Long.MIN_VALUE;
    private int scannedLayers = -1;

    static MomKnifeTargeting get(ServerPlayer owner) {
        Map<UUID, WeakReference<MomKnifeTargeting>> pools = POOLS.computeIfAbsent(owner.serverLevel(), ignored -> new HashMap<>());
        WeakReference<MomKnifeTargeting> reference = pools.get(owner.getUUID());
        MomKnifeTargeting pool = reference == null ? null : reference.get();
        if (pool == null) {
            pools.entrySet().removeIf(entry -> entry.getValue().get() == null);
            pool = new MomKnifeTargeting();
            pools.put(owner.getUUID(), new WeakReference<>(pool));
        }
        return pool;
    }

    @Nullable
    LivingEntity select(ServerPlayer owner, Vec3 knifePosition, int formationIndex,
                        RandomSource random, int layerCount) {
        double radius = scanRadius(Math.max(1, layerCount));
        refresh(owner, layerCount);
        while (!available.isEmpty()) {
            LivingEntity selected;
            if (formationIndex == 0) {
                selected = available.get(0);
                double nearest = selected.getBoundingBox().getCenter().distanceToSqr(knifePosition);
                for (int index = 1; index < available.size(); index++) {
                    LivingEntity candidate = available.get(index);
                    double distance = candidate.getBoundingBox().getCenter().distanceToSqr(knifePosition);
                    if (distance < nearest) {
                        selected = candidate;
                        nearest = distance;
                    }
                }
            } else {
                selected = available.get(random.nextInt(available.size()));
            }
            // Callbacks can invalidate a candidate after this tick's shared validation.
            if (eligible(owner, selected, owner.getBoundingBox().getCenter(),
                    Vec3.directionFromRotation(0, owner.getYRot()), radius)) return selected;
            available.remove(selected);
        }
        return null;
    }

    private void refresh(ServerPlayer owner, int layerCount) {
        long now = owner.level().getGameTime();
        int layers = Math.max(1, layerCount);
        double radius = scanRadius(layers);
        boolean rescan = scanTick == Long.MIN_VALUE || now < scanTick
                || now - scanTick >= SCAN_INTERVAL || scannedLayers != layers;
        if (!rescan && validationTick == now) return;
        Vec3 center = owner.getBoundingBox().getCenter();
        Vec3 forward = Vec3.directionFromRotation(0, owner.getYRot());
        if (rescan) {
            scanTick = now;
            scannedLayers = layers;
            candidates.clear();
            candidates.addAll(owner.serverLevel().getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(radius), candidate -> eligible(owner, candidate, center, forward, radius)));
        }
        available.clear();
        for (LivingEntity candidate : candidates) {
            if (rescan || eligible(owner, candidate, center, forward, radius)) available.add(candidate);
        }
        validationTick = now;
    }

    static boolean validTarget(LivingEntity owner, @Nullable LivingEntity candidate) {
        return candidate != null && candidate != owner && candidate.level() == owner.level()
                && candidate.isAlive() && !candidate.isRemoved() && !candidate.isInvulnerable()
                && !EntityHelper.isFriendly(candidate, owner);
    }

    private static boolean eligible(ServerPlayer owner, LivingEntity candidate, Vec3 center,
                                    Vec3 forward, double radius) {
        return validTarget(owner, candidate)
                && inRearRange(center, forward, candidate.getBoundingBox().getCenter(), radius);
    }

    static boolean inRearRange(Vec3 ownerCenter, float ownerYaw, Vec3 targetCenter, double radius) {
        return inRearRange(ownerCenter, Vec3.directionFromRotation(0, ownerYaw), targetCenter, radius);
    }

    private static boolean inRearRange(Vec3 center, Vec3 forward, Vec3 targetCenter, double radius) {
        Vec3 offset = targetCenter.subtract(center);
        if (offset.lengthSqr() > radius * radius) return false;
        double horizontal = offset.horizontalDistance();
        return horizontal > 1e-6 && forward.x * offset.x + forward.z * offset.z < -0.25 * horizontal;
    }

    static double scanRadius(int layerCount) {
        return 5.0 + 0.5 * Math.max(1, layerCount);
    }
}
