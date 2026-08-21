package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import org.jetbrains.annotations.NotNull;

public final class AttackPatternContext {
    private final AttackContext referenceContext;
    private final int bulletCount;

    public AttackPatternContext(@NotNull AttackContext referenceContext, int bulletCount) {
        this.referenceContext = referenceContext;
        this.bulletCount = bulletCount;
    }

    public AttackContext getReferenceContext() {
        return referenceContext;
    }

    public int getBulletCount() {
        return bulletCount;
    }
}
