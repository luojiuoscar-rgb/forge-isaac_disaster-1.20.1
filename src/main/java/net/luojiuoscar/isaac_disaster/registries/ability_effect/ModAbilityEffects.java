package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModAbilityEffects {
    public static final ResourceKey<Registry<IAbilityEffect>> ABILITY_EFFECT =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "ability_effect"));

    public static final DeferredRegister<IAbilityEffect> ABILITY_EFFECT_REGISTRY =
            DeferredRegister.create(ABILITY_EFFECT, IsaacDisaster.MOD_ID);

    public static final RegistryObject<IAbilityEffect> HEAL =
            ABILITY_EFFECT_REGISTRY.register("heal", Heal::new);
    public static final RegistryObject<IAbilityEffect> WOODEN_NICKEL =
            ABILITY_EFFECT_REGISTRY.register("wooden_nickel", WoodenNickel::new);
    public static final RegistryObject<IAbilityEffect> POTION =
            ABILITY_EFFECT_REGISTRY.register("potion", Potions::new);
    public static final RegistryObject<IAbilityEffect> THE_NECRONMICON =
            ABILITY_EFFECT_REGISTRY.register("the_necronmicon", TheNecronmicon::new);
    public static final RegistryObject<IAbilityEffect> APPLY_EFFECT_TO_NEARBY =
            ABILITY_EFFECT_REGISTRY.register("apply_effect_to_nearby", ApplyEffectToNearby::new);
    public static final RegistryObject<IAbilityEffect> D6 =
            ABILITY_EFFECT_REGISTRY.register("d6", D6::new);
    public static final RegistryObject<IAbilityEffect> STACK_POTION =
            ABILITY_EFFECT_REGISTRY.register("stack_potion", StackPotion::new);
    public static final RegistryObject<IAbilityEffect> TELEPORT =
            ABILITY_EFFECT_REGISTRY.register("teleport", Teleport::new);
    public static final RegistryObject<IAbilityEffect> TAMMYS_HEAD =
            ABILITY_EFFECT_REGISTRY.register("tammys_head", TammysHead::new);
    public static final RegistryObject<IAbilityEffect> SWALLOW_TRINKETS =
            ABILITY_EFFECT_REGISTRY.register("swallow_trinkets", SwallowTrinkets::new);
    public static final RegistryObject<IAbilityEffect> GIVE_ITEM =
            ABILITY_EFFECT_REGISTRY.register("give_item", GiveItem::new);
    public static final RegistryObject<IAbilityEffect> USE_PILL =
            ABILITY_EFFECT_REGISTRY.register("use_pill", UsePill::new);
    public static final RegistryObject<IAbilityEffect> THROW_BOMB =
            ABILITY_EFFECT_REGISTRY.register("throw_bomb", ThrowBomb::new);
    public static final RegistryObject<IAbilityEffect> LEMON =
            ABILITY_EFFECT_REGISTRY.register("lemon", Lemon::new);
    public static final RegistryObject<IAbilityEffect> KAMIKAZE =
            ABILITY_EFFECT_REGISTRY.register("kamikaze", Kamikaze::new);
    public static final RegistryObject<IAbilityEffect> IV_BAG =
            ABILITY_EFFECT_REGISTRY.register("iv_bag", IvBag::new);
    public static final RegistryObject<IAbilityEffect> DULL_RAZOR =
            ABILITY_EFFECT_REGISTRY.register("dull_razor", DullRazor::new);
    public static final RegistryObject<IAbilityEffect> DIPLOPIA =
            ABILITY_EFFECT_REGISTRY.register("diplopia", Diplopia::new);
    public static final RegistryObject<IAbilityEffect> CROOKED_PENNY =
            ABILITY_EFFECT_REGISTRY.register("crooked_penny", CrookedPenny::new);
    public static final RegistryObject<IAbilityEffect> SPAWN_BOMB_NEARBY =
            ABILITY_EFFECT_REGISTRY.register("spawn_bomb_nearby", SpawnBombNearby::new);
    public static final RegistryObject<IAbilityEffect> THE_WAFER =
            ABILITY_EFFECT_REGISTRY.register("the_wafer", TheWafer::new);
    public static final RegistryObject<IAbilityEffect> RANDOM_HARMFUL_POTION =
            ABILITY_EFFECT_REGISTRY.register("random_harmful_potion", RandomHarmfulPotion::new);
    public static final RegistryObject<IAbilityEffect> BREAK_BLOCK_AND_DROP =
            ABILITY_EFFECT_REGISTRY.register("break_block_and_drop", BreakBlockAndDrop::new);
    public static final RegistryObject<IAbilityEffect> SHOOT_LASER =
            ABILITY_EFFECT_REGISTRY.register("shoot_laser", ShootLaser::new);
    public static final RegistryObject<IAbilityEffect> SWALLOWED_PENNY =
            ABILITY_EFFECT_REGISTRY.register("swallowed_penny", SwallowedPenny::new);
    public static final RegistryObject<IAbilityEffect> PIGGY_BANK =
            ABILITY_EFFECT_REGISTRY.register("piggy_bank", PiggyBank::new);
    public static final RegistryObject<IAbilityEffect> LUCKY_ROCK =
            ABILITY_EFFECT_REGISTRY.register("lucky_rock", LuckyRock::new);
    public static final RegistryObject<IAbilityEffect> LASER_PLUS_BRIMSTONE =
            ABILITY_EFFECT_REGISTRY.register("laser_plus_brimstone", LaserPlusBrimstone::new);
    public static final RegistryObject<IAbilityEffect> LASER_PLUS_FETUS =
            ABILITY_EFFECT_REGISTRY.register("laser_plus_fetus", LaserPlusFetus::new);
    public static final RegistryObject<IAbilityEffect> IPECAC =
            ABILITY_EFFECT_REGISTRY.register("ipecac", Ipecac::new);
    public static final RegistryObject<IAbilityEffect> HABIT =
            ABILITY_EFFECT_REGISTRY.register("habit", Habit::new);
    public static final RegistryObject<IAbilityEffect> CURSE_OF_THE_TOWER =
            ABILITY_EFFECT_REGISTRY.register("curse_of_the_tower", CurseOfTheTower::new);
    public static final RegistryObject<IAbilityEffect> CURSED_PENNY =
            ABILITY_EFFECT_REGISTRY.register("cursed_penny", CursedPenny::new);
    public static final RegistryObject<IAbilityEffect> CURSED_EYE =
            ABILITY_EFFECT_REGISTRY.register("cursed_eye", CursedEye::new);
    public static final RegistryObject<IAbilityEffect> BRIMSTONE_PLUS_C_SECTION =
            ABILITY_EFFECT_REGISTRY.register("brimstone_plus_c_section", BrimstonePlusCSection::new);
    public static final RegistryObject<IAbilityEffect> CHARM_OF_THE_VAMPIRE =
            ABILITY_EFFECT_REGISTRY.register("charm_of_the_vampire", CharmOfTheVampire::new);
    public static final RegistryObject<IAbilityEffect> CARTRIDGE =
            ABILITY_EFFECT_REGISTRY.register("cartridge", Cartridge::new);
    public static final RegistryObject<IAbilityEffect> FART =
            ABILITY_EFFECT_REGISTRY.register("fart", Fart::new);
    public static final RegistryObject<IAbilityEffect> BUTT_PENNY =
            ABILITY_EFFECT_REGISTRY.register("butt_penny", ButtPenny::new);
    public static final RegistryObject<IAbilityEffect> BULLET_BOUNCE_ON_ENTITY =
            ABILITY_EFFECT_REGISTRY.register("bullet_bounce_on_entity", BulletBounceOnEntity::new);
    public static final RegistryObject<IAbilityEffect> BULLET_BOUNCE_ON_BLOCK =
            ABILITY_EFFECT_REGISTRY.register("bullet_bounce_on_block", BulletBounceOnBlock::new);
    public static final RegistryObject<IAbilityEffect> BLIND_RAGE =
            ABILITY_EFFECT_REGISTRY.register("blind_rage", BlindRage::new);
    public static final RegistryObject<IAbilityEffect> ATTRACT_ITEM =
            ABILITY_EFFECT_REGISTRY.register("attract_item", AttractItem::new);
    public static final RegistryObject<IAbilityEffect> REMOVE_ALL_HARMFUL_POTION =
            ABILITY_EFFECT_REGISTRY.register("remove_all_harmful_potion", RemoveAllHarmfulPotion::new);
    public static final RegistryObject<IAbilityEffect> GILDED_KEY =
            ABILITY_EFFECT_REGISTRY.register("gilded_key", GildedKey::new);
    public static final RegistryObject<IAbilityEffect> MAGNETO =
            ABILITY_EFFECT_REGISTRY.register("magneto", Magneto::new);
    public static final RegistryObject<IAbilityEffect> MONEY_IS_POWER =
            ABILITY_EFFECT_REGISTRY.register("money_is_power", MoneyIsPower::new);
    public static final RegistryObject<IAbilityEffect> ROCK_BOTTOM =
            ABILITY_EFFECT_REGISTRY.register("rock_bottom", RockBottom::new);
    public static final RegistryObject<IAbilityEffect> LOOT_MONEY =
            ABILITY_EFFECT_REGISTRY.register("loot_money", LootMoney::new);
    public static final RegistryObject<IAbilityEffect> THE_LEFT_HAND =
            ABILITY_EFFECT_REGISTRY.register("the_left_hand", TheLeftHand::new);
    public static final RegistryObject<IAbilityEffect> STACK_HOLY_SHIELD =
            ABILITY_EFFECT_REGISTRY.register("stack_holy_shield", StackHolyShield::new);



}
