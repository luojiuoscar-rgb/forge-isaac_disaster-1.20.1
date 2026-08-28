package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import java.util.Objects;

/**
 * Common, server-safe definition of a bullet visual.
 *
 * <p>Client renderers are bound separately to the same registry entry, so this type must not
 * reference model, buffer, or other client-only classes.</p>
 */
public abstract class BulletVisual {
    private final BulletVisualTarget target;
    private final double priority;

    protected BulletVisual(BulletVisualTarget target, double priority) {
        this.target = Objects.requireNonNull(target, "target");
        this.priority = priority;
    }

    public final BulletVisualTarget getTarget() {
        return target;
    }

    public final double getPriority() {
        return priority;
    }
}
