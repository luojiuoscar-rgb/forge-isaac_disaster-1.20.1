package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPassiveAbility {
    public static final ResourceKey<Registry<PassiveAbility>> PASSIVE_ABILITY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "passive_ability"));

    public static final DeferredRegister<PassiveAbility> PASSIVE_ABILITY_REGISTRY =
            DeferredRegister.create(PASSIVE_ABILITY_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<PassiveAbility> A_DOLLAR =
            PASSIVE_ABILITY_REGISTRY.register("a_dollar",
                    () -> new ADollar(ItemId.A_DOLLAR.getId(), ItemId.A_DOLLAR.getLevel()));

    public static final RegistryObject<PassiveAbility> A_QUARTER =
            PASSIVE_ABILITY_REGISTRY.register("a_quarter",
                    () -> new AQuarter(ItemId.A_QUARTER.getId(), ItemId.A_QUARTER.getLevel()));

    public static final RegistryObject<PassiveAbility> A_SNACK =
            PASSIVE_ABILITY_REGISTRY.register("a_snack",
                    () -> new ASnack(ItemId.A_SNACK.getId(), ItemId.A_SNACK.getLevel()));

    public static final RegistryObject<PassiveAbility> BINGE_EATER =
            PASSIVE_ABILITY_REGISTRY.register("binge_eater",
                    () -> new BingeEater(ItemId.BINGE_EATER.getId(), ItemId.BINGE_EATER.getLevel()));

    public static final RegistryObject<PassiveAbility> BLACK_CANDLE =
            PASSIVE_ABILITY_REGISTRY.register("black_candle",
                    () -> new BlackCandle(ItemId.BLACK_CANDLE.getId(), ItemId.BLACK_CANDLE.getLevel()));

    public static final RegistryObject<PassiveAbility> BLOOD_OF_THE_MARTYR =
            PASSIVE_ABILITY_REGISTRY.register("blood_of_the_martyr",
                    () -> new BloodOfTheMartyr(ItemId.BLOOD_OF_THE_MARTYR.getId(), ItemId.BLOOD_OF_THE_MARTYR.getLevel()));

    public static final RegistryObject<PassiveAbility> BLUE_CAP =
            PASSIVE_ABILITY_REGISTRY.register("blue_cap",
                    () -> new BlueCap(ItemId.BLUE_CAP.getId(), ItemId.BLUE_CAP.getLevel()));

    public static final RegistryObject<PassiveAbility> BOBBY_BOMB =
            PASSIVE_ABILITY_REGISTRY.register("bobby_bomb",
                    () -> new BobbyBomb(ItemId.BOBBY_BOMB.getId(), ItemId.BOBBY_BOMB.getLevel()));

    public static final RegistryObject<PassiveAbility> BOMBER_BOY =
            PASSIVE_ABILITY_REGISTRY.register("bomber_boy",
                    () -> new BomberBoy(ItemId.BOMBER_BOY.getId(), ItemId.BOMBER_BOY.getLevel()));

    public static final RegistryObject<PassiveAbility> BOOM =
            PASSIVE_ABILITY_REGISTRY.register("boom",
                    () -> new Boom(ItemId.BOOM.getId(), ItemId.BOOM.getLevel()));

    public static final RegistryObject<PassiveAbility> BREAKFAST =
            PASSIVE_ABILITY_REGISTRY.register("breakfast",
                    () -> new Breakfast(ItemId.BREAKFAST.getId(), ItemId.BREAKFAST.getLevel()));

    public static final RegistryObject<PassiveAbility> CAFFEINE_PILL =
            PASSIVE_ABILITY_REGISTRY.register("caffeine_pill",
                    () -> new CaffeinePill(ItemId.CAFFEINE_PILL.getId(), ItemId.CAFFEINE_PILL.getLevel()));

    public static final RegistryObject<PassiveAbility> CAR_BATTERY =
            PASSIVE_ABILITY_REGISTRY.register("car_battery",
                    () -> new CarBattery(ItemId.CAR_BATTERY.getId(), ItemId.CAR_BATTERY.getLevel()));

    public static final RegistryObject<PassiveAbility> CHAOS =
            PASSIVE_ABILITY_REGISTRY.register("chaos",
                    () -> new Chaos(ItemId.CHAOS.getId(), ItemId.CHAOS.getLevel()));

    public static final RegistryObject<PassiveAbility> CRICKETS_HEAD =
            PASSIVE_ABILITY_REGISTRY.register("crickets_head",
                    () -> new CricketsHead(ItemId.CRICKETS_HEAD.getId(), ItemId.CRICKETS_HEAD.getLevel()));

    public static final RegistryObject<PassiveAbility> CUPIDS_ARROW =
            PASSIVE_ABILITY_REGISTRY.register("cupids_arrow",
                    () -> new CupidsArrow(ItemId.CUPIDS_ARROW.getId(), ItemId.CUPIDS_ARROW.getLevel()));

    public static final RegistryObject<PassiveAbility> CURSE_OF_THE_TOWER =
            PASSIVE_ABILITY_REGISTRY.register("curse_of_the_tower",
                    () -> new CurseOfTheTower(ItemId.CURSE_OF_THE_TOWER.getId(), ItemId.CURSE_OF_THE_TOWER.getLevel()));

    public static final RegistryObject<PassiveAbility> DEAD_DOVE =
            PASSIVE_ABILITY_REGISTRY.register("dead_dove",
                    () -> new DeadDove(ItemId.DEAD_DOVE.getId(), ItemId.DEAD_DOVE.getLevel()));

    public static final RegistryObject<PassiveAbility> DESSERT =
            PASSIVE_ABILITY_REGISTRY.register("dessert",
                    () -> new Dessert(ItemId.DESSERT.getId(), ItemId.DESSERT.getLevel()));

    public static final RegistryObject<PassiveAbility> DINNER =
            PASSIVE_ABILITY_REGISTRY.register("dinner",
                    () -> new Dinner(ItemId.DINNER.getId(), ItemId.DINNER.getLevel()));

    public static final RegistryObject<PassiveAbility> ECHO_CHAMBER =
            PASSIVE_ABILITY_REGISTRY.register("echo_chamber",
                    () -> new EchoChamber(ItemId.ECHO_CHAMBER.getId(), ItemId.ECHO_CHAMBER.getLevel()));

    public static final RegistryObject<PassiveAbility> EXPERIMENTAL_TREATMENT =
            PASSIVE_ABILITY_REGISTRY.register("experimental_treatment",
                    () -> new ExperimentalTreatment(ItemId.EXPERIMENTAL_TREATMENT.getId(), ItemId.EXPERIMENTAL_TREATMENT.getLevel()));

    public static final RegistryObject<PassiveAbility> FALSE_PHD =
            PASSIVE_ABILITY_REGISTRY.register("false_phd",
                    () -> new FalsePhd(ItemId.FALSE_PHD.getId(), ItemId.FALSE_PHD.getLevel()));

    public static final RegistryObject<PassiveAbility> FAST_BOMB =
            PASSIVE_ABILITY_REGISTRY.register("fast_bomb",
                    () -> new FastBomb(ItemId.FAST_BOMB.getId(), ItemId.FAST_BOMB.getLevel()));

    public static final RegistryObject<PassiveAbility> GLASS_EYE =
            PASSIVE_ABILITY_REGISTRY.register("glass_eye",
                    () -> new GlassEye(ItemId.GLASS_EYE.getId(), ItemId.GLASS_EYE.getLevel()));

    public static final RegistryObject<PassiveAbility> GLITCHED_CROWN =
            PASSIVE_ABILITY_REGISTRY.register("glitched_crown",
                    () -> new GlitchedCrown(ItemId.GLITCHED_CROWN.getId(), ItemId.GLITCHED_CROWN.getLevel()));

    public static final RegistryObject<PassiveAbility> GROWTH_HORMONES =
            PASSIVE_ABILITY_REGISTRY.register("growth_hormones",
                    () -> new GrowthHormones(ItemId.GROWTH_HORMONES.getId(), ItemId.GROWTH_HORMONES.getLevel()));

    public static final RegistryObject<PassiveAbility> HABIT =
            PASSIVE_ABILITY_REGISTRY.register("habit",
                    () -> new Habit(ItemId.HABIT.getId(), ItemId.HABIT.getLevel()));

    public static final RegistryObject<PassiveAbility> HEART =
            PASSIVE_ABILITY_REGISTRY.register("heart",
                    () -> new Heart(ItemId.HEART.getId(), ItemId.HEART.getLevel()));

    public static final RegistryObject<PassiveAbility> HOLY_MANTLE =
            PASSIVE_ABILITY_REGISTRY.register("holy_mantle",
                    () -> new HolyMantle(ItemId.HOLY_MANTLE.getId(), ItemId.HOLY_MANTLE.getLevel()));

    public static final RegistryObject<PassiveAbility> HOST_HAT =
            PASSIVE_ABILITY_REGISTRY.register("host_hat",
                    () -> new HostHat(ItemId.HOST_HAT.getId(), ItemId.HOST_HAT.getLevel()));

    public static final RegistryObject<PassiveAbility> HOT_BOMB =
            PASSIVE_ABILITY_REGISTRY.register("hot_bomb",
                    () -> new HotBomb(ItemId.HOT_BOMB.getId(), ItemId.HOT_BOMB.getLevel()));

    public static final RegistryObject<PassiveAbility> IPECAC =
            PASSIVE_ABILITY_REGISTRY.register("ipecac",
                    () -> new Ipecac(ItemId.IPECAC.getId(), ItemId.IPECAC.getLevel()));

    public static final RegistryObject<PassiveAbility> LUNCH =
            PASSIVE_ABILITY_REGISTRY.register("lunch",
                    () -> new Lunch(ItemId.LUNCH.getId(), ItemId.LUNCH.getLevel()));

    public static final RegistryObject<PassiveAbility> MAGIC_MUSHROOM =
            PASSIVE_ABILITY_REGISTRY.register("magic_mushroom",
                    () -> new MagicMushroom(ItemId.MAGIC_MUSHROOM.getId(), ItemId.MAGIC_MUSHROOM.getLevel()));

    public static final RegistryObject<PassiveAbility> MAGIC_SCAB =
            PASSIVE_ABILITY_REGISTRY.register("magic_scab",
                    () -> new MagicScab(ItemId.MAGIC_SCAB.getId(), ItemId.MAGIC_SCAB.getLevel()));

    public static final RegistryObject<PassiveAbility> MARKED =
            PASSIVE_ABILITY_REGISTRY.register("marked",
                    () -> new Marked(ItemId.MARKED.getId(), ItemId.MARKED.getLevel()));

    public static final RegistryObject<PassiveAbility> MIDNIGHT_SNACK =
            PASSIVE_ABILITY_REGISTRY.register("midnight_snack",
                    () -> new MidnightSnack(ItemId.MIDNIGHT_SNACK.getId(), ItemId.MIDNIGHT_SNACK.getLevel()));

    public static final RegistryObject<PassiveAbility> MINI_MUSH =
            PASSIVE_ABILITY_REGISTRY.register("mini_mush",
                    () -> new MiniMush(ItemId.MINI_MUSH.getId(), ItemId.MINI_MUSH.getLevel()));

    public static final RegistryObject<PassiveAbility> MITRE =
            PASSIVE_ABILITY_REGISTRY.register("mitre",
                    () -> new Mitre(ItemId.MITRE.getId(), ItemId.MITRE.getLevel()));

    public static final RegistryObject<PassiveAbility> MONEY_IS_POWER =
            PASSIVE_ABILITY_REGISTRY.register("money_is_power",
                    () -> new MoneyIsPower(ItemId.MONEY_IS_POWER.getId(), ItemId.MONEY_IS_POWER.getLevel()));

    public static final RegistryObject<PassiveAbility> MR_MEGA =
            PASSIVE_ABILITY_REGISTRY.register("mr_mega",
                    () -> new MrMega(ItemId.MR_MEGA.getId(), ItemId.MR_MEGA.getLevel()));

    public static final RegistryObject<PassiveAbility> MUTANT_SPIDER =
            PASSIVE_ABILITY_REGISTRY.register("mutant_spider",
                    () -> new MutantSpider(ItemId.MUTANT_SPIDER.getId(), ItemId.MUTANT_SPIDER.getLevel()));

    public static final RegistryObject<PassiveAbility> MY_REFLECTION =
            PASSIVE_ABILITY_REGISTRY.register("my_reflection",
                    () -> new MyReflection(ItemId.MY_REFLECTION.getId(), ItemId.MY_REFLECTION.getLevel()));

    public static final RegistryObject<PassiveAbility> PERFECT_VISION =
            PASSIVE_ABILITY_REGISTRY.register("perfect_vision",
                    () -> new PerfectVision(ItemId.PERFECT_VISION.getId(), ItemId.PERFECT_VISION.getLevel()));

    public static final RegistryObject<PassiveAbility> PHD =
            PASSIVE_ABILITY_REGISTRY.register("phd",
                    () -> new Phd(ItemId.PHD.getId(), ItemId.PHD.getLevel()));

    public static final RegistryObject<PassiveAbility> PIGGY_BANK =
            PASSIVE_ABILITY_REGISTRY.register("piggy_bank",
                    () -> new PiggyBank(ItemId.PIGGY_BANK.getId(), ItemId.PIGGY_BANK.getLevel()));

    public static final RegistryObject<PassiveAbility> PISCES =
            PASSIVE_ABILITY_REGISTRY.register("pisces",
                    () -> new Pisces(ItemId.PISCES.getId(), ItemId.PISCES.getLevel()));

    public static final RegistryObject<PassiveAbility> POLYPHEMUS =
            PASSIVE_ABILITY_REGISTRY.register("polyphemus",
                    () -> new Polyphemus(ItemId.POLYPHEMUS.getId(), ItemId.POLYPHEMUS.getLevel()));

    public static final RegistryObject<PassiveAbility> PYRO =
            PASSIVE_ABILITY_REGISTRY.register("pyro",
                    () -> new Pyro(ItemId.PYRO.getId(), ItemId.PYRO.getLevel()));

    public static final RegistryObject<PassiveAbility> PYROMANIAC =
            PASSIVE_ABILITY_REGISTRY.register("pyromaniac",
                    () -> new Pyromaniac(ItemId.PYROMANIAC.getId(), ItemId.PYROMANIAC.getLevel()));

    public static final RegistryObject<PassiveAbility> RAW_LIVER =
            PASSIVE_ABILITY_REGISTRY.register("raw_liver",
                    () -> new RawLiver(ItemId.RAW_LIVER.getId(), ItemId.RAW_LIVER.getLevel()));

    public static final RegistryObject<PassiveAbility> ROID_RAGE =
            PASSIVE_ABILITY_REGISTRY.register("roid_rage",
                    () -> new RoidRage(ItemId.ROID_RAGE.getId(), ItemId.ROID_RAGE.getLevel()));

    public static final RegistryObject<PassiveAbility> ROTTEN_MEAT =
            PASSIVE_ABILITY_REGISTRY.register("rotten_meat",
                    () -> new RottenMeat(ItemId.ROTTEN_MEAT.getId(), ItemId.ROTTEN_MEAT.getLevel()));

    public static final RegistryObject<PassiveAbility> RUBBER_CEMENT =
            PASSIVE_ABILITY_REGISTRY.register("rubber_cement",
                    () -> new RubberCement(ItemId.RUBBER_CEMENT.getId(), ItemId.RUBBER_CEMENT.getLevel()));

    public static final RegistryObject<PassiveAbility> SACK_HEAD =
            PASSIVE_ABILITY_REGISTRY.register("sack_head",
                    () -> new SackHead(ItemId.SACK_HEAD.getId(), ItemId.SACK_HEAD.getLevel()));

    public static final RegistryObject<PassiveAbility> SACRED_ORB =
            PASSIVE_ABILITY_REGISTRY.register("sacred_orb",
                    () -> new SacredOrb(ItemId.SACRED_ORB.getId(), ItemId.SACRED_ORB.getLevel()));

    public static final RegistryObject<PassiveAbility> SAFETY_PIN =
            PASSIVE_ABILITY_REGISTRY.register("safety_pin",
                    () -> new SafetyPin(ItemId.SAFETY_PIN.getId(), ItemId.SAFETY_PIN.getLevel()));

    public static final RegistryObject<PassiveAbility> SCATTER_BOMB =
            PASSIVE_ABILITY_REGISTRY.register("scatter_bomb",
                    () -> new ScatterBomb(ItemId.SCATTER_BOMB.getId(), ItemId.SCATTER_BOMB.getLevel()));

    public static final RegistryObject<PassiveAbility> SCREW =
            PASSIVE_ABILITY_REGISTRY.register("screw",
                    () -> new Screw(ItemId.SCREW.getId(), ItemId.SCREW.getLevel()));

    public static final RegistryObject<PassiveAbility> SPEED_BALL =
            PASSIVE_ABILITY_REGISTRY.register("speed_ball",
                    () -> new SpeedBall(ItemId.SPEED_BALL.getId(), ItemId.SPEED_BALL.getLevel()));

    public static final RegistryObject<PassiveAbility> SPOON_BENDER =
            PASSIVE_ABILITY_REGISTRY.register("spoon_bender",
                    () -> new SpoonBender(ItemId.SPOON_BENDER.getId(), ItemId.SPOON_BENDER.getLevel()));

    public static final RegistryObject<PassiveAbility> STEVEN =
            PASSIVE_ABILITY_REGISTRY.register("steven",
                    () -> new Steven(ItemId.STEVEN.getId(), ItemId.STEVEN.getLevel()));

    public static final RegistryObject<PassiveAbility> SUPPER =
            PASSIVE_ABILITY_REGISTRY.register("supper",
                    () -> new Supper(ItemId.SUPPER.getId(), ItemId.SUPPER.getLevel()));

    public static final RegistryObject<PassiveAbility> SYNTHOIL =
            PASSIVE_ABILITY_REGISTRY.register("synthoil",
                    () -> new Synthoil(ItemId.SYNTHOIL.getId(), ItemId.SYNTHOIL.getLevel()));

    public static final RegistryObject<PassiveAbility> TAROT_CLOTH =
            PASSIVE_ABILITY_REGISTRY.register("tarot_cloth",
                    () -> new TarotCloth(ItemId.TAROT_CLOTH.getId(), ItemId.TAROT_CLOTH.getLevel()));

    public static final RegistryObject<PassiveAbility> TECHNOLOGY =
            PASSIVE_ABILITY_REGISTRY.register("technology",
                    () -> new Technology(ItemId.TECHNOLOGY.getId(), ItemId.TECHNOLOGY.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_BATTERY =
            PASSIVE_ABILITY_REGISTRY.register("the_battery",
                    () -> new TheBattery(ItemId.THE_BATTERY.getId(), ItemId.THE_BATTERY.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_BODY =
            PASSIVE_ABILITY_REGISTRY.register("the_body",
                    () -> new TheBody(ItemId.THE_BODY.getId(), ItemId.THE_BODY.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_COMMON_COLD =
            PASSIVE_ABILITY_REGISTRY.register("the_common_cold",
                    () -> new TheCommonCold(ItemId.THE_COMMON_COLD.getId(), ItemId.THE_COMMON_COLD.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_INNER_EYE =
            PASSIVE_ABILITY_REGISTRY.register("the_inner_eye",
                    () -> new TheInnerEye(ItemId.THE_INNER_EYE.getId(), ItemId.THE_INNER_EYE.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_SAD_ONION =
            PASSIVE_ABILITY_REGISTRY.register("the_sad_onion",
                    () -> new TheSadOnion(ItemId.THE_SAD_ONION.getId(), ItemId.THE_SAD_ONION.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_SOUL =
            PASSIVE_ABILITY_REGISTRY.register("the_soul",
                    () -> new TheSoul(ItemId.THE_SOUL.getId(), ItemId.THE_SOUL.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_WAFER =
            PASSIVE_ABILITY_REGISTRY.register("the_wafer",
                    () -> new TheWafer(ItemId.THE_WAFER.getId(), ItemId.THE_WAFER.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_WIZ =
            PASSIVE_ABILITY_REGISTRY.register("the_wiz",
                    () -> new TheWiz(ItemId.THE_WIZ.getId(), ItemId.THE_WIZ.getLevel()));

    public static final RegistryObject<PassiveAbility> TINY_PLANET =
            PASSIVE_ABILITY_REGISTRY.register("tiny_planet",
                    () -> new TinyPlanet(ItemId.TINY_PLANET.getId(), ItemId.TINY_PLANET.getLevel()));

    public static final RegistryObject<PassiveAbility> TOOTH_PICKS =
            PASSIVE_ABILITY_REGISTRY.register("tooth_picks",
                    () -> new ToothPicks(ItemId.TOOTH_PICKS.getId(), ItemId.TOOTH_PICKS.getLevel()));

    public static final RegistryObject<PassiveAbility> TORN_PHOTO =
            PASSIVE_ABILITY_REGISTRY.register("torn_photo",
                    () -> new TornPhoto(ItemId.TORN_PHOTO.getId(), ItemId.TORN_PHOTO.getLevel()));

    public static final RegistryObject<PassiveAbility> TRANSCENDENCE =
            PASSIVE_ABILITY_REGISTRY.register("transcendence",
                    () -> new Transcendence(ItemId.TRANSCENDENCE.getId(), ItemId.TRANSCENDENCE.getLevel()));

    public static final RegistryObject<PassiveAbility> VOLT_4P5 =
            PASSIVE_ABILITY_REGISTRY.register("volt_4p5",
                    () -> new Volt4p5(ItemId.VOLT_4P5.getId(), ItemId.VOLT_4P5.getLevel()));

    public static final RegistryObject<PassiveAbility> VOLT_9 =
            PASSIVE_ABILITY_REGISTRY.register("volt_9",
                    () -> new Volt9(ItemId.VOLT_9.getId(), ItemId.VOLT_9.getLevel()));

    public static final RegistryObject<PassiveAbility> WHORE_OF_BABYLON =
            PASSIVE_ABILITY_REGISTRY.register("whore_of_babylon",
                    () -> new WhoreOfBabylon(ItemId.WHORE_OF_BABYLON.getId(), ItemId.WHORE_OF_BABYLON.getLevel()));

    public static final RegistryObject<PassiveAbility> WIRE_COAT_HANGER =
            PASSIVE_ABILITY_REGISTRY.register("wire_coat_hanger",
                    () -> new WireCoatHanger(ItemId.WIRE_COAT_HANGER.getId(), ItemId.WIRE_COAT_HANGER.getLevel()));

    public static final RegistryObject<PassiveAbility> WOODEN_SPOON =
            PASSIVE_ABILITY_REGISTRY.register("wooden_spoon",
                    () -> new WoodenSpoon(ItemId.WOODEN_SPOON.getId(), ItemId.WOODEN_SPOON.getLevel()));

    public static final RegistryObject<PassiveAbility> BRIMSTONE =
            PASSIVE_ABILITY_REGISTRY.register("brimstone",
                    () -> new Brimstone(ItemId.BRIMSTONE.getId(), ItemId.BRIMSTONE.getLevel()));

    public static final RegistryObject<PassiveAbility> C_SECTION =
            PASSIVE_ABILITY_REGISTRY.register("c_section",
                    () -> new CSection(ItemId.C_SECTION.getId(), ItemId.C_SECTION.getLevel()));

    public static final RegistryObject<PassiveAbility> CURSED_EYE =
            PASSIVE_ABILITY_REGISTRY.register("cursed_eye",
                    () -> new CursedEye(ItemId.CURSED_EYE.getId(), ItemId.CURSED_EYE.getLevel()));

    public static final RegistryObject<PassiveAbility> NEPTUNUS =
            PASSIVE_ABILITY_REGISTRY.register("neptunus",
                    () -> new Neptunus(ItemId.NEPTUNUS.getId(), ItemId.NEPTUNUS.getLevel()));

    public static final RegistryObject<PassiveAbility> TECHNOLOGY2 =
            PASSIVE_ABILITY_REGISTRY.register("technology2",
                    () -> new Technology2(ItemId.TECHNOLOGY2.getId(), ItemId.TECHNOLOGY2.getLevel()));

    public static final RegistryObject<PassiveAbility> ROCK_BOTTOM =
            PASSIVE_ABILITY_REGISTRY.register("rock_bottom",
                    () -> new RockBottom(ItemId.ROCK_BOTTOM.getId(), ItemId.ROCK_BOTTOM.getLevel()));

    public static final RegistryObject<PassiveAbility> TERRA =
            PASSIVE_ABILITY_REGISTRY.register("terra",
                    () -> new Terra(ItemId.TERRA.getId(), ItemId.TERRA.getLevel()));

    public static final RegistryObject<PassiveAbility> VENUS =
            PASSIVE_ABILITY_REGISTRY.register("venus",
                    () -> new Venus(ItemId.VENUS.getId(), ItemId.VENUS.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_VIRUS =
            PASSIVE_ABILITY_REGISTRY.register("the_virus",
                    () -> new TheVirus(ItemId.THE_VIRUS.getId(), ItemId.THE_VIRUS.getLevel()));

    public static final RegistryObject<PassiveAbility> SKELETON_KEY =
            PASSIVE_ABILITY_REGISTRY.register("skeleton_key",
                    () -> new SkeletonKey(ItemId.SKELETON_KEY.getId(), ItemId.SKELETON_KEY.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_BELT =
            PASSIVE_ABILITY_REGISTRY.register("the_belt",
                    () -> new TheBelt(ItemId.THE_BELT.getId(), ItemId.THE_BELT.getLevel()));

    public static final RegistryObject<PassiveAbility> LUCKY_FOOT =
            PASSIVE_ABILITY_REGISTRY.register("lucky_foot",
                    () -> new LuckyFoot(ItemId.LUCKY_FOOT.getId(), ItemId.LUCKY_FOOT.getLevel()));

    public static final RegistryObject<PassiveAbility> CHARM_OF_THE_VAMPIRE =
            PASSIVE_ABILITY_REGISTRY.register("charm_of_the_vampire",
                    () -> new CharmOfTheVampire(ItemId.CHARM_OF_THE_VAMPIRE.getId(), ItemId.CHARM_OF_THE_VAMPIRE.getLevel()));

    public static final RegistryObject<PassiveAbility> SUPER_BANDAGE =
            PASSIVE_ABILITY_REGISTRY.register("super_bandage",
                    () -> new SuperBandage(ItemId.SUPER_BANDAGE.getId(), ItemId.SUPER_BANDAGE.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_SMALL_ROCK =
            PASSIVE_ABILITY_REGISTRY.register("the_small_rock",
                    () -> new TheSmallRock(ItemId.THE_SMALL_ROCK.getId(), ItemId.THE_SMALL_ROCK.getLevel()));

    public static final RegistryObject<PassiveAbility> SACK_OF_PENNIES =
            PASSIVE_ABILITY_REGISTRY.register("sack_of_pennies",
                    () -> new SackOfPennies(ItemId.SACK_OF_PENNIES.getId(), ItemId.SACK_OF_PENNIES.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_RELIC =
            PASSIVE_ABILITY_REGISTRY.register("the_relic",
                    () -> new TheRelic(ItemId.THE_RELIC.getId(), ItemId.THE_RELIC.getLevel()));

    public static final RegistryObject<PassiveAbility> BOMB_BAG =
            PASSIVE_ABILITY_REGISTRY.register("bomb_bag",
                    () -> new BombBag(ItemId.BOMB_BAG.getId(), ItemId.BOMB_BAG.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_HALO =
            PASSIVE_ABILITY_REGISTRY.register("the_halo",
                    () -> new TheHalo(ItemId.THE_HALO.getId(), ItemId.THE_HALO.getLevel()));

    public static final RegistryObject<PassiveAbility> OUIJA_BOARD =
            PASSIVE_ABILITY_REGISTRY.register("ouija_board",
                    () -> new OuijaBoard(ItemId.OUIJA_BOARD.getId(), ItemId.OUIJA_BOARD.getLevel()));

    public static final RegistryObject<PassiveAbility> THIN_ODD_MUSHROOM =
            PASSIVE_ABILITY_REGISTRY.register("thin_odd_mushroom",
                    () -> new ThinOddMushroom(ItemId.THIN_ODD_MUSHROOM.getId(), ItemId.THIN_ODD_MUSHROOM.getLevel()));

    public static final RegistryObject<PassiveAbility> LARGE_ODD_MUSHROOM =
            PASSIVE_ABILITY_REGISTRY.register("large_odd_mushroom",
                    () -> new LargeOddMushroom(ItemId.LARGE_ODD_MUSHROOM.getId(), ItemId.LARGE_ODD_MUSHROOM.getLevel()));

    public static final RegistryObject<PassiveAbility> PENTAGRAM =
            PASSIVE_ABILITY_REGISTRY.register("pentagram",
                    () -> new Pentagram(ItemId.PENTAGRAM.getId(), ItemId.PENTAGRAM.getLevel()));

    public static final RegistryObject<PassiveAbility> MAGNETO =
            PASSIVE_ABILITY_REGISTRY.register("magneto",
                    () -> new Magneto(ItemId.MAGNETO.getId(), ItemId.MAGNETO.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_MARK =
            PASSIVE_ABILITY_REGISTRY.register("the_mark",
                    () -> new TheMark(ItemId.THE_MARK.getId(), ItemId.THE_MARK.getLevel()));

    public static final RegistryObject<PassiveAbility> THE_PACT =
            PASSIVE_ABILITY_REGISTRY.register("the_pact",
                    () -> new ThePact(ItemId.THE_PACT.getId(), ItemId.THE_PACT.getLevel()));

    public static final RegistryObject<PassiveAbility> LORD_OF_THE_PIT =
            PASSIVE_ABILITY_REGISTRY.register("lord_of_the_pit",
                    () -> new LordOfThePit(ItemId.LORD_OF_THE_PIT.getId(), ItemId.LORD_OF_THE_PIT.getLevel()));

    public static final RegistryObject<PassiveAbility> BUCKET_OF_LARD =
            PASSIVE_ABILITY_REGISTRY.register("bucket_of_lard",
                    () -> new BucketOfLard(ItemId.BUCKET_OF_LARD.getId(), ItemId.BUCKET_OF_LARD.getLevel()));

    public static final RegistryObject<PassiveAbility> STIGMATA =
            PASSIVE_ABILITY_REGISTRY.register("stigmata",
                    () -> new Stigmata(ItemId.STIGMATA.getId(), ItemId.STIGMATA.getLevel()));

    public static final RegistryObject<PassiveAbility> PAGEANT_BOY =
            PASSIVE_ABILITY_REGISTRY.register("pageant_boy",
                    () -> new PageantBoy(ItemId.PAGEANT_BOY.getId(), ItemId.PAGEANT_BOY.getLevel()));

    public static final RegistryObject<PassiveAbility> SPIRIT_OF_THE_NIGHT =
            PASSIVE_ABILITY_REGISTRY.register("spirit_of_the_night",
                    () -> new SpiritOfTheNight(ItemId.SPIRIT_OF_THE_NIGHT.getId(), ItemId.SPIRIT_OF_THE_NIGHT.getLevel()));

    public static final RegistryObject<PassiveAbility> STEAM_SALE =
            PASSIVE_ABILITY_REGISTRY.register("steam_sale",
                    () -> new SteamSale(ItemId.STEAM_SALE.getId(), ItemId.STEAM_SALE.getLevel()));

    public static final RegistryObject<PassiveAbility> IRON_BAR =
            PASSIVE_ABILITY_REGISTRY.register("iron_bar",
                    () -> new IronBar(ItemId.IRON_BAR.getId(), ItemId.IRON_BAR.getLevel()));

    public static final RegistryObject<PassiveAbility> MIDAS_TOUCH =
            PASSIVE_ABILITY_REGISTRY.register("midas_touch",
                    () -> new MidasTouch(ItemId.MIDAS_TOUCH.getId(), ItemId.MIDAS_TOUCH.getLevel()));

    public static final RegistryObject<PassiveAbility> BOGO_BOMBS =
            PASSIVE_ABILITY_REGISTRY.register("bogo_bombs",
                    () -> new BogoBombs(ItemId.BOGO_BOMBS.getId(), ItemId.BOGO_BOMBS.getLevel()));

    public static final RegistryObject<PassiveAbility> LOKIS_HORNS =
            PASSIVE_ABILITY_REGISTRY.register("lokis_horns",
                    () -> new LokisHorns(ItemId.LOKIS_HORNS.getId(), ItemId.LOKIS_HORNS.getLevel()));

    public static final RegistryObject<PassiveAbility> SACRED_HEART =
            PASSIVE_ABILITY_REGISTRY.register("sacred_heart",
                    () -> new SacredHeart(ItemId.SACRED_HEART.getId(), ItemId.SACRED_HEART.getLevel()));

    public static final RegistryObject<PassiveAbility> CAT_O_NINE_TAILS =
            PASSIVE_ABILITY_REGISTRY.register("cat_o_nine_tails",
                    () -> new CatONineTails(ItemId.CAT_O_NINE_TAILS.getId(), ItemId.CAT_O_NINE_TAILS.getLevel()));

    public static final RegistryObject<PassiveAbility> STEM_CELLS =
            PASSIVE_ABILITY_REGISTRY.register("stem_cells",
                    () -> new StemCells(ItemId.STEM_CELLS.getId(), ItemId.STEM_CELLS.getLevel()));

    public static final RegistryObject<PassiveAbility> FATE =
            PASSIVE_ABILITY_REGISTRY.register("fate",
                    () -> new Fate(ItemId.FATE.getId(), ItemId.FATE.getLevel()));

    public static final RegistryObject<PassiveAbility> HOLY_GRAIL =
            PASSIVE_ABILITY_REGISTRY.register("holy_grail",
                    () -> new HolyGrail(ItemId.HOLY_GRAIL.getId(), ItemId.HOLY_GRAIL.getLevel()));

    public static final RegistryObject<PassiveAbility> SMB_SUPER_FAN =
            PASSIVE_ABILITY_REGISTRY.register("smb_super_fan",
                    () -> new SMBSuperFan(ItemId.SMB_SUPER_FAN.getId(), ItemId.SMB_SUPER_FAN.getLevel()));

    public static final RegistryObject<PassiveAbility> MEAT =
            PASSIVE_ABILITY_REGISTRY.register("meat",
                    () -> new Meat(ItemId.MEAT.getId(), ItemId.MEAT.getLevel()));

    public static final RegistryObject<PassiveAbility> MAGIC_8_BALL =
            PASSIVE_ABILITY_REGISTRY.register("magic_8_ball",
                    () -> new Magic8Ball(ItemId.MAGIC_8_BALL.getId(), ItemId.MAGIC_8_BALL.getLevel()));

    public static final RegistryObject<PassiveAbility> MOMS_COIN_PURSE =
            PASSIVE_ABILITY_REGISTRY.register("moms_coin_purse",
                    () -> new MomsCoinPurse(ItemId.MOMS_COIN_PURSE.getId(), ItemId.MOMS_COIN_PURSE.getLevel()));

    public static final RegistryObject<PassiveAbility> SQUEEZY =
            PASSIVE_ABILITY_REGISTRY.register("squeezy",
                    () -> new Squeezy(ItemId.SQUEEZY.getId(), ItemId.SQUEEZY.getLevel()));

    public static final RegistryObject<PassiveAbility> JESUS_JUICE =
            PASSIVE_ABILITY_REGISTRY.register("jesus_juice",
                    () -> new JesusJuice(ItemId.JESUS_JUICE.getId(), ItemId.JESUS_JUICE.getLevel()));

    public static final RegistryObject<PassiveAbility> MOMS_KEY =
            PASSIVE_ABILITY_REGISTRY.register("moms_key",
                    () -> new MomsKey(ItemId.MOMS_KEY.getId(), ItemId.MOMS_KEY.getLevel()));

    public static final RegistryObject<PassiveAbility> MOMS_EYESHADOW =
            PASSIVE_ABILITY_REGISTRY.register("moms_eyeshadow",
                    () -> new MomsEyeshadow(ItemId.MOMS_EYESHADOW.getId(), ItemId.MOMS_EYESHADOW.getLevel()));

    public static final RegistryObject<PassiveAbility> FANNY_PACK =
            PASSIVE_ABILITY_REGISTRY.register("fanny_pack",
                    () -> new FannyPack(ItemId.FANNY_PACK.getId(), ItemId.FANNY_PACK.getLevel()));

}
