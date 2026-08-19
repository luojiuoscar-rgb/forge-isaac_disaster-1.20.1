package net.luojiuoscar.isaac_disaster.registries.revive_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.revive_module.impl.InnerChild;
import net.luojiuoscar.isaac_disaster.registries.revive_module.impl.SimpleRevive;
import net.luojiuoscar.isaac_disaster.registries.revive_module.impl.TotemOfUndying;
import net.luojiuoscar.isaac_disaster.registries.revive_module.impl.OneUp;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModReviveModule {
    public static final ResourceKey<Registry<ReviveModule>> REVIVE_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "revive_module"));

    public static final DeferredRegister<ReviveModule> REVIVE_MODULE_REGISTRY =
            DeferredRegister.create(REVIVE_MODULE_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<ReviveModule> TOTEM_OF_UNDYING =
            REVIVE_MODULE_REGISTRY.register("totem_of_undying", TotemOfUndying::new);
    public static final RegistryObject<ReviveModule> SIMPLE_REVIVE =
            REVIVE_MODULE_REGISTRY.register("simple_revive", SimpleRevive::new);
    public static final RegistryObject<ReviveModule> ONE_UP =
            REVIVE_MODULE_REGISTRY.register("one_up", OneUp::new);
    public static final RegistryObject<ReviveModule> INNER_CHILD =
            REVIVE_MODULE_REGISTRY.register("inner_child", InnerChild::new);

}
