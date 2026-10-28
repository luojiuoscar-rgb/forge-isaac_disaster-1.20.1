package net.luojiuoscar.isaac_disaster.registries.ground_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.AquariusPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.LemonPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.PotionPuddleEffect;
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
            GROUND_EFFECT_REGISTRY.register("water", () -> new GroundEffectType<>(WaterPuddleEffect::new));

    public static final RegistryObject<GroundEffectType<LemonPuddleEffect>> LEMON =
            GROUND_EFFECT_REGISTRY.register("lemon", () -> new GroundEffectType<>(LemonPuddleEffect::new));

    public static final RegistryObject<GroundEffectType<PotionPuddleEffect>> POTION =
            GROUND_EFFECT_REGISTRY.register("potion", () -> new GroundEffectType<>(PotionPuddleEffect::new));

    public static final RegistryObject<GroundEffectType<AquariusPuddleEffect>> AQUARIUS =
            GROUND_EFFECT_REGISTRY.register("aquarius", () -> new GroundEffectType<>(AquariusPuddleEffect::new));

    private ModGroundEffects() { }
}
