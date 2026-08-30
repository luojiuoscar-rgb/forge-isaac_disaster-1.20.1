package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Client adapter catalog for Forge {@link BulletVisual} entries.
 *
 * <p>This does not define visual IDs or priority. Those remain exclusively in the Forge registry;
 * the catalog merely attaches client code to an already registered entry.</p>
 */
public final class BulletVisualClientRenderers {
    private static final Map<RegistryObject<? extends BulletVisual>, BindingFactory> FACTORIES =
            new LinkedHashMap<>();

    private BulletVisualClientRenderers() {
    }

    public static synchronized <V extends BulletVisual> void register(
            RegistryObject<V> visual,
            BulletVisualRendererFactory<V> factory
    ) {
        RegistryObject<V> requiredVisual = Objects.requireNonNull(visual, "visual");
        BulletVisualRendererFactory<V> requiredFactory = Objects.requireNonNull(factory, "factory");
        if (FACTORIES.putIfAbsent(requiredVisual, context -> bind(requiredFactory, context)) != null) {
            throw new IllegalStateException("Duplicate client renderer binding: " + requiredVisual.getId());
        }
    }

    public static synchronized Dispatcher createDispatcher(EntityRendererProvider.Context context) {
        Map<BulletVisual, BoundRenderer> renderers = new IdentityHashMap<>();
        for (Map.Entry<RegistryObject<? extends BulletVisual>, BindingFactory> entry : FACTORIES.entrySet()) {
            renderers.put(entry.getKey().get(), entry.getValue().create(context));
        }
        return new Dispatcher(renderers);
    }

    public static synchronized boolean hasRegisteredRenderer(BulletVisual visual) {
        for (RegistryObject<? extends BulletVisual> registeredVisual : FACTORIES.keySet()) {
            if (registeredVisual.get() == visual) {
                return true;
            }
        }
        return false;
    }

    private static <V extends BulletVisual> BoundRenderer bind(
            BulletVisualRendererFactory<V> factory,
            EntityRendererProvider.Context context
    ) {
        BulletVisualRenderer<V> renderer = factory.create(context);
        return new BoundRenderer() {
            @Override
            public void render(BulletVisual visual, BulletRenderContext renderContext, PoseStack poseStack,
                               MultiBufferSource buffer) {
                renderer.render(cast(visual), renderContext, poseStack, buffer);
            }

            @Override
            public ResourceLocation textureLocation(BulletVisual visual, TearBullet bullet) {
                return renderer.textureLocation(cast(visual), bullet);
            }

            @SuppressWarnings("unchecked")
            private V cast(BulletVisual visual) {
                return (V) visual;
            }
        };
    }

    @FunctionalInterface
    private interface BindingFactory {
        BoundRenderer create(EntityRendererProvider.Context context);
    }

    public static final class Dispatcher {
        private final Map<BulletVisual, BoundRenderer> renderers;

        private Dispatcher(Map<BulletVisual, BoundRenderer> renderers) {
            this.renderers = Map.copyOf(renderers);
        }

        public boolean hasRenderer(BulletVisual visual) {
            return renderers.containsKey(visual);
        }

        @Nullable
        public BoundRenderer getRenderer(BulletVisual visual) {
            return renderers.get(visual);
        }
    }

    public interface BoundRenderer {
        void render(BulletVisual visual, BulletRenderContext context, PoseStack poseStack,
                    MultiBufferSource buffer);

        ResourceLocation textureLocation(BulletVisual visual, TearBullet bullet);
    }
}
