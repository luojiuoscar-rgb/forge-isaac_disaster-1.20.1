package net.luojiuoscar.isaac_disaster.registries.familiar;

import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry descriptor that marks one vanilla EntityType as an Isaac familiar type.
 *
 * <p>The descriptor is registered separately from the EntityType so addon mods can expose
 * familiar entities without placing the same EntityType instance in two Forge registries.</p>
 */
public final class FamiliarEntityType {
    private final Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier;
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
        if (limit < 1) limit = 1;
        this.entityTypeSupplier = Objects.requireNonNull(entityTypeSupplier);
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
