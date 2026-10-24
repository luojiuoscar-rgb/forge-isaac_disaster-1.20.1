package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContactGeometry;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectCoverage;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.server.level.ServerLevel;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
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
    public static final int MAX_CELLS_PER_EFFECT = 4096;
    public static final int CONTACT_COOLDOWN_TICKS = 10;
    public static final int EFFECT_COOLDOWN_TICKS = 5;
    private static final int SUPPORT_CHECK_INTERVAL = 10;
    private static final double CELL_SIZE = 2.0D;
    private static final double SURFACE_EPSILON = 1.0E-4D;

    private final Map<Long, AbstractGroundEffect> effects = new LinkedHashMap<>();
    private final Map<Cell, Set<Long>> spatialIndex = new HashMap<>();
    private final Map<Integer, Integer> generations = new HashMap<>();
    private final Queue<Integer> freeSlots = new ArrayDeque<>();
    private final Set<Long> pendingDespawns = new HashSet<>();
    private final Map<ContactKey, Long> nextDamageTick = new HashMap<>();
    private final Map<UUID, Long> nextEffectTick = new HashMap<>();
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
    public <T extends AbstractGroundEffect> T spawn(GroundEffectType<T> type, @Nullable UUID ownerUuid,
                                                    Vec3 position, double radius, int lifetime, float damage) {
        return spawn(type, ownerUuid, position, radius, lifetime, damage, 0);
    }

    @Nullable
    public <T extends AbstractGroundEffect> T spawn(GroundEffectType<T> type, @Nullable UUID ownerUuid,
                                                    Vec3 position, double radius, int lifetime, float damage,
                                                    int animationDurationTicks) {
        if (type == null || position == null || !Double.isFinite(position.x)
                || !Double.isFinite(position.y) || !Double.isFinite(position.z)) {
            IsaacDisaster.LOGGER.warn("Ignoring ground-effect spawn with missing type or invalid position");
            return null;
        }
        animationDurationTicks = Math.max(0, animationDurationTicks);
        if (!(radius > 0.0D) || !Double.isFinite(radius) || lifetime <= 0) return null;
        if (cellCount(position, radius) > MAX_CELLS_PER_EFFECT) {
            IsaacDisaster.LOGGER.warn("Ignoring ground-effect spawn exceeding grid budget: radius={}", radius);
            return null;
        }
        float safeDamage = Float.isFinite(damage) ? Math.max(0.0F, damage) : 0.0F;
        Set<Long> nearby = nearby(position, radius);
        AbstractGroundEffect repaint = null;
        for (long id : nearby) {
            AbstractGroundEffect old = effects.get(id);
            if (old.getType() == type && Objects.equals(ownerUuid, old.ownerUuid())
                    && old.position().equals(position) && old.radius() == radius && old.lifetime() == lifetime
                    && old.damage() == safeDamage && old.animationDurationTicks() == animationDurationTicks) {
                repaint = old;
                break;
            }
        }
        boolean paintNow = repaint != null ? repaint.isSettled() : animationDurationTicks == 0;
        GroundEffectCoverage brush = repaint == null
                ? GroundEffectCoverage.puddle(radius, nextCreationOrder) : repaint.footprint();
        List<AbstractGroundEffect> erased = paintNow
                ? fullyCovered(position, radius, brush, repaint, nearby) : List.of();
        // Growing instances consume capacity now; only immediate paint can reclaim erased instances.
        if (effects.size() - erased.size() + (repaint == null ? 1 : 0) > MAX_ACTIVE_EFFECTS) return null;
        T created = null;
        if (repaint == null) {
            Identity identity = allocateIdentity();
            try {
                created = type.create(identity.slot(), identity.generation(), ownerUuid,
                        position, radius, lifetime, safeDamage, nextCreationOrder, animationDurationTicks);
            } finally {
                // Validate addon factories before erasing any paint or committing the reserved identity.
                if (created == null) freeSlots.add(identity.slot());
            }
            if (created == null) return null;
        }
        erased.forEach(this::remove);
        if (repaint != null) {
            repaint.repaint(paintNow ? nextCreationOrder++ : repaint.creationOrder());
            @SuppressWarnings("unchecked") T refreshed = (T) repaint;
            return refreshed;
        }
        nextCreationOrder++;
        effects.put(created.identity(), created);
        addToIndex(created);
        return created;
    }

    private Set<Long> nearby(Vec3 position, double radius) {
        Set<Long> nearby = new HashSet<>();
        for (Cell cell : cells(position, radius)) nearby.addAll(spatialIndex.getOrDefault(cell, Set.of()));
        return nearby;
    }

    private List<AbstractGroundEffect> fullyCovered(Vec3 position, double radius,
            GroundEffectCoverage brush, @Nullable AbstractGroundEffect repaint, Set<Long> nearby) {
        List<AbstractGroundEffect> erased = new ArrayList<>();
        for (long id : nearby) {
            AbstractGroundEffect old = effects.get(id);
            if (old == null || old == repaint || !old.isSettled()
                    || Math.abs(old.position().y - position.y) > SURFACE_EPSILON) continue;
            double dx = old.position().x - position.x;
            double dz = old.position().z - position.z;
            double sum = radius + old.radius();
            if (dx * dx + dz * dz < sum * sum && brush.covers(old.footprint(), dx, dz)) erased.add(old);
        }
        return erased;
    }

    public void tick(ServerLevel level) {
        gameTick++;
        nextDamageTick.entrySet().removeIf(entry -> entry.getValue() <= gameTick);
        nextEffectTick.entrySet().removeIf(entry -> entry.getValue() <= gameTick);
        List<AbstractGroundEffect> expired = new ArrayList<>();
        List<AbstractGroundEffect> completed = new ArrayList<>();
        for (AbstractGroundEffect effect : effects.values()) {
            // Expiry and support loss win over settlement, including the exact completion tick.
            if (!effect.tick() || (gameTick + effect.slot()) % SUPPORT_CHECK_INTERVAL == 0
                    && !GroundEffectPlacement.hasSupport(level, effect.position())) expired.add(effect);
            else if (!effect.isSettled() && effect.isAnimationComplete()) completed.add(effect);
        }
        expired.forEach(this::remove);
        // shapeSeed preserves the original spawn sequence even after lifetime refreshes and repaints.
        completed.sort(Comparator.comparingLong(AbstractGroundEffect::shapeSeed));
        for (AbstractGroundEffect effect : completed) {
            fullyCovered(effect.position(), effect.radius(), effect.footprint(), effect,
                    nearby(effect.position(), effect.radius())).forEach(this::remove);
            effect.settle(nextCreationOrder++);
        }
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
        if (center == null || !Double.isFinite(center.x) || !Double.isFinite(center.y) || !Double.isFinite(center.z)
                || radius < 0 || !Double.isFinite(radius)) return List.of();
        double radiusSquared = radius * radius;
        double cells = cellCount(center, radius);
        if (cells > MAX_CELLS_PER_EFFECT || effects.size() < cells) {
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
        nextDamageTick.clear();
        nextEffectTick.clear();
        freeSlots.clear();
        for (int slot = 0; slot < nextSlot; slot++) freeSlots.add(slot);
    }

    private void applyContacts(ServerLevel level) {
        if (effects.isEmpty()) return;
        Map<ContactKey, AbstractGroundEffect> damageContacts = new HashMap<>();
        Map<UUID, List<AbstractGroundEffect>> effectContacts = new HashMap<>();
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
                    LivingEntity::isAlive)) {
                Set<Long> testedEffects = tested.computeIfAbsent(target.getUUID(), ignored -> new HashSet<>());
                for (Long identity : entry.getValue()) {
                    if (!testedEffects.add(identity)) continue;
                    AbstractGroundEffect effect = effects.get(identity);
                    if (effect == null) continue;
                    ContactKey key = new ContactKey(target.getUUID(), effect.ownerUuid());
                    boolean damageReady = effect.damage() > 0
                            && gameTick >= nextDamageTick.getOrDefault(key, Long.MIN_VALUE);
                    boolean effectReady = gameTick >= nextEffectTick.getOrDefault(target.getUUID(), Long.MIN_VALUE);
                    if ((!damageReady && !effectReady)
                            || !GroundEffectContactGeometry.touches(effect, target.getBoundingBox())) continue;
                    if (!resolvedOwners.containsKey(effect.ownerUuid())) {
                        resolvedOwners.put(effect.ownerUuid(), resolveOwner(level, effect.ownerUuid()));
                    }
                    LivingEntity owner = resolvedOwners.get(effect.ownerUuid());
                    boolean damageEligible = !target.isInvulnerable()
                            && (effect.ownerUuid() == null || owner != null)
                            && eligible.computeIfAbsent(key,
                                    ignored -> owner == null || TrackingTargetRules.eligible(level, owner, target));
                    if (damageReady && damageEligible) {
                        damageContacts.merge(key, effect, (current, incoming) ->
                                incoming.creationOrder() > current.creationOrder() ? incoming : current);
                    }
                    if (effectReady && (effect.affectsAllLivingEntities() || damageEligible)) {
                        effectContacts.computeIfAbsent(target.getUUID(), ignored -> new ArrayList<>()).add(effect);
                    }
                }
            }
        }

        for (var entry : effectContacts.entrySet()) {
            LivingEntity target = level.getEntity(entry.getKey()) instanceof LivingEntity living ? living : null;
            if (target == null) continue;
            for (AbstractGroundEffect effect : entry.getValue()) {
                invokeContact(effect, new GroundEffectContact(level, target, resolvedOwners.get(effect.ownerUuid())), false);
            }
            nextEffectTick.put(entry.getKey(), gameTick + EFFECT_COOLDOWN_TICKS);
        }

        for (Map.Entry<ContactKey, AbstractGroundEffect> entry : damageContacts.entrySet()) {
            ContactKey key = entry.getKey();
            LivingEntity target = level.getEntity(key.target()) instanceof LivingEntity living ? living : null;
            if (target == null) continue;
            LivingEntity owner = resolvedOwners.get(key.effectOwner());
            AbstractGroundEffect effect = entry.getValue();
            if (invokeContact(effect, new GroundEffectContact(level, target, owner), true)) {
                nextDamageTick.put(key, gameTick + CONTACT_COOLDOWN_TICKS);
            }
        }
    }

    private boolean invokeContact(AbstractGroundEffect effect, GroundEffectContact contact, boolean damage) {
        if (!effects.containsKey(effect.identity())) return false;
        try {
            if (damage) effect.onContactDamage(contact);
            else effect.onContactEffect(contact);
            return true;
        } catch (RuntimeException failure) {
            IsaacDisaster.LOGGER.warn("Removing ground effect after contact failure: slot={}", effect.slot(), failure);
            remove(effect);
            return false;
        }
    }

    @Nullable
    private static LivingEntity resolveOwner(ServerLevel level, @Nullable UUID ownerUuid) {
        if (ownerUuid == null) return null;
        if (level.getEntity(ownerUuid) instanceof LivingEntity living) return living;
        return level.getServer().getPlayerList().getPlayer(ownerUuid);
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

    private static double cellCount(Vec3 position, double radius) {
        double minX = Math.floor((position.x - radius) / CELL_SIZE);
        double maxX = Math.floor((position.x + radius) / CELL_SIZE);
        double minZ = Math.floor((position.z - radius) / CELL_SIZE);
        double maxZ = Math.floor((position.z + radius) / CELL_SIZE);
        if (!Double.isFinite(minX) || !Double.isFinite(maxX) || !Double.isFinite(minZ) || !Double.isFinite(maxZ)
                || minX < Integer.MIN_VALUE || maxX >= Integer.MAX_VALUE
                || minZ < Integer.MIN_VALUE || maxZ >= Integer.MAX_VALUE) return Double.POSITIVE_INFINITY;
        return (maxX - minX + 1) * (maxZ - minZ + 1);
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
