package net.luojiuoscar.isaac_disaster.registries.familiar;

import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registry descriptor that marks one vanilla EntityType as an Isaac familiar type.
 *
 * <p>The descriptor is registered separately from the EntityType so addon mods can expose
 * familiar entities without placing the same EntityType instance in two Forge registries.</p>
 */
public class FamiliarEntityType {
    private final Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier;
    @Nullable
    private final Class<? extends AbstractIsaacFamiliarEntity> entityClass;
    private final int limit;
    private final Supplier<? extends FamiliarLimitGroup> group;
    private final int priority;
    private final boolean retainOverflow;

    public FamiliarEntityType(
            Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier) {
        this(entityTypeSupplier, 1);
    }

    public FamiliarEntityType(
            Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier, int limit) {
        this(entityTypeSupplier, limit, null, 0, false);
    }

    public FamiliarEntityType(
            Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier, int limit,
            @Nullable Supplier<? extends FamiliarLimitGroup> group, int priority, boolean retainOverflow) {
        this(entityTypeSupplier, null, limit, group, priority, retainOverflow);
    }

    public FamiliarEntityType(
            Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier,
            @Nullable Class<? extends AbstractIsaacFamiliarEntity> entityClass, int limit,
            @Nullable Supplier<? extends FamiliarLimitGroup> group, int priority, boolean retainOverflow) {
        if (limit < 1) limit = 1;
        this.entityTypeSupplier = Objects.requireNonNull(entityTypeSupplier);
        this.entityClass = entityClass;
        this.limit = limit;
        this.group = group;
        this.priority = priority;
        this.retainOverflow = retainOverflow;
    }

    /** Maximum effective count; retained records may exceed this limit. */
    public int getLimit() {
        return limit;
    }

    @Nullable
    public FamiliarLimitGroup getGroup() { return group == null ? null : Objects.requireNonNull(group.get()); }
    public int getPriority() { return priority; }
    public boolean retainOverflow() { return retainOverflow; }

    @Nullable
    public Class<? extends AbstractIsaacFamiliarEntity> getEntityClass() {
        return entityClass;
    }

    /** Checks an entity marker without resolving the factory or constructing an entity. */
    public boolean hasEntityTag(Class<?> marker) {
        return entityClass != null && marker.isAssignableFrom(entityClass);
    }

    /** Adds one type-defined batch through the normal familiar quota and admission system. */
    public boolean addEntry(ServerPlayer player, ResourceLocation typeId, RandomSource random) {
        if (FamiliarHelper.getFamiliarType(typeId) != this) return false;
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.RESOURCE_LOCATIONS, List.of(typeId));
        context.set(ContextKeys.COUNT, Math.max(0, getAdditionCount(random)));
        return ModExecutableEffects.ADD_FAMILIARS.get() instanceof IAbilityEffect effect
                && effect.applyEffect(context);
    }

    protected int getAdditionCount(RandomSource random) {
        return 1;
    }

    /** Immediately spawns one type-defined batch at an explicit position using the caller's random source. */
    public List<AbstractIsaacFamiliarEntity> spawnEntry(ServerPlayer player, ResourceLocation typeId, Vec3 position, RandomSource random) {
        if (FamiliarHelper.getFamiliarType(typeId) != this) return List.of();
        return FamiliarHelper.spawnFamiliars(player, typeId, position, Math.max(0, getAdditionCount(random)));
    }

    /**
     * Returns the registered Minecraft entity type represented by this descriptor.
     */
    public EntityType<? extends AbstractIsaacFamiliarEntity> getEntityType() {
        return entityTypeSupplier.get();
    }

    /**
     * Creates a familiar entity in the supplied level without adding it to the world.
     */
    @Nullable
    public AbstractIsaacFamiliarEntity create(Level level) {
        return getEntityType().create(level);
    }
}
