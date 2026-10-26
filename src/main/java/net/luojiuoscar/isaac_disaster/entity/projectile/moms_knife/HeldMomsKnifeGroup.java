package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.CollectHeldKnivesEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Objects;

/** Coordinates held slots without owning projectile state, a global queue, or saved player data. */
public final class HeldMomsKnifeGroup {
    public static final int MAX_KNIVES = 16;

    private HeldMomsKnifeGroup() {}

    /** Held position and direction in the player's view frame; contributors never allocate slots. */
    public record Pose(Vec3 offset, Vec3 direction) {
        public Pose {
            Objects.requireNonNull(offset, "offset");
            Objects.requireNonNull(direction, "direction");
            if (!Double.isFinite(offset.lengthSqr())) offset = Vec3.ZERO;
            direction = PlayerMomsKnifeMotion.unit(direction);
        }
    }

    record Slot(ResourceLocation sourceId, int index) {
        Slot {
            Objects.requireNonNull(sourceId, "sourceId");
            index = Math.max(0, index);
        }
    }

    private record HeldKnifeEntry(ResourceLocation sourceId, int index, Vec3 offset, Vec3 direction) {
        Slot slot() { return new Slot(sourceId, index); }
    }

    public record QueuedKnife(PlayerMomsKnifeEntity entity, AttackContext context) {}

    public static Pose layout(int count, int slot, double radius) {
        count = Mth.clamp(count, 1, MAX_KNIVES);
        slot = Mth.clamp(slot, 0, count - 1);
        if (count == 2) return new Pose(new Vec3(slot == 0 ? 0.25 : -0.25, 0, 0), PlayerMomsKnifeMotion.FORWARD);
        double angle = Math.toRadians(((count - 1) / 2.0 - slot) * 2 * Math.max(11 - count, 5));
        Vec3 direction = new Vec3(Math.sin(angle), 0, Math.cos(angle));
        return new Pose(direction.subtract(PlayerMomsKnifeMotion.FORWARD).scale(radius), direction);
    }

    public static int heldCount(ServerPlayer player) {
        return Mth.clamp((int) player.getAttributeValue(ModAttributes.BULLET_COUNT.get()), 1, MAX_KNIVES);
    }

