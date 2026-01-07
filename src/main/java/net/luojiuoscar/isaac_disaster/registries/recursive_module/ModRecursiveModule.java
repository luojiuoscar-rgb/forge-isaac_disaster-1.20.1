package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModRecursiveModule {
    public static final ResourceKey<Registry<IRecursiveModule>> RECURSIVE_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "recursive_module"));

    public static final DeferredRegister<IRecursiveModule> RECURSIVE_MODULE_REGISTRY =
            DeferredRegister.create(RECURSIVE_MODULE_KEY, IsaacDisaster.MOD_ID);


    public static final RegistryObject<IRecursiveModule> HOLY_MANTLE =
            RECURSIVE_MODULE_REGISTRY.register("holy_mantle", HolyMantle::new);
    public static final RegistryObject<IRecursiveModule> WHORE_OF_BABYLON =
            RECURSIVE_MODULE_REGISTRY.register("whore_of_babylon", WhoreOfBabylon::new);
    public static final RegistryObject<IRecursiveModule> THE_SOUL =
            RECURSIVE_MODULE_REGISTRY.register("the_soul", TheSoul::new);
    public static final RegistryObject<IRecursiveModule> BLACK_CANDLE =
            RECURSIVE_MODULE_REGISTRY.register("black_candle", BlackCandle::new);
    public static final RegistryObject<IRecursiveModule> FIRE_RESISTANCE =
            RECURSIVE_MODULE_REGISTRY.register("fire_resistance", FireResistance::new);
    public static final RegistryObject<IRecursiveModule> MONEY_IS_POWER =
            RECURSIVE_MODULE_REGISTRY.register("money_is_power", MoneyIsPower::new);
    public static final RegistryObject<IRecursiveModule> THE_WIZ =
            RECURSIVE_MODULE_REGISTRY.register("the_wiz", TheWiz::new);
    public static final RegistryObject<IRecursiveModule> GILDED_KEY =
            RECURSIVE_MODULE_REGISTRY.register("gilded_key", GildedKey::new);
    public static final RegistryObject<IRecursiveModule> THE_LEFT_HAND =
            RECURSIVE_MODULE_REGISTRY.register("the_left_hand", TheLeftHand::new);
    public static final RegistryObject<IRecursiveModule> ROCK_BOTTOM =
            RECURSIVE_MODULE_REGISTRY.register("rock_bottom", RockBottom::new);

}
