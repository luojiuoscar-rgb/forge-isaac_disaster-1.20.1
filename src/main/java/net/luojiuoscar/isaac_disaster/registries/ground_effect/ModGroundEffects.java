package net.luojiuoscar.isaac_disaster.registries.ground_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Ground-effect factories exposed to the mod and addons through a custom Forge registry. */
public final class ModGroundEffects {
    public static final ResourceKey<Registry<GroundEffectType<?>>> GROUND_EFFECT_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "ground_effect"));
    public static final DeferredRegister<GroundEffectType<?>> GROUND_EFFECT_REGISTRY =
            DeferredRegister.create(GROUND_EFFECT_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<GroundEffectType<WaterPuddleEffect>> WATER =
            GROUND_EFFECT_REGISTRY.register(WaterPuddleEffect.TYPE.getPath(), () -> WaterPuddleEffect.DEFINITION);

    private ModGroundEffects() { }
}
