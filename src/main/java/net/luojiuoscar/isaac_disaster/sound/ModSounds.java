package net.luojiuoscar.isaac_disaster.sound;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IsaacDisaster.MOD_ID);

    public static final RegistryObject<SoundEvent> DEFAULT_OBTAIN_ITEM = registerSoundEvent("default_obtain_item");




    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name)));
    }


    public static void register(IEventBus event){
        SOUND_EVENTS.register(event);
    }
}
