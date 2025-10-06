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

    public static final RegistryObject<Attribute> SCALE =
            MOD_ATTRIBUTE.register("scale",
                    () -> new RangedAttribute("attribute.name.generic.scale", 1.0D, 0.1D, 10.0D)
                            .setSyncable(true));

    public static void register(IEventBus bus) {
        MOD_ATTRIBUTE.register(bus);
    }
}
