package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Forge registry containing the common definitions for all bullet visuals. */
public final class ModBulletVisuals {
    public static final ResourceKey<Registry<BulletVisual>> BULLET_VISUAL_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(IsaacDisaster.MOD_ID, "bullet_visual"));

    public static final DeferredRegister<BulletVisual> BULLET_VISUAL_REGISTRY =
            DeferredRegister.create(BULLET_VISUAL_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<TearBulletVisual> DEFAULT_TEAR = BULLET_VISUAL_REGISTRY.register(
            "default_tear",
            () -> new TearBulletVisual(
                    0.0D,
                    new ResourceLocation(IsaacDisaster.MOD_ID, "textures/particle/tear_bullet.png"),
                    true));

    public static final RegistryObject<TearBulletVisual> COMPOUND_FRACTURE_BONE_TEAR =
            BULLET_VISUAL_REGISTRY.register(
                    "compound_fracture_bone_tear",
                    () -> new TearBulletVisual(
                            1.0D,
                            new ResourceLocation(IsaacDisaster.MOD_ID,
                                    "textures/particle/compound_fracture_bone_tear.png"),
                            false));

    public static final RegistryObject<FetusBulletVisual> DEFAULT_FETUS = BULLET_VISUAL_REGISTRY.register(
            "default_fetus",
            () -> new FetusBulletVisual(0.0D, null, true));

    public static final RegistryObject<FetusBulletVisual> COMPOUND_FRACTURE_FETUS_SKELETON =
            BULLET_VISUAL_REGISTRY.register(
                    "compound_fracture_fetus_skeleton",
                    () -> new FetusBulletVisual(
                            1.0D,
                            new ResourceLocation("minecraft", "textures/entity/skeleton/skeleton.png"),
                            true));

    private ModBulletVisuals() {
    }
}
