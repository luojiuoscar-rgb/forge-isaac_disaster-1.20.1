package net.luojiuoscar.isaac_disaster.entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.GravityBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
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

    public static final RegistryObject<EntityType<TearBullet>> TEAR_BULLET =
            MOD_ENTITIES.register("tear_bullet",
                    () -> EntityType.Builder.<TearBullet>of(TearBullet::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f) // 小一点的碰撞箱
                            .clientTrackingRange(64) // 客户端追踪距离
                            .updateInterval(2) // 同步tick
                            .build("tear_bullet")
            );

    public static final RegistryObject<EntityType<GravityBullet>> GRAVITY_BULLET =
            MOD_ENTITIES.register("gravity_bullet",
                    () -> EntityType.Builder.<GravityBullet>of(GravityBullet::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f) // 小一点的碰撞箱
                            .clientTrackingRange(64) // 客户端追踪距离
                            .updateInterval(2) // 同步tick
                            .build("gravity_bullet")
            );

    public static final RegistryObject<EntityType<LemonEffectCloud>> SELECTIVE_EFFECT_CLOUD =
            MOD_ENTITIES.register("selective_effect_cloud",
                    () -> EntityType.Builder.<LemonEffectCloud>of(LemonEffectCloud::new, MobCategory.MISC)
                            .sized(6.0F, 0.5F) // 大概大小
                            .build("selective_effect_cloud"));
}
