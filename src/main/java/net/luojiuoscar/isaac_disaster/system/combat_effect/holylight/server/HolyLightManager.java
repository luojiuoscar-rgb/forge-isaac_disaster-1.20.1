package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server;

import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightGeometry;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightTimeline;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** One world's transient strikes; game time and the fire latch remain server authoritative. */
public final class HolyLightManager {
    public static final double MAX_RADIUS = 16;

    private final Map<Long, Strike> strikes = new LinkedHashMap<>();
    private long nextId = 1;
    private ServerLevel ownerLevel;

    public boolean spawn(ServerLevel level, LivingEntity caster, Vec3 candidate, float damage,
                         double radius, boolean skipWarning) {
        return spawn(level, caster, candidate, damage, radius, skipWarning, 1);
    }

    public boolean spawn(ServerLevel level, LivingEntity caster, Vec3 candidate, float damage,
                         double radius, boolean skipWarning, double warningSpeed) {
        if (level == null || caster == null || caster.level() != level || caster.isRemoved()
                || ownerLevel != null && ownerLevel != level || !Float.isFinite(damage)
                || !Double.isFinite(radius) || radius <= 0 || nextId <= 0) return false;
        var placement = HolyLightPlacement.resolve(level, candidate);
        if (placement.isEmpty()) return false;
        ownerLevel = level;
        var resolved = placement.get();
        long id = nextId++;
        HolyLightSnapshot snapshot = new HolyLightSnapshot(id, resolved.position(), Math.min(radius, MAX_RADIUS),
                resolved.beamHeight(), level.getGameTime(), HolyLightTimeline.warningTicks(warningSpeed, skipWarning),
                resolved.grounded(), level.random.nextLong());
        Strike strike = new Strike(snapshot, caster.getUUID(), Math.max(0, damage));
        strikes.put(id, strike);
        if (skipWarning) fire(level, strike);
        return true;
    }

    public void tick(ServerLevel level) {
        if (level == null || ownerLevel != level) return;
        long now = level.getGameTime();
        // Damage callbacks may create or clear strikes; iterate the strikes present at tick entry.
        for (Strike strike : new ArrayList<>(strikes.values())) {
            HolyLightSnapshot snapshot = strike.snapshot;
            if (!strikes.containsKey(snapshot.id())) continue;
            long age = Math.max(0, now - snapshot.startTick());
            if (age >= snapshot.duration() || !level.hasChunkAt(BlockPos.containing(snapshot.position()))
                    || snapshot.grounded() && !HolyLightPlacement.hasSupport(level, snapshot.position())) {
                strikes.remove(snapshot.id());
                continue;
            }
            if (!strike.fired && age >= snapshot.warningTicks()) {
                fire(level, strike);
            }
        }
    }

    public List<HolyLightSnapshot> snapshots() {
        return strikes.values().stream().map(strike -> strike.snapshot).toList();
    }

    public void clear() {
        strikes.clear();
    }

    private void fire(ServerLevel level, Strike strike) {
        strike.fired = true;
        HolyLightSnapshot snapshot = strike.snapshot;
        Vec3 position = snapshot.position();
        // The firing event owns audio; a client's first visual snapshot may arrive a tick later.
        level.playSound(null, position.x, position.y, position.z,
                ModSounds.HOLY_LIGHT_SHOT.get(), SoundSource.BLOCKS, 0.85F, 1);
        if (strike.damage <= 0 || !(level.getEntity(strike.caster) instanceof LivingEntity caster)
                || caster.isRemoved()) return;
        Vec3 source = snapshot.position().add(0, 0.1, 0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                HolyLightGeometry.bounds(snapshot.position(), snapshot.radius())
                        .inflate(HolyLightGeometry.BOUNDARY_EPSILON))) {
            if (!HolyLightGeometry.intersects(snapshot.position(), snapshot.radius(), target.getBoundingBox())
                    || !TrackingTargetRules.eligible(level, caster, target)) continue;
            Vec3 contact = HolyLightGeometry.closestPoint(source, target.getBoundingBox());
            if (!clearPath(level, caster, source, contact)) continue;
            target.hurt(ModDamageSources.holyLight(level, caster), strike.damage);
        }
    }

    private static boolean clearPath(ServerLevel level, LivingEntity caster, Vec3 from, Vec3 to) {
        BlockPos min = BlockPos.containing(Math.min(from.x, to.x), Math.min(from.y, to.y), Math.min(from.z, to.z));
        BlockPos max = BlockPos.containing(Math.max(from.x, to.x), Math.max(from.y, to.y), Math.max(from.z, to.z));
        if (!level.hasChunksAt(min, max)) return false;
        return from.distanceToSqr(to) < 1.0E-12 || level.clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getType() == HitResult.Type.MISS;
    }

    private static final class Strike {
        private final HolyLightSnapshot snapshot;
        private final UUID caster;
        private final float damage;
        private boolean fired;

        private Strike(HolyLightSnapshot snapshot, UUID caster, float damage) {
            this.snapshot = snapshot;
            this.caster = caster;
            this.damage = damage;
        }
    }
}
