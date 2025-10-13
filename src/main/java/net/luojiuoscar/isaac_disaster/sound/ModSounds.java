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
    public static final RegistryObject<SoundEvent> STEVE_HURT_OLD = registerSoundEvent("steve_hurt_old");


    // item sounds
    public static final RegistryObject<SoundEvent> YUM_HEART_USE = registerSoundEvent("yum_heart_use");
    public static final RegistryObject<SoundEvent> THE_BOOK_OF_BELIAL_USE = registerSoundEvent("the_book_of_belial_use");
    public static final RegistryObject<SoundEvent> THE_NECRONMICON_USE = registerSoundEvent("the_necronmicon_use");
    public static final RegistryObject<SoundEvent> HOLY_SHIELD_BROKE = registerSoundEvent("holy_shield_broke");
    public static final RegistryObject<SoundEvent> LEMON_MISHAP_USE = registerSoundEvent("lemon_mishap_use");
    public static final RegistryObject<SoundEvent> BATTERY = registerSoundEvent("battery");
    public static final RegistryObject<SoundEvent> BATTERY_SMALL = registerSoundEvent("battery_small");

    // pickup sounds
    public static final RegistryObject<SoundEvent> GIGA_BOMB_EXPLOSION = registerSoundEvent("giga_bomb_explosion");
    public static final RegistryObject<SoundEvent> RED_HEART = registerSoundEvent("red_heart");
    public static final RegistryObject<SoundEvent> SOUL_HEART = registerSoundEvent("soul_heart");
    public static final RegistryObject<SoundEvent> BLACK_HEART = registerSoundEvent("black_heart");
    public static final RegistryObject<SoundEvent> BLACK_HEART_ACTIVE = registerSoundEvent("black_heart_active");
    public static final RegistryObject<SoundEvent> ISAAC_HEAD_SHOOT = registerSoundEvent("isaac_head_shoot");
    public static final RegistryObject<SoundEvent> FART_NORMAL = registerSoundEvent("fart_normal");
    public static final RegistryObject<SoundEvent> FART_HUGE = registerSoundEvent("fart_huge");

    // pills
    public static final RegistryObject<SoundEvent> I_FOUND_PILLS_USE = registerSoundEvent("i_found_pills_use");
    public static final RegistryObject<SoundEvent> PILL_USE_GOOD = registerSoundEvent("pill_use_good");
    public static final RegistryObject<SoundEvent> PILL_USE_GOOD_H = registerSoundEvent("pill_use_good_h");
    public static final RegistryObject<SoundEvent> PILL_USE_GOOD_WITH_EFFECT = registerSoundEvent("pill_use_good_with_effect");
    public static final RegistryObject<SoundEvent> PILL_USE_BAD = registerSoundEvent("pill_use_bad");
    public static final RegistryObject<SoundEvent> PILL_USE_BAD_H = registerSoundEvent("pill_use_bad_h");
    public static final RegistryObject<SoundEvent> BALLS_OF_STEEL = registerSoundEvent("balls_of_steel");
    public static final RegistryObject<SoundEvent> BALLS_OF_STEEL_H = registerSoundEvent("balls_of_steel_h");
    public static final RegistryObject<SoundEvent> I_FOUND_PILLS = registerSoundEvent("i_found_pills");
    public static final RegistryObject<SoundEvent> I_FOUND_PILLS_H = registerSoundEvent("i_found_pills_h");
    public static final RegistryObject<SoundEvent> FULL_HEALTH = registerSoundEvent("full_health");
    public static final RegistryObject<SoundEvent> FULL_HEALTH_H = registerSoundEvent("full_health_h");
    public static final RegistryObject<SoundEvent> HEALTH_DOWN = registerSoundEvent("health_down");
    public static final RegistryObject<SoundEvent> HEALTH_DOWN_H = registerSoundEvent("health_down_h");
    public static final RegistryObject<SoundEvent> HEALTH_UP = registerSoundEvent("health_up");
    public static final RegistryObject<SoundEvent> HEALTH_UP_H = registerSoundEvent("health_up_h");
    public static final RegistryObject<SoundEvent> BAD_GAS = registerSoundEvent("bad_gas");
    public static final RegistryObject<SoundEvent> BAD_GAS_H = registerSoundEvent("bad_gas_h");
    public static final RegistryObject<SoundEvent> BAD_TRIP = registerSoundEvent("bad_trip");
    public static final RegistryObject<SoundEvent> BAD_TRIP_H = registerSoundEvent("bad_trip_h");
    public static final RegistryObject<SoundEvent> EXPLOSIVE_DIARRHEA = registerSoundEvent("explosive_diarrhea");
    public static final RegistryObject<SoundEvent> EXPLOSIVE_DIARRHEA_H = registerSoundEvent("explosive_diarrhea_h");
    public static final RegistryObject<SoundEvent> PUBERTY = registerSoundEvent("puberty");
    public static final RegistryObject<SoundEvent> PUBERTY_H = registerSoundEvent("puberty_h");
    public static final RegistryObject<SoundEvent> RANGE_UP = registerSoundEvent("range_up");
    public static final RegistryObject<SoundEvent> RANGE_UP_H = registerSoundEvent("range_up_h");
    public static final RegistryObject<SoundEvent> RANGE_DOWN = registerSoundEvent("range_down");
    public static final RegistryObject<SoundEvent> RANGE_DOWN_H = registerSoundEvent("range_down_h");
    public static final RegistryObject<SoundEvent> SPEED_UP = registerSoundEvent("speed_up");
    public static final RegistryObject<SoundEvent> SPEED_UP_H = registerSoundEvent("speed_up_h");
    public static final RegistryObject<SoundEvent> SPEED_DOWN = registerSoundEvent("speed_down");
    public static final RegistryObject<SoundEvent> SPEED_DOWN_H = registerSoundEvent("speed_down_h");
    public static final RegistryObject<SoundEvent> TEARS_UP = registerSoundEvent("tears_up");
    public static final RegistryObject<SoundEvent> TEARS_UP_H = registerSoundEvent("tears_up_h");
    public static final RegistryObject<SoundEvent> TEARS_DOWN = registerSoundEvent("tears_down");
    public static final RegistryObject<SoundEvent> TEARS_DOWN_H = registerSoundEvent("tears_down_h");
    public static final RegistryObject<SoundEvent> LUCK_UP = registerSoundEvent("luck_up");
    public static final RegistryObject<SoundEvent> LUCK_UP_H = registerSoundEvent("luck_up_h");
    public static final RegistryObject<SoundEvent> LUCK_DOWN = registerSoundEvent("luck_down");
    public static final RegistryObject<SoundEvent> LUCK_DOWN_H = registerSoundEvent("luck_down_h");
    public static final RegistryObject<SoundEvent> TELEPILLS = registerSoundEvent("telepills");
    public static final RegistryObject<SoundEvent> TELEPILLS_H = registerSoundEvent("telepills_h");
    public static final RegistryObject<SoundEvent> HEMATEMESIS = registerSoundEvent("hematemesis");
    public static final RegistryObject<SoundEvent> HEMATEMESIS_H = registerSoundEvent("hematemesis_h");
    public static final RegistryObject<SoundEvent> PARALYSIS = registerSoundEvent("paralysis");
    public static final RegistryObject<SoundEvent> PARALYSIS_H = registerSoundEvent("paralysis_h");
    public static final RegistryObject<SoundEvent> I_CAN_SEE_FOREVER = registerSoundEvent("i_can_see_forever");
    public static final RegistryObject<SoundEvent> I_CAN_SEE_FOREVER_H = registerSoundEvent("i_can_see_forever_h");
    public static final RegistryObject<SoundEvent> PHEROMONES = registerSoundEvent("pheromones");
    public static final RegistryObject<SoundEvent> PHEROMONES_H = registerSoundEvent("pheromones_h");
    public static final RegistryObject<SoundEvent> AMNESIA = registerSoundEvent("amnesia");
    public static final RegistryObject<SoundEvent> AMNESIA_H = registerSoundEvent("amnesia_h");
    public static final RegistryObject<SoundEvent> LEMON_PARTY = registerSoundEvent("lemon_party");
    public static final RegistryObject<SoundEvent> LEMON_PARTY_H = registerSoundEvent("lemon_party_h");
    public static final RegistryObject<SoundEvent> PERCS = registerSoundEvent("percs");
    public static final RegistryObject<SoundEvent> PERCS_H = registerSoundEvent("percs_h");
    public static final RegistryObject<SoundEvent> ADDICTED = registerSoundEvent("addicted");
    public static final RegistryObject<SoundEvent> ADDICTED_H = registerSoundEvent("addicted_h");
    public static final RegistryObject<SoundEvent> ONE_MAKES_YOU_LARGER = registerSoundEvent("one_makes_you_larger");
    public static final RegistryObject<SoundEvent> ONE_MAKES_YOU_LARGER_H = registerSoundEvent("one_makes_you_larger_h");
    public static final RegistryObject<SoundEvent> ONE_MAKES_YOU_SMALL = registerSoundEvent("one_makes_you_small");
    public static final RegistryObject<SoundEvent> ONE_MAKES_YOU_SMALL_H = registerSoundEvent("one_makes_you_small_h");
    public static final RegistryObject<SoundEvent> POWER_PILL = registerSoundEvent("power_pill");
    public static final RegistryObject<SoundEvent> POWER_PILL_H = registerSoundEvent("power_pill_h");
    public static final RegistryObject<SoundEvent> RETRO_VISION = registerSoundEvent("retro_vision");
    public static final RegistryObject<SoundEvent> RETRO_VISION_H = registerSoundEvent("retro_vision_h");
    public static final RegistryObject<SoundEvent> FRIENDS_TILL_THE_END = registerSoundEvent("friends_till_the_end");
    public static final RegistryObject<SoundEvent> FRIENDS_TILL_THE_END_H = registerSoundEvent("friends_till_the_end_h");
    public static final RegistryObject<SoundEvent> SOMETHINGS_WRONG = registerSoundEvent("somethings_wrong");
    public static final RegistryObject<SoundEvent> SOMETHINGS_WRONG_H = registerSoundEvent("somethings_wrong_h");
    public static final RegistryObject<SoundEvent> IM_DROWSY = registerSoundEvent("im_drowsy");
    public static final RegistryObject<SoundEvent> IM_DROWSY_H = registerSoundEvent("im_drowsy_h");
    public static final RegistryObject<SoundEvent> IM_EXCITED = registerSoundEvent("im_excited");
    public static final RegistryObject<SoundEvent> IM_EXCITED_H = registerSoundEvent("im_excited_h");
    public static final RegistryObject<SoundEvent> FEELS_LIKE_IM_WALKING_ON_SUNSHINE = registerSoundEvent("feels_like_im_walking_on_sunshine");
    public static final RegistryObject<SoundEvent> FEELS_LIKE_IM_WALKING_ON_SUNSHINE_H = registerSoundEvent("feels_like_im_walking_on_sunshine_h");
    public static final RegistryObject<SoundEvent> SHOT_SPEED_DOWN = registerSoundEvent("shot_speed_down");
    public static final RegistryObject<SoundEvent> SHOT_SPEED_DOWN_H = registerSoundEvent("shot_speed_down_h");
    public static final RegistryObject<SoundEvent> SHOT_SPEED_UP = registerSoundEvent("shot_speed_up");
    public static final RegistryObject<SoundEvent> SHOT_SPEED_UP_H = registerSoundEvent("shot_speed_up_h");
    public static final RegistryObject<SoundEvent> EXPERIMENTAL_PILL = registerSoundEvent("experimental_pill");
    public static final RegistryObject<SoundEvent> EXPERIMENTAL_PILL_H = registerSoundEvent("experimental_pill_h");










    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name)));
    }


    public static void register(IEventBus event){
        SOUND_EVENTS.register(event);
    }
}
