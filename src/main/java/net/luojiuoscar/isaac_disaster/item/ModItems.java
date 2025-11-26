package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.item.block.IdentifierBlockItem;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.item.custom.ChestPlaceholder;
import net.luojiuoscar.isaac_disaster.item.custom.DebugStick;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item.pickup.*;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.luojiuoscar.isaac_disaster.manager.ItemListManager.*;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    // trinket
    private static RegistryObject<Item> registerTrinket(String name, TrinketId id) {
        RegistryObject<Item> reg = ITEMS.register(name, () -> new Trinket(new Item.Properties(), id.getId()));
        TRINKET_LIST.add(reg);
        TrinketId.registerItem(id.getId(), reg);
        return reg;
    }

    public static final RegistryObject<Item> SWALLOWED_PENNY = registerTrinket("swallowed_penny", TrinketId.SWALLOWED_PENNY);
    public static final RegistryObject<Item> AAA_BATTERY    = registerTrinket("aaa_battery", TrinketId.AAA_BATTERY);
    public static final RegistryObject<Item> BROKEN_REMOTE  = registerTrinket("broken_remote", TrinketId.BROKEN_REMOTE);
    public static final RegistryObject<Item> CARTRIDGE      = registerTrinket("cartridge", TrinketId.CARTRIDGE);
    public static final RegistryObject<Item> LUCKY_ROCK     = registerTrinket("lucky_rock", TrinketId.LUCKY_ROCK);
    public static final RegistryObject<Item> LUCKY_TOE      = registerTrinket("lucky_toe", TrinketId.LUCKY_TOE);
    public static final RegistryObject<Item> CANCER_TRINKET = registerTrinket("cancer_trinket", TrinketId.CANCER_TRINKET);
    public static final RegistryObject<Item> BLIND_RAGE     = registerTrinket("blind_rage", TrinketId.BLIND_RAGE);
    public static final RegistryObject<Item> PERFECTION     = registerTrinket("perfection", TrinketId.PERFECTION);
    public static final RegistryObject<Item> DAEMONS_TAIL   = registerTrinket("daemons_tail", TrinketId.DAEMONS_TAIL);
    public static final RegistryObject<Item> PAPER_CLIP     = registerTrinket("paper_clip", TrinketId.PAPER_CLIP);
    public static final RegistryObject<Item> SAFETY_CAP     = registerTrinket("safety_cap", TrinketId.SAFETY_CAP);
    public static final RegistryObject<Item> ACE_OF_SPADES_TRINKET = registerTrinket("ace_of_spades_trinket", TrinketId.ACE_OF_SPADES_TRINKET);
    public static final RegistryObject<Item> CHILDS_HEART   = registerTrinket("childs_heart", TrinketId.CHILDS_HEART);
    public static final RegistryObject<Item> MATCH_STICK    = registerTrinket("match_stick", TrinketId.MATCH_STICK);
    public static final RegistryObject<Item> RUSTED_KEY     = registerTrinket("rusted_key", TrinketId.RUSTED_KEY);
    public static final RegistryObject<Item> POKER_CHIP     = registerTrinket("poker_chip", TrinketId.POKER_CHIP);
    public static final RegistryObject<Item> GILDED_KEY     = registerTrinket("gilded_key", TrinketId.GILDED_KEY);
    public static final RegistryObject<Item> THE_LEFT_HAND  = registerTrinket("the_left_hand", TrinketId.THE_LEFT_HAND);
    public static final RegistryObject<Item> WIGGLE_WORM    = registerTrinket("wiggle_worm", TrinketId.WIGGLE_WORM);
    public static final RegistryObject<Item> BRAIN_WORM     = registerTrinket("brain_worm", TrinketId.BRAIN_WORM);
    public static final RegistryObject<Item> WHIP_WORM      = registerTrinket("whip_worm", TrinketId.WHIP_WORM);
    public static final RegistryObject<Item> TAPE_WORM      = registerTrinket("tape_worm", TrinketId.TAPE_WORM);
    public static final RegistryObject<Item> LAZY_WORM      = registerTrinket("lazy_worm", TrinketId.LAZY_WORM);
    public static final RegistryObject<Item> RING_WORM      = registerTrinket("ring_worm", TrinketId.RING_WORM);
    public static final RegistryObject<Item> OUROBOROS_WORM = registerTrinket("ouroboros_worm", TrinketId.OUROBOROS_WORM);
    public static final RegistryObject<Item> HOOK_WORM      = registerTrinket("hook_worm", TrinketId.HOOK_WORM);


    // trinket end
    // pickups
    public static final RegistryObject<Item> ISAAC_HEAD = ITEMS.register("isaac_head",
            () -> new IsaacHead(new Item.Properties(), PickupId.ISAAC_HEAD.getId()));
    static { PICKUP_LIST.add(ISAAC_HEAD); }

    public static final RegistryObject<Item> PENNY = ITEMS.register("penny",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(PENNY); }

    public static final RegistryObject<Item> NICKEL = ITEMS.register("nickel",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(NICKEL); }

    public static final RegistryObject<Item> DIME = ITEMS.register("dime",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(DIME); }

    public static final RegistryObject<Item> HALF_RED_HEART = ITEMS.register("half_red_heart",
            () -> new Heart(new Item.Properties(), PickupId.HALF_RED_HEART.getId(), Rarity.COMMON));
    static { PICKUP_LIST.add(HALF_RED_HEART); }

    public static final RegistryObject<Item> RED_HEART = ITEMS.register("red_heart",
            () -> new Heart(new Item.Properties(), PickupId.RED_HEART.getId(), Rarity.COMMON));
    static { PICKUP_LIST.add(RED_HEART); }

    public static final RegistryObject<Item> DOUBLE_RED_HEART = ITEMS.register("double_red_heart",
            () -> new Heart(new Item.Properties(), PickupId.DOUBLE_RED_HEART.getId(), Rarity.COMMON));
    static { PICKUP_LIST.add(DOUBLE_RED_HEART); }

    public static final RegistryObject<Item> HALF_SOUL_HEART = ITEMS.register("half_soul_heart",
            () -> new Heart(new Item.Properties(), PickupId.HALF_SOUL_HEART.getId(), Rarity.UNCOMMON));
    static { PICKUP_LIST.add(HALF_SOUL_HEART); }

    public static final RegistryObject<Item> SOUL_HEART = ITEMS.register("soul_heart",
            () -> new Heart(new Item.Properties(), PickupId.SOUL_HEART.getId(), Rarity.RARE));
    static { PICKUP_LIST.add(SOUL_HEART); }

    public static final RegistryObject<Item> BLENDED_HEART = ITEMS.register("blended_heart",
            () -> new Heart(new Item.Properties(), PickupId.BLENDED_HEART.getId(), Rarity.RARE));
    static { PICKUP_LIST.add(BLENDED_HEART); }

    public static final RegistryObject<Item> BLACK_HEART = ITEMS.register("black_heart",
            () -> new Heart(new Item.Properties(), PickupId.BLACK_HEART.getId(), Rarity.EPIC));
    static { PICKUP_LIST.add(BLACK_HEART); }

    public static final RegistryObject<Item> BONE_HEART = ITEMS.register("bone_heart",
            () -> new Heart(new Item.Properties(), PickupId.BONE_HEART.getId(), Rarity.EPIC));
    static { PICKUP_LIST.add(BONE_HEART); }

    public static final RegistryObject<Item> ETERNAL_HEART = ITEMS.register("eternal_heart",
            () -> new Heart(new Item.Properties(), PickupId.ETERNAL_HEART.getId(), Rarity.EPIC));
    static { PICKUP_LIST.add(ETERNAL_HEART); }

    public static final RegistryObject<Item> GOLDEN_HEART = ITEMS.register("golden_heart",
            () -> new Heart(new Item.Properties(), PickupId.GOLDEN_HEART.getId(), Rarity.RARE));
    static { PICKUP_LIST.add(GOLDEN_HEART); }


    public static final RegistryObject<Item> BOMB = ITEMS.register("bomb",
            () -> new Bomb(new Item.Properties(), PickupId.BOMB.getId()));
    static { PICKUP_LIST.add(BOMB); }

    public static final RegistryObject<Item> GIGA_BOMB = ITEMS.register("giga_bomb",
            () -> new Bomb(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), PickupId.GIGA_BOMB.getId()));
    static { PICKUP_LIST.add(GIGA_BOMB); }

    public static final RegistryObject<Item> GOLDEN_BOMB = ITEMS.register("golden_bomb",
            () -> new Bomb(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), PickupId.GOLDEN_BOMB.getId()));
    static { PICKUP_LIST.add(GOLDEN_BOMB); }


    public static final RegistryObject<Item> KEY = ITEMS.register("key",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(KEY); }

    public static final RegistryObject<Item> GOLDEN_KEY = ITEMS.register("golden_key",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    static { PICKUP_LIST.add(GOLDEN_KEY); }

    public static final RegistryObject<Item> SMALL_BATTERY = ITEMS.register("small_battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(4), PickupId.SMALL_BATTERY.getId()));
    static { PICKUP_LIST.add(SMALL_BATTERY); }

    public static final RegistryObject<Item> BATTERY = ITEMS.register("battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(4), PickupId.BATTERY.getId()));
    static { PICKUP_LIST.add(BATTERY); }

    public static final RegistryObject<Item> MEGA_BATTERY = ITEMS.register("mega_battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), PickupId.MEGA_BATTERY.getId()));
    static { PICKUP_LIST.add(MEGA_BATTERY); }

    public static final RegistryObject<Item> GOLDEN_BATTERY = ITEMS.register("golden_battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), PickupId.GOLDEN_BATTERY.getId()));
    static { PICKUP_LIST.add(GOLDEN_BATTERY); }

    public static final RegistryObject<Item> GRAB_BAG = ITEMS.register("grab_bag",
            () -> new Sack(new Item.Properties(), PickupId.GRAB_BAG.getId()));
    static { PICKUP_LIST.add(GRAB_BAG); }

    public static final RegistryObject<Item> BLACK_SACK = ITEMS.register("black_sack",
            () -> new Sack(new Item.Properties(), PickupId.BLACK_SACK.getId()));
    static { PICKUP_LIST.add(BLACK_SACK); }


    public static final RegistryObject<Item> THE_FOOL = ITEMS.register("the_fool",
            () -> new Card(new Item.Properties(), PickupId.THE_FOOL.getId()));
    static { PICKUP_LIST.add(THE_FOOL); }

    public static final RegistryObject<Item> THE_MAGICIAN = ITEMS.register("the_magician",
            () -> new Card(new Item.Properties(), PickupId.THE_MAGICIAN.getId()));
    static { PICKUP_LIST.add(THE_MAGICIAN); }

    public static final RegistryObject<Item> THE_HIGH_PRIESTESS = ITEMS.register("the_high_priestess",
            () -> new Card(new Item.Properties(), PickupId.THE_HIGH_PRIESTESS.getId()));
    static { PICKUP_LIST.add(THE_HIGH_PRIESTESS); }

    public static final RegistryObject<Item> THE_EMPRESS = ITEMS.register("the_empress",
            () -> new Card(new Item.Properties(), PickupId.THE_EMPRESS.getId()));
    static { PICKUP_LIST.add(THE_EMPRESS); }

    public static final RegistryObject<Item> THE_HIEROPHANT = ITEMS.register("the_hierophant",
            () -> new Card(new Item.Properties(), PickupId.THE_HIEROPHANT.getId()));
    static { PICKUP_LIST.add(THE_HIEROPHANT); }

    public static final RegistryObject<Item> THE_LOVERS = ITEMS.register("the_lovers",
            () -> new Card(new Item.Properties(), PickupId.THE_LOVERS.getId()));
    static { PICKUP_LIST.add(THE_LOVERS); }

    public static final RegistryObject<Item> THE_CHARIOT = ITEMS.register("the_chariot",
            () -> new Card(new Item.Properties(), PickupId.THE_CHARIOT.getId()));
    static { PICKUP_LIST.add(THE_CHARIOT); }

    public static final RegistryObject<Item> JUSTICE = ITEMS.register("justice",
            () -> new Card(new Item.Properties(), PickupId.JUSTICE.getId()));
    static { PICKUP_LIST.add(JUSTICE); }

    public static final RegistryObject<Item> THE_HANGED_MAN = ITEMS.register("the_hanged_man",
            () -> new Card(new Item.Properties(), PickupId.THE_HANGED_MAN.getId()));
    static { PICKUP_LIST.add(THE_HANGED_MAN); }

    public static final RegistryObject<Item> DEATH = ITEMS.register("death",
            () -> new Card(new Item.Properties(), PickupId.DEATH.getId()));
    static { PICKUP_LIST.add(DEATH); }

    public static final RegistryObject<Item> THE_DEVIL = ITEMS.register("the_devil",
            () -> new Card(new Item.Properties(), PickupId.THE_DEVIL.getId()));
    static { PICKUP_LIST.add(THE_DEVIL); }

    public static final RegistryObject<Item> THE_TOWER = ITEMS.register("the_tower",
            () -> new Card(new Item.Properties(), PickupId.THE_TOWER.getId()));
    static { PICKUP_LIST.add(THE_TOWER); }

    public static final RegistryObject<Item> THE_STARS = ITEMS.register("the_stars",
            () -> new Card(new Item.Properties(), PickupId.THE_STARS.getId()));
    static { PICKUP_LIST.add(THE_STARS); }

    public static final RegistryObject<Item> THE_MOON = ITEMS.register("the_moon",
            () -> new Card(new Item.Properties(), PickupId.THE_MOON.getId()));
    static { PICKUP_LIST.add(THE_MOON); }

    public static final RegistryObject<Item> THE_SUN = ITEMS.register("the_sun",
            () -> new Card(new Item.Properties(), PickupId.THE_SUN.getId()));
    static { PICKUP_LIST.add(THE_SUN); }

    public static final RegistryObject<Item> THE_WORLD = ITEMS.register("the_world",
            () -> new Card(new Item.Properties(), PickupId.THE_WORLD.getId()));
    static { PICKUP_LIST.add(THE_WORLD); }

    public static final RegistryObject<Item> THE_MAGICIAN_R = ITEMS.register("the_magician_r",
            () -> new Card(new Item.Properties(), PickupId.THE_MAGICIAN_R.getId()));
    static { PICKUP_LIST.add(THE_MAGICIAN_R); }

    public static final RegistryObject<Item> THE_HIGH_PRIESTESS_R = ITEMS.register("the_high_priestess_r",
            () -> new Card(new Item.Properties(), PickupId.THE_HIGH_PRIESTESS_R.getId()));
    static { PICKUP_LIST.add(THE_HIGH_PRIESTESS_R); }

    public static final RegistryObject<Item> THE_EMPRESS_R = ITEMS.register("the_empress_r",
            () -> new Card(new Item.Properties(), PickupId.THE_EMPRESS_R.getId()));
    static { PICKUP_LIST.add(THE_EMPRESS_R); }

    public static final RegistryObject<Item> THE_HIEROPHANT_R = ITEMS.register("the_hierophant_r",
            () -> new Card(new Item.Properties(), PickupId.THE_HIEROPHANT_R.getId()));
    static { PICKUP_LIST.add(THE_HIEROPHANT_R); }

    public static final RegistryObject<Item> THE_CHARIOT_R = ITEMS.register("the_chariot_r",
            () -> new Card(new Item.Properties(), PickupId.THE_CHARIOT_R.getId()));
    static { PICKUP_LIST.add(THE_CHARIOT_R); }

    public static final RegistryObject<Item> JUSTICE_R = ITEMS.register("justice_r",
            () -> new Card(new Item.Properties(), PickupId.JUSTICE_R.getId()));
    static { PICKUP_LIST.add(JUSTICE_R); }

    public static final RegistryObject<Item> STRENGTH_R = ITEMS.register("strength_r",
            () -> new Card(new Item.Properties(), PickupId.STRENGTH_R.getId()));
    static { PICKUP_LIST.add(STRENGTH_R); }

    public static final RegistryObject<Item> TEMPERANCE_R = ITEMS.register("temperance_r",
            () -> new Card(new Item.Properties(), PickupId.TEMPERANCE_R.getId()));
    static { PICKUP_LIST.add(TEMPERANCE_R); }

    public static final RegistryObject<Item> THE_TOWER_R = ITEMS.register("the_tower_r",
            () -> new Card(new Item.Properties(), PickupId.THE_TOWER_R.getId()));
    static { PICKUP_LIST.add(THE_TOWER_R); }

    public static final RegistryObject<Item> TWO_OF_DIAMONDS = ITEMS.register("2_of_diamonds",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_DIAMONDS.getId()));
    static { PICKUP_LIST.add(TWO_OF_DIAMONDS); }

    public static final RegistryObject<Item> TWO_OF_CLUBS = ITEMS.register("2_of_clubs",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_CLUBS.getId()));
    static { PICKUP_LIST.add(TWO_OF_CLUBS); }

    public static final RegistryObject<Item> TWO_OF_HEARTS = ITEMS.register("2_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(TWO_OF_HEARTS); }

    public static final RegistryObject<Item> TWO_OF_SPADES = ITEMS.register("2_of_spades",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_SPADES.getId()));
    static { PICKUP_LIST.add(TWO_OF_SPADES); }

    public static final RegistryObject<Item> ACE_OF_CLUBS = ITEMS.register("ace_of_clubs",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_CLUBS.getId()));
    static { PICKUP_LIST.add(ACE_OF_CLUBS); }

    public static final RegistryObject<Item> ACE_OF_DIAMONDS = ITEMS.register("ace_of_diamonds",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_DIAMONDS.getId()));
    static { PICKUP_LIST.add(ACE_OF_DIAMONDS); }

    public static final RegistryObject<Item> ACE_OF_HEARTS = ITEMS.register("ace_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(ACE_OF_HEARTS); }

    public static final RegistryObject<Item> ACE_OF_SPADES = ITEMS.register("ace_of_spades",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_SPADES.getId()));
    static { PICKUP_LIST.add(ACE_OF_SPADES); }

    public static final RegistryObject<Item> QUEEN_OF_HEARTS = ITEMS.register("queen_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.QUEEN_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(QUEEN_OF_HEARTS); }

    public static final RegistryObject<Item> QUESTION_CARD = ITEMS.register("question_card",
            () -> new Card(new Item.Properties(), PickupId.QUESTION_CARD.getId()));
    static { PICKUP_LIST.add(QUESTION_CARD); }

    public static final RegistryObject<Item> HOLY_CARD = ITEMS.register("holy_card",
            () -> new Card(new Item.Properties(), PickupId.HOLY_CARD.getId()));
    static { PICKUP_LIST.add(HOLY_CARD); }

    public static final RegistryObject<Item> ANCIENT_RECALL = ITEMS.register("ancient_recall",
            () -> new Card(new Item.Properties(), PickupId.ANCIENT_RECALL.getId()));
    static { PICKUP_LIST.add(ANCIENT_RECALL); }

    public static final RegistryObject<Item> WILD_CARD = ITEMS.register("wild_card",
            () -> new WildCard(new Item.Properties(), PickupId.WILD_CARD.getId()));
    static { PICKUP_LIST.add(WILD_CARD); }



    public static final RegistryObject<Item> TAROTS_BACK = ITEMS.register("tarots_back",
            () -> new Card(new Item.Properties(), PickupId.TAROTS_BACK.getId()));
    static { PICKUP_LIST.add(TAROTS_BACK); }
    public static final RegistryObject<Item> REVERSE_TAROTS_BACK = ITEMS.register("reverse_tarots_back",
            () -> new Card(new Item.Properties(), PickupId.REVERSE_TAROTS_BACK.getId()));
    static { PICKUP_LIST.add(REVERSE_TAROTS_BACK); }
    public static final RegistryObject<Item> POKERS_BACK = ITEMS.register("pokers_back",
            () -> new Card(new Item.Properties(), PickupId.POKERS_BACK.getId()));
    static { PICKUP_LIST.add(POKERS_BACK); }
    public static final RegistryObject<Item> SPECIALS_BACK = ITEMS.register("specials_back",
            () -> new Card(new Item.Properties(), PickupId.SPECIALS_BACK.getId()));
    static { PICKUP_LIST.add(SPECIALS_BACK); }

    // pickup end
    // misc
    public static final RegistryObject<Item> DEBUG_STICK = ITEMS.register("debug_stick",
            () -> new DebugStick(new Item.Properties()));
    static { MISC_LIST.add(DEBUG_STICK); }

    public static final RegistryObject<Item> PEDESTAL_ITEM = ITEMS.register("pedestal",
            () -> new BlockItem(ModBlocks.PEDESTAL_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(PEDESTAL_ITEM); }

    public static final RegistryObject<Item> NORMAL_CHEST_ITEM = ITEMS.register("chest",
            () -> new IsaacChestBlockItem(ModBlocks.NORMAL_CHEST_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(NORMAL_CHEST_ITEM); }

    public static final RegistryObject<Item> LOCKED_CHEST_ITEM = ITEMS.register("locked_chest",
            () -> new IsaacChestBlockItem(ModBlocks.LOCKED_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.COMMON)));
    static { MISC_LIST.add(LOCKED_CHEST_ITEM); }

    public static final RegistryObject<Item> OLD_CHEST_ITEM = ITEMS.register("old_chest",
            () -> new IsaacChestBlockItem(ModBlocks.OLD_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    static { MISC_LIST.add(OLD_CHEST_ITEM); }

    public static final RegistryObject<Item> ETERNAL_CHEST_ITEM = ITEMS.register("eternal_chest",
            () -> new IsaacChestBlockItem(ModBlocks.ETERNAL_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    static { MISC_LIST.add(ETERNAL_CHEST_ITEM); }

    public static final RegistryObject<Item> RED_CHEST_ITEM = ITEMS.register("red_chest",
            () -> new IsaacChestBlockItem(ModBlocks.RED_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    static { MISC_LIST.add(RED_CHEST_ITEM); }

    public static final RegistryObject<Item> WOODEN_CHEST_ITEM = ITEMS.register("wooden_chest",
            () -> new IsaacChestBlockItem(ModBlocks.WOODEN_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    static { MISC_LIST.add(WOODEN_CHEST_ITEM); }

    public static final RegistryObject<Item> BOMB_CHEST_ITEM = ITEMS.register("bomb_chest",
            () -> new IsaacChestBlockItem(ModBlocks.BOMB_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.COMMON)));
    static { MISC_LIST.add(BOMB_CHEST_ITEM); }

    public static final RegistryObject<Item> CHEST_PLACEHOLDER_ITEM = ITEMS.register("chest_placeholder",
            () -> new ChestPlaceholder(ModBlocks.CHEST_PLACEHOLDER_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(CHEST_PLACEHOLDER_ITEM); }

    private static RegistryObject<Item> registerIdentifier(String name, RegistryObject<Block> block) {
        RegistryObject<Item> reg = ITEMS.register(name, () -> new IdentifierBlockItem(
                block.get(), new Item.Properties()));
        MISC_LIST.add(reg);
        return reg;
    }

    public static final RegistryObject<Item> NORMAL_IDENTIFIER_ITEM     = registerIdentifier("normal_identifier", ModBlocks.NORMAL_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> TREASURE_IDENTIFIER_ITEM   = registerIdentifier("treasure_identifier", ModBlocks.TREASURE_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> SHOP_IDENTIFIER_ITEM       = registerIdentifier("shop_identifier", ModBlocks.SHOP_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> SECRET_IDENTIFIER_ITEM     = registerIdentifier("secret_identifier", ModBlocks.SECRET_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> SUPER_SECRET_IDENTIFIER_ITEM = registerIdentifier("super_secret_identifier", ModBlocks.SUPER_SECRET_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> ULTRA_SECRET_IDENTIFIER_ITEM = registerIdentifier("ultra_secret_identifier", ModBlocks.ULTRA_SECRET_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> BOSS_IDENTIFIER_ITEM       = registerIdentifier("boss_identifier", ModBlocks.BOSS_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> ELITE_IDENTIFIER_ITEM      = registerIdentifier("elite_identifier", ModBlocks.ELITE_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> GAMBLE_IDENTIFIER_ITEM     = registerIdentifier("gamble_identifier", ModBlocks.GAMBLE_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> LIBRARY_IDENTIFIER_ITEM    = registerIdentifier("library_identifier", ModBlocks.LIBRARY_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> CURSE_IDENTIFIER_ITEM      = registerIdentifier("curse_identifier", ModBlocks.CURSE_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> ANGEL_IDENTIFIER_ITEM      = registerIdentifier("angel_identifier", ModBlocks.ANGEL_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> DEVIL_IDENTIFIER_ITEM      = registerIdentifier("devil_identifier", ModBlocks.DEVIL_IDENTIFIER_BLOCK);
    public static final RegistryObject<Item> PLANETARIUM_IDENTIFIER_ITEM = registerIdentifier("planetarium_identifier", ModBlocks.PLANETARIUM_IDENTIFIER_BLOCK);

    // misc end
    private static RegistryObject<Item> registerGeneric(String name) {
        RegistryObject<Item> reg = ITEMS.register(name, () -> new Item(new Item.Properties()));
        GENERIC_LIST.add(reg);
        return reg;
    }



    public static final RegistryObject<Item> LOCK                   = registerGeneric("lock");
    public static final RegistryObject<Item> QUESTION_MARK          = registerGeneric("question_mark");
}
