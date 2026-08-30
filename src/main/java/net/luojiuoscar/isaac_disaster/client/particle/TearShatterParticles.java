package net.luojiuoscar.isaac_disaster.client.particle;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.helper.ProjectileCollisionHelper;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.FetusBulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.TearBulletVisual;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisualResolver;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisualClientRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Client-only factory for the scale-aware projectile shatter effect. */
public final class TearShatterParticles {
    private TearShatterParticles() {
    }

    public static void spawn(TearBullet bullet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        FragmentMaterial material = resolveMaterial(bullet);
        if (material == null) {
            return;
        }

        double scale = ProjectileCollisionHelper.normalizeScale(bullet.getScale());
        int count = ProjectileCollisionHelper.fragmentCount(scale);
        float fragmentSize = ProjectileCollisionHelper.fragmentQuadSize(scale);
        float collisionSize = ProjectileCollisionHelper.fragmentCollisionSize(scale);
        AABB bounds = bullet.getBoundingBox();
        RandomSource random = level.random;

        for (int index = 0; index < count; index++) {
            Vec3 position = findSafeSpawnPosition(level, bounds, collisionSize, random);
            if (position == null) {
                continue;
            }
            Vec3 velocity = new Vec3(
                    random.nextFloat() * 0.08D - 0.04D,
                    random.nextFloat() * 0.08D - 0.04D,
                    random.nextFloat() * 0.08D - 0.04D);
            Minecraft.getInstance().particleEngine.add(new TearShatterParticle(
                    level, position, velocity, material.texture(), fragmentSize, material.color(), material.alpha()));
        }
    }

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    @Nullable
    private static Vec3 findSafeSpawnPosition(ClientLevel level, AABB bounds, float collisionSize, RandomSource random) {
        double halfSize = collisionSize * 0.5D;
        double minX = bounds.minX + halfSize;
        double maxX = bounds.maxX - halfSize;
        double minY = bounds.minY + halfSize;
        double maxY = bounds.maxY - halfSize;
        double minZ = bounds.minZ + halfSize;
        double maxZ = bounds.maxZ - halfSize;
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return null;
        }

        for (int attempt = 0; attempt < 4; attempt++) {
            Vec3 position = new Vec3(
                    randomBetween(random, minX, maxX),
                    randomBetween(random, minY, maxY),
                    randomBetween(random, minZ, maxZ));
            if (level.noCollision(AABB.ofSize(position, collisionSize, collisionSize, collisionSize))) {
                return position;
            }
        }
        return null;
    }

    @Nullable
    private static FragmentMaterial resolveMaterial(TearBullet bullet) {
        BulletVisual visual = BulletVisualResolver.resolve(bullet, BulletVisualClientRenderers::hasRegisteredRenderer);
        if (visual instanceof TearBulletVisual tearVisual) {
            return new FragmentMaterial(tearVisual.getTexture(), tearVisual.acceptsTint() ? bullet.getColor() : 0xFFFFFF,
                    bullet.getAlpha());
        }
        if (bullet instanceof FetusBullet && visual instanceof FetusBulletVisual fetusVisual) {
            return new FragmentMaterial(fetusVisual.getShatterTexture(),
                    fetusVisual.acceptsTint() ? bullet.getColor() : 0xFFFFFF, bullet.getAlpha());
        }
        return null;
    }

    private record FragmentMaterial(ResourceLocation texture, int color, float alpha) {
    }
}
