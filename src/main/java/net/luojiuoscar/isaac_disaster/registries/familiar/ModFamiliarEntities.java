package net.luojiuoscar.isaac_disaster.registries.familiar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.rotten_baby.RottenBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.loki_baby.LokiBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.robo_baby.RoboBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.robo_baby_2.RoboBaby2Entity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.demon_baby.DemonBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby.BrotherBobbyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.sister_maggy.SisterMaggyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.ghost_baby.GhostBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.harlequin_baby.HarlequinBabyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfConquestEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfDeathEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfFamineEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfPestilenceEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfWarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.BlueSpiderEntity;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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

    public static final RegistryObject<FamiliarEntityType> BROTHER_BOBBY =
            FAMILIAR_ENTITY_REGISTRY.register("brother_bobby", () ->
                    new FamiliarEntityType(ModEntities.BROTHER_BOBBY, BrotherBobbyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> SISTER_MAGGY =
            FAMILIAR_ENTITY_REGISTRY.register("sister_maggy", () ->
                    new FamiliarEntityType(ModEntities.SISTER_MAGGY, SisterMaggyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> GHOST_BABY =
            FAMILIAR_ENTITY_REGISTRY.register("ghost_baby", () ->
                    new FamiliarEntityType(ModEntities.GHOST_BABY, GhostBabyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> HARLEQUIN_BABY =
            FAMILIAR_ENTITY_REGISTRY.register("harlequin_baby", () ->
                    new FamiliarEntityType(ModEntities.HARLEQUIN_BABY, HarlequinBabyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> ROTTEN_BABY =
            FAMILIAR_ENTITY_REGISTRY.register("rotten_baby", () ->
                    new FamiliarEntityType(ModEntities.ROTTEN_BABY, RottenBabyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> LOKI_BABY =
            FAMILIAR_ENTITY_REGISTRY.register("loki_baby", () ->
                    new FamiliarEntityType(ModEntities.LOKI_BABY, LokiBabyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> ROBO_BABY =
            FAMILIAR_ENTITY_REGISTRY.register("robo_baby", () ->
                    new FamiliarEntityType(ModEntities.ROBO_BABY, RoboBabyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> ROBO_BABY_2 =
            FAMILIAR_ENTITY_REGISTRY.register("robo_baby_2", () ->
                    new FamiliarEntityType(ModEntities.ROBO_BABY_2, RoboBaby2Entity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> DEMON_BABY =
            FAMILIAR_ENTITY_REGISTRY.register("demon_baby", () ->
                    new FamiliarEntityType(ModEntities.DEMON_BABY, DemonBabyEntity.class, 64, ModFamiliarLimitGroups.BABIES, 0, true));

    public static final RegistryObject<FamiliarEntityType> BLUE_FLY =
            FAMILIAR_ENTITY_REGISTRY.register("blue_fly", () ->
                    new FamiliarEntityType(ModEntities.BLUE_FLY, BlueFlyEntity.class, 64, ModFamiliarLimitGroups.FLIES, 0, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_PESTILENCE =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_pestilence", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_PESTILENCE, LocustOfPestilenceEntity.class, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_FAMINE =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_famine", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_FAMINE, LocustOfFamineEntity.class, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_DEATH =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_death", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_DEATH, LocustOfDeathEntity.class, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_CONQUEST =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_conquest", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_CONQUEST, LocustOfConquestEntity.class, 64, ModFamiliarLimitGroups.FLIES, 1, false) {
                        @Override
                        protected int getAdditionCount(RandomSource random) {
                            return random.nextInt(4) + 1;
                        }
                    });

    public static final RegistryObject<FamiliarEntityType> LOCUST_OF_WAR =
            FAMILIAR_ENTITY_REGISTRY.register("locust_of_war", () ->
                    new FamiliarEntityType(ModEntities.LOCUST_OF_WAR, LocustOfWarEntity.class, 64, ModFamiliarLimitGroups.FLIES, 1, false));

    public static final RegistryObject<FamiliarEntityType> BLUE_SPIDER =
            FAMILIAR_ENTITY_REGISTRY.register("blue_spider", () ->
                    new FamiliarEntityType(ModEntities.BLUE_SPIDER, BlueSpiderEntity.class, 64, ModFamiliarLimitGroups.SPIDERS, 0, false));

    private ModFamiliarEntities() {
    }
}
