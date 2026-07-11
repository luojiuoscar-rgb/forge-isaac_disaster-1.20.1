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

    public FamiliarEntityType(
            Supplier<? extends EntityType<? extends AbstractIsaacFamiliarEntity>> entityTypeSupplier) {
        this.entityTypeSupplier = Objects.requireNonNull(entityTypeSupplier);
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
