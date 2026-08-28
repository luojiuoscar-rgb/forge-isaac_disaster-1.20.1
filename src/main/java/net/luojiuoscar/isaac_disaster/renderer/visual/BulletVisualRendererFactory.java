package net.luojiuoscar.isaac_disaster.renderer.visual;

import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Creates a client renderer adapter after Minecraft's entity model layers are available. */
@FunctionalInterface
public interface BulletVisualRendererFactory<V extends BulletVisual> {
    BulletVisualRenderer<V> create(EntityRendererProvider.Context context);
}
