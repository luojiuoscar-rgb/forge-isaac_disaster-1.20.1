package net.luojiuoscar.isaac_disaster.registries.pill_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPillEffect {

    public static final ResourceKey<Registry<IPillEffect>> PILL_EFFECT_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pill_effect"));

    public static final DeferredRegister<IPillEffect> PILL_EFFECT_REGISTRY =
            DeferredRegister.create(PILL_EFFECT_KEY, IsaacDisaster.MOD_ID);

    // 第一个列表对应的代码
    public static final RegistryObject<IPillEffect> ADDICTED =
            PILL_EFFECT_REGISTRY.register("addicted", Addicted::new);

    public static final RegistryObject<IPillEffect> AMNESIA =
            PILL_EFFECT_REGISTRY.register("amnesia", Amnesia::new);

    public static final RegistryObject<IPillEffect> BAD_GAS =
            PILL_EFFECT_REGISTRY.register("bad_gas", BadGas::new);

    public static final RegistryObject<IPillEffect> BAD_TRIP =
            PILL_EFFECT_REGISTRY.register("bad_trip", BadTrip::new);

    public static final RegistryObject<IPillEffect> BALLS_OF_STEEL =
            PILL_EFFECT_REGISTRY.register("balls_of_steel", BallsOfSteel::new);

    public static final RegistryObject<IPillEffect> BOMBS_ARE_KEY =
            PILL_EFFECT_REGISTRY.register("bombs_are_key", BombsAreKey::new);

    public static final RegistryObject<IPillEffect> ENERGY48 =
            PILL_EFFECT_REGISTRY.register("energy48", Energy48::new);

    public static final RegistryObject<IPillEffect> EXPERIMENTAL_PILL =
            PILL_EFFECT_REGISTRY.register("experimental_pill", ExperimentalPill::new);

    public static final RegistryObject<IPillEffect> EXPLOSIVE_DIARRHEA =
            PILL_EFFECT_REGISTRY.register("explosive_diarrhea", ExplosiveDiarrhea::new);

    public static final RegistryObject<IPillEffect> FEELS_LIKE_IM_WALKING_ON_SUNSHINE =
            PILL_EFFECT_REGISTRY.register("feels_like_im_walking_on_sunshine", FeelsLikeImWalkingOnSunshine::new);

    public static final RegistryObject<IPillEffect> FRIENDS_TILL_THE_END =
            PILL_EFFECT_REGISTRY.register("friends_till_the_end", FriendsTillTheEnd::new);

    public static final RegistryObject<IPillEffect> FULL_HEALTH =
            PILL_EFFECT_REGISTRY.register("full_health", FullHealth::new);

    public static final RegistryObject<IPillEffect> GULP =
            PILL_EFFECT_REGISTRY.register("gulp", Gulp::new);

    public static final RegistryObject<IPillEffect> HEALTH_DOWN =
            PILL_EFFECT_REGISTRY.register("health_down", HealthDown::new);

    public static final RegistryObject<IPillEffect> HEALTH_UP =
            PILL_EFFECT_REGISTRY.register("health_up", HealthUp::new);

    public static final RegistryObject<IPillEffect> HEMATEMESIS =
            PILL_EFFECT_REGISTRY.register("hematemesis", Hematemesis::new);

    public static final RegistryObject<IPillEffect> I_CAN_SEE_FOREVER =
            PILL_EFFECT_REGISTRY.register("i_can_see_forever", ICanSeeForever::new);

    public static final RegistryObject<IPillEffect> I_FOUND_PILLS =
            PILL_EFFECT_REGISTRY.register("i_found_pills", IFoundPills::new);

    public static final RegistryObject<IPillEffect> IM_DROWSY =
            PILL_EFFECT_REGISTRY.register("im_drowsy", ImDrowsy::new);

    public static final RegistryObject<IPillEffect> IM_EXCITED =
            PILL_EFFECT_REGISTRY.register("im_excited", ImExcited::new);

    public static final RegistryObject<IPillEffect> LEMON_PARTY =
            PILL_EFFECT_REGISTRY.register("lemon_party", LemonParty::new);

    public static final RegistryObject<IPillEffect> LUCK_DOWN =
            PILL_EFFECT_REGISTRY.register("luck_down", LuckDown::new);

    public static final RegistryObject<IPillEffect> LUCK_UP =
            PILL_EFFECT_REGISTRY.register("luck_up", LuckUp::new);

    public static final RegistryObject<IPillEffect> ONE_MAKES_YOU_LARGER =
            PILL_EFFECT_REGISTRY.register("one_makes_you_larger", OneMakesYouLarger::new);

    public static final RegistryObject<IPillEffect> ONE_MAKES_YOU_SMALL =
            PILL_EFFECT_REGISTRY.register("one_makes_you_small", OneMakesYouSmall::new);

    public static final RegistryObject<IPillEffect> PARALYSIS =
            PILL_EFFECT_REGISTRY.register("paralysis", Paralysis::new);

    public static final RegistryObject<IPillEffect> PERCS =
            PILL_EFFECT_REGISTRY.register("percs", Percs::new);

    public static final RegistryObject<IPillEffect> PHEROMONES =
            PILL_EFFECT_REGISTRY.register("pheromones", Pheromones::new);

    public static final RegistryObject<IPillEffect> POWER_PILL =
            PILL_EFFECT_REGISTRY.register("power_pill", PowerPill::new);

    public static final RegistryObject<IPillEffect> PUBERTY =
            PILL_EFFECT_REGISTRY.register("puberty", Puberty::new);

    public static final RegistryObject<IPillEffect> QUESTION_PILL =
            PILL_EFFECT_REGISTRY.register("question_pill", QuestionPill::new);

    public static final RegistryObject<IPillEffect> RANGE_DOWN =
            PILL_EFFECT_REGISTRY.register("range_down", RangeDown::new);

    public static final RegistryObject<IPillEffect> RANGE_UP =
            PILL_EFFECT_REGISTRY.register("range_up", RangeUp::new);

    public static final RegistryObject<IPillEffect> RETRO_VISION =
            PILL_EFFECT_REGISTRY.register("retro_vision", RetroVision::new);

    public static final RegistryObject<IPillEffect> RUA_WIZARD =
            PILL_EFFECT_REGISTRY.register("rua_wizard", RUAWizard::new);

    public static final RegistryObject<IPillEffect> SHOT_SPEED_DOWN =
            PILL_EFFECT_REGISTRY.register("shot_speed_down", ShotSpeedDown::new);

    public static final RegistryObject<IPillEffect> SHOT_SPEED_UP =
            PILL_EFFECT_REGISTRY.register("shot_speed_up", ShotSpeedUp::new);

    public static final RegistryObject<IPillEffect> SOMETHINGS_WRONG =
            PILL_EFFECT_REGISTRY.register("somethings_wrong", SomethingsWrong::new);

    public static final RegistryObject<IPillEffect> SPEED_DOWN =
            PILL_EFFECT_REGISTRY.register("speed_down", SpeedDown::new);

    public static final RegistryObject<IPillEffect> SPEED_UP =
            PILL_EFFECT_REGISTRY.register("speed_up", SpeedUp::new);

    public static final RegistryObject<IPillEffect> TEARS_DOWN =
            PILL_EFFECT_REGISTRY.register("tears_down", TearsDown::new);

    public static final RegistryObject<IPillEffect> TEARS_UP =
            PILL_EFFECT_REGISTRY.register("tears_up", TearsUp::new);

    public static final RegistryObject<IPillEffect> TELEPILLS =
            PILL_EFFECT_REGISTRY.register("telepills", Telepills::new);

    public static final RegistryObject<IPillEffect> VURP =
            PILL_EFFECT_REGISTRY.register("vurp", Vurp::new);
}
