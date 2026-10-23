package net.luojiuoscar.isaac_disaster.groundeffect.server;

import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContactGeometry;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectCoverage;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Server-owned collection of transient, non-entity ground effects. */
public final class GroundEffectManager {
    public static final int MAX_ACTIVE_EFFECTS = 20_000;
    public static final int CONTACT_COOLDOWN_TICKS = 10;
    private static final int SUPPORT_CHECK_INTERVAL = 10;
    private static final double CELL_SIZE = 2.0D;
    private static final double SURFACE_EPSILON = 1.0E-4D;

    private final Map<Long, AbstractGroundEffect> effects = new LinkedHashMap<>();
    private final Map<Cell, Set<Long>> spatialIndex = new HashMap<>();
    private final Map<Integer, Integer> generations = new HashMap<>();
    private final Queue<Integer> freeSlots = new ArrayDeque<>();
    private final Set<Long> pendingDespawns = new HashSet<>();
    private final Map<ContactKey, Long> nextContactTick = new HashMap<>();
    private int nextSlot;
    private long nextCreationOrder;
    private long gameTick;

    public long gameTick() {
        return gameTick;
    }

    public int activeCount() {
        return effects.size();
    }

    @Nullable
    public WaterPuddleEffect spawnWater(@Nullable ServerPlayer owner, Vec3 position) {
        return spawnWater(owner == null ? null : owner.getUUID(), position,
                WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME);
    }

    @Nullable
    public WaterPuddleEffect spawnWater(@Nullable UUID ownerUuid, Vec3 position,
                                        double radius, int lifetime) {
        return spawn(WaterPuddleEffect.DEFINITION, ownerUuid, position, radius, lifetime);
    }

    /** Creates an instance through the registered type definition; no registry lookup is needed per tick. */
    @Nullable
    public <T extends AbstractGroundEffect> T spawn(GroundEffectType<T> type, @Nullable UUID ownerUuid,
                                                    Vec3 position, double radius, int lifetime) {
        Objects.requireNonNull(type, "type");
        if (!(radius > 0.0D) || !Double.isFinite(radius) || lifetime <= 0) return null;
        Set<Long> nearby = new HashSet<>();
        for (Cell cell : cells(position, radius)) nearby.addAll(spatialIndex.getOrDefault(cell, Set.of()));
        AbstractGroundEffect repaint = null;
        for (long id : nearby) {
            AbstractGroundEffect old = effects.get(id);
            if (old.getType() == type && Objects.equals(ownerUuid, old.ownerUuid())
                    && old.position().equals(position) && old.radius() == radius && old.lifetime() == lifetime) {
                repaint = old;
                break;
            }
        }
        GroundEffectCoverage brush = repaint == null
                ? GroundEffectCoverage.puddle(radius, nextCreationOrder) : repaint.footprint();
        Map<AbstractGroundEffect, GroundEffectCoverage> changed = new LinkedHashMap<>();
        int erased = 0;
        for (long id : nearby) {
            AbstractGroundEffect old = effects.get(id);
            if (old == repaint || Math.abs(old.position().y - position.y) > SURFACE_EPSILON) continue;
            double dx = position.x - old.position().x;
            double dz = position.z - old.position().z;
            double sum = radius + old.radius();
            if (dx * dx + dz * dz >= sum * sum) continue;
            GroundEffectCoverage remainder = old.coverage().subtract(brush, dx, dz);
            if (remainder == old.coverage()) continue;
            if (remainder.serializedBytes() > GroundEffectCoverage.MAX_SERIALIZED_BYTES) return null;
            changed.put(old, remainder);
            if (remainder.isEmpty()) erased++;
        }
        // Admission is transactional: a rejected brush cannot erase any old paint.
        if (effects.size() - erased + (repaint == null ? 1 : 0) > MAX_ACTIVE_EFFECTS) return null;
        T created = null;
        if (repaint == null) {
            Identity identity = allocateIdentity();
            try {
                created = type.create(identity.slot(), identity.generation(), ownerUuid,
                        position, radius, lifetime, nextCreationOrder);
            } finally {
                // A broken addon factory must neither erase existing paint nor leak a reserved slot.
                if (created == null) freeSlots.add(identity.slot());
            }
        }
        changed.forEach((old, remainder) -> {
            if (remainder.isEmpty()) remove(old);
            else old.setCoverage(remainder);
        });
        if (repaint != null) {
            repaint.repaint(nextCreationOrder++);
            // Every instance admitted under a descriptor is constructed by that descriptor's factory.
            @SuppressWarnings("unchecked") T refreshed = (T) repaint;
            return refreshed;
        }
        nextCreationOrder++;
        effects.put(created.identity(), created);
        addToIndex(created);
        return created;
    }

