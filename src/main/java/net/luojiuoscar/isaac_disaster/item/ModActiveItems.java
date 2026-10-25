package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.DisposableActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbilities;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModActiveItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> ANARCHIST_COOKBOOK = ITEMS.register("anarchist_cookbook",
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbilities.ANARCHIST_COOKBOOK));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(ANARCHIST_COOKBOOK);}

    public static final RegistryObject<Item> YUM_HEART = ITEMS.register("yum_heart",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.YUM_HEART));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(YUM_HEART);}

    public static final RegistryObject<Item> THE_BOOK_OF_BELIAL = ITEMS.register("the_book_of_belial",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.THE_BOOK_OF_BELIAL));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_BOOK_OF_BELIAL);}

    public static final RegistryObject<Item> BOOK_OF_SHADOW = ITEMS.register("book_of_shadow",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.BOOK_OF_SHADOW));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(BOOK_OF_SHADOW);}

    public static final RegistryObject<Item> THE_BIBLE = ITEMS.register("the_bible",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.THE_BIBLE));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_BIBLE);}

    public static final RegistryObject<Item> THE_NECRONMICON = ITEMS.register("the_necronmicon",
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbilities.THE_NECRONMICON));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_NECRONMICON);}

    public static final RegistryObject<Item> WOODEN_NICKEL = ITEMS.register("wooden_nickel",
            () -> new ActiveItem(new Item.Properties(), 3, 3, ModActiveAbilities.WOODEN_NICKEL));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(WOODEN_NICKEL);}

    public static final RegistryObject<Item> TELEPORT = ITEMS.register("teleport",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbilities.TELEPORT));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(TELEPORT);}

    public static final RegistryObject<Item> LEMON_MISHAP = ITEMS.register("lemon_mishap",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbilities.LEMON_MISHAP));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(LEMON_MISHAP);}

    public static final RegistryObject<Item> FREE_LEMONADE = ITEMS.register("free_lemonade",
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbilities.FREE_LEMONADE));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(FREE_LEMONADE);}

    public static final RegistryObject<Item> THE_GAMEKID = ITEMS.register("the_gamekid",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.THE_GAMEKID));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_GAMEKID);}

    public static final RegistryObject<Item> UNICORN_STUMP = ITEMS.register("unicorn_stump",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.UNICORN_STUMP));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(UNICORN_STUMP);}

    public static final RegistryObject<Item> MY_LITTLE_UNICORN = ITEMS.register("my_little_unicorn",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.MY_LITTLE_UNICORN));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MY_LITTLE_UNICORN);}

    public static final RegistryObject<Item> PLACEBO = ITEMS.register("placebo",
            () -> new ActiveItem(new Item.Properties(), 24, 24, ModActiveAbilities.PLACEBO));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(PLACEBO);}

    public static final RegistryObject<Item> DIPLOPIA = ITEMS.register("diplopia",
            () -> new DisposableActiveItem(new Item.Properties(), 0, 0, ModActiveAbilities.DIPLOPIA));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(DIPLOPIA);}

    public static final RegistryObject<Item> CROOKED_PENNY = ITEMS.register("crooked_penny",
            () -> new ActiveItem(new Item.Properties(), 18, 18, ModActiveAbilities.CROOKED_PENNY));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(CROOKED_PENNY);}

    public static final RegistryObject<Item> DULL_RAZOR = ITEMS.register("dull_razor",
            () -> new ActiveItem(new Item.Properties(), 2, 2, ModActiveAbilities.DULL_RAZOR));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(DULL_RAZOR);}

    public static final RegistryObject<Item> TELEPATHY_FOR_DUMMIES = ITEMS.register("telepathy_for_dummies",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.TELEPATHY_FOR_DUMMIES));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(TELEPATHY_FOR_DUMMIES);}

    public static final RegistryObject<Item> SMELTER = ITEMS.register("smelter",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.SMELTER));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(SMELTER);}

    public static final RegistryObject<Item> THE_D6 = ITEMS.register("the_d6",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.THE_D6));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_D6);}

    public static final RegistryObject<Item> KAMIKAZE = ITEMS.register("kamikaze",
            () -> new ActiveItem(new Item.Properties(), 0, 0, ModActiveAbilities.KAMIKAZE));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(KAMIKAZE);}

    public static final RegistryObject<Item> MR_BOOM = ITEMS.register("mr_boom",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbilities.MR_BOOM));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MR_BOOM);}

    public static final RegistryObject<Item> TAMMYS_HEAD = ITEMS.register("tammys_head",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbilities.TAMMYS_HEAD));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(TAMMYS_HEAD);}

    public static final RegistryObject<Item> THE_HOURGLASS = ITEMS.register("the_hourglass",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.THE_HOURGLASS));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_HOURGLASS);}

    public static final RegistryObject<Item> IV_BAG = ITEMS.register("iv_bag",
            () -> new ActiveItem(new Item.Properties(), 0, 0, ModActiveAbilities.IV_BAG));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(IV_BAG);}

    public static final RegistryObject<Item> PRAYER_CARD = ITEMS.register("prayer_card",
            () -> new ActiveItem(new Item.Properties(), 24, 24, ModActiveAbilities.PRAYER_CARD));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(PRAYER_CARD);}

    public static final RegistryObject<Item> SHOOP_DA_WHOOP = ITEMS.register("shoop_da_whoop",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.SHOOP_DA_WHOOP));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(SHOOP_DA_WHOOP);}

    public static final RegistryObject<Item> THE_POOP = ITEMS.register("the_poop",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbilities.THE_POOP));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_POOP);}

    public static final RegistryObject<Item> THE_BEAN = ITEMS.register("the_bean",
            () -> new ActiveItem(new Item.Properties(), 2, 2, ModActiveAbilities.THE_BEAN));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_BEAN);}

    public static final RegistryObject<Item> BLOOD_RIGHTS = ITEMS.register("blood_rights",
            () -> new ActiveItem(new Item.Properties(), 0, 0, ModActiveAbilities.BLOOD_RIGHTS));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(BLOOD_RIGHTS);}

    public static final RegistryObject<Item> BOX = ITEMS.register("box",
            () -> new DisposableActiveItem(new Item.Properties(), 0, 0, ModActiveAbilities.BOX));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(BOX);}

    public static final RegistryObject<Item> MOMS_BRA = ITEMS.register("moms_bra",
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbilities.MOMS_BRA));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MOMS_BRA);}

    public static final RegistryObject<Item> DECK_OF_CARDS = ITEMS.register("deck_of_cards",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.DECK_OF_CARDS));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(DECK_OF_CARDS);}

    public static final RegistryObject<Item> THE_BOOK_OF_SIN = ITEMS.register("the_book_of_sin",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.THE_BOOK_OF_SIN));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_BOOK_OF_SIN);}

    public static final RegistryObject<Item> MOMS_BOTTLE_OF_PILLS = ITEMS.register("moms_bottle_of_pills",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.MOMS_BOTTLE_OF_PILLS));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MOMS_BOTTLE_OF_PILLS);}

    public static final RegistryObject<Item> MAGIC_FINGERS = ITEMS.register("magic_fingers",
            () -> new ActiveItem(new Item.Properties(), 0, 0, ModActiveAbilities.MAGIC_FINGERS));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MAGIC_FINGERS);}

    public static final RegistryObject<Item> D8 = ITEMS.register("d8",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbilities.D8));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(D8);}

    public static final RegistryObject<Item> KIDNEY_BEAN = ITEMS.register("kidney_bean",
            () -> new ActiveItem(new Item.Properties(), 2, 2, ModActiveAbilities.KIDNEY_BEAN));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(KIDNEY_BEAN);}

    public static final RegistryObject<Item> ETERNAL_D6 = ITEMS.register("eternal_d6",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbilities.ETERNAL_D6));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(ETERNAL_D6);}

    public static final RegistryObject<Item> D12 = ITEMS.register("d12",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbilities.D12));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(D12);}

    public static final RegistryObject<Item> MOMS_PAD = ITEMS.register("moms_pad",
            () -> new ActiveItem(new Item.Properties(), 2, 2, ModActiveAbilities.MOMS_PAD));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MOMS_PAD);}

    public static final RegistryObject<Item> GUPPYS_HEAD = ITEMS.register("guppys_head",
            () -> new ActiveItem(new Item.Properties(), 1, 1, ModActiveAbilities.GUPPYS_HEAD));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(GUPPYS_HEAD);}

    public static final RegistryObject<Item> BOX_OF_SPIDERS = ITEMS.register("box_of_spiders",
            () -> new ActiveItem(new Item.Properties(), 2, 2, ModActiveAbilities.BOX_OF_SPIDERS));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(BOX_OF_SPIDERS);}

}
