package net.luojiuoscar.isaac_disaster.registries.visual;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModVisualLayers {
    public static final ResourceKey<Registry<VisualLayer>> VISUAL_LAYER_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "visual_layer"));

    public static final DeferredRegister<VisualLayer> VISUAL_LAYER_REGISTRY =
            DeferredRegister.create(VISUAL_LAYER_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<VisualLayer> GOLDEN = VISUAL_LAYER_REGISTRY.register(
            "golden", () -> new VisualLayer(VisualLayerGroups.BODY_MATERIAL, 20));

    public static final RegistryObject<VisualLayer> PETRIFIED = VISUAL_LAYER_REGISTRY.register(
            "petrified", () -> new VisualLayer(VisualLayerGroups.BODY_MATERIAL, 10));

    public static final RegistryObject<VisualLayer> FROZEN = VISUAL_LAYER_REGISTRY.register(
            "frozen", () -> new VisualLayer(null, 0));

    private ModVisualLayers() {
    }
}