    public void tick(ServerLevel level) {
        gameTick++;
        nextContactTick.entrySet().removeIf(entry -> entry.getValue() <= gameTick);
        List<AbstractGroundEffect> expired = new ArrayList<>();
        for (AbstractGroundEffect effect : effects.values()) {
            // Stagger fixed-position probes; a lost support is removed within ten server ticks.
            if (!effect.tick() || (gameTick + effect.slot()) % SUPPORT_CHECK_INTERVAL == 0
                    && !GroundEffectPlacement.hasSupport(level, effect.position())) expired.add(effect);
        }
        expired.forEach(this::remove);
        applyContacts(level);
    }

    public void remove(AbstractGroundEffect effect) {
        if (effects.remove(effect.identity()) == null) return;
        removeFromIndex(effect);
        pendingDespawns.add(effect.identity());
        freeSlots.add(effect.slot());
    }

    public List<AbstractGroundEffect> activeEffects() {
        return List.copyOf(effects.values());
    }

    /** Finds effects whose centers lie inside the inclusive three-dimensional visibility radius. */
    public List<AbstractGroundEffect> queryVisible(Vec3 center, double radius) {
        if (radius < 0 || !Double.isFinite(radius)) return List.of();
        double radiusSquared = radius * radius;
        double xCells = Math.floor((center.x + radius) / CELL_SIZE)
                - Math.floor((center.x - radius) / CELL_SIZE) + 1;
        double zCells = Math.floor((center.z + radius) / CELL_SIZE)
                - Math.floor((center.z - radius) / CELL_SIZE) + 1;
        if (effects.size() < xCells * zCells) {
            return effects.values().stream()
                    .filter(effect -> effect.position().distanceToSqr(center) <= radiusSquared).toList();
        }
        Set<Long> candidates = new HashSet<>();
        for (Cell cell : cells(center, radius)) {
            candidates.addAll(spatialIndex.getOrDefault(cell, Set.of()));
        }
        List<AbstractGroundEffect> visible = new ArrayList<>();
        for (long identity : candidates) {
            AbstractGroundEffect effect = effects.get(identity);
            if (effect != null && effect.position().distanceToSqr(center) <= radiusSquared) visible.add(effect);
        }
        return visible;
    }

    public Set<Long> drainDespawns() {
        Set<Long> result = Set.copyOf(pendingDespawns);
        pendingDespawns.clear();
        return result;
    }

    public void clear() {
        pendingDespawns.addAll(effects.keySet());
        effects.clear();
        spatialIndex.clear();
        nextContactTick.clear();
        freeSlots.clear();
        for (int slot = 0; slot < nextSlot; slot++) freeSlots.add(slot);
    }

