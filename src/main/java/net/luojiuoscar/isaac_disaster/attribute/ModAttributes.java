package net.luojiuoscar.isaac_disaster.attribute;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.config.IsaacConfigDefaults;
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
            () -> new RangedAttribute("attribute.name.generic.scale", IsaacConfigDefaults.DEFAULT_SCALE, 0.2D, 8.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_SPEED = MOD_ATTRIBUTE.register("bullet_speed",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_speed", IsaacConfigDefaults.DEFAULT_BULLET_SPEED, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_KNOCKBACK = MOD_ATTRIBUTE.register("bullet_knockback",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_knockback", IsaacConfigDefaults.DEFAULT_BULLET_KNOCKBACK, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_RANGE = MOD_ATTRIBUTE.register("bullet_range",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_range", IsaacConfigDefaults.DEFAULT_BULLET_RANGE, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> TEARS = MOD_ATTRIBUTE.register("tears",
            () -> new RangedAttribute("attribute.isaac_disaster.tears", IsaacConfigDefaults.DEFAULT_TEARS, -1024.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> FIRE_RATE = MOD_ATTRIBUTE.register("fire_rate",
            () -> new RangedAttribute("attribute.isaac_disaster.fire_rate", IsaacConfigDefaults.DEFAULT_FIRE_RATE_ADDITION, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BLOCK_BREAKING_SPEED = MOD_ATTRIBUTE.register("block_breaking_speed",
            () -> new RangedAttribute("attribute.isaac_disaster.block_breaking_speed", IsaacConfigDefaults.DEFAULT_BLOCK_BREAKING_SPEED, -1024.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_SCALE = MOD_ATTRIBUTE.register("bullet_scale",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_scale", IsaacConfigDefaults.DEFAULT_BULLET_SCALE, -1024.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_COUNT = MOD_ATTRIBUTE.register("bullet_count",
            () -> new RangedAttribute("attribute.isaac_disaster.bullet_count", IsaacConfigDefaults.DEFAULT_BULLET_COUNT, 1.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> COLLISION_DAMAGE = MOD_ATTRIBUTE.register("collision_damage",
            () -> new RangedAttribute("attribute.isaac_disaster.collision_damage", 0.0D, 0.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> PILL_QUALITY = MOD_ATTRIBUTE.register("pill_quality",
            () -> new RangedAttribute("attribute.isaac_disaster.pill_quality", IsaacConfigDefaults.DEFAULT_PILL_QUALITY, -1024.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> TRINKET_SLOTS = MOD_ATTRIBUTE.register("trinket_slots",
            () -> new RangedAttribute("attribute.isaac_disaster.trinket_slots", IsaacConfigDefaults.DEFAULT_TRINKET_SLOTS, 0.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> PASSIVE_ITEM_SLOTS = MOD_ATTRIBUTE.register("passive_item_slots",
            () -> new RangedAttribute("attribute.isaac_disaster.passive_item_slots", IsaacConfigDefaults.DEFAULT_PASSIVE_ITEM_SLOTS, 0.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> FLY_TIME = MOD_ATTRIBUTE.register("fly_time",
            () -> new RangedAttribute("attribute.isaac_disaster.fly_time", IsaacConfigDefaults.DEFAULT_FLY_TIME, 0.0D, 1024D).setSyncable(true));

    public static final RegistryObject<Attribute> DEVIL_CHANCE = MOD_ATTRIBUTE.register("devil_chance",
            () -> new RangedAttribute("attribute.isaac_disaster.devil_chance", 0.0D, 0.0D, 1D).setSyncable(true));
    public static final RegistryObject<Attribute> ANGEL_CHANCE = MOD_ATTRIBUTE.register("angel_chance",
            () -> new RangedAttribute("attribute.isaac_disaster.angel_chance", 0.0D, 0.0D, 1D).setSyncable(true));
    public static final RegistryObject<Attribute> PLANETARIUM_CHANCE = MOD_ATTRIBUTE.register("planetarium_chance",
            () -> new RangedAttribute("attribute.isaac_disaster.planetarium_chance", 0.0D, 0.0D, 1D).setSyncable(true));
}
