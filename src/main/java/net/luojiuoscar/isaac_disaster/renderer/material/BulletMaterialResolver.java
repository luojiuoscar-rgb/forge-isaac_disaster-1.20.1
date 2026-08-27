package net.luojiuoscar.isaac_disaster.renderer.material;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class BulletMaterialResolver {
    public static final ResourceLocation TEAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/particle/tear_bullet.png");

    private BulletMaterialResolver() {
    }

    public static BulletMaterial tear(TearBullet bullet) {
        return BulletMaterial.tinted(TEAR_TEXTURE, RenderType.entityTranslucent(TEAR_TEXTURE),
                bullet.getColor(), bullet.getAlpha());
    }

    public static BulletMaterial of(ResourceLocation texture, int color, float alpha) {
        return BulletMaterial.tinted(texture, RenderType.entityTranslucent(texture), color, alpha);
    }
}
