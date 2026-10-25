package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import java.util.Map;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special.BulletTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special.PlayerPermanentModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTriggerModules {
    public static final ResourceKey<Registry<TriggerModule>> TRIGGER_MODULE_KEY =
        ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module"));

    public static final DeferredRegister<TriggerModule> TRIGGER_MODULE_REGISTRY =
        DeferredRegister.create(TRIGGER_MODULE_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<TriggerModule> TINY_PLANET =
        TRIGGER_MODULE_REGISTRY.register(
            "tiny_planet",
            () ->
                new TrajectoryAttachment(
                    null,
                    Map.of(
                        ModAttackTypes.BULLET.getId(),
                            ModTrajectoryModules.TINY_PLANET_BULLET.getId(),
                        ModAttackTypes.LASER.getId(),
                            ModTrajectoryModules.TINY_PLANET_LASER.getId())));
    public static final RegistryObject<TriggerModule> MY_REFLECTION =
        TRIGGER_MODULE_REGISTRY.register(
            "my_reflection",
            () ->
                new TrajectoryAttachment(
                    null,
                    Map.of(
                        ModAttackTypes.BULLET.getId(),
                            ModTrajectoryModules.MY_REFLECTION_BULLET.getId(),
                        ModAttackTypes.LASER.getId(),
                            ModTrajectoryModules.MY_REFLECTION_LASER.getId())));
    public static final RegistryObject<TriggerModule> HOOK_WORM =
        TRIGGER_MODULE_REGISTRY.register(
            "hook_worm", () -> new TrajectoryAttachment(ModTrajectoryModules.HOOK_WORM.getId()));
    public static final RegistryObject<TriggerModule> WIGGLE_WORM =
        TRIGGER_MODULE_REGISTRY.register(
            "wiggle_worm", () -> new TrajectoryAttachment(ModTrajectoryModules.WIGGLE_WORM.getId()));
    public static final RegistryObject<TriggerModule> RING_WORM =
        TRIGGER_MODULE_REGISTRY.register(
            "ring_worm", () -> new TrajectoryAttachment(ModTrajectoryModules.RING_WORM.getId()));
    public static final RegistryObject<TriggerModule> OUROBOROS_WORM =
        TRIGGER_MODULE_REGISTRY.register(
            "ouroboros_worm",
            () -> new TrajectoryAttachment(ModTrajectoryModules.OUROBOROS_WORM.getId()));

    public static final RegistryObject<TriggerModule> PIGGY_BANK =
        TRIGGER_MODULE_REGISTRY.register("piggy_bank", PiggyBank::new);
    public static final RegistryObject<TriggerModule> CURSE_OF_THE_TOWER =
        TRIGGER_MODULE_REGISTRY.register("curse_of_the_tower", CurseOfTheTower::new);
    public static final RegistryObject<TriggerModule> HABIT =
        TRIGGER_MODULE_REGISTRY.register("habit", Habit::new);
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
    public static final RegistryObject<TriggerModule> BIBLE_TRACT =
        TRIGGER_MODULE_REGISTRY.register("bible_tract", BibleTract::new);
    public static final RegistryObject<TriggerModule> BOMBER_BOY =
        TRIGGER_MODULE_REGISTRY.register("bomber_boy", BomberBoy::new);
    public static final RegistryObject<TriggerModule> SCATTER_BOMB =
        TRIGGER_MODULE_REGISTRY.register("scatter_bomb", ScatterBomb::new);
    public static final RegistryObject<TriggerModule> HOT_BOMB =
        TRIGGER_MODULE_REGISTRY.register("hot_bomb", HotBomb::new);
    public static final RegistryObject<TriggerModule> BOGO_BOMBS =
        TRIGGER_MODULE_REGISTRY.register("bogo_bombs", BogoBombs::new);
    public static final RegistryObject<TriggerModule> LOKIS_HORNS =
        TRIGGER_MODULE_REGISTRY.register("lokis_horns", LokisHorns::new);
    public static final RegistryObject<TriggerModule> MOMS_KEY =
        TRIGGER_MODULE_REGISTRY.register("moms_key", MomsKey::new);
    public static final RegistryObject<TriggerModule> MOMS_EYESHADOW =
        TRIGGER_MODULE_REGISTRY.register("moms_eyeshadow", MomsEyeshadow::new);
    public static final RegistryObject<TriggerModule> FANNY_PACK =
        TRIGGER_MODULE_REGISTRY.register("fanny_pack", FannyPack::new);
    public static final RegistryObject<TriggerModule> SAD_BOMB =
        TRIGGER_MODULE_REGISTRY.register("sad_bomb", SadBomb::new);
    public static final RegistryObject<TriggerModule> VOLT45 =
        TRIGGER_MODULE_REGISTRY.register("volt45", Volt45::new);
    public static final RegistryObject<TriggerModule> CONTRACT_FROM_BELOW =
        TRIGGER_MODULE_REGISTRY.register("contract_from_below", ContractFromBelow::new);
    public static final RegistryObject<TriggerModule> MOMS_PERFUME =
        TRIGGER_MODULE_REGISTRY.register("moms_perfume", MomsPerfume::new);
    public static final RegistryObject<TriggerModule> GUPPYS_COLLAR =
        TRIGGER_MODULE_REGISTRY.register("guppys_collar", GuppysCollar::new);
    public static final RegistryObject<TriggerModule> PARASITE =
        TRIGGER_MODULE_REGISTRY.register("parasite", Parasite::new);
    public static final RegistryObject<TriggerModule> CRICKETS_BODY =
        TRIGGER_MODULE_REGISTRY.register("crickets_body", CricketsBody::new);
    public static final RegistryObject<TriggerModule> COMPOUND_FRACTURE =
        TRIGGER_MODULE_REGISTRY.register("compound_fracture", CompoundFracture::new);
    public static final RegistryObject<TriggerModule> HAEMOLACRIA =
        TRIGGER_MODULE_REGISTRY.register("haemolacria", Haemolacria::new);
    public static final RegistryObject<TriggerModule> BRIMSTONE_MONSTROS_LUNG =
        TRIGGER_MODULE_REGISTRY.register("brimstone_monstros_lung", BrimstoneMonstrosLung::new);
    public static final RegistryObject<TriggerModule> LEAD_PENCIL =
        TRIGGER_MODULE_REGISTRY.register("lead_pencil", LeadPencil::new);

    public static final RegistryObject<TriggerModule> MOMS_CONTACTS =
            TRIGGER_MODULE_REGISTRY.register("moms_contacts", MomsContacts::new);

    public static final RegistryObject<TriggerModule> DARK_MATTER =
            TRIGGER_MODULE_REGISTRY.register("dark_matter", DarkMatter::new);

    public static final RegistryObject<TriggerModule> CELTIC_CROSS =
            TRIGGER_MODULE_REGISTRY.register("celtic_cross", CelticCross::new);

    public static final RegistryObject<TriggerModule> OLD_BANDAGE =
            TRIGGER_MODULE_REGISTRY.register("old_bandage", OldBandage::new);

    public static final RegistryObject<TriggerModule> MARBLES =
            TRIGGER_MODULE_REGISTRY.register("marbles", Marbles::new);

    public static final RegistryObject<TriggerModule> SCORPIO =
            TRIGGER_MODULE_REGISTRY.register("scorpio", Scorpio::new);

    public static final RegistryObject<TriggerModule> HEAD_OF_THE_KEEPER =
            TRIGGER_MODULE_REGISTRY.register("head_of_the_keeper", HeadOfTheKeeper::new);

    public static final RegistryObject<TriggerModule> CONE_HEAD =
            TRIGGER_MODULE_REGISTRY.register("cone_head", ConeHead::new);

    public static final RegistryObject<TriggerModule> HUMBLEING_BUNDLE =
            TRIGGER_MODULE_REGISTRY.register("humbleing_bundle", HumbleingBundle::new);

    public static final RegistryObject<TriggerModule> BUTT_BOMBS =
            TRIGGER_MODULE_REGISTRY.register("butt_bombs", ButtBombs::new);

    public static final RegistryObject<TriggerModule> INFAMY =
            TRIGGER_MODULE_REGISTRY.register("infamy", Infamy::new);

    public static final RegistryObject<TriggerModule> APPLE =
            TRIGGER_MODULE_REGISTRY.register("apple", Apple::new);

    public static final RegistryObject<TriggerModule> JUMPER_CABLES =
            TRIGGER_MODULE_REGISTRY.register("jumper_cables", JumperCables::new);
    public static final RegistryObject<TriggerModule> SINUS_INFECTION =
            TRIGGER_MODULE_REGISTRY.register("sinus_infection", SinusInfection::new);

}
