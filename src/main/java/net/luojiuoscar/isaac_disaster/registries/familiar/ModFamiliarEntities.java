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
            FAMILIAR_ENTITY_REGISTRY.register("mom_knife", () ->
                    new FamiliarEntityType(ModEntities.MOM_KNIFE, 27)); // Three formation layers: 5 + 9 + 13.

    public static final RegistryObject<FamiliarEntityType> BLUE_FLY =
            FAMILIAR_ENTITY_REGISTRY.register("blue_fly", () ->
                    new FamiliarEntityType(ModEntities.BLUE_FLY, 64, ModFamiliarLimitGroups.FLIES, 0, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_PESTILENCE =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_pestilence", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_PESTILENCE, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_FAMINE =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_famine", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_FAMINE, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_DEATH =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_death", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_DEATH, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_CONQUEST =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_conquest", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_CONQUEST, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_WAR =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_war", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_WAR, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> BLUE_SPIDER =
            FAMILIAR_ENTITY_REGISTRY.register("blue_spider", () ->
                    new FamiliarEntityType(ModEntities.BLUE_SPIDER, 64, ModFamiliarLimitGroups.SPIDERS, 0, false));

    private ModFamiliarEntities() {
    }
}
