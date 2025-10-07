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

    // other
    public static final RegistryObject<SoundEvent> DEFAULT_OBTAIN_ITEM = registerSoundEvent("default_obtain_item");


    // item sounds
    public static final RegistryObject<SoundEvent> YUM_HEART_USE = registerSoundEvent("yum_heart_use");
    public static final RegistryObject<SoundEvent> THE_BOOK_OF_BELIAL_USE = registerSoundEvent("the_book_of_belial_use");
    public static final RegistryObject<SoundEvent> THE_NECRONMICON_USE = registerSoundEvent("the_necronmicon_use");
    public static final RegistryObject<SoundEvent> HOLY_SHIELD_BROKE = registerSoundEvent("holy_shield_broke");

    // pickup sounds
    public static final RegistryObject<SoundEvent> GIGA_BOMB_EXPLOSION = registerSoundEvent("giga_bomb_explosion");
    public static final RegistryObject<SoundEvent> RED_HEART = registerSoundEvent("red_heart");
    public static final RegistryObject<SoundEvent> SOUL_HEART = registerSoundEvent("soul_heart");
    public static final RegistryObject<SoundEvent> BLACK_HEART = registerSoundEvent("black_heart");
    public static final RegistryObject<SoundEvent> BLACK_HEART_ACTIVE = registerSoundEvent("black_heart_active");
    public static final RegistryObject<SoundEvent> ISAAC_HEAD_SHOOT = registerSoundEvent("isaac_head_shoot");


    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name)));
    }


    public static void register(IEventBus event){
        SOUND_EVENTS.register(event);
    }
}
