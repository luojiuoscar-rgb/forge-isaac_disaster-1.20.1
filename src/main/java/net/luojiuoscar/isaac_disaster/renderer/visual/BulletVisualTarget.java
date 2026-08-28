package net.luojiuoscar.isaac_disaster.renderer.visual;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;

public enum BulletVisualTarget {
    TEAR,
    FETUS;

    public static BulletVisualTarget of(TearBullet bullet) {
        return bullet instanceof FetusBullet ? FETUS : TEAR;
    }
}
