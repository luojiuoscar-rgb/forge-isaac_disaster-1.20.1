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
}
