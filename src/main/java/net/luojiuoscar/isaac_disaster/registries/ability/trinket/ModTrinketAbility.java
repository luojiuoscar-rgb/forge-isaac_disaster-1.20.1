package net.luojiuoscar.isaac_disaster.registries.ability.trinket;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTrinketAbility {
    public static final ResourceKey<Registry<TrinketAbility>> TRINKET_ABILITY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trinket_ability"));

    public static final DeferredRegister<TrinketAbility> TRINKET_ABILITY_REGISTRY =
            DeferredRegister.create(TRINKET_ABILITY_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<TrinketAbility> AAA_BATTERY =
            TRINKET_ABILITY_REGISTRY.register("aaa_battery",
                    () -> new AAABattery(TrinketId.AAA_BATTERY.getId(), TrinketId.AAA_BATTERY.getLevel()));

    public static final RegistryObject<TrinketAbility> ACE_OF_SPADES_TRINKET =
            TRINKET_ABILITY_REGISTRY.register("ace_of_spades_trinket",
                    () -> new AceOfSpadesTrinket(TrinketId.ACE_OF_SPADES_TRINKET.getId(), TrinketId.ACE_OF_SPADES_TRINKET.getLevel()));

    public static final RegistryObject<TrinketAbility> BLIND_RAGE =
            TRINKET_ABILITY_REGISTRY.register("blind_rage",
                    () -> new BlindRage(TrinketId.BLIND_RAGE.getId(), TrinketId.BLIND_RAGE.getLevel()));

    public static final RegistryObject<TrinketAbility> BRAIN_WORM =
            TRINKET_ABILITY_REGISTRY.register("brain_worm",
                    () -> new BrainWorm(TrinketId.BRAIN_WORM.getId(), TrinketId.BRAIN_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> BROKEN_REMOTE =
            TRINKET_ABILITY_REGISTRY.register("broken_remote",
                    () -> new BrokenRemote(TrinketId.BROKEN_REMOTE.getId(), TrinketId.BROKEN_REMOTE.getLevel()));

    public static final RegistryObject<TrinketAbility> CANCER_TRINKET =
            TRINKET_ABILITY_REGISTRY.register("cancer_trinket",
                    () -> new CancerTrinket(TrinketId.CANCER_TRINKET.getId(), TrinketId.CANCER_TRINKET.getLevel()));

    public static final RegistryObject<TrinketAbility> CARTRIDGE =
            TRINKET_ABILITY_REGISTRY.register("cartridge",
                    () -> new Cartridge(TrinketId.CARTRIDGE.getId(), TrinketId.CARTRIDGE.getLevel()));

    public static final RegistryObject<TrinketAbility> CHILDS_HEART =
            TRINKET_ABILITY_REGISTRY.register("childs_heart",
                    () -> new ChildsHeart(TrinketId.CHILDS_HEART.getId(), TrinketId.CHILDS_HEART.getLevel()));

    public static final RegistryObject<TrinketAbility> DAEMONS_TAIL =
            TRINKET_ABILITY_REGISTRY.register("daemons_tail",
                    () -> new DaemonsTail(TrinketId.DAEMONS_TAIL.getId(), TrinketId.DAEMONS_TAIL.getLevel()));

    public static final RegistryObject<TrinketAbility> GILDED_KEY =
            TRINKET_ABILITY_REGISTRY.register("gilded_key",
                    () -> new GildedKey(TrinketId.GILDED_KEY.getId(), TrinketId.GILDED_KEY.getLevel()));

    public static final RegistryObject<TrinketAbility> HOOK_WORM =
            TRINKET_ABILITY_REGISTRY.register("hook_worm",
                    () -> new HookWorm(TrinketId.HOOK_WORM.getId(), TrinketId.HOOK_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> LAZY_WORM =
            TRINKET_ABILITY_REGISTRY.register("lazy_worm",
                    () -> new LazyWorm(TrinketId.LAZY_WORM.getId(), TrinketId.LAZY_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> LUCKY_ROCK =
            TRINKET_ABILITY_REGISTRY.register("lucky_rock",
                    () -> new LuckyRock(TrinketId.LUCKY_ROCK.getId(), TrinketId.LUCKY_ROCK.getLevel()));

    public static final RegistryObject<TrinketAbility> LUCKY_TOE =
            TRINKET_ABILITY_REGISTRY.register("lucky_toe",
                    () -> new LuckyToe(TrinketId.LUCKY_TOE.getId(), TrinketId.LUCKY_TOE.getLevel()));

    public static final RegistryObject<TrinketAbility> MATCH_STICK =
            TRINKET_ABILITY_REGISTRY.register("match_stick",
                    () -> new MatchStick(TrinketId.MATCH_STICK.getId(), TrinketId.MATCH_STICK.getLevel()));

    public static final RegistryObject<TrinketAbility> OUROBOROS_WORM =
            TRINKET_ABILITY_REGISTRY.register("ouroboros_worm",
                    () -> new OuroborosWorm(TrinketId.OUROBOROS_WORM.getId(), TrinketId.OUROBOROS_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> PAPER_CLIP =
            TRINKET_ABILITY_REGISTRY.register("paper_clip",
                    () -> new PaperClip(TrinketId.PAPER_CLIP.getId(), TrinketId.PAPER_CLIP.getLevel()));

    public static final RegistryObject<TrinketAbility> PERFECTION =
            TRINKET_ABILITY_REGISTRY.register("perfection",
                    () -> new Perfection(TrinketId.PERFECTION.getId(), TrinketId.PERFECTION.getLevel()));

    public static final RegistryObject<TrinketAbility> POKER_CHIP =
            TRINKET_ABILITY_REGISTRY.register("poker_chip",
                    () -> new PokerChip(TrinketId.POKER_CHIP.getId(), TrinketId.POKER_CHIP.getLevel()));

    public static final RegistryObject<TrinketAbility> RING_WORM =
            TRINKET_ABILITY_REGISTRY.register("ring_worm",
                    () -> new RingWorm(TrinketId.RING_WORM.getId(), TrinketId.RING_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> RUSTED_KEY =
            TRINKET_ABILITY_REGISTRY.register("rusted_key",
                    () -> new RustedKey(TrinketId.RUSTED_KEY.getId(), TrinketId.RUSTED_KEY.getLevel()));

    public static final RegistryObject<TrinketAbility> SAFETY_CAP =
            TRINKET_ABILITY_REGISTRY.register("safety_cap",
                    () -> new SafetyCap(TrinketId.SAFETY_CAP.getId(), TrinketId.SAFETY_CAP.getLevel()));

    public static final RegistryObject<TrinketAbility> SWALLOWED_PENNY =
            TRINKET_ABILITY_REGISTRY.register("swallowed_penny",
                    () -> new SwallowedPenny(TrinketId.SWALLOWED_PENNY.getId(), TrinketId.SWALLOWED_PENNY.getLevel()));

    public static final RegistryObject<TrinketAbility> TAPE_WORM =
            TRINKET_ABILITY_REGISTRY.register("tape_worm",
                    () -> new TapeWorm(TrinketId.TAPE_WORM.getId(), TrinketId.TAPE_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> THE_LEFT_HAND =
            TRINKET_ABILITY_REGISTRY.register("the_left_hand",
                    () -> new TheLeftHand(TrinketId.THE_LEFT_HAND.getId(), TrinketId.THE_LEFT_HAND.getLevel()));

    public static final RegistryObject<TrinketAbility> WHIP_WORM =
            TRINKET_ABILITY_REGISTRY.register("whip_worm",
                    () -> new WhipWorm(TrinketId.WHIP_WORM.getId(), TrinketId.WHIP_WORM.getLevel()));

    public static final RegistryObject<TrinketAbility> WIGGLE_WORM =
            TRINKET_ABILITY_REGISTRY.register("wiggle_worm",
                    () -> new WiggleWorm(TrinketId.WIGGLE_WORM.getId(), TrinketId.WIGGLE_WORM.getLevel()));

}