    public static List<PlayerMomsKnifeEntity> knives(ServerPlayer player) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability ->
                ability.getRuntimeEntities().getEntities(player.serverLevel(), PlayerMomsKnifeEntity.class).stream()
                        .filter(knife -> knife.getOwner() == player && knife.getMode() == PlayerMomsKnifeEntity.Mode.RETURNING)
                        .toList()).orElseGet(List::of);
    }

    /** Collects held appearance without preparing an attack or rolling firing probabilities. */
    private static List<HeldKnifeEntry> collect(ServerPlayer player) {
        List<HeldKnifeEntry> entries = new ArrayList<>();
        int count = heldCount(player);
        for (int slot = 0; slot < count; slot++) entries.add(baseEntry(player, count, slot));
        var event = new CollectHeldKnivesEvent(player);
        MinecraftForge.EVENT_BUS.post(event);
        event.getGroups().forEach((sourceId, poses) -> {
            if (sourceId.equals(ModAttackTypes.MOMS_KNIFE.getId())) return;
            for (int index = 0; index < poses.size(); index++) {
                Pose pose = poses.get(index);
                entries.add(new HeldKnifeEntry(sourceId, index, pose.offset(), pose.direction()));
            }
        });
        return entries;
    }

    private static HeldKnifeEntry baseEntry(ServerPlayer player, int count, int slot) {
        Pose layout = layout(count, slot, Math.max(0.6, player.getBbWidth()));
        return new HeldKnifeEntry(ModAttackTypes.MOMS_KNIFE.getId(), slot, layout.offset(), layout.direction());
    }

    /** Creates the capped front volley, leaving additional source groups outside this limit. */
    public static List<AttackContext> createBaseContexts(ServerPlayer player, int count) {
        count = Mth.clamp(count, 0, MAX_KNIVES);
        List<AttackContext> contexts = new ArrayList<>(count);
        for (int slot = 0; slot < count; slot++) contexts.add(createContext(player, baseEntry(player, count, slot)));
        return contexts;
    }

    private static AttackContext createContext(ServerPlayer player, HeldKnifeEntry entry) {
        var type = ModAttackTypes.MOMS_KNIFE.get();
        return AttackContext.builder(player, player).attackType(type)
                .position(PlayerMomsKnifeMotion.idlePosition(player, 1).add(PlayerMomsKnifeMotion.offsetToWorld(
                        entry.offset(), player.getYRot(), player.getXRot())))
                .mainAxis(PlayerMomsKnifeMotion.toWorld(entry.direction(), player.getYRot(), player.getXRot()))
                .range(type.getRange(player)).speed(type.getBulletSpeed(player)).build();
    }

    public static boolean isInFlight(ServerPlayer player) {
        return knives(player).stream().anyMatch(PlayerMomsKnifeEntity::isInFlight);
    }

    public static List<PlayerMomsKnifeEntity> refresh(ServerPlayer player) {
        if (!MomsKnifeAttack.isSelectedAndReady(player)) return List.of();
        List<PlayerMomsKnifeEntity> current = knives(player);
        if (current.stream().anyMatch(PlayerMomsKnifeEntity::isInFlight)) return current;
        List<HeldKnifeEntry> entries = collect(player);
        Set<Slot> desired = new HashSet<>();
        for (var entry : entries) desired.add(entry.slot());
        for (var knife : current) if (!desired.contains(knife.getHeldAttackSlot())) knife.discard();
        List<PlayerMomsKnifeEntity> result = new ArrayList<>();
        for (var entry : entries) {
            PlayerMomsKnifeEntity knife = getOrCreate(player, entry);
            if (knife != null) {
                knife.configureHeld(entry.slot(), entry.offset(), entry.direction());
                result.add(knife);
            }
        }
        return result;
    }

    @Nullable
    private static PlayerMomsKnifeEntity getOrCreate(ServerPlayer player, HeldKnifeEntry entry) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability -> {
            var entities = ability.getRuntimeEntities();
            var slot = entry.slot();
            var id = slot.sourceId();
            PlayerMomsKnifeEntity knife = entities.getEntity(id, slot.index(), player.serverLevel(), PlayerMomsKnifeEntity.class);
            if (knife != null && (knife.getOwner() != player || knife.getMode() != PlayerMomsKnifeEntity.Mode.RETURNING
                    || !knife.getHeldAttackSlot().equals(slot))) {
                entities.unregister(id, slot.index(), knife.getUUID());
                knife = null;
            }
            if (knife == null) {
                knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), player.level());
                knife.initialize(player, slot, entry.offset(), entry.direction());
                if (!player.serverLevel().addFreshEntity(knife)) return null;
                entities.register(id, slot.index(), knife.getUUID());
            }
            return knife;
        }).orElse(null);
    }

    /** Selects and binds a request's entities before preparation, without launching them. */
    public static List<QueuedKnife> select(ServerPlayer player, List<AttackContext> contexts,
                                            Collection<ResourceLocation> sources) {
        if (!((MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get()).isChargeEligible(player)) return List.of();
        List<QueuedKnife> queue = new ArrayList<>();
        int count = Math.min(MAX_KNIVES, contexts.size());
        for (int index = 0; index < count; index++) {
            AttackContext context = contexts.get(index);
            HeldKnifeEntry entry = entryForContext(player,
                    new Slot(ModAttackTypes.MOMS_KNIFE.getId(), index), context);
            if (entry != null) select(player, entry, context, queue);
        }
        if (!sources.isEmpty()) {
            for (var entry : collect(player)) {
                if (!entry.sourceId().equals(ModAttackTypes.MOMS_KNIFE.getId()) && sources.contains(entry.sourceId())) {
                    select(player, entry, createContext(player, entry), queue);
                }
            }
        }
        return List.copyOf(queue);
    }

    private static void select(ServerPlayer player, HeldKnifeEntry entry, AttackContext context,
                               List<QueuedKnife> queue) {
        PlayerMomsKnifeEntity knife = getOrCreate(player, entry);
        if (knife != null) queue.add(new QueuedKnife(knife, context));
    }

    @Nullable
    private static HeldKnifeEntry entryForContext(ServerPlayer player, Slot slot, AttackContext context) {
        if (context.getOwner() != player || !Double.isFinite(context.getPos().lengthSqr())) return null;
        Vec3 idle = PlayerMomsKnifeMotion.idlePosition(player, 1);
        return new HeldKnifeEntry(slot.sourceId(), slot.index(),
                PlayerMomsKnifeMotion.offsetToLocal(context.getPos().subtract(idle), player.getYRot(), player.getXRot()),
                PlayerMomsKnifeMotion.toLocal(context.getMainAxis(), player.getYRot(), player.getXRot()));
    }

    /** Launches confirmed entities in context order without replacing missing entries. */
    @Nullable
    public static AttackContext launch(ServerPlayer player, List<QueuedKnife> queue, List<AttackContext> contexts,
                                double charge, int barrageCount) {
        if (queue.size() != contexts.size()) throw new IllegalArgumentException("prepared knife queue size changed");
        AttackContext firstLaunched = null;
        for (int index = 0; index < queue.size(); index++) {
            PlayerMomsKnifeEntity knife = queue.get(index).entity();
            Slot slot = knife.getHeldAttackSlot();
            if (knife.isRemoved() || knife.getOwner() != player || !player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(ability -> knife.getUUID().equals(ability.getRuntimeEntities().getId(slot.sourceId(), slot.index())))
                    .orElse(false)) continue;
            AttackContext context = contexts.get(index);
            HeldKnifeEntry entry = entryForContext(player, slot, context);
            if (entry == null) continue;
            knife.configureHeld(slot, entry.offset(), entry.direction());
            if (knife.launch(PlayerMomsKnifeMotion.range(context.getBulletRange(), charge), context.getBulletSpeed(),
                    context, firstLaunched == null ? barrageCount : 0)) {
                if (firstLaunched == null) firstLaunched = context;
            }
        }
        return firstLaunched;
    }
}
