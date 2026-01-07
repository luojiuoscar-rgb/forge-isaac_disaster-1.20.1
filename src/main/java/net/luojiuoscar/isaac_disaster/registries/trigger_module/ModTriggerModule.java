package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTriggerModule {
    public static final ResourceKey<Registry<ITriggerModule>> TRIGGER_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module"));

    public static final DeferredRegister<ITriggerModule> TRIGGER_MODULE_REGISTRY =
            DeferredRegister.create(TRIGGER_MODULE_KEY, IsaacDisaster.MOD_ID);


    public static final RegistryObject<ITriggerModule> PIGGY_BANK =
            TRIGGER_MODULE_REGISTRY.register("piggy_bank", PiggyBank::new);
    public static final RegistryObject<ITriggerModule> CURSE_OF_THE_TOWER =
            TRIGGER_MODULE_REGISTRY.register("curse_of_the_tower", CurseOfTheTower::new);
    public static final RegistryObject<ITriggerModule> HABIT =
            TRIGGER_MODULE_REGISTRY.register("habit", Habit::new);
    public static final RegistryObject<ITriggerModule> THE_WAFER =
            TRIGGER_MODULE_REGISTRY.register("the_wafer", TheWafer::new);
    public static final RegistryObject<ITriggerModule> THE_COMMON_COLD =
            TRIGGER_MODULE_REGISTRY.register("the_common_cold", TheCommonCold::new);
    public static final RegistryObject<ITriggerModule> SWALLOWED_PENNY =
            TRIGGER_MODULE_REGISTRY.register("swallowed_penny", SwallowedPenny::new);
    public static final RegistryObject<ITriggerModule> LUCKY_ROCK =
            TRIGGER_MODULE_REGISTRY.register("lucky_rock", LuckyRock::new);
    public static final RegistryObject<ITriggerModule> CARTRIDGE =
            TRIGGER_MODULE_REGISTRY.register("cartridge", Cartridge::new);
    public static final RegistryObject<ITriggerModule> BLIND_RAGE =
            TRIGGER_MODULE_REGISTRY.register("blind_rage", BlindRage::new);
    public static final RegistryObject<ITriggerModule> IPECAC =
            TRIGGER_MODULE_REGISTRY.register("ipecac", Ipecac::new);
    public static final RegistryObject<ITriggerModule> BOUNCE_ON_BLOCK =
            TRIGGER_MODULE_REGISTRY.register("bounce_on_block", BounceOnBlock::new);
    public static final RegistryObject<ITriggerModule> BOUNCE_ON_ENTITY =
            TRIGGER_MODULE_REGISTRY.register("bounce_on_entity", BounceOnEntity::new);
    public static final RegistryObject<ITriggerModule> LASER =
            TRIGGER_MODULE_REGISTRY.register("laser", Laser::new);
    public static final RegistryObject<ITriggerModule> C_SECTION =
            TRIGGER_MODULE_REGISTRY.register("c_section", CSection::new);
    public static final RegistryObject<ITriggerModule> CURSED_EYE =
            TRIGGER_MODULE_REGISTRY.register("cursed_eye", CursedEye::new);
    public static final RegistryObject<ITriggerModule> TECHNOLOGY2 =
            TRIGGER_MODULE_REGISTRY.register("technology2", Technology2::new);



}
