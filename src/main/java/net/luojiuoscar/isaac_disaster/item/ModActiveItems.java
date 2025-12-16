package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.DisposableActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
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
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbility.ANARCHIST_COOKBOOK));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(ANARCHIST_COOKBOOK);}

    public static final RegistryObject<Item> YUM_HEART = ITEMS.register("yum_heart",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbility.YUM_HEART));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(YUM_HEART);}

    public static final RegistryObject<Item> THE_BOOK_OF_BELIAL = ITEMS.register("the_book_of_belial",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbility.THE_BOOK_OF_BELIAL));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_BOOK_OF_BELIAL);}

    public static final RegistryObject<Item> BOOK_OF_SHADOW = ITEMS.register("book_of_shadow",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.BOOK_OF_SHADOW));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(BOOK_OF_SHADOW);}

    public static final RegistryObject<Item> THE_BIBLE = ITEMS.register("the_bible",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.THE_BIBLE));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_BIBLE);}

    public static final RegistryObject<Item> THE_NECRONMICON = ITEMS.register("the_necronmicon",
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbility.THE_NECRONMICON));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_NECRONMICON);}

    public static final RegistryObject<Item> WOODEN_NICKEL = ITEMS.register("wooden_nickel",
            () -> new ActiveItem(new Item.Properties(), 3, 3, ModActiveAbility.WOODEN_NICKEL));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(WOODEN_NICKEL);}

    public static final RegistryObject<Item> TELEPORT = ITEMS.register("teleport",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbility.TELEPORT));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(TELEPORT);}

    public static final RegistryObject<Item> LEMON_MISHAP = ITEMS.register("lemon_mishap",
            () -> new ActiveItem(new Item.Properties(), 4, 4, ModActiveAbility.LEMON_MISHAP));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(LEMON_MISHAP);}

    public static final RegistryObject<Item> FREE_LEMONADE = ITEMS.register("free_lemonade",
            () -> new ActiveItem(new Item.Properties(), 6, 6, ModActiveAbility.FREE_LEMONADE));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(FREE_LEMONADE);}

    public static final RegistryObject<Item> THE_GAMEKID = ITEMS.register("the_gamekid",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.THE_GAMEKID));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_GAMEKID);}

    public static final RegistryObject<Item> UNICORN_STUMP = ITEMS.register("unicorn_stump",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.UNICORN_STUMP));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(UNICORN_STUMP);}

    public static final RegistryObject<Item> MY_LITTLE_UNICORN = ITEMS.register("my_little_unicorn",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.MY_LITTLE_UNICORN));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(MY_LITTLE_UNICORN);}

    public static final RegistryObject<Item> PLACEBO = ITEMS.register("placebo",
            () -> new ActiveItem(new Item.Properties(), 24, 24, ModActiveAbility.PLACEBO));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(PLACEBO);}

    public static final RegistryObject<Item> DIPLOPIA = ITEMS.register("diplopia",
            () -> new DisposableActiveItem(new Item.Properties(), 0, 0, ModActiveAbility.DIPLOPIA));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(DIPLOPIA);}

    public static final RegistryObject<Item> CROOKED_PENNY = ITEMS.register("crooked_penny",
            () -> new ActiveItem(new Item.Properties(), 18, 18, ModActiveAbility.CROOKED_PENNY));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(CROOKED_PENNY);}

    public static final RegistryObject<Item> DULL_RAZOR = ITEMS.register("dull_razor",
            () -> new ActiveItem(new Item.Properties(), 2, 2, ModActiveAbility.DULL_RAZOR));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(DULL_RAZOR);}

    public static final RegistryObject<Item> TELEPATHY_FOR_DUMMIES = ITEMS.register("telepathy_for_dummies",
            () -> new ActiveItem(new Item.Properties(), 8, 8, ModActiveAbility.TELEPATHY_FOR_DUMMIES));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(TELEPATHY_FOR_DUMMIES);}

    public static final RegistryObject<Item> SMELTER = ITEMS.register("smelter",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.SMELTER));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(SMELTER);}

    public static final RegistryObject<Item> THE_D6 = ITEMS.register("the_d6",
            () -> new ActiveItem(new Item.Properties(), 12, 12, ModActiveAbility.THE_D6));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(THE_D6);}

    public static final RegistryObject<Item> KAMIKAZE = ITEMS.register("kamikaze",
            () -> new ActiveItem(new Item.Properties(), 0, 0, ModActiveAbility.KAMIKAZE));
    static {ItemListManager.ACTIVE_ITEM_LIST.add(KAMIKAZE);}

}