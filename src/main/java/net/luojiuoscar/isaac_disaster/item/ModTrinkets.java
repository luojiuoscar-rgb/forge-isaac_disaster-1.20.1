package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.ModTrinketAbility;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModTrinkets {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> SWALLOWED_PENNY = ITEMS.register("swallowed_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.SWALLOWED_PENNY));
    static {ItemListManager.TRINKET_LIST.add(SWALLOWED_PENNY);}

    public static final RegistryObject<Item> AAA_BATTERY = ITEMS.register("aaa_battery",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.AAA_BATTERY));
    static {ItemListManager.TRINKET_LIST.add(AAA_BATTERY);}

    public static final RegistryObject<Item> BROKEN_REMOTE = ITEMS.register("broken_remote",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BROKEN_REMOTE));
    static {ItemListManager.TRINKET_LIST.add(BROKEN_REMOTE);}

    public static final RegistryObject<Item> CARTRIDGE = ITEMS.register("cartridge",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.CARTRIDGE));
    static {ItemListManager.TRINKET_LIST.add(CARTRIDGE);}

    public static final RegistryObject<Item> LUCKY_ROCK = ITEMS.register("lucky_rock",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.LUCKY_ROCK));
    static {ItemListManager.TRINKET_LIST.add(LUCKY_ROCK);}

    public static final RegistryObject<Item> LUCKY_TOE = ITEMS.register("lucky_toe",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.LUCKY_TOE));
    static {ItemListManager.TRINKET_LIST.add(LUCKY_TOE);}

    public static final RegistryObject<Item> CANCER_TRINKET = ITEMS.register("cancer_trinket",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.CANCER_TRINKET));
    static {ItemListManager.TRINKET_LIST.add(CANCER_TRINKET);}

    public static final RegistryObject<Item> BLIND_RAGE = ITEMS.register("blind_rage",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BLIND_RAGE));
    static {ItemListManager.TRINKET_LIST.add(BLIND_RAGE);}

    public static final RegistryObject<Item> PERFECTION = ITEMS.register("perfection",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.PERFECTION));
    static {ItemListManager.TRINKET_LIST.add(PERFECTION);}

    public static final RegistryObject<Item> DAEMONS_TAIL = ITEMS.register("daemons_tail",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.DAEMONS_TAIL));
    static {ItemListManager.TRINKET_LIST.add(DAEMONS_TAIL);}

    public static final RegistryObject<Item> PAPER_CLIP = ITEMS.register("paper_clip",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.PAPER_CLIP));
    static {ItemListManager.TRINKET_LIST.add(PAPER_CLIP);}

    public static final RegistryObject<Item> SAFETY_CAP = ITEMS.register("safety_cap",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.SAFETY_CAP));
    static {ItemListManager.TRINKET_LIST.add(SAFETY_CAP);}

    public static final RegistryObject<Item> ACE_OF_SPADES_TRINKET = ITEMS.register("ace_of_spades_trinket",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.ACE_OF_SPADES_TRINKET));
    static {ItemListManager.TRINKET_LIST.add(ACE_OF_SPADES_TRINKET);}

    public static final RegistryObject<Item> CHILDS_HEART = ITEMS.register("childs_heart",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.CHILDS_HEART));
    static {ItemListManager.TRINKET_LIST.add(CHILDS_HEART);}

    public static final RegistryObject<Item> MATCH_STICK = ITEMS.register("match_stick",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.MATCH_STICK));
    static {ItemListManager.TRINKET_LIST.add(MATCH_STICK);}

    public static final RegistryObject<Item> RUSTED_KEY = ITEMS.register("rusted_key",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.RUSTED_KEY));
    static {ItemListManager.TRINKET_LIST.add(RUSTED_KEY);}

    public static final RegistryObject<Item> POKER_CHIP = ITEMS.register("poker_chip",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.POKER_CHIP));
    static {ItemListManager.TRINKET_LIST.add(POKER_CHIP);}

    public static final RegistryObject<Item> GILDED_KEY = ITEMS.register("gilded_key",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.GILDED_KEY));
    static {ItemListManager.TRINKET_LIST.add(GILDED_KEY);}

    public static final RegistryObject<Item> THE_LEFT_HAND = ITEMS.register("the_left_hand",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.THE_LEFT_HAND));
    static {ItemListManager.TRINKET_LIST.add(THE_LEFT_HAND);}

    public static final RegistryObject<Item> WIGGLE_WORM = ITEMS.register("wiggle_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.WIGGLE_WORM));
    static {ItemListManager.TRINKET_LIST.add(WIGGLE_WORM);}

    public static final RegistryObject<Item> BRAIN_WORM = ITEMS.register("brain_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BRAIN_WORM));
    static {ItemListManager.TRINKET_LIST.add(BRAIN_WORM);}

    public static final RegistryObject<Item> WHIP_WORM = ITEMS.register("whip_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.WHIP_WORM));
    static {ItemListManager.TRINKET_LIST.add(WHIP_WORM);}

    public static final RegistryObject<Item> TAPE_WORM = ITEMS.register("tape_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.TAPE_WORM));
    static {ItemListManager.TRINKET_LIST.add(TAPE_WORM);}

    public static final RegistryObject<Item> LAZY_WORM = ITEMS.register("lazy_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.LAZY_WORM));
    static {ItemListManager.TRINKET_LIST.add(LAZY_WORM);}

    public static final RegistryObject<Item> RING_WORM = ITEMS.register("ring_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.RING_WORM));
    static {ItemListManager.TRINKET_LIST.add(RING_WORM);}

    public static final RegistryObject<Item> OUROBOROS_WORM = ITEMS.register("ouroboros_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.OUROBOROS_WORM));
    static {ItemListManager.TRINKET_LIST.add(OUROBOROS_WORM);}

    public static final RegistryObject<Item> HOOK_WORM = ITEMS.register("hook_worm",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.HOOK_WORM));
    static {ItemListManager.TRINKET_LIST.add(HOOK_WORM);}

    public static final RegistryObject<Item> BLOODY_PENNY = ITEMS.register("bloody_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BLOODY_PENNY));
    static {ItemListManager.TRINKET_LIST.add(BLOODY_PENNY);}

    public static final RegistryObject<Item> BURNT_PENNY = ITEMS.register("burnt_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BURNT_PENNY));
    static {ItemListManager.TRINKET_LIST.add(BURNT_PENNY);}

    public static final RegistryObject<Item> FLAT_PENNY = ITEMS.register("flat_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.FLAT_PENNY));
    static {ItemListManager.TRINKET_LIST.add(FLAT_PENNY);}

    public static final RegistryObject<Item> BLESSED_PENNY = ITEMS.register("blessed_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BLESSED_PENNY));
    static {ItemListManager.TRINKET_LIST.add(BLESSED_PENNY);}

    public static final RegistryObject<Item> CHARGED_PENNY = ITEMS.register("charged_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.CHARGED_PENNY));
    static {ItemListManager.TRINKET_LIST.add(CHARGED_PENNY);}

    public static final RegistryObject<Item> COUNTERFEIT_PENNY = ITEMS.register("counterfeit_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.COUNTERFEIT_PENNY));
    static {ItemListManager.TRINKET_LIST.add(COUNTERFEIT_PENNY);}

    public static final RegistryObject<Item> BUTT_PENNY = ITEMS.register("butt_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BUTT_PENNY));
    static {ItemListManager.TRINKET_LIST.add(BUTT_PENNY);}

    public static final RegistryObject<Item> CURSED_PENNY = ITEMS.register("cursed_penny",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.CURSED_PENNY));
    static {ItemListManager.TRINKET_LIST.add(CURSED_PENNY);}

    public static final RegistryObject<Item> PETRIFIED_POOP = ITEMS.register("petrified_poop",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.PETRIFIED_POOP));
    static {ItemListManager.TRINKET_LIST.add(PETRIFIED_POOP);}

    public static final RegistryObject<Item> CALLUS = ITEMS.register("callus",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.CALLUS));
    static {ItemListManager.TRINKET_LIST.add(CALLUS);}

    public static final RegistryObject<Item> BLACK_LIPSTICK = ITEMS.register("black_lipstick",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BLACK_LIPSTICK));
    static {ItemListManager.TRINKET_LIST.add(BLACK_LIPSTICK);}

    public static final RegistryObject<Item> BIBLE_TRACT = ITEMS.register("bible_tract",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.BIBLE_TRACT));
    static {ItemListManager.TRINKET_LIST.add(BIBLE_TRACT);}

    public static final RegistryObject<Item> SAFETY_SCISSORS = ITEMS.register("safety_scissors",
            () -> new Trinket(new Item.Properties(), ModTrinketAbility.SAFETY_SCISSORS));
    static {ItemListManager.TRINKET_LIST.add(SAFETY_SCISSORS);}
}
