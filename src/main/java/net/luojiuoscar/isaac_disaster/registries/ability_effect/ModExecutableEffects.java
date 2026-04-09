package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModExecutableEffects {
    public static final ResourceKey<Registry<IExecutableEffect>> EXECUTABLE_EFFECT =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "executable_effect"));

    public static final DeferredRegister<IExecutableEffect> EXECUTABLE_EFFECT_REGISTRY =
            DeferredRegister.create(EXECUTABLE_EFFECT, IsaacDisaster.MOD_ID);

    //<editor-fold desc="ability effects">
    public static final RegistryObject<IExecutableEffect> HEAL =
            EXECUTABLE_EFFECT_REGISTRY.register("heal", Heal::new);
    public static final RegistryObject<IExecutableEffect> WOODEN_NICKEL =
            EXECUTABLE_EFFECT_REGISTRY.register("wooden_nickel", WoodenNickel::new);
    public static final RegistryObject<IExecutableEffect> POTIONS =
            EXECUTABLE_EFFECT_REGISTRY.register("potions", Potions::new);
    public static final RegistryObject<IExecutableEffect> THE_NECRONMICON =
            EXECUTABLE_EFFECT_REGISTRY.register("the_necronmicon", TheNecronmicon::new);
    public static final RegistryObject<IExecutableEffect> APPLY_EFFECT_TO_NEARBY =
            EXECUTABLE_EFFECT_REGISTRY.register("apply_effect_to_nearby", ApplyEffectToNearby::new);
    public static final RegistryObject<IExecutableEffect> D6 =
            EXECUTABLE_EFFECT_REGISTRY.register("d6", D6::new);
    public static final RegistryObject<IExecutableEffect> STACK_POTION =
            EXECUTABLE_EFFECT_REGISTRY.register("stack_potion", StackPotion::new);
    public static final RegistryObject<IExecutableEffect> TELEPORT =
            EXECUTABLE_EFFECT_REGISTRY.register("teleport", Teleport::new);
    public static final RegistryObject<IExecutableEffect> TAMMYS_HEAD =
            EXECUTABLE_EFFECT_REGISTRY.register("tammys_head", TammysHead::new);
    public static final RegistryObject<IExecutableEffect> SWALLOW_TRINKETS =
            EXECUTABLE_EFFECT_REGISTRY.register("swallow_trinkets", SwallowTrinkets::new);
    public static final RegistryObject<IExecutableEffect> GIVE_ITEM =
            EXECUTABLE_EFFECT_REGISTRY.register("give_item", GiveItem::new);
    public static final RegistryObject<IExecutableEffect> USE_PILL =
            EXECUTABLE_EFFECT_REGISTRY.register("use_pill", UsePill::new);
    public static final RegistryObject<IExecutableEffect> THROW_BOMB =
            EXECUTABLE_EFFECT_REGISTRY.register("throw_bomb", ThrowBomb::new);
    public static final RegistryObject<IExecutableEffect> LEMON =
            EXECUTABLE_EFFECT_REGISTRY.register("lemon", Lemon::new);
    public static final RegistryObject<IExecutableEffect> KAMIKAZE =
            EXECUTABLE_EFFECT_REGISTRY.register("kamikaze", Kamikaze::new);
    public static final RegistryObject<IExecutableEffect> IV_BAG =
            EXECUTABLE_EFFECT_REGISTRY.register("iv_bag", IvBag::new);
    public static final RegistryObject<IExecutableEffect> DULL_RAZOR =
            EXECUTABLE_EFFECT_REGISTRY.register("dull_razor", DullRazor::new);
    public static final RegistryObject<IExecutableEffect> DIPLOPIA =
            EXECUTABLE_EFFECT_REGISTRY.register("diplopia", Diplopia::new);
    public static final RegistryObject<IExecutableEffect> CROOKED_PENNY =
            EXECUTABLE_EFFECT_REGISTRY.register("crooked_penny", CrookedPenny::new);
    public static final RegistryObject<IExecutableEffect> SPAWN_BOMB_NEARBY =
            EXECUTABLE_EFFECT_REGISTRY.register("spawn_bomb_nearby", SpawnBombNearby::new);
    public static final RegistryObject<IExecutableEffect> THE_WAFER =
            EXECUTABLE_EFFECT_REGISTRY.register("the_wafer", TheWafer::new);
    public static final RegistryObject<IExecutableEffect> RANDOM_HARMFUL_POTION =
            EXECUTABLE_EFFECT_REGISTRY.register("random_harmful_potion", RandomHarmfulPotion::new);
    public static final RegistryObject<IExecutableEffect> BREAK_BLOCK_AND_DROP =
            EXECUTABLE_EFFECT_REGISTRY.register("break_block_and_drop", BreakBlockAndDrop::new);
    public static final RegistryObject<IExecutableEffect> SHOOT_LASER =
            EXECUTABLE_EFFECT_REGISTRY.register("shoot_laser", ShootLaser::new);
    public static final RegistryObject<IExecutableEffect> SWALLOWED_PENNY =
            EXECUTABLE_EFFECT_REGISTRY.register("swallowed_penny", SwallowedPenny::new);
    public static final RegistryObject<IExecutableEffect> PIGGY_BANK =
            EXECUTABLE_EFFECT_REGISTRY.register("piggy_bank", PiggyBank::new);
    public static final RegistryObject<IExecutableEffect> LUCKY_ROCK =
            EXECUTABLE_EFFECT_REGISTRY.register("lucky_rock", LuckyRock::new);
    public static final RegistryObject<IExecutableEffect> LASER_PLUS_BRIMSTONE =
            EXECUTABLE_EFFECT_REGISTRY.register("laser_plus_brimstone", LaserPlusBrimstone::new);
    public static final RegistryObject<IExecutableEffect> LASER_PLUS_FETUS =
            EXECUTABLE_EFFECT_REGISTRY.register("laser_plus_fetus", LaserPlusFetus::new);
    public static final RegistryObject<IExecutableEffect> IPECAC =
            EXECUTABLE_EFFECT_REGISTRY.register("ipecac", Ipecac::new);
    public static final RegistryObject<IExecutableEffect> HABIT =
            EXECUTABLE_EFFECT_REGISTRY.register("habit", Habit::new);
    public static final RegistryObject<IExecutableEffect> CURSE_OF_THE_TOWER =
            EXECUTABLE_EFFECT_REGISTRY.register("curse_of_the_tower", CurseOfTheTower::new);
    public static final RegistryObject<IExecutableEffect> CURSED_PENNY =
            EXECUTABLE_EFFECT_REGISTRY.register("cursed_penny", CursedPenny::new);
    public static final RegistryObject<IExecutableEffect> CURSED_EYE =
            EXECUTABLE_EFFECT_REGISTRY.register("cursed_eye", CursedEye::new);
    public static final RegistryObject<IExecutableEffect> BRIMSTONE_PLUS_C_SECTION =
            EXECUTABLE_EFFECT_REGISTRY.register("brimstone_plus_c_section", BrimstonePlusCSection::new);
    public static final RegistryObject<IExecutableEffect> CHARM_OF_THE_VAMPIRE =
            EXECUTABLE_EFFECT_REGISTRY.register("charm_of_the_vampire", CharmOfTheVampire::new);
    public static final RegistryObject<IExecutableEffect> CARTRIDGE =
            EXECUTABLE_EFFECT_REGISTRY.register("cartridge", Cartridge::new);
    public static final RegistryObject<IExecutableEffect> FART =
            EXECUTABLE_EFFECT_REGISTRY.register("fart", Fart::new);
    public static final RegistryObject<IExecutableEffect> BUTT_PENNY =
            EXECUTABLE_EFFECT_REGISTRY.register("butt_penny", ButtPenny::new);
    public static final RegistryObject<IExecutableEffect> BULLET_BOUNCE_ON_ENTITY =
            EXECUTABLE_EFFECT_REGISTRY.register("bullet_bounce_on_entity", BulletBounceOnEntity::new);
    public static final RegistryObject<IExecutableEffect> BULLET_BOUNCE_ON_BLOCK =
            EXECUTABLE_EFFECT_REGISTRY.register("bullet_bounce_on_block", BulletBounceOnBlock::new);
    public static final RegistryObject<IExecutableEffect> BLIND_RAGE =
            EXECUTABLE_EFFECT_REGISTRY.register("blind_rage", BlindRage::new);
    public static final RegistryObject<IExecutableEffect> ATTRACT_ITEM =
            EXECUTABLE_EFFECT_REGISTRY.register("attract_item", AttractItem::new);
    public static final RegistryObject<IExecutableEffect> REMOVE_ALL_HARMFUL_POTION =
            EXECUTABLE_EFFECT_REGISTRY.register("remove_all_harmful_potion", RemoveAllHarmfulPotion::new);
    public static final RegistryObject<IExecutableEffect> GILDED_KEY =
            EXECUTABLE_EFFECT_REGISTRY.register("gilded_key", GildedKey::new);
    public static final RegistryObject<IExecutableEffect> MAGNETO =
            EXECUTABLE_EFFECT_REGISTRY.register("magneto", Magneto::new);
    public static final RegistryObject<IExecutableEffect> MONEY_IS_POWER =
            EXECUTABLE_EFFECT_REGISTRY.register("money_is_power", MoneyIsPower::new);
    public static final RegistryObject<IExecutableEffect> ROCK_BOTTOM =
            EXECUTABLE_EFFECT_REGISTRY.register("rock_bottom", RockBottom::new);
    public static final RegistryObject<IExecutableEffect> LOOT_MONEY =
            EXECUTABLE_EFFECT_REGISTRY.register("loot_money", LootMoney::new);
    public static final RegistryObject<IExecutableEffect> THE_LEFT_HAND =
            EXECUTABLE_EFFECT_REGISTRY.register("the_left_hand", TheLeftHand::new);
    public static final RegistryObject<IExecutableEffect> STACK_HOLY_SHIELD =
            EXECUTABLE_EFFECT_REGISTRY.register("stack_holy_shield", StackHolyShield::new);
    public static final RegistryObject<IExecutableEffect> TRANSFORM_ENTITY_TO_LOOT =
            EXECUTABLE_EFFECT_REGISTRY.register("transform_entity_to_loot", TransformEntityToLoot::new);
    public static final RegistryObject<IExecutableEffect> ANCIENT_RECALL =
            EXECUTABLE_EFFECT_REGISTRY.register("ancient_recall", AncientRecall::new);
    public static final RegistryObject<IExecutableEffect> CREDIT_CARD =
            EXECUTABLE_EFFECT_REGISTRY.register("credit_card", CreditCard::new);
    public static final RegistryObject<IExecutableEffect> JUSTICE =
            EXECUTABLE_EFFECT_REGISTRY.register("justice", Justice::new);
    public static final RegistryObject<IExecutableEffect> JUSTICE_R =
            EXECUTABLE_EFFECT_REGISTRY.register("justice_r", JusticeR::new);
    public static final RegistryObject<IExecutableEffect> LOOT_AT_POS =
            EXECUTABLE_EFFECT_REGISTRY.register("loot_at_pos", LootAtPosition::new);
    public static final RegistryObject<IExecutableEffect> GIVE_ITEM_VIA_LOOT =
            EXECUTABLE_EFFECT_REGISTRY.register("give_item_via_loot", GiveItemViaLoot::new);
    public static final RegistryObject<IExecutableEffect> QUESTION_CARD =
            EXECUTABLE_EFFECT_REGISTRY.register("question_card", QuestionCard::new);
    public static final RegistryObject<IExecutableEffect> TEMPERANCE_R =
            EXECUTABLE_EFFECT_REGISTRY.register("temperance_r", TemperanceR::new);
    public static final RegistryObject<IExecutableEffect> STACK_FRAGILE_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("stack_fragile_heart", StackFragileHeart::new);
    public static final RegistryObject<IExecutableEffect> TELEPORT_TO_SPAWN =
            EXECUTABLE_EFFECT_REGISTRY.register("teleport_to_spawn", TeleportToSpawn::new);
    public static final RegistryObject<IExecutableEffect> THE_HIGH_PRIESTESS =
            EXECUTABLE_EFFECT_REGISTRY.register("the_high_priestess", TheHighPriestess::new);
    public static final RegistryObject<IExecutableEffect> THE_HIGH_PRIESTESS_R =
            EXECUTABLE_EFFECT_REGISTRY.register("the_high_priestess_r", TheHighPriestessR::new);
    public static final RegistryObject<IExecutableEffect> TELEPORT_TO_IDENTIFIER =
            EXECUTABLE_EFFECT_REGISTRY.register("teleport_to_identifier", TeleportToIdentifier::new);
    public static final RegistryObject<IExecutableEffect> COPY_NEAREST_PEDESTAL =
            EXECUTABLE_EFFECT_REGISTRY.register("copy_nearest_pedestal", CopyNearestPedestal::new);
    public static final RegistryObject<IExecutableEffect> THE_SUN =
            EXECUTABLE_EFFECT_REGISTRY.register("the_sun", TheSun::new);
    public static final RegistryObject<IExecutableEffect> SPAWN_RANDOM_STRUCTURE =
            EXECUTABLE_EFFECT_REGISTRY.register("spawn_random_structure", SpawnRandomStructure::new);
    public static final RegistryObject<IExecutableEffect> DOUBLE_ITEM =
            EXECUTABLE_EFFECT_REGISTRY.register("double_item", DoubleItem::new);
    public static final RegistryObject<IExecutableEffect> TWO_OF_DIAMONDS =
            EXECUTABLE_EFFECT_REGISTRY.register("two_of_diamonds", TwoOfDiamonds::new);
    public static final RegistryObject<IExecutableEffect> TWO_OF_HEARTS =
            EXECUTABLE_EFFECT_REGISTRY.register("two_of_hearts", TwoOfHearts::new);
    public static final RegistryObject<IExecutableEffect> WILD_CARD =
            EXECUTABLE_EFFECT_REGISTRY.register("wild_card", WildCard::new);
    public static final RegistryObject<IExecutableEffect> CHARGE_ITEM_VIA_BATTERY =
            EXECUTABLE_EFFECT_REGISTRY.register("charge_item_via_battery", ChargeItemViaBattery::new);
    public static final RegistryObject<IExecutableEffect> ABSORPTION =
            EXECUTABLE_EFFECT_REGISTRY.register("absorption", Absorption::new);
    public static final RegistryObject<IExecutableEffect> STACK_NECRONMICON_SHIELD =
            EXECUTABLE_EFFECT_REGISTRY.register("stack_necronmicon_shield", StackNecronmiconShield::new);
    public static final RegistryObject<IExecutableEffect> GENERATE_LOOT =
            EXECUTABLE_EFFECT_REGISTRY.register("generate_loot", GenerateLoot::new);
    public static final RegistryObject<IExecutableEffect> BLENDED_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("blended_heart", BlendedHeart::new);
    public static final RegistryObject<IExecutableEffect> THROW_GIGA_BOMB =
            EXECUTABLE_EFFECT_REGISTRY.register("throw_giga_bomb", ThrowGigaBomb::new);
    public static final RegistryObject<IExecutableEffect> ADD_COOLDOWN_TO_ITEM =
            EXECUTABLE_EFFECT_REGISTRY.register("add_cooldown_to_item", AddCoolDownToItem::new);
    public static final RegistryObject<IExecutableEffect> CHARGE_ALL =
            EXECUTABLE_EFFECT_REGISTRY.register("charge_all", ChargeAll::new);
    public static final RegistryObject<IExecutableEffect> STACK_GILDING_EFFECT =
            EXECUTABLE_EFFECT_REGISTRY.register("stack_gilding_effect", StackGildingEffect::new);
    public static final RegistryObject<IExecutableEffect> NECRONMICON_SHIELD_ACTIVE =
            EXECUTABLE_EFFECT_REGISTRY.register("necronmicon_shield_active", NecronmiconShieldActive::new);
    public static final RegistryObject<IExecutableEffect> ETERNAL_HEART_PUNISH =
            EXECUTABLE_EFFECT_REGISTRY.register("eternal_heart_punish", EternalHeartPunish::new);
    public static final RegistryObject<IExecutableEffect> GILDING_ACTIVE =
            EXECUTABLE_EFFECT_REGISTRY.register("gilding_active", GildingActive::new);
    public static final RegistryObject<IExecutableEffect> EXPLOSION_IMMUNE =
            EXECUTABLE_EFFECT_REGISTRY.register("explosion_immune", ExplosionImmune::new);
    public static final RegistryObject<IExecutableEffect> EXPLOSION_REGENERATION =
            EXECUTABLE_EFFECT_REGISTRY.register("explosion_regeneration", ExplosionRegeneration::new);
    public static final RegistryObject<IExecutableEffect> HOLY_SHIELD_ACTIVE =
            EXECUTABLE_EFFECT_REGISTRY.register("holy_shield_active", HolyShieldActive::new);
    public static final RegistryObject<IExecutableEffect> ADULT_SET =
            EXECUTABLE_EFFECT_REGISTRY.register("adult_set", AdultSet::new);
    public static final RegistryObject<IExecutableEffect> FRAGILE_HEART_ACTIVE =
            EXECUTABLE_EFFECT_REGISTRY.register("fragile_heart_active", FragileHeartActive::new);
    public static final RegistryObject<IExecutableEffect> CURSE_OF_THE_MAZE =
            EXECUTABLE_EFFECT_REGISTRY.register("curse_of_the_maze", CurseOfTheMaze::new);
    public static final RegistryObject<IExecutableEffect> DROP_PERFECTION =
            EXECUTABLE_EFFECT_REGISTRY.register("drop_perfection", DropPerfection::new);
    public static final RegistryObject<IExecutableEffect> ADDICTED =
            EXECUTABLE_EFFECT_REGISTRY.register("addicted", Addicted::new);
    public static final RegistryObject<IExecutableEffect> AMNESIA =
            EXECUTABLE_EFFECT_REGISTRY.register("amnesia", Amnesia::new);
    public static final RegistryObject<IExecutableEffect> BAD_GAS =
            EXECUTABLE_EFFECT_REGISTRY.register("bad_gas", BadGas::new);
    public static final RegistryObject<IExecutableEffect> BAD_TRIP =
            EXECUTABLE_EFFECT_REGISTRY.register("bad_trip", BadTrip::new);
    public static final RegistryObject<IExecutableEffect> BALLS_OF_STEEL =
            EXECUTABLE_EFFECT_REGISTRY.register("balls_of_steel", BallsOfSteel::new);
    public static final RegistryObject<IExecutableEffect> BOMBS_ARE_KEY =
            EXECUTABLE_EFFECT_REGISTRY.register("bombs_are_key", BombsAreKey::new);
    public static final RegistryObject<IExecutableEffect> I_CAN_SEE_FOREVER =
            EXECUTABLE_EFFECT_REGISTRY.register("i_can_see_forever", ICanSeeForever::new);
    public static final RegistryObject<IExecutableEffect> ENERGY_48 =
            EXECUTABLE_EFFECT_REGISTRY.register("energy_48", Energy48::new);
    public static final RegistryObject<IExecutableEffect> EXPERIMENTAL_PILL =
            EXECUTABLE_EFFECT_REGISTRY.register("experimental_pill", ExperimentalPill::new);
    public static final RegistryObject<IExecutableEffect> EXPLOSIVE_DIARRHEA =
            EXECUTABLE_EFFECT_REGISTRY.register("explosive_diarrhea", ExplosiveDiarrhea::new);
    public static final RegistryObject<IExecutableEffect> FEELS_LIKE_IM_WALKING_ON_SUNSHINE =
            EXECUTABLE_EFFECT_REGISTRY.register("feels_like_im_walking_on_sunshine", FeelsLikeImWalkingOnSunshine::new);
    public static final RegistryObject<IExecutableEffect> I_FOUND_PILLS =
            EXECUTABLE_EFFECT_REGISTRY.register("i_found_pills", IFoundPills::new);
    public static final RegistryObject<IExecutableEffect> FRIENDS_TILL_THE_END =
            EXECUTABLE_EFFECT_REGISTRY.register("friends_till_the_end", FriendsTillTheEnd::new);
    public static final RegistryObject<IExecutableEffect> FULL_HEALTH =
            EXECUTABLE_EFFECT_REGISTRY.register("full_health", FullHealth::new);
    public static final RegistryObject<IExecutableEffect> GULP =
            EXECUTABLE_EFFECT_REGISTRY.register("gulp", Gulp::new);
    public static final RegistryObject<IExecutableEffect> HEALTH_DOWN =
            EXECUTABLE_EFFECT_REGISTRY.register("health_down", HealthDown::new);
    public static final RegistryObject<IExecutableEffect> HEALTH_UP =
            EXECUTABLE_EFFECT_REGISTRY.register("health_up", HealthUp::new);
    public static final RegistryObject<IExecutableEffect> HEMATEMESIS =
            EXECUTABLE_EFFECT_REGISTRY.register("hematemesis", Hematemesis::new);
    public static final RegistryObject<IExecutableEffect> IM_DROWSY =
            EXECUTABLE_EFFECT_REGISTRY.register("im_drowsy", ImDrowsy::new);
    public static final RegistryObject<IExecutableEffect> IM_EXCITED =
            EXECUTABLE_EFFECT_REGISTRY.register("im_excited", ImExcited::new);
    public static final RegistryObject<IExecutableEffect> LEMON_PARTY =
            EXECUTABLE_EFFECT_REGISTRY.register("lemon_party", LemonParty::new);
    public static final RegistryObject<IExecutableEffect> LUCK_DOWN =
            EXECUTABLE_EFFECT_REGISTRY.register("luck_down", LuckDown::new);
    public static final RegistryObject<IExecutableEffect> LUCK_UP =
            EXECUTABLE_EFFECT_REGISTRY.register("luck_up", LuckUp::new);
    public static final RegistryObject<IExecutableEffect> ONE_MAKES_YOU_LARGER =
            EXECUTABLE_EFFECT_REGISTRY.register("one_makes_you_larger", OneMakesYouLarger::new);
    public static final RegistryObject<IExecutableEffect> ONE_MAKES_YOU_SMALL =
            EXECUTABLE_EFFECT_REGISTRY.register("one_makes_you_small", OneMakesYouSmall::new);
    public static final RegistryObject<IExecutableEffect> PARALYSIS =
            EXECUTABLE_EFFECT_REGISTRY.register("paralysis", Paralysis::new);
    public static final RegistryObject<IExecutableEffect> PERCS =
            EXECUTABLE_EFFECT_REGISTRY.register("percs", Percs::new);
    public static final RegistryObject<IExecutableEffect> PHEROMONES =
            EXECUTABLE_EFFECT_REGISTRY.register("pheromones", Pheromones::new);
    public static final RegistryObject<IExecutableEffect> POWER_PILL =
            EXECUTABLE_EFFECT_REGISTRY.register("power_pill", PowerPill::new);
    public static final RegistryObject<IExecutableEffect> PUBERTY =
            EXECUTABLE_EFFECT_REGISTRY.register("puberty", Puberty::new);
    public static final RegistryObject<IExecutableEffect> QUESTION_PILL =
            EXECUTABLE_EFFECT_REGISTRY.register("question_pill", QuestionPill::new);
    public static final RegistryObject<IExecutableEffect> RANGE_DOWN =
            EXECUTABLE_EFFECT_REGISTRY.register("range_down", RangeDown::new);
    public static final RegistryObject<IExecutableEffect> RANGE_UP =
            EXECUTABLE_EFFECT_REGISTRY.register("range_up", RangeUp::new);
    public static final RegistryObject<IExecutableEffect> RETRO_VISION =
            EXECUTABLE_EFFECT_REGISTRY.register("retro_vision", RetroVision::new);
    public static final RegistryObject<IExecutableEffect> RUA_WIZARD =
            EXECUTABLE_EFFECT_REGISTRY.register("rua_wizard", RUAWizard::new);
    public static final RegistryObject<IExecutableEffect> SHOT_SPEED_DOWN =
            EXECUTABLE_EFFECT_REGISTRY.register("shot_speed_down", ShotSpeedDown::new);
    public static final RegistryObject<IExecutableEffect> SHOT_SPEED_UP =
            EXECUTABLE_EFFECT_REGISTRY.register("shot_speed_up", ShotSpeedUp::new);
    public static final RegistryObject<IExecutableEffect> SOMETHINGS_WRONG =
            EXECUTABLE_EFFECT_REGISTRY.register("somethings_wrong", SomethingsWrong::new);
    public static final RegistryObject<IExecutableEffect> SPEED_DOWN =
            EXECUTABLE_EFFECT_REGISTRY.register("speed_down", SpeedDown::new);
    public static final RegistryObject<IExecutableEffect> SPEED_UP =
            EXECUTABLE_EFFECT_REGISTRY.register("speed_up", SpeedUp::new);
    public static final RegistryObject<IExecutableEffect> TEARS_DOWN =
            EXECUTABLE_EFFECT_REGISTRY.register("tears_down", TearsDown::new);
    public static final RegistryObject<IExecutableEffect> TEARS_UP =
            EXECUTABLE_EFFECT_REGISTRY.register("tears_up", TearsUp::new);
    public static final RegistryObject<IExecutableEffect> TELEPILLS =
            EXECUTABLE_EFFECT_REGISTRY.register("telepills", Telepills::new);
    public static final RegistryObject<IExecutableEffect> VURP =
            EXECUTABLE_EFFECT_REGISTRY.register("vurp", Vurp::new);
    public static final RegistryObject<IExecutableEffect> MITRE =
            EXECUTABLE_EFFECT_REGISTRY.register("mitre", Mitre::new);
    public static final RegistryObject<IExecutableEffect> SACK_HEAD =
            EXECUTABLE_EFFECT_REGISTRY.register("sack_head", SackHead::new);
    public static final RegistryObject<IExecutableEffect> DAEMONS_TAIL =
            EXECUTABLE_EFFECT_REGISTRY.register("daemons_tail", DaemonsTail::new);
    public static final RegistryObject<IExecutableEffect> PENNY_TRINKET =
            EXECUTABLE_EFFECT_REGISTRY.register("penny_trinket", PennyTrinket::new);
    public static final RegistryObject<IExecutableEffect> CHEST_LOOT_TRINKET =
            EXECUTABLE_EFFECT_REGISTRY.register("chest_loot_trinket", ChestLootTrinket::new);


    //</editor-fold>












    //<editor-fold desc="ability effect entries">
    public static final RegistryObject<IExecutableEffect> FREE_LEMONADE =
            EXECUTABLE_EFFECT_REGISTRY.register("free_lemonade", () -> new AbilityEffectEntry(
                    LEMON, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(0.8, 100., 0., 10., 4.))
            ));
    public static final RegistryObject<IExecutableEffect> LEMON_MISHAP =
            EXECUTABLE_EFFECT_REGISTRY.register("lemon_mishap", () -> new AbilityEffectEntry(
                    LEMON, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(0.4, 100., 0., 10., 2.5))
            ));
    public static final RegistryObject<IExecutableEffect> MR_BOOM =
            EXECUTABLE_EFFECT_REGISTRY.register("mr_boom", () -> new AbilityEffectEntry(
                    THROW_BOMB, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(80., 7.))
            ));
    public static final RegistryObject<IExecutableEffect> MY_LITTLE_UNICORN =
            EXECUTABLE_EFFECT_REGISTRY.register("my_little_unicorn", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                int duration = 120;
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(MobEffects.MOVEMENT_SPEED, duration, 0),
                        new PotionProfile(ModEffects.INVINCIBLE.get(), duration, 0),
                        new PotionProfile(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration, 0),
                        new PotionProfile(ModEffects.RAMPAGE.get(), duration, 3)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> PLACEBO =
            EXECUTABLE_EFFECT_REGISTRY.register("placebo", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                InteractionHand hand = ctx.get(ContextKeys.HAND);
                if (hand == null) return;
                if (!(ctx.getEntity() instanceof Player player)) return;

                ItemStack pill = null;
                if (hand == InteractionHand.MAIN_HAND){
                    pill = player.getOffhandItem();
                }else if (hand == InteractionHand.OFF_HAND){
                    pill = player.getMainHandItem();
                }
                if (pill == null) return;

                ctx.set(ContextKeys.ITEM, pill.getItem());
            }));
    public static final RegistryObject<IExecutableEffect> ANARCHIST_COOKBOOK =
            EXECUTABLE_EFFECT_REGISTRY.register("anarchist_cookbook", () -> new AbilityEffectEntry(
                    SPAWN_BOMB_NEARBY, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(0.5, 6.))
            ));
    public static final RegistryObject<IExecutableEffect> BOOK_OF_SHADOW =
            EXECUTABLE_EFFECT_REGISTRY.register("book_of_shadow", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.INVINCIBLE.get(), 200, 0)
                ));
                ctx.set(ContextKeys.BOOLEAN, List.of(true, false));
            }));
    public static final RegistryObject<IExecutableEffect> PRAYER_CARD =
            EXECUTABLE_EFFECT_REGISTRY.register("prayer_card", () -> new AbilityEffectEntry(
                    GIVE_ITEM, ctx -> ctx.set(ContextKeys.ITEM, ModItems.ETERNAL_HEART.get())
            ));
    public static final RegistryObject<IExecutableEffect> TELEPATHY_FOR_DUMMIES =
            EXECUTABLE_EFFECT_REGISTRY.register("telepathy_for_dummies", () -> new AbilityEffectEntry(
                    STACK_POTION, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.TELEPATHY.get(), 200, 0)
                ));
                ctx.set(ContextKeys.BOOLEAN, List.of(false, true));
            }));
    public static final RegistryObject<IExecutableEffect> THE_BIBLE =
            EXECUTABLE_EFFECT_REGISTRY.register("the_bible", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.TRANSCENDENCE.get(),
                                (int) StatManager.FLY_TIME.getBonus() * 2,
                                0)
                ))
            ));
    public static final RegistryObject<IExecutableEffect> THE_BOOK_OF_BELIAL =
            EXECUTABLE_EFFECT_REGISTRY.register("the_book_of_belial", () -> new AbilityEffectEntry(
                    STACK_POTION, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.POWER_OF_BELIAL.get(), 240, 0,
                                240, 1, true)
                ));
                ctx.set(ContextKeys.BOOLEAN, List.of(false, true));
            }));
    public static final RegistryObject<IExecutableEffect> THE_GAMEKID =
            EXECUTABLE_EFFECT_REGISTRY.register("the_gamekid", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                int duration = 200;
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.PAC_MAN.get(), duration, 0),
                        new PotionProfile(ModEffects.INVINCIBLE.get(), duration, 0),
                        new PotionProfile(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration, 0)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_HOURGLASS =
            EXECUTABLE_EFFECT_REGISTRY.register("the_hourglass", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.BOOLEAN, List.of(true));
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.POTIONS.get());
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 160,
                                1, 160, 1, true)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> UNICORN_STAMP =
            EXECUTABLE_EFFECT_REGISTRY.register("unicorn_stamp", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                int duration = 120;
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(MobEffects.MOVEMENT_SPEED, duration, 0),
                        new PotionProfile(MobEffects.INVISIBILITY, duration, 0),
                        new PotionProfile(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration, 0)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_COMMON_COLD =
            EXECUTABLE_EFFECT_REGISTRY.register("the_common_cold", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.POISON.get(), 70, 0)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> BOMB_BAG =
            EXECUTABLE_EFFECT_REGISTRY.register("bomb_bag", () -> new AbilityEffectEntry(
                    GIVE_ITEM, ctx -> ctx.set(ContextKeys.ITEM, ModItems.BOMB.get())
            ));
    public static final RegistryObject<IExecutableEffect> FIRE_RESISTENCE =
            EXECUTABLE_EFFECT_REGISTRY.register("fire_resistence", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(new PotionProfile(
                            MobEffects.FIRE_RESISTANCE,
                            240,
                            0)))
            ));
    public static final RegistryObject<IExecutableEffect> THE_RELIC =
            EXECUTABLE_EFFECT_REGISTRY.register("the_relic", () -> new AbilityEffectEntry(
                    GIVE_ITEM, ctx -> ctx.set(ContextKeys.ITEM, ModItems.SOUL_HEART.get())
            ));
    public static final RegistryObject<IExecutableEffect> THE_SOUL =
            EXECUTABLE_EFFECT_REGISTRY.register("the_soul", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(
                    new PotionProfile(
                            ModEffects.SOUL_STATE.get(),
                            120,
                            0,
                            0,
                            0,
                            false
                    )))
            ));
    public static final RegistryObject<IExecutableEffect> THE_WIZ =
            EXECUTABLE_EFFECT_REGISTRY.register("the_wiz", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(
                    new PotionProfile(
                            ModEffects.THE_WIZ.get(),
                            150,
                            1,
                            0,
                            0,
                            false
                    )))
            ));
    public static final RegistryObject<IExecutableEffect> VENUS =
            EXECUTABLE_EFFECT_REGISTRY.register("venus", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.POTIONS.get());
                int amplifier = ctx.getOrDefault(ContextKeys.AMPLIFIER ,1.).intValue();
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(
                                ModEffects.CHARM.get(),
                                Mth.clamp(amplifier * 60 + 120, 120, 240),
                                0
                        )
                ));
                ctx.set(ContextKeys.BOOLEAN, List.of(true));
                ctx.set(ContextKeys.DOUBLE, List.of(0.75));
            }));
    public static final RegistryObject<IExecutableEffect> ACE_OF_CLUBS =
            EXECUTABLE_EFFECT_REGISTRY.register("ace_of_clubs", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.TRANSFORM_ENTITY_TO_LOOT.get());
                ctx.set(ContextKeys.BOOLEAN, List.of(true)); // exclude friends
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_BOMBS));
            }));
    public static final RegistryObject<IExecutableEffect> ACE_OF_DIAMONDS =
            EXECUTABLE_EFFECT_REGISTRY.register("ace_of_diamonds", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.TRANSFORM_ENTITY_TO_LOOT.get());
                ctx.set(ContextKeys.BOOLEAN, List.of(true));
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_COINS));
            }));
    public static final RegistryObject<IExecutableEffect> ACE_OF_HEARTS =
            EXECUTABLE_EFFECT_REGISTRY.register("ace_of_hearts", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.TRANSFORM_ENTITY_TO_LOOT.get());
                ctx.set(ContextKeys.BOOLEAN, List.of(true));
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_HEARTS));
            }));
    public static final RegistryObject<IExecutableEffect> ACE_OF_SPADES =
            EXECUTABLE_EFFECT_REGISTRY.register("ace_of_spades", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.TRANSFORM_ENTITY_TO_LOOT.get());
                ctx.set(ContextKeys.BOOLEAN, List.of(true));
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_KEYS));
            }));
    public static final RegistryObject<IExecutableEffect> POKERS_BACK =
            EXECUTABLE_EFFECT_REGISTRY.register("pokers_back", () -> new AbilityEffectEntry(
                    LOOT_AT_POS, ctx -> {
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_POKERS));
            }));
    public static final RegistryObject<IExecutableEffect> QUEEN_OF_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("queen_of_heart", () -> new AbilityEffectEntry(
                    GIVE_ITEM_VIA_LOOT, ctx -> {
                ctx.set(ContextKeys.ITEM, ModItems.RED_HEART.get());
                ctx.set(ContextKeys.DOUBLE, List.of(1., 21.));
            }));
    public static final RegistryObject<IExecutableEffect> REVERSE_TAROTS_BACK =
            EXECUTABLE_EFFECT_REGISTRY.register("reverse_tarots_back", () -> new AbilityEffectEntry(
                    LOOT_AT_POS, ctx -> {
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_TAROTS_R));
            }));
    public static final RegistryObject<IExecutableEffect> TAROTS_BACK =
            EXECUTABLE_EFFECT_REGISTRY.register("tarots_back", () -> new AbilityEffectEntry(
                    LOOT_AT_POS, ctx -> {
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_TAROTS));
            }));
    public static final RegistryObject<IExecutableEffect> SPECIALS_BACK =
            EXECUTABLE_EFFECT_REGISTRY.register("specials_back", () -> new AbilityEffectEntry(
                    LOOT_AT_POS, ctx -> {
                ctx.set(ContextKeys.RESOURCE_LOCATIONS, List.of(LootTableManager.RANDOM_SPECIALS));
            }));
    public static final RegistryObject<IExecutableEffect> STRENGTH_R =
            EXECUTABLE_EFFECT_REGISTRY.register("strength_r", () -> new AbilityEffectEntry(
                    APPLY_EFFECT_TO_NEARBY, ctx -> {
                ctx.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.POTIONS.get());
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(
                                ModEffects.FRAILTY.get(),
                                600,
                                0
                        )
                ));
                ctx.set(ContextKeys.BOOLEAN, List.of(true));
            }));
    public static final RegistryObject<IExecutableEffect> THE_CHARIOT =
            EXECUTABLE_EFFECT_REGISTRY.register("the_chariot", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(MobEffects.MOVEMENT_SPEED, 160, 0),
                        new PotionProfile(ModEffects.INVINCIBLE.get(), 160, 0),
                        new PotionProfile(ModEffects.LACRIMAL_HYPOSECRETION.get(), 160, 0),
                        new PotionProfile(ModEffects.RAMPAGE.get(), 160, 3)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_CHARIOT_R =
            EXECUTABLE_EFFECT_REGISTRY.register("the_chariot_r", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.DIZZINESS.get(), 120, 255),
                        new PotionProfile(ModEffects.INVINCIBLE.get(), 120, 0),
                        new PotionProfile(MobEffects.DIG_SPEED, 120, 9)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_DEVIL =
            EXECUTABLE_EFFECT_REGISTRY.register("the_devil", () -> new AbilityEffectEntry(
                    STACK_POTION, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.POWER_OF_BELIAL.get(), 600, 0,
                                0, 1, true)
                ));
                ctx.set(ContextKeys.BOOLEAN, List.of(false, true));
            }));
    public static final RegistryObject<IExecutableEffect> THE_EMPRESS =
            EXECUTABLE_EFFECT_REGISTRY.register("the_empress", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.BABYLON.get(), 600, 0,
                                600, 1, true)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_EMPRESS_R =
            EXECUTABLE_EFFECT_REGISTRY.register("the_empress_r", () -> new CompositeExecutableEffect(
                    List.of(new AbilityEffectEntry(POTIONS, context -> {
                        context.set(ContextKeys.POTIONS, List.of(
                                new PotionProfile(MobEffects.DIG_SPEED, 600, 1)
                        ));
                    }), STACK_FRAGILE_HEART.get())
            ));
    public static final RegistryObject<IExecutableEffect> THE_HANGED_MAN =
            EXECUTABLE_EFFECT_REGISTRY.register("the_hanged_man", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                        ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.TRANSCENDENCE.get(), 600, 0)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_HIEROPHANT =
            EXECUTABLE_EFFECT_REGISTRY.register("the_hierophant", () -> new AbilityEffectEntry(
                    GIVE_ITEM_VIA_LOOT, ctx -> {
                ctx.set(ContextKeys.ITEM, ModItems.SOUL_HEART.get());
                int amplifier = ctx.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
                double a = amplifier + 1.;
                double b = amplifier + 2.;

                ctx.set(ContextKeys.DOUBLE, List.of(a, b));
            }));
    public static final RegistryObject<IExecutableEffect> THE_HIEROPHANT_R =
            EXECUTABLE_EFFECT_REGISTRY.register("the_hierophant_r", () -> new AbilityEffectEntry(
                    GIVE_ITEM_VIA_LOOT, ctx -> {
                ctx.set(ContextKeys.ITEM, ModItems.BONE_HEART.get());
                int amplifier = ctx.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
                double a = amplifier + 1.;
                double b = amplifier + 2.;

                ctx.set(ContextKeys.DOUBLE, List.of(a, b));
            }));
    public static final RegistryObject<IExecutableEffect> THE_LOVERS =
            EXECUTABLE_EFFECT_REGISTRY.register("the_lovers", () -> new AbilityEffectEntry(
                    GIVE_ITEM_VIA_LOOT, ctx -> {
                ctx.set(ContextKeys.ITEM, ModItems.RED_HEART.get());
                int amplifier = ctx.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
                double a = amplifier + 1.;
                double b = amplifier + 2.;

                ctx.set(ContextKeys.DOUBLE, List.of(a, b));
            }));
    public static final RegistryObject<IExecutableEffect> THE_MAGICIAN =
            EXECUTABLE_EFFECT_REGISTRY.register("the_magician", () -> new AbilityEffectEntry(
                    STACK_POTION, ctx -> {
                List<PotionProfile> potionProfiles = new ArrayList<>();
                potionProfiles.add(new PotionProfile(ModEffects.TELEPATHY.get(), 600, 1,
                        600, 1, true));
                int amplifier = ctx.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
                if (amplifier > 1){
                    potionProfiles.add(new PotionProfile(ModEffects.POWER_OF_BELIAL.get(), 600, 1,
                            600, 1, true));
                }

                ctx.set(ContextKeys.POTIONS, potionProfiles);
                ctx.set(ContextKeys.BOOLEAN, List.of(false, true));
            }));
    public static final RegistryObject<IExecutableEffect> THE_MAGICIAN_R =
            EXECUTABLE_EFFECT_REGISTRY.register("the_magician_r", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.SOUL_STATE.get(), 300, 0)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> THE_MOON =
            EXECUTABLE_EFFECT_REGISTRY.register("the_moon", () -> new AbilityEffectEntry(
                    TELEPORT_TO_IDENTIFIER, ctx -> {
                ctx.set(ContextKeys.RESOURCE_LOCATIONS,
                        List.of(ModBlockEntities.SECRET_IDENTIFIER_BLOCK_ENTITY.getId()));
            }));
    public static final RegistryObject<IExecutableEffect> THE_STARS =
            EXECUTABLE_EFFECT_REGISTRY.register("the_stars", () -> new AbilityEffectEntry(
                    TELEPORT_TO_IDENTIFIER, ctx -> {
                ctx.set(ContextKeys.RESOURCE_LOCATIONS,
                        List.of(ModBlockEntities.PLANETARIUM_IDENTIFIER_BLOCK_ENTITY.getId(),
                                ModBlockEntities.TREASURE_IDENTIFIER_BLOCK_ENTITY.getId()));
            }));
    public static final RegistryObject<IExecutableEffect> THE_WORLD =
            EXECUTABLE_EFFECT_REGISTRY.register("the_world", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> {
                ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(ModEffects.THE_WORLD.get(), 200, 0)
                ));
            }));
    public static final RegistryObject<IExecutableEffect> TWO_OF_CLUBS =
            EXECUTABLE_EFFECT_REGISTRY.register("two_of_clubs", () -> new AbilityEffectEntry(
                    DOUBLE_ITEM, ctx -> ctx.set(ContextKeys.ITEM, ModItems.BOMB.get())
            ));
    public static final RegistryObject<IExecutableEffect> TWO_OF_SPADES =
            EXECUTABLE_EFFECT_REGISTRY.register("two_of_spades", () -> new AbilityEffectEntry(
                    DOUBLE_ITEM, ctx -> ctx.set(ContextKeys.ITEM, ModItems.KEY.get())
            ));
    public static final RegistryObject<IExecutableEffect> BATTERY =
            EXECUTABLE_EFFECT_REGISTRY.register("battery", () -> new AbilityEffectEntry(
                    CHARGE_ITEM_VIA_BATTERY, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(12.))
            ));
    public static final RegistryObject<IExecutableEffect> SMALL_BATTERY =
            EXECUTABLE_EFFECT_REGISTRY.register("small_battery", () -> new AbilityEffectEntry(
                    CHARGE_ITEM_VIA_BATTERY, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(4.))
            ));
    public static final RegistryObject<IExecutableEffect> MEGA_BATTERY =
            EXECUTABLE_EFFECT_REGISTRY.register("mega_battery", () -> new AbilityEffectEntry(
                    CHARGE_ITEM_VIA_BATTERY, ctx -> {
                        ctx.set(ContextKeys.DOUBLE, List.of(24.));
                        ctx.set(ContextKeys.BOOLEAN, List.of(true));
            }));
    public static final RegistryObject<IExecutableEffect> STRENGTH =
            EXECUTABLE_EFFECT_REGISTRY.register("strength", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(
                            new PotionProfile(MobEffects.DAMAGE_BOOST, 600, 1)
                    ))
            ));
    public static final RegistryObject<IExecutableEffect> BLACK_SACK =
            EXECUTABLE_EFFECT_REGISTRY.register("black_sack", () -> new AbilityEffectEntry(
                    GENERATE_LOOT, ctx -> ctx.set(ContextKeys.RESOURCE_LOCATIONS,
                    List.of(LootTableManager.BLACK_SACK))
            ));
    public static final RegistryObject<IExecutableEffect> GRAB_BAG =
            EXECUTABLE_EFFECT_REGISTRY.register("grab_bag", () -> new AbilityEffectEntry(
                    GENERATE_LOOT, ctx -> ctx.set(ContextKeys.RESOURCE_LOCATIONS,
                    List.of(LootTableManager.GRAB_BAG))
            ));
    public static final RegistryObject<IExecutableEffect> DOUBLE_RED_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("double_red_heart", () -> new AbilityEffectEntry(
                    HEAL, ctx -> ctx.set(ContextKeys.AMPLIFIER, 2.)
            ));
    public static final RegistryObject<IExecutableEffect> GIVE_FRAILTY =
            EXECUTABLE_EFFECT_REGISTRY.register("give_frailty", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(
                            new PotionProfile(ModEffects.FRAILTY.get(), 600, 0,
                                    0, 0, true)
            ))));
    public static final RegistryObject<IExecutableEffect> HALF_RED_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("half_red_heart", () -> new AbilityEffectEntry(
                    HEAL, ctx -> ctx.set(ContextKeys.AMPLIFIER, .5)
            ));
    public static final RegistryObject<IExecutableEffect> GIVE_HALF_FRAILTY =
            EXECUTABLE_EFFECT_REGISTRY.register("give_half_frailty", () -> new AbilityEffectEntry(
                    POTIONS, ctx -> ctx.set(ContextKeys.POTIONS, List.of(
                    new PotionProfile(ModEffects.FRAILTY.get(), 300, 0,
                            0, 0, true)
            ))));
    public static final RegistryObject<IExecutableEffect> HALF_SOUL_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("half_soul_heart", () -> new AbilityEffectEntry(
                    ABSORPTION, ctx -> ctx.set(ContextKeys.AMPLIFIER, .5)
            ));
    public static final RegistryObject<IExecutableEffect> THROW_MEGA_BOMB =
            EXECUTABLE_EFFECT_REGISTRY.register("throw_mega_bomb", () -> new AbilityEffectEntry(
                    THROW_BOMB, ctx -> ctx.set(ContextKeys.DOUBLE, List.of(80., 7.))
            ));
    public static final RegistryObject<IExecutableEffect> ETERNAL_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("eternal_heart", () -> new AbilityEffectEntry(
                    STACK_POTION, ctx -> {
                        ctx.set(ContextKeys.POTIONS, List.of(
                        new PotionProfile(
                                ModEffects.ETERNAL_HEART.get(),
                                3600,
                                0,
                                0,
                                0,
                                false
                        )
                ));
                        ctx.set(ContextKeys.BOOLEAN, List.of(false, true));
            }));
    public static final RegistryObject<IExecutableEffect> GOLDEN_HEART =
            EXECUTABLE_EFFECT_REGISTRY.register("golden_heart", () -> new AbilityEffectEntry(
                    STACK_GILDING_EFFECT, ctx -> {
                        double amplifier = ctx.getEntity().getRandom().nextInt(5,9);
                        ctx.set(ContextKeys.AMPLIFIER, amplifier);
                    }
            ));


    //</editor-fold>

}
