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

    // attack
    public static final RegistryObject<SoundEvent> TEAR_BULLET_SHOT = registerSoundEvent("tear_bullet_shot");
    public static final RegistryObject<SoundEvent> LASER_SHOT = registerSoundEvent("laser_shot");
    public static final RegistryObject<SoundEvent> BRIMSTONE_SHOT_NORMAL = registerSoundEvent("brimstone_shot.normal");
    public static final RegistryObject<SoundEvent> BRIMSTONE_SHOT_HUGE = registerSoundEvent("brimstone_shot.huge");
    public static final RegistryObject<SoundEvent> C_SECTION_SHOT = registerSoundEvent("c_section_shot");


    // other
    public static final RegistryObject<SoundEvent> DEFAULT_OBTAIN_ITEM = registerSoundEvent("default_obtain_item");
    public static final RegistryObject<SoundEvent> STEVE_HURT_OLD = registerSoundEvent("steve_hurt_old");
    public static final RegistryObject<SoundEvent> BATTERY = registerSoundEvent("battery");
    public static final RegistryObject<SoundEvent> BATTERY_SMALL = registerSoundEvent("battery_small");

    // item sounds
    public static final RegistryObject<SoundEvent> YUM_HEART_USE = registerSoundEvent("yum_heart_use");
    public static final RegistryObject<SoundEvent> THE_BOOK_OF_BELIAL_USE = registerSoundEvent("the_book_of_belial_use");
    public static final RegistryObject<SoundEvent> THE_NECRONMICON_USE = registerSoundEvent("the_necronmicon_use");
    public static final RegistryObject<SoundEvent> HOLY_SHIELD_BROKE = registerSoundEvent("holy_shield_broke");
    public static final RegistryObject<SoundEvent> LEMON_MISHAP_USE = registerSoundEvent("lemon_mishap_use");
    public static final RegistryObject<SoundEvent> MAGIC_MUSHROOM_OBTAIN = registerSoundEvent("magic_mushroom_obtain");


    // pickup sounds
    public static final RegistryObject<SoundEvent> GIGA_BOMB_EXPLOSION = registerSoundEvent("giga_bomb_explosion");
    public static final RegistryObject<SoundEvent> RED_HEART = registerSoundEvent("red_heart");
    public static final RegistryObject<SoundEvent> SOUL_HEART = registerSoundEvent("soul_heart");
    public static final RegistryObject<SoundEvent> BLACK_HEART = registerSoundEvent("black_heart");
    public static final RegistryObject<SoundEvent> BLACK_HEART_ACTIVE = registerSoundEvent("black_heart_active");
    public static final RegistryObject<SoundEvent> FART_NORMAL = registerSoundEvent("fart_normal");
    public static final RegistryObject<SoundEvent> FART_HUGE = registerSoundEvent("fart_huge");
    public static final RegistryObject<SoundEvent> BONE_HEART = registerSoundEvent("bone_heart");
    public static final RegistryObject<SoundEvent> UNLOCK = registerSoundEvent("unlock");
    public static final RegistryObject<SoundEvent> ETERNAL_HEART = registerSoundEvent("eternal_heart");
    public static final RegistryObject<SoundEvent> GOLDEN_HEART = registerSoundEvent("golden_heart");

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
    public static final RegistryObject<SoundEvent> GULP = registerSoundEvent("gulp");
    public static final RegistryObject<SoundEvent> GULP_H = registerSoundEvent("gulp_h");
    public static final RegistryObject<SoundEvent> VURP = registerSoundEvent("vurp");
    public static final RegistryObject<SoundEvent> VURP_H = registerSoundEvent("vurp_h");
    public static final RegistryObject<SoundEvent> RUA_WIZARD = registerSoundEvent("are_you_a_wizard");
    public static final RegistryObject<SoundEvent> RUA_WIZARD_H = registerSoundEvent("are_you_a_wizard_h");
    public static final RegistryObject<SoundEvent> ENERGY_48 = registerSoundEvent("48_hour_energy");
    public static final RegistryObject<SoundEvent> ENERGY_48_H = registerSoundEvent("48_hour_energy_h");
    public static final RegistryObject<SoundEvent> QUESTION_PILL = registerSoundEvent("question_marks");
    public static final RegistryObject<SoundEvent> QUESTION_PILL_H = registerSoundEvent("question_marks_h");
    public static final RegistryObject<SoundEvent> BOMBS_ARE_KEY = registerSoundEvent("bombs_are_key");
    public static final RegistryObject<SoundEvent> BOMBS_ARE_KEY_H = registerSoundEvent("bombs_are_key_h");





    // cards
    public static final RegistryObject<SoundEvent> THE_FOOL = registerSoundEvent("the_fool");
    public static final RegistryObject<SoundEvent> THE_FOOL_R = registerSoundEvent("the_fool_r");
    public static final RegistryObject<SoundEvent> THE_MAGICIAN = registerSoundEvent("the_magician");
    public static final RegistryObject<SoundEvent> THE_MAGICIAN_R = registerSoundEvent("the_magician_r");
    public static final RegistryObject<SoundEvent> THE_HIGH_PRIESTESS = registerSoundEvent("the_high_priestess");
    public static final RegistryObject<SoundEvent> THE_HIGH_PRIESTESS_R = registerSoundEvent("the_high_priestess_r");
    public static final RegistryObject<SoundEvent> THE_EMPRESS = registerSoundEvent("the_empress");
    public static final RegistryObject<SoundEvent> THE_EMPRESS_R = registerSoundEvent("the_empress_r");
    public static final RegistryObject<SoundEvent> THE_EMPEROR = registerSoundEvent("the_emperor");
    public static final RegistryObject<SoundEvent> THE_EMPEROR_R = registerSoundEvent("the_emperor_r");
    public static final RegistryObject<SoundEvent> THE_HIEROPHANT = registerSoundEvent("the_hierophant");
    public static final RegistryObject<SoundEvent> THE_HIEROPHANT_R = registerSoundEvent("the_hierophant_r");
    public static final RegistryObject<SoundEvent> THE_LOVERS = registerSoundEvent("the_lovers");
    public static final RegistryObject<SoundEvent> THE_LOVERS_R = registerSoundEvent("the_lovers_r");
    public static final RegistryObject<SoundEvent> THE_CHARIOT = registerSoundEvent("the_chariot");
    public static final RegistryObject<SoundEvent> THE_CHARIOT_R = registerSoundEvent("the_chariot_r");
    public static final RegistryObject<SoundEvent> JUSTICE = registerSoundEvent("justice");
    public static final RegistryObject<SoundEvent> JUSTICE_R = registerSoundEvent("justice_r");
    public static final RegistryObject<SoundEvent> THE_HERMIT = registerSoundEvent("the_hermit");
    public static final RegistryObject<SoundEvent> THE_HERMIT_R = registerSoundEvent("the_hermit_r");
    public static final RegistryObject<SoundEvent> WHEEL_OF_FORTUNE = registerSoundEvent("wheel_of_fortune");
    public static final RegistryObject<SoundEvent> WHEEL_OF_FORTUNE_R = registerSoundEvent("wheel_of_fortune_r");
    public static final RegistryObject<SoundEvent> STRENGTH = registerSoundEvent("strength");
    public static final RegistryObject<SoundEvent> STRENGTH_R = registerSoundEvent("strength_r");
    public static final RegistryObject<SoundEvent> THE_HANGED_MAN = registerSoundEvent("the_hanged_man");
    public static final RegistryObject<SoundEvent> THE_HANGED_MAN_R = registerSoundEvent("the_hanged_man_r");
    public static final RegistryObject<SoundEvent> DEATH = registerSoundEvent("death");
    public static final RegistryObject<SoundEvent> DEATH_R = registerSoundEvent("death_r");
    public static final RegistryObject<SoundEvent> TEMPERANCE = registerSoundEvent("temperance");
    public static final RegistryObject<SoundEvent> TEMPERANCE_R = registerSoundEvent("temperance_r");
    public static final RegistryObject<SoundEvent> THE_DEVIL = registerSoundEvent("the_devil");
    public static final RegistryObject<SoundEvent> THE_DEVIL_R = registerSoundEvent("the_devil_r");
    public static final RegistryObject<SoundEvent> THE_TOWER = registerSoundEvent("the_tower");
    public static final RegistryObject<SoundEvent> THE_TOWER_R = registerSoundEvent("the_tower_r");
    public static final RegistryObject<SoundEvent> THE_STARS = registerSoundEvent("the_stars");
    public static final RegistryObject<SoundEvent> THE_STARS_R = registerSoundEvent("the_stars_r");
    public static final RegistryObject<SoundEvent> THE_MOON = registerSoundEvent("the_moon");
    public static final RegistryObject<SoundEvent> THE_MOON_R = registerSoundEvent("the_moon_r");
    public static final RegistryObject<SoundEvent> THE_SUN = registerSoundEvent("the_sun");
    public static final RegistryObject<SoundEvent> THE_SUN_R = registerSoundEvent("the_sun_r");
    public static final RegistryObject<SoundEvent> JUDGEMENT = registerSoundEvent("judgement");
    public static final RegistryObject<SoundEvent> JUDGEMENT_R = registerSoundEvent("judgement_r");
    public static final RegistryObject<SoundEvent> THE_WORLD = registerSoundEvent("the_world");
    public static final RegistryObject<SoundEvent> THE_WORLD_R = registerSoundEvent("the_world_r");
    public static final RegistryObject<SoundEvent> TWO_OF_CLUBS = registerSoundEvent("2_of_clubs");
    public static final RegistryObject<SoundEvent> TWO_OF_DIAMONDS = registerSoundEvent("2_of_diamonds");
    public static final RegistryObject<SoundEvent> TWO_OF_SPADES = registerSoundEvent("2_of_spades");
    public static final RegistryObject<SoundEvent> TWO_OF_HEARTS = registerSoundEvent("2_of_hearts");
    public static final RegistryObject<SoundEvent> ACE_OF_CLUBS = registerSoundEvent("ace_of_clubs");
    public static final RegistryObject<SoundEvent> ACE_OF_DIAMONDS = registerSoundEvent("ace_of_diamonds");
    public static final RegistryObject<SoundEvent> ACE_OF_SPADES = registerSoundEvent("ace_of_spades");
    public static final RegistryObject<SoundEvent> ACE_OF_HEARTS = registerSoundEvent("ace_of_hearts");
    public static final RegistryObject<SoundEvent> JOKER = registerSoundEvent("joker");
    public static final RegistryObject<SoundEvent> HAGALAZ = registerSoundEvent("hagalaz");
    public static final RegistryObject<SoundEvent> ALGIZ = registerSoundEvent("algiz");
    public static final RegistryObject<SoundEvent> ANSUZ = registerSoundEvent("ansuz");
    public static final RegistryObject<SoundEvent> BERKANO = registerSoundEvent("berkano");
    public static final RegistryObject<SoundEvent> BLACK_RUNE = registerSoundEvent("black_rune");
    public static final RegistryObject<SoundEvent> DAGAZ = registerSoundEvent("dagaz");
    public static final RegistryObject<SoundEvent> EHWAZ = registerSoundEvent("ehwaz");
    public static final RegistryObject<SoundEvent> JERA = registerSoundEvent("jera");
    public static final RegistryObject<SoundEvent> PERTHRO = registerSoundEvent("perthro");
    public static final RegistryObject<SoundEvent> RUNE_SHARD = registerSoundEvent("rune_shard");
    public static final RegistryObject<SoundEvent> CHAOS_CARD = registerSoundEvent("chaos_card");
    public static final RegistryObject<SoundEvent> CREDIT_CARD = registerSoundEvent("credit_card");
    public static final RegistryObject<SoundEvent> RULES_CARD = registerSoundEvent("rules_card");
    public static final RegistryObject<SoundEvent> A_CARD_AGAINST_HUMANITY = registerSoundEvent("a_card_against_humanity");
    public static final RegistryObject<SoundEvent> SUICIDE_KING = registerSoundEvent("suicide_king");
    public static final RegistryObject<SoundEvent> GET_OUT_OF_JAIL_CARD = registerSoundEvent("get_out_of_jail_card");
    public static final RegistryObject<SoundEvent> QUESTION_CARD = registerSoundEvent("question_card");
    public static final RegistryObject<SoundEvent> DICE_SHARD = registerSoundEvent("dice_shard");
    public static final RegistryObject<SoundEvent> EMERGENCY_CONTACT = registerSoundEvent("emergency_contact");
    public static final RegistryObject<SoundEvent> HOLY_CARD = registerSoundEvent("holy_card");
    public static final RegistryObject<SoundEvent> HUGE_GROWTH = registerSoundEvent("huge_growth");
    public static final RegistryObject<SoundEvent> ANCIENT_RECALL = registerSoundEvent("ancient_recall");
    public static final RegistryObject<SoundEvent> ERA_WALK = registerSoundEvent("era_walk");
    public static final RegistryObject<SoundEvent> CRACKED_KEY = registerSoundEvent("cracked_key");
    public static final RegistryObject<SoundEvent> QUEEN_OF_HEARTS = registerSoundEvent("queen_of_hearts");
    public static final RegistryObject<SoundEvent> WILD_CARD = registerSoundEvent("wild_card");









    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name)));
    }


    public static void register(IEventBus event){
        SOUND_EVENTS.register(event);
    }
}
