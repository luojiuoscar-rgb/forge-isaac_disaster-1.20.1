package net.luojiuoscar.isaac_disaster.registries.familiar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModFamiliarLimitGroups {
    public static final ResourceKey<Registry<FamiliarLimitGroup>> FAMILIAR_LIMIT_GROUP_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "familiar_limit_group"));
    public static final DeferredRegister<FamiliarLimitGroup> FAMILIAR_LIMIT_GROUP_REGISTRY =
            DeferredRegister.create(FAMILIAR_LIMIT_GROUP_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<FamiliarLimitGroup> FLIES =
            FAMILIAR_LIMIT_GROUP_REGISTRY.register("flies", () -> new FamiliarLimitGroup(64));

    public static final RegistryObject<FamiliarLimitGroup> SPIDERS =
            FAMILIAR_LIMIT_GROUP_REGISTRY.register("spiders", () -> new FamiliarLimitGroup(64));

    public static final RegistryObject<FamiliarLimitGroup> BABIES =
            FAMILIAR_LIMIT_GROUP_REGISTRY.register("babies", () -> new FamiliarLimitGroup(64));

    private ModFamiliarLimitGroups() { }
}