    private void applyContacts(ServerLevel level) {
        if (effects.isEmpty()) return;
        Map<ContactKey, List<AbstractGroundEffect>> contacts = new HashMap<>();
        Map<UUID, Set<Long>> tested = new HashMap<>();
        Map<UUID, LivingEntity> resolvedOwners = new HashMap<>();
        Map<ContactKey, Boolean> eligible = new HashMap<>();
        for (Map.Entry<Cell, Set<Long>> entry : spatialIndex.entrySet()) {
            Cell cell = entry.getKey();
            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for (long identity : entry.getValue()) {
                AbstractGroundEffect effect = effects.get(identity);
                if (effect == null) continue;
                minY = Math.min(minY, effect.position().y);
                maxY = Math.max(maxY, effect.position().y);
            }
            if (minY > maxY) continue;
            AABB query = cell.bounds(minY - GroundEffectContactGeometry.HEIGHT_TOLERANCE - SURFACE_EPSILON,
                    maxY + GroundEffectContactGeometry.HEIGHT_TOLERANCE + SURFACE_EPSILON);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, query,
                    entity -> entity.isAlive() && !entity.isInvulnerable())) {
                Set<Long> testedEffects = tested.computeIfAbsent(target.getUUID(), ignored -> new HashSet<>());
                for (Long identity : entry.getValue()) {
                    if (!testedEffects.add(identity)) continue;
                    AbstractGroundEffect effect = effects.get(identity);
                    if (effect == null) continue;
                    ContactKey key = new ContactKey(target.getUUID(), effect.ownerUuid());
                    if (gameTick < nextContactTick.getOrDefault(key, Long.MIN_VALUE)
                            || !GroundEffectContactGeometry.touches(effect, target.getBoundingBox())) continue;
                    if (!resolvedOwners.containsKey(effect.ownerUuid())) {
                        resolvedOwners.put(effect.ownerUuid(), resolveOwner(level, effect.ownerUuid()));
                    }
                    LivingEntity owner = resolvedOwners.get(effect.ownerUuid());
                    if (!eligible.computeIfAbsent(key,
                            ignored -> owner == null || TrackingTargetRules.eligible(level, owner, target))) continue;
                    contacts.computeIfAbsent(key, ignored -> new ArrayList<>()).add(effect);
                }
            }
        }

        for (Map.Entry<ContactKey, List<AbstractGroundEffect>> entry : contacts.entrySet()) {
            ContactKey key = entry.getKey();
            LivingEntity target = level.getEntity(key.target()) instanceof LivingEntity living ? living : null;
            if (target == null) continue;
            LivingEntity owner = resolvedOwners.get(key.effectOwner());
            AbstractGroundEffect latest = entry.getValue().get(0);
            for (AbstractGroundEffect effect : entry.getValue()) {
                if (effect.creationOrder() > latest.creationOrder()) latest = effect;
            }
            ResourceLocation selectedType = latest.typeId();
            List<AbstractGroundEffect> overlapping = entry.getValue().stream()
                    .filter(effect -> effect.typeId().equals(selectedType)).toList();
            latest.onContactEffect(new GroundEffectContact(level, target, owner, overlapping));
            nextContactTick.put(key, gameTick + CONTACT_COOLDOWN_TICKS);
        }
    }

    @Nullable
    private static LivingEntity resolveOwner(ServerLevel level, @Nullable UUID ownerUuid) {
        if (ownerUuid == null) return null;
        return level.getEntity(ownerUuid) instanceof LivingEntity living ? living : null;
    }

    private Identity allocateIdentity() {
        Integer slot = freeSlots.poll();
        if (slot == null) slot = nextSlot++;
        int generation = generations.merge(slot, 1, Integer::sum);
        return new Identity(slot, generation);
    }

    private void addToIndex(AbstractGroundEffect effect) {
        for (Cell cell : cells(effect.position(), effect.radius())) {
            spatialIndex.computeIfAbsent(cell, ignored -> new HashSet<>()).add(effect.identity());
        }
    }

    private void removeFromIndex(AbstractGroundEffect effect) {
        for (Cell cell : cells(effect.position(), effect.radius())) {
            Set<Long> ids = spatialIndex.get(cell);
            if (ids == null) continue;
            ids.remove(effect.identity());
            if (ids.isEmpty()) spatialIndex.remove(cell);
        }
    }

    private static Set<Cell> cells(Vec3 position, double radius) {
        int minX = (int) Math.floor((position.x - radius) / CELL_SIZE);
        int maxX = (int) Math.floor((position.x + radius) / CELL_SIZE);
        int minZ = (int) Math.floor((position.z - radius) / CELL_SIZE);
        int maxZ = (int) Math.floor((position.z + radius) / CELL_SIZE);
        Set<Cell> result = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) result.add(new Cell(x, z));
        }
        return result;
    }

    private record Identity(int slot, int generation) { }

    private record Cell(int x, int z) {
        AABB bounds(double minY, double maxY) {
            return new AABB(x * CELL_SIZE, minY, z * CELL_SIZE,
                    (x + 1) * CELL_SIZE, maxY, (z + 1) * CELL_SIZE);
        }
    }

    private record ContactKey(UUID target, @Nullable UUID effectOwner) { }
}
