package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.ParasiteSplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.CricketsBodySplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.CompoundFractureSplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.EnhancedCompoundFractureSplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.HaemolacriaSplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.impl.HaemolacriaBrimstoneSplitModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Forge registry for split behavior definitions. */
public final class ModSplitModules {
    public static final ResourceKey<Registry<SplitModule>> SPLIT_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "split_module"));
    public static final DeferredRegister<SplitModule> SPLIT_MODULE_REGISTRY =
            DeferredRegister.create(SPLIT_MODULE_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<SplitModule> PARASITE =
            SPLIT_MODULE_REGISTRY.register("parasite", ParasiteSplitModule::new);
    public static final RegistryObject<SplitModule> CRICKETS_BODY =
            SPLIT_MODULE_REGISTRY.register("crickets_body", CricketsBodySplitModule::new);
    public static final RegistryObject<SplitModule> COMPOUND_FRACTURE =
            SPLIT_MODULE_REGISTRY.register("compound_fracture", CompoundFractureSplitModule::new);
    public static final RegistryObject<SplitModule> COMPOUND_FRACTURE_ENHANCED =
            SPLIT_MODULE_REGISTRY.register("compound_fracture_enhanced", EnhancedCompoundFractureSplitModule::new);
    public static final RegistryObject<SplitModule> HAEMOLACRIA =
            SPLIT_MODULE_REGISTRY.register("haemolacria", HaemolacriaSplitModule::new);
    public static final RegistryObject<SplitModule> HAEMOLACRIA_BRIMSTONE =
            SPLIT_MODULE_REGISTRY.register("haemolacria_brimstone", HaemolacriaBrimstoneSplitModule::new);

}
