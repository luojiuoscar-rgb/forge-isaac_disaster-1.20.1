package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/** Forge registry for split behavior definitions. */
public final class ModSplitModules {
    public static final ResourceKey<Registry<SplitModule>> SPLIT_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "split_module"));
    public static final DeferredRegister<SplitModule> SPLIT_MODULE_REGISTRY =
            DeferredRegister.create(SPLIT_MODULE_KEY, IsaacDisaster.MOD_ID);


}
