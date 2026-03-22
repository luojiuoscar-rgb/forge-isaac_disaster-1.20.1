package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.*;
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



}
