package net.luojiuoscar.isaac_disaster.renderer.visual;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;

public record BulletRenderContext(
        TearBullet bullet,
        float partialTicks,
        int packedLight
) {
}
