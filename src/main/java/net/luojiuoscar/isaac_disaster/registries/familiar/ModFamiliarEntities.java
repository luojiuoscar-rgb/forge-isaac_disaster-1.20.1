package net.luojiuoscar.isaac_disaster.registries.familiar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Built-in familiar descriptors exposed through the addon-facing familiar registry.
 */
public final class ModFamiliarEntities {
    public static final ResourceKey<Registry<FamiliarEntityType>> FAMILIAR_ENTITY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "familiar_entity"));

    public static final DeferredRegister<FamiliarEntityType> FAMILIAR_ENTITY_REGISTRY =
            DeferredRegister.create(FAMILIAR_ENTITY_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<FamiliarEntityType> MOM_KNIFE =
            FAMILIAR_ENTITY_REGISTRY.register("mom_knife", () -> new FamiliarEntityType(ModEntities.MOM_KNIFE));

    private ModFamiliarEntities() {
    }
}
