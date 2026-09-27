package net.luojiuoscar.isaac_disaster.client.particle;

import net.luojiuoscar.isaac_disaster.helper.ProjectileCollisionHelper;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.FetusBulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.TearBulletVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/** Client-only factory for the scale-aware projectile shatter effect. */
public final class TearShatterParticles {
    private TearShatterParticles() {
    }

    /** Spawns shatter fragments from a lightweight bullet snapshot. */
    public static void spawn(ClientLevel level, Vec3 position, Vec3 velocity, double scale, int color,
                             float alpha, List<ResourceLocation> visualIds, boolean fetus) {
        FragmentMaterial material = resolveMaterial(visualIds, color, alpha, fetus);
        if (material == null) return;
        int count = ProjectileCollisionHelper.fragmentCount(scale);
        float fragmentSize = ProjectileCollisionHelper.fragmentQuadSize(scale);
        RandomSource random = level.random;
        for (int index = 0; index < count; index++) {
            Vec3 fragmentVelocity = velocity.add(random.nextFloat() * 0.08D - 0.04D,
                    random.nextFloat() * 0.08D - 0.04D, random.nextFloat() * 0.08D - 0.04D);
            TearShatterParticle particle = new TearShatterParticle(level, position, fragmentVelocity,
                    material.texture(), fragmentSize, material.color(), material.alpha());
            particle.setLifetime(6);
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    @Nullable
    private static FragmentMaterial resolveMaterial(Iterable<ResourceLocation> ids, int color, float alpha, boolean fetus) {
        var registry = net.minecraftforge.registries.RegistryManager.ACTIVE.getRegistry(
                net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals.BULLET_VISUAL_KEY);
        if (registry == null) return null;
        BulletVisual best = null;
        for (ResourceLocation id : ids) {
            BulletVisual candidate = registry.getValue(id);
            if (candidate == null || (fetus != (candidate instanceof FetusBulletVisual))) continue;
            if (best == null || candidate.getPriority() > best.getPriority()) best = candidate;
        }
        if (best == null) best = fetus ? net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals.DEFAULT_FETUS.get()
                : net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals.DEFAULT_TEAR.get();
        if (best instanceof TearBulletVisual tear) return new FragmentMaterial(tear.getTexture(), tear.acceptsTint() ? color : 0xFFFFFF, alpha);
        if (best instanceof FetusBulletVisual fetusVisual) return new FragmentMaterial(fetusVisual.getShatterTexture(), fetusVisual.acceptsTint() ? color : 0xFFFFFF, alpha);
        return null;
    }

    private record FragmentMaterial(ResourceLocation texture, int color, float alpha) {
    }
}
