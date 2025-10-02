package net.luojiuoscar.isaac_disaster.entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;



public class ModEntity {
    public static final DeferredRegister<EntityType<?>> MOD_ENTITY
            = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        MOD_ENTITY.register(eventBus);
    }

    public static final RegistryObject<EntityType<IsaacBomb>> ISAAC_BOMB = MOD_ENTITY.register("isaac_bomb",
            () -> EntityType.Builder.<IsaacBomb>of(IsaacBomb::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f) // 碰撞箱大小，根据你的实体调整
                    .build("isaac_bomb")
    );
}
