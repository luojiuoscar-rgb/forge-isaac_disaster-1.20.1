package net.luojiuoscar.isaac_disaster.entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.BlueSpiderEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfConquestEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfDeathEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfFamineEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfPestilenceEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfWarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby.BrotherBobbyEntity;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;



public class ModEntities {
    public static final DeferredRegister<EntityType<?>> MOD_ENTITIES
            = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        MOD_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<IsaacBomb>> ISAAC_BOMB = MOD_ENTITIES.register("isaac_bomb",
            () -> EntityType.Builder.<IsaacBomb>of(IsaacBomb::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f) // 碰撞箱大小
                    .build("isaac_bomb")
    );

    public static final RegistryObject<EntityType<IsaacBomb>> GIGA_BOMB = MOD_ENTITIES.register("giga_bomb",
            () -> EntityType.Builder.<IsaacBomb>of(GigaBomb::new, MobCategory.MISC)
                    .sized(2.5f, 2.5f) // 碰撞箱大小
                    .build("giga_bomb")
    );

    public static final RegistryObject<EntityType<LemonEffectCloud>> SELECTIVE_EFFECT_CLOUD =
            MOD_ENTITIES.register("selective_effect_cloud",
                    () -> EntityType.Builder.<LemonEffectCloud>of(LemonEffectCloud::new, MobCategory.MISC)
                            .sized(6.0F, 0.5F) // 大概大小
                            .build("selective_effect_cloud"));

    public static final RegistryObject<EntityType<MomKnifeEntity>> MOM_KNIFE =
            MOD_ENTITIES.register("mom_knife",
                    () -> EntityType.Builder.<MomKnifeEntity>of(MomKnifeEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("mom_knife"));

    public static final RegistryObject<EntityType<BrotherBobbyEntity>> BROTHER_BOBBY =
            MOD_ENTITIES.register("brother_bobby",
                    () -> EntityType.Builder.<BrotherBobbyEntity>of(BrotherBobbyEntity::new, MobCategory.MISC)
                            .sized(0.875F * BrotherBobbyEntity.BASE_SCALE, 1.4375F * BrotherBobbyEntity.BASE_SCALE)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("brother_bobby"));

    public static final RegistryObject<EntityType<BlueFlyEntity>> BLUE_FLY =
            MOD_ENTITIES.register("blue_fly",
                    () -> EntityType.Builder.<BlueFlyEntity>of(BlueFlyEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("blue_fly"));

    public static final RegistryObject<EntityType<LocustOfPestilenceEntity>> LOCUST_OF_PESTILENCE =
            MOD_ENTITIES.register("locust_of_pestilence",
                    () -> EntityType.Builder.<LocustOfPestilenceEntity>of(LocustOfPestilenceEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_pestilence"));

    public static final RegistryObject<EntityType<LocustOfFamineEntity>> LOCUST_OF_FAMINE =
            MOD_ENTITIES.register("locust_of_famine",
                    () -> EntityType.Builder.<LocustOfFamineEntity>of(LocustOfFamineEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_famine"));

    public static final RegistryObject<EntityType<LocustOfDeathEntity>> LOCUST_OF_DEATH =
            MOD_ENTITIES.register("locust_of_death",
                    () -> EntityType.Builder.<LocustOfDeathEntity>of(LocustOfDeathEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_death"));

    public static final RegistryObject<EntityType<LocustOfConquestEntity>> LOCUST_OF_CONQUEST =
            MOD_ENTITIES.register("locust_of_conquest",
                    () -> EntityType.Builder.<LocustOfConquestEntity>of(LocustOfConquestEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_conquest"));

    public static final RegistryObject<EntityType<LocustOfWarEntity>> LOCUST_OF_WAR =
            MOD_ENTITIES.register("locust_of_war",
                    () -> EntityType.Builder.<LocustOfWarEntity>of(LocustOfWarEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.125F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("locust_of_war"));

    public static final RegistryObject<EntityType<BlueSpiderEntity>> BLUE_SPIDER =
            MOD_ENTITIES.register("blue_spider",
                    () -> EntityType.Builder.<BlueSpiderEntity>of(BlueSpiderEntity::new, MobCategory.MISC)
                            .sized(0.125F, 0.15F)
                            .clientTrackingRange(3)
                            .updateInterval(1)
                            .build("blue_spider"));
}
