package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special.BulletTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special.HighPriorityPlayerPermanentModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special.PlayerPermanentModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTriggerModule {
    public static final ResourceKey<Registry<TriggerModule>> TRIGGER_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module"));

    public static final DeferredRegister<TriggerModule> TRIGGER_MODULE_REGISTRY =
            DeferredRegister.create(TRIGGER_MODULE_KEY, IsaacDisaster.MOD_ID);


    public static final RegistryObject<TriggerModule> PIGGY_BANK =
            TRIGGER_MODULE_REGISTRY.register("piggy_bank", PiggyBank::new);
    public static final RegistryObject<TriggerModule> CURSE_OF_THE_TOWER =
            TRIGGER_MODULE_REGISTRY.register("curse_of_the_tower", CurseOfTheTower::new);
    public static final RegistryObject<TriggerModule> HABIT =
            TRIGGER_MODULE_REGISTRY.register("habit", Habit::new);
    public static final RegistryObject<TriggerModule> THE_WAFER =
            TRIGGER_MODULE_REGISTRY.register("the_wafer", TheWafer::new);
    public static final RegistryObject<TriggerModule> THE_COMMON_COLD =
            TRIGGER_MODULE_REGISTRY.register("the_common_cold", TheCommonCold::new);
    public static final RegistryObject<TriggerModule> SWALLOWED_PENNY =
            TRIGGER_MODULE_REGISTRY.register("swallowed_penny", SwallowedPenny::new);
    public static final RegistryObject<TriggerModule> LUCKY_ROCK =
            TRIGGER_MODULE_REGISTRY.register("lucky_rock", LuckyRock::new);
    public static final RegistryObject<TriggerModule> CARTRIDGE =
            TRIGGER_MODULE_REGISTRY.register("cartridge", Cartridge::new);
    public static final RegistryObject<TriggerModule> BLIND_RAGE =
            TRIGGER_MODULE_REGISTRY.register("blind_rage", BlindRage::new);
    public static final RegistryObject<TriggerModule> IPECAC =
            TRIGGER_MODULE_REGISTRY.register("ipecac", Ipecac::new);
    public static final RegistryObject<TriggerModule> BOUNCE_ON_BLOCK =
            TRIGGER_MODULE_REGISTRY.register("bounce_on_block", BounceOnBlock::new);
    public static final RegistryObject<TriggerModule> BOUNCE_ON_ENTITY =
            TRIGGER_MODULE_REGISTRY.register("bounce_on_entity", BounceOnEntity::new);
    public static final RegistryObject<TriggerModule> LASER =
            TRIGGER_MODULE_REGISTRY.register("laser", Laser::new);
    public static final RegistryObject<TriggerModule> C_SECTION =
            TRIGGER_MODULE_REGISTRY.register("c_section", CSection::new);
    public static final RegistryObject<TriggerModule> CURSED_EYE =
            TRIGGER_MODULE_REGISTRY.register("cursed_eye", CursedEye::new);
    public static final RegistryObject<TriggerModule> TECHNOLOGY2 =
            TRIGGER_MODULE_REGISTRY.register("technology2", Technology2::new);
    public static final RegistryObject<TriggerModule> TERRA =
            TRIGGER_MODULE_REGISTRY.register("terra", Terra::new);
    public static final RegistryObject<TriggerModule> THE_VIRUS =
            TRIGGER_MODULE_REGISTRY.register("the_virus", TheVirus::new);
    public static final RegistryObject<TriggerModule> CHARM_OF_THE_VAMPIRE =
            TRIGGER_MODULE_REGISTRY.register("charm_of_the_vampire", CharmOfTheVampire::new);
    public static final RegistryObject<TriggerModule> BUTT_PENNY =
            TRIGGER_MODULE_REGISTRY.register("butt_penny", ButtPenny::new);
    public static final RegistryObject<TriggerModule> CURSED_PENNY =
            TRIGGER_MODULE_REGISTRY.register("cursed_penny", CursedPenny::new);
    public static final RegistryObject<TriggerModule> BULLET_TRIGGER_MODULE =
            TRIGGER_MODULE_REGISTRY.register("bullet_trigger_module", BulletTriggerModule::new);
    public static final RegistryObject<TriggerModule> PLAYER_PERMANENT_MODULE =
            TRIGGER_MODULE_REGISTRY.register("player_permanent_module", PlayerPermanentModule::new);
    public static final RegistryObject<TriggerModule> HIGH_PRIORITY_PLAYER_PERMANENT_MODULE =
            TRIGGER_MODULE_REGISTRY.register("high_priority_player_permanent_module", HighPriorityPlayerPermanentModule::new);
    public static final RegistryObject<TriggerModule> EXPLOSION_IMMUNE =
            TRIGGER_MODULE_REGISTRY.register("explosion_immune", ExplosionImmune::new);
    public static final RegistryObject<TriggerModule> EXPLOSION_REGENERATION =
            TRIGGER_MODULE_REGISTRY.register("explosion_regeneration", ExplosionRegeneration::new);
    public static final RegistryObject<TriggerModule> MITRE =
            TRIGGER_MODULE_REGISTRY.register("mitre", Mitre::new);
    public static final RegistryObject<TriggerModule> SACK_HEAD =
            TRIGGER_MODULE_REGISTRY.register("sack_head", SackHead::new);
    public static final RegistryObject<TriggerModule> DAEMONS_TAIL =
            TRIGGER_MODULE_REGISTRY.register("daemons_tail", DaemonsTail::new);
    public static final RegistryObject<TriggerModule> PENNY_TRINKET =
            TRIGGER_MODULE_REGISTRY.register("penny_trinket", PennyTrinket::new);
    public static final RegistryObject<TriggerModule> CHEST_LOOT_TRINKET =
            TRIGGER_MODULE_REGISTRY.register("chest_loot_trinket", ChestLootTrinket::new);
    public static final RegistryObject<TriggerModule> IRON_BAR =
            TRIGGER_MODULE_REGISTRY.register("iron_bar", IronBar::new);
    public static final RegistryObject<TriggerModule> MIDAS_TOUCH =
            TRIGGER_MODULE_REGISTRY.register("midas_touch", MidasTouch::new);
    public static final RegistryObject<TriggerModule> PETRIFIED_POOP =
            TRIGGER_MODULE_REGISTRY.register("petrified_poop", PetrifiedPoop::new);
    public static final RegistryObject<TriggerModule> CALLUS =
            TRIGGER_MODULE_REGISTRY.register("callus", Callus::new);
    public static final RegistryObject<TriggerModule> BLACK_LIPSTICK =
            TRIGGER_MODULE_REGISTRY.register("black_lipstick", BlackLipstick::new);

}
