package net.luojiuoscar.isaac_disaster.attribute;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModAttributes {
    public static final DeferredRegister<Attribute> MOD_ATTRIBUTE =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, IsaacDisaster.MOD_ID);

    public static void register(IEventBus bus) {
        MOD_ATTRIBUTE.register(bus);
    }
    public static final RegistryObject<Attribute> SCALE = MOD_ATTRIBUTE.register("scale",
            () -> new RangedAttribute("attribute.name.generic.scale", 1.0D, 0.1D, 10.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_SPEED = MOD_ATTRIBUTE.register("bullet_speed",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_speed", 1.0D, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_RANGE = MOD_ATTRIBUTE.register("bullet_range",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_range", 18.0D, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> TEARS = MOD_ATTRIBUTE.register("tears",
            () -> new RangedAttribute("attribute.isaac_disaster.tears", 0.0D, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> TEARS_CORRECTION = MOD_ATTRIBUTE.register("tears_correction",
            () -> new RangedAttribute("attribute.isaac_disaster.tears_correction", 0.0D, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_COLOR = MOD_ATTRIBUTE.register("bullet_color",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_color", 16777215D, 0.0D, 16777215D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_ALPHA = MOD_ATTRIBUTE.register("bullet_alpha",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_alpha", 1.0D, 0.1D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_FILTER = MOD_ATTRIBUTE.register("bullet_filter",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_filter", 16777215D, 0.0D, 16777215D).setSyncable(true));

    public static final RegistryObject<Attribute> BLOCK_BREAKING_SPEED = MOD_ATTRIBUTE.register("block_breaking_speed",
            () -> new RangedAttribute("attribute.isaac_disaster.block_breaking_speed", 0.0D, -1024.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_SCALE = MOD_ATTRIBUTE.register("bullet_scale",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_scale", 0.0D, -1024.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_COUNT = MOD_ATTRIBUTE.register("bullet_count",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_count", 1.0D, 1.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> COLLISION_DAMAGE = MOD_ATTRIBUTE.register("collision_damage",
            () -> new RangedAttribute("attribute.isaac_disaster.collision_damage", 0.0D, 0.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> PILL_QUALITY = MOD_ATTRIBUTE.register("pill_quality",
            () -> new RangedAttribute("attribute.isaac_disaster.pill_quality", 0.0D, -1024.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> FLY_TIME = MOD_ATTRIBUTE.register("fly_time",
            () -> new RangedAttribute("attribute.isaac_disaster.fly_time", 0.0D, 0.0D, 1024D).setSyncable(true));











}
