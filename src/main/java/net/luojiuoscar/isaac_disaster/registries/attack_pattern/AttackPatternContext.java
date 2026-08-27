package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import org.jetbrains.annotations.NotNull;

public final class AttackPatternContext {
    private final AttackContext mainBulletContext;
    private final int bulletCount;

    public AttackPatternContext(@NotNull AttackContext mainBulletContext, int bulletCount) {
        this.mainBulletContext = mainBulletContext;
        this.bulletCount = bulletCount;
    }

    public AttackContext getMainBulletContext() {
        return mainBulletContext;
    }

    public int getBulletCount() {
        return bulletCount;
    }
}
