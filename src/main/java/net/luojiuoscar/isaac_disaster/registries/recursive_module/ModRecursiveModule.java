package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModRecursiveModule {
    public static final ResourceKey<Registry<RecursiveModule>> RECURSIVE_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "recursive_module"));

    public static final DeferredRegister<RecursiveModule> RECURSIVE_MODULE_REGISTRY =
            DeferredRegister.create(RECURSIVE_MODULE_KEY, IsaacDisaster.MOD_ID);


    public static final RegistryObject<RecursiveModule> HOLY_MANTLE =
            RECURSIVE_MODULE_REGISTRY.register("holy_mantle", HolyMantle::new);
    public static final RegistryObject<RecursiveModule> WHORE_OF_BABYLON =
            RECURSIVE_MODULE_REGISTRY.register("whore_of_babylon", WhoreOfBabylon::new);
    public static final RegistryObject<RecursiveModule> THE_SOUL =
            RECURSIVE_MODULE_REGISTRY.register("the_soul", TheSoul::new);
    public static final RegistryObject<RecursiveModule> BLACK_CANDLE =
            RECURSIVE_MODULE_REGISTRY.register("black_candle", BlackCandle::new);
    public static final RegistryObject<RecursiveModule> FIRE_RESISTANCE =
            RECURSIVE_MODULE_REGISTRY.register("fire_resistance", FireResistance::new);
    public static final RegistryObject<RecursiveModule> MONEY_IS_POWER =
            RECURSIVE_MODULE_REGISTRY.register("money_is_power", MoneyIsPower::new);
    public static final RegistryObject<RecursiveModule> THE_WIZ =
            RECURSIVE_MODULE_REGISTRY.register("the_wiz", TheWiz::new);
    public static final RegistryObject<RecursiveModule> GILDED_KEY =
            RECURSIVE_MODULE_REGISTRY.register("gilded_key", GildedKey::new);
    public static final RegistryObject<RecursiveModule> THE_LEFT_HAND =
            RECURSIVE_MODULE_REGISTRY.register("the_left_hand", TheLeftHand::new);
    public static final RegistryObject<RecursiveModule> ROCK_BOTTOM =
            RECURSIVE_MODULE_REGISTRY.register("rock_bottom", RockBottom::new);
    public static final RegistryObject<RecursiveModule> VENUS =
            RECURSIVE_MODULE_REGISTRY.register("venus", Venus::new);
    public static final RegistryObject<RecursiveModule> SACK_OF_PENNIES =
            RECURSIVE_MODULE_REGISTRY.register("sack_of_pennies", SackOfPennies::new);
    public static final RegistryObject<RecursiveModule> THE_RELIC =
            RECURSIVE_MODULE_REGISTRY.register("the_relic", TheRelic::new);
    public static final RegistryObject<RecursiveModule> BOMB_BAG =
            RECURSIVE_MODULE_REGISTRY.register("bomb_bag", BombBag::new);
    public static final RegistryObject<RecursiveModule> ATTRACT_ITEM =
            RECURSIVE_MODULE_REGISTRY.register("attract_item", AttractItem::new);
    public static final RegistryObject<RecursiveModule> MAGNETO =
            RECURSIVE_MODULE_REGISTRY.register("magneto", Magneto::new);
    public static final RegistryObject<RecursiveModule> STEAM_SALE =
            RECURSIVE_MODULE_REGISTRY.register("steam_sale", SteamSale::new);
    public static final RegistryObject<RecursiveModule> SAFETY_SCISSORS =
            RECURSIVE_MODULE_REGISTRY.register("safety_scissors", SafetyScissors::new);
}
