package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** Registered policy hook for exceptional split composition or inheritance semantics. */
public abstract class SplitRule {
    private final Set<ResourceLocation> targetModules;

    protected SplitRule(Set<ResourceLocation> targetModules) {
        if (targetModules == null || targetModules.isEmpty()) {
            throw new IllegalArgumentException("A split rule requires at least one target module");
        }
        this.targetModules = Set.copyOf(targetModules);
    }

    public final Set<ResourceLocation> getTargetModules() {
        return targetModules;
    }

    /**
     * Returns whether the candidate may generate children in the current event.
     */
    /**
     * Returns whether the candidate module may generate children for this event.
     *
     * @param event the current parent-bullet split event
     * @param module the registered candidate module
     * @return {@code true} when this rule permits the candidate
     */
    public boolean allows(@NotNull BulletSplitEvent event, @NotNull SplitModule module) {
        return true;
    }
}
