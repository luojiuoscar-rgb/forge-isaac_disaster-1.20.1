package net.luojiuoscar.isaac_disaster.registries.periodic_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.impl.*;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPeriodicModules {
    public static final ResourceKey<Registry<PeriodicModule>> PERIODIC_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "periodic_module"));

    public static final DeferredRegister<PeriodicModule> PERIODIC_MODULE_REGISTRY =
            DeferredRegister.create(PERIODIC_MODULE_KEY, IsaacDisaster.MOD_ID);


    public static final RegistryObject<PeriodicModule> HOLY_MANTLE =
            PERIODIC_MODULE_REGISTRY.register("holy_mantle", HolyMantle::new);
    public static final RegistryObject<PeriodicModule> WHORE_OF_BABYLON =
            PERIODIC_MODULE_REGISTRY.register("whore_of_babylon", WhoreOfBabylon::new);
    public static final RegistryObject<PeriodicModule> THE_SOUL =
            PERIODIC_MODULE_REGISTRY.register("the_soul", TheSoul::new);
    public static final RegistryObject<PeriodicModule> BLACK_CANDLE =
            PERIODIC_MODULE_REGISTRY.register("black_candle", BlackCandle::new);
    public static final RegistryObject<PeriodicModule> FIRE_RESISTANCE =
            PERIODIC_MODULE_REGISTRY.register("fire_resistance", FireResistance::new);
    public static final RegistryObject<PeriodicModule> MONEY_IS_POWER =
            PERIODIC_MODULE_REGISTRY.register("money_is_power", MoneyIsPower::new);
    public static final RegistryObject<PeriodicModule> THE_WIZ =
            PERIODIC_MODULE_REGISTRY.register("the_wiz", TheWiz::new);
    public static final RegistryObject<PeriodicModule> GILDED_KEY =
            PERIODIC_MODULE_REGISTRY.register("gilded_key", GildedKey::new);
    public static final RegistryObject<PeriodicModule> THE_LEFT_HAND =
            PERIODIC_MODULE_REGISTRY.register("the_left_hand", TheLeftHand::new);
    public static final RegistryObject<PeriodicModule> VENUS =
            PERIODIC_MODULE_REGISTRY.register("venus", Venus::new);
    public static final RegistryObject<PeriodicModule> SACK_OF_PENNIES =
            PERIODIC_MODULE_REGISTRY.register("sack_of_pennies", SackOfPennies::new);
    public static final RegistryObject<PeriodicModule> THE_RELIC =
            PERIODIC_MODULE_REGISTRY.register("the_relic", TheRelic::new);
    public static final RegistryObject<PeriodicModule> BOMB_BAG =
            PERIODIC_MODULE_REGISTRY.register("bomb_bag", BombBag::new);
    public static final RegistryObject<PeriodicModule> ATTRACT_ITEM =
            PERIODIC_MODULE_REGISTRY.register("attract_item", AttractItem::new);
    public static final RegistryObject<PeriodicModule> MAGNETO =
            PERIODIC_MODULE_REGISTRY.register("magneto", Magneto::new);
    public static final RegistryObject<PeriodicModule> STEAM_SALE =
            PERIODIC_MODULE_REGISTRY.register("steam_sale", SteamSale::new);
    public static final RegistryObject<PeriodicModule> SAFETY_SCISSORS =
            PERIODIC_MODULE_REGISTRY.register("safety_scissors", SafetyScissors::new);
    public static final RegistryObject<PeriodicModule> PLACENTA =
            PERIODIC_MODULE_REGISTRY.register("placenta", Placenta::new);
    public static final RegistryObject<PeriodicModule> THE_WAFER =
            PERIODIC_MODULE_REGISTRY.register("the_wafer", TheWafer::new);
    public static final RegistryObject<PeriodicModule> DADS_RING =
            PERIODIC_MODULE_REGISTRY.register("dads_ring", DadsRing::new);
    public static final RegistryObject<PeriodicModule> SPOOKY_2 =
            PERIODIC_MODULE_REGISTRY.register("two_spooky", TwoSpooky::new);

    public static final RegistryObject<PeriodicModule> DARK_PRINCES_CROWN =
            PERIODIC_MODULE_REGISTRY.register("dark_princes_crown", DarkPrincesCrown::new);

    public static final RegistryObject<PeriodicModule> DEAD_TOOTH =
            PERIODIC_MODULE_REGISTRY.register("dead_tooth", DeadTooth::new);

    public static final RegistryObject<PeriodicModule> ADRENALINE =
            PERIODIC_MODULE_REGISTRY.register("adrenaline", Adrenaline::new);

    public static final RegistryObject<PeriodicModule> LOCUST_OF_WAR =
            PERIODIC_MODULE_REGISTRY.register("locust_of_war", () ->
                    new LocustGeneration(ModFamiliarEntities.LOCUST_OF_WAR.getId()));
    public static final RegistryObject<PeriodicModule> LOCUST_OF_PESTILENCE =
            PERIODIC_MODULE_REGISTRY.register("locust_of_pestilence", () ->
                    new LocustGeneration(ModFamiliarEntities.LOCUST_OF_PESTILENCE.getId()));
    public static final RegistryObject<PeriodicModule> LOCUST_OF_FAMINE =
            PERIODIC_MODULE_REGISTRY.register("locust_of_famine", () ->
                    new LocustGeneration(ModFamiliarEntities.LOCUST_OF_FAMINE.getId()));
    public static final RegistryObject<PeriodicModule> LOCUST_OF_DEATH =
            PERIODIC_MODULE_REGISTRY.register("locust_of_death", () ->
                    new LocustGeneration(ModFamiliarEntities.LOCUST_OF_DEATH.getId()));
    public static final RegistryObject<PeriodicModule> LOCUST_OF_CONQUEST =
            PERIODIC_MODULE_REGISTRY.register("locust_of_conquest", () ->
                    new ConquestLocustGeneration(ModFamiliarEntities.LOCUST_OF_CONQUEST.getId()));
    public static final RegistryObject<PeriodicModule> AQUARIUS =
            PERIODIC_MODULE_REGISTRY.register("aquarius", Aquarius::new);

}
