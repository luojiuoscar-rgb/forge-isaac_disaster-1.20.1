package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items.*;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.cards.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPickupAbility {
    public static final ResourceKey<Registry<PickupAbility>> PICKUP_ABILITY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickup_ability"));

    public static final DeferredRegister<PickupAbility> PICKUP_ABILITY_REGISTRY =
            DeferredRegister.create(PICKUP_ABILITY_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<PickupAbility> BATTERY =
            PICKUP_ABILITY_REGISTRY.register("battery", Battery::new);

    public static final RegistryObject<PickupAbility> BLACK_HEART =
            PICKUP_ABILITY_REGISTRY.register("black_heart", BlackHeart::new);

    public static final RegistryObject<PickupAbility> BLACK_SACK =
            PICKUP_ABILITY_REGISTRY.register("black_sack", BlackSack::new);

    public static final RegistryObject<PickupAbility> BLENDED_HEART =
            PICKUP_ABILITY_REGISTRY.register("blended_heart", BlendedHeart::new);

    public static final RegistryObject<PickupAbility> BOMB =
            PICKUP_ABILITY_REGISTRY.register("bomb", Bomb::new);

    public static final RegistryObject<PickupAbility> BONE_HEART =
            PICKUP_ABILITY_REGISTRY.register("bone_heart", BoneHeart::new);

    public static final RegistryObject<PickupAbility> DOUBLE_RED_HEART =
            PICKUP_ABILITY_REGISTRY.register("double_red_heart", DoubleRedHeart::new);

    public static final RegistryObject<PickupAbility> ETERNAL_HEART =
            PICKUP_ABILITY_REGISTRY.register("eternal_heart", EternalHeart::new);

    public static final RegistryObject<PickupAbility> GIGA_BOMB =
            PICKUP_ABILITY_REGISTRY.register("giga_bomb", GigaBomb::new);

    public static final RegistryObject<PickupAbility> GOLDEN_BATTERY =
            PICKUP_ABILITY_REGISTRY.register("golden_battery", GoldenBattery::new);

    public static final RegistryObject<PickupAbility> GOLDEN_BOMB =
            PICKUP_ABILITY_REGISTRY.register("golden_bomb", GoldenBomb::new);

    public static final RegistryObject<PickupAbility> GOLDEN_HEART =
            PICKUP_ABILITY_REGISTRY.register("golden_heart", GoldenHeart::new);

    public static final RegistryObject<PickupAbility> GRAB_BAG =
            PICKUP_ABILITY_REGISTRY.register("grab_bag", GrabBag::new);

    public static final RegistryObject<PickupAbility> HALF_RED_HEART =
            PICKUP_ABILITY_REGISTRY.register("half_red_heart", HalfRedHeart::new);

    public static final RegistryObject<PickupAbility> HALF_SOUL_HEART =
            PICKUP_ABILITY_REGISTRY.register("half_soul_heart", HalfSoulHeart::new);

    public static final RegistryObject<PickupAbility> MEGA_BATTERY =
            PICKUP_ABILITY_REGISTRY.register("mega_battery", MegaBattery::new);

    public static final RegistryObject<PickupAbility> RED_HEART =
            PICKUP_ABILITY_REGISTRY.register("red_heart", RedHeart::new);

    public static final RegistryObject<PickupAbility> SMALL_BATTERY =
            PICKUP_ABILITY_REGISTRY.register("small_battery", SmallBattery::new);

    public static final RegistryObject<PickupAbility> SOUL_HEART =
            PICKUP_ABILITY_REGISTRY.register("soul_heart", SoulHeart::new);

    public static final RegistryObject<PickupAbility> ACE_OF_CLUBS =
            PICKUP_ABILITY_REGISTRY.register("ace_of_clubs", AceOfClubs::new);

    public static final RegistryObject<PickupAbility> ACE_OF_DIAMONDS =
            PICKUP_ABILITY_REGISTRY.register("ace_of_diamonds", AceOfDiamonds::new);

    public static final RegistryObject<PickupAbility> ACE_OF_HEARTS =
            PICKUP_ABILITY_REGISTRY.register("ace_of_hearts", AceOfHearts::new);

    public static final RegistryObject<PickupAbility> ACE_OF_SPADES =
            PICKUP_ABILITY_REGISTRY.register("ace_of_spades", AceOfSpades::new);

    public static final RegistryObject<PickupAbility> ANCIENT_RECALL =
            PICKUP_ABILITY_REGISTRY.register("ancient_recall", AncientRecall::new);

    public static final RegistryObject<PickupAbility> DEATH =
            PICKUP_ABILITY_REGISTRY.register("death", Death::new);

    public static final RegistryObject<PickupAbility> HOLY_CARD =
            PICKUP_ABILITY_REGISTRY.register("holy_card", HolyCard::new);

    public static final RegistryObject<PickupAbility> JUSTICE =
            PICKUP_ABILITY_REGISTRY.register("justice", Justice::new);

    public static final RegistryObject<PickupAbility> JUSTICE_R =
            PICKUP_ABILITY_REGISTRY.register("justice_r", JusticeR::new);

    public static final RegistryObject<PickupAbility> POKERS_BACK =
            PICKUP_ABILITY_REGISTRY.register("pokers_back", PokersBack::new);

    public static final RegistryObject<PickupAbility> QUEEN_OF_HEARTS =
            PICKUP_ABILITY_REGISTRY.register("queen_of_hearts", QueenOfHearts::new);

    public static final RegistryObject<PickupAbility> QUESTION_CARD =
            PICKUP_ABILITY_REGISTRY.register("question_card", QuestionCard::new);

    public static final RegistryObject<PickupAbility> REVERSE_TAROTS_BACK =
            PICKUP_ABILITY_REGISTRY.register("reverse_tarots_back", ReverseTarotsBack::new);

    public static final RegistryObject<PickupAbility> SPECIALS_BACK =
            PICKUP_ABILITY_REGISTRY.register("specials_back", SpecialsBack::new);

    public static final RegistryObject<PickupAbility> STRENGTH_R =
            PICKUP_ABILITY_REGISTRY.register("strength_r", StrengthR::new);

    public static final RegistryObject<PickupAbility> TAROTS_BACK =
            PICKUP_ABILITY_REGISTRY.register("tarots_back", TarotsBack::new);

    public static final RegistryObject<PickupAbility> TEMPERANCE_R =
            PICKUP_ABILITY_REGISTRY.register("temperance_r", TemperanceR::new);

    public static final RegistryObject<PickupAbility> THE_CHARIOT =
            PICKUP_ABILITY_REGISTRY.register("the_chariot", TheChariot::new);

    public static final RegistryObject<PickupAbility> THE_CHARIOT_R =
            PICKUP_ABILITY_REGISTRY.register("the_chariot_r", TheChariotR::new);

    public static final RegistryObject<PickupAbility> THE_DEVIL =
            PICKUP_ABILITY_REGISTRY.register("the_devil", TheDevil::new);

    public static final RegistryObject<PickupAbility> THE_EMPRESS =
            PICKUP_ABILITY_REGISTRY.register("the_empress", TheEmpress::new);

    public static final RegistryObject<PickupAbility> THE_EMPRESS_R =
            PICKUP_ABILITY_REGISTRY.register("the_empress_r", TheEmpressR::new);

    public static final RegistryObject<PickupAbility> THE_FOOL =
            PICKUP_ABILITY_REGISTRY.register("the_fool", TheFool::new);

    public static final RegistryObject<PickupAbility> THE_HANGED_MAN =
            PICKUP_ABILITY_REGISTRY.register("the_hanged_man", TheHangedMan::new);

    public static final RegistryObject<PickupAbility> THE_HIEROPHANT =
            PICKUP_ABILITY_REGISTRY.register("the_hierophant", TheHierophant::new);

    public static final RegistryObject<PickupAbility> THE_HIEROPHANT_R =
            PICKUP_ABILITY_REGISTRY.register("the_hierophant_r", TheHierophantR::new);

    public static final RegistryObject<PickupAbility> THE_HIGH_PRIESTESS =
            PICKUP_ABILITY_REGISTRY.register("the_high_priestess", TheHighPriestess::new);

    public static final RegistryObject<PickupAbility> THE_HIGH_PRIESTESS_R =
            PICKUP_ABILITY_REGISTRY.register("the_high_priestess_r", TheHighPriestessR::new);

    public static final RegistryObject<PickupAbility> THE_LOVERS =
            PICKUP_ABILITY_REGISTRY.register("the_lovers", TheLovers::new);

    public static final RegistryObject<PickupAbility> THE_MAGICIAN =
            PICKUP_ABILITY_REGISTRY.register("the_magician", TheMagician::new);

    public static final RegistryObject<PickupAbility> THE_MAGICIAN_R =
            PICKUP_ABILITY_REGISTRY.register("the_magician_r", TheMagicianR::new);

    public static final RegistryObject<PickupAbility> THE_MOON =
            PICKUP_ABILITY_REGISTRY.register("the_moon", TheMoon::new);

    public static final RegistryObject<PickupAbility> THE_STARS =
            PICKUP_ABILITY_REGISTRY.register("the_stars", TheStars::new);

    public static final RegistryObject<PickupAbility> THE_SUN =
            PICKUP_ABILITY_REGISTRY.register("the_sun", TheSun::new);

    public static final RegistryObject<PickupAbility> THE_TOWER =
            PICKUP_ABILITY_REGISTRY.register("the_tower", TheTower::new);

    public static final RegistryObject<PickupAbility> THE_TOWER_R =
            PICKUP_ABILITY_REGISTRY.register("the_tower_r", TheTowerR::new);

    public static final RegistryObject<PickupAbility> THE_WORLD =
            PICKUP_ABILITY_REGISTRY.register("the_world", TheWorld::new);

    public static final RegistryObject<PickupAbility> TWO_OF_CLUBS =
            PICKUP_ABILITY_REGISTRY.register("two_of_clubs", TwoOfClubs::new);

    public static final RegistryObject<PickupAbility> TWO_OF_DIAMONDS =
            PICKUP_ABILITY_REGISTRY.register("two_of_diamonds", TwoOfDiamonds::new);

    public static final RegistryObject<PickupAbility> TWO_OF_HEARTS =
            PICKUP_ABILITY_REGISTRY.register("two_of_hearts", TwoOfHearts::new);

    public static final RegistryObject<PickupAbility> TWO_OF_SPADES =
            PICKUP_ABILITY_REGISTRY.register("two_of_spades", TwoOfSpades::new);

    public static final RegistryObject<PickupAbility> WILD_CARD =
            PICKUP_ABILITY_REGISTRY.register("wild_card", WildCard::new);

    public static final RegistryObject<PickupAbility> CREDIT_CARD =
            PICKUP_ABILITY_REGISTRY.register("credit_card", CreditCard::new);

    public static final RegistryObject<PickupAbility> POOP =
            PICKUP_ABILITY_REGISTRY.register("poop", Poop::new);

    public static final RegistryObject<PickupAbility> GOLDEN_POOP =
            PICKUP_ABILITY_REGISTRY.register("golden_poop", GoldenPoop::new);

    public static final RegistryObject<PickupAbility> RAINBOW_POOP =
            PICKUP_ABILITY_REGISTRY.register("rainbow_poop", RainbowPoop::new);
}
