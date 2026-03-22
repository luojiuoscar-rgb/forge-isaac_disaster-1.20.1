package net.luojiuoscar.isaac_disaster.registries.ability.active;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModActiveAbility {
    public static final ResourceKey<Registry<ActiveAbility>> ACTIVE_ABILITY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "active_ability"));

    public static final DeferredRegister<ActiveAbility> ACTIVE_ABILITY_REGISTRY =
            DeferredRegister.create(ACTIVE_ABILITY_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<ActiveAbility> ANARCHIST_COOKBOOK =
            ACTIVE_ABILITY_REGISTRY.register("anarchist_cookbook",
                    () -> new AnarchistCookbook(ItemId.ANARCHIST_COOKBOOK.getId(), ItemId.ANARCHIST_COOKBOOK.getLevel()));

    public static final RegistryObject<ActiveAbility> BOOK_OF_SHADOW =
            ACTIVE_ABILITY_REGISTRY.register("book_of_shadow",
                    () -> new BookOfShadow(ItemId.BOOK_OF_SHADOW.getId(), ItemId.BOOK_OF_SHADOW.getLevel()));

    public static final RegistryObject<ActiveAbility> CROOKED_PENNY =
            ACTIVE_ABILITY_REGISTRY.register("crooked_penny",
                    () -> new CrookedPenny(ItemId.CROOKED_PENNY.getId(), ItemId.CROOKED_PENNY.getLevel()));

    public static final RegistryObject<ActiveAbility> DIPLOPIA =
            ACTIVE_ABILITY_REGISTRY.register("diplopia",
                    () -> new Diplopia(ItemId.DIPLOPIA.getId(), ItemId.DIPLOPIA.getLevel()));

    public static final RegistryObject<ActiveAbility> DULL_RAZOR =
            ACTIVE_ABILITY_REGISTRY.register("dull_razor",
                    () -> new DullRazor(ItemId.DULL_RAZOR.getId(), ItemId.DULL_RAZOR.getLevel()));

    public static final RegistryObject<ActiveAbility> FREE_LEMONADE =
            ACTIVE_ABILITY_REGISTRY.register("free_lemonade",
                    () -> new FreeLemonade(ItemId.FREE_LEMONADE.getId(), ItemId.FREE_LEMONADE.getLevel()));

    public static final RegistryObject<ActiveAbility> KAMIKAZE =
            ACTIVE_ABILITY_REGISTRY.register("kamikaze",
                    () -> new Kamikaze(ItemId.KAMIKAZE.getId(), ItemId.KAMIKAZE.getLevel()));

    public static final RegistryObject<ActiveAbility> LEMON_MISHAP =
            ACTIVE_ABILITY_REGISTRY.register("lemon_mishap",
                    () -> new LemonMishap(ItemId.LEMON_MISHAP.getId(), ItemId.LEMON_MISHAP.getLevel()));

    public static final RegistryObject<ActiveAbility> MY_LITTLE_UNICORN =
            ACTIVE_ABILITY_REGISTRY.register("my_little_unicorn",
                    () -> new MyLittleUnicorn(ItemId.MY_LITTLE_UNICORN.getId(), ItemId.MY_LITTLE_UNICORN.getLevel()));

    public static final RegistryObject<ActiveAbility> PLACEBO =
            ACTIVE_ABILITY_REGISTRY.register("placebo",
                    () -> new Placebo(ItemId.PLACEBO.getId(), ItemId.PLACEBO.getLevel()));

    public static final RegistryObject<ActiveAbility> SMELTER =
            ACTIVE_ABILITY_REGISTRY.register("smelter",
                    () -> new Smelter(ItemId.SMELTER.getId(), ItemId.SMELTER.getLevel()));

    public static final RegistryObject<ActiveAbility> TELEPATHY_FOR_DUMMIES =
            ACTIVE_ABILITY_REGISTRY.register("telepathy_for_dummies",
                    () -> new TelepathyForDummies(ItemId.TELEPATHY_FOR_DUMMIES.getId(), ItemId.TELEPATHY_FOR_DUMMIES.getLevel()));

    public static final RegistryObject<ActiveAbility> TELEPORT =
            ACTIVE_ABILITY_REGISTRY.register("teleport",
                    () -> new Teleport(ItemId.TELEPORT.getId(), ItemId.TELEPORT.getLevel()));

    public static final RegistryObject<ActiveAbility> THE_BIBLE =
            ACTIVE_ABILITY_REGISTRY.register("the_bible",
                    () -> new TheBible(ItemId.THE_BIBLE.getId(), ItemId.THE_BIBLE.getLevel()));

    public static final RegistryObject<ActiveAbility> THE_BOOK_OF_BELIAL =
            ACTIVE_ABILITY_REGISTRY.register("the_book_of_belial",
                    () -> new TheBookOfBelial(ItemId.THE_BOOK_OF_BELIAL.getId(), ItemId.THE_BOOK_OF_BELIAL.getLevel()));

    public static final RegistryObject<ActiveAbility> THE_D6 =
            ACTIVE_ABILITY_REGISTRY.register("the_d6",
                    () -> new TheD6(ItemId.THE_D6.getId(), ItemId.THE_D6.getLevel()));

    public static final RegistryObject<ActiveAbility> THE_GAMEKID =
            ACTIVE_ABILITY_REGISTRY.register("the_gamekid",
                    () -> new TheGamekid(ItemId.THE_GAMEKID.getId(), ItemId.THE_GAMEKID.getLevel()));

    public static final RegistryObject<ActiveAbility> THE_NECRONMICON =
            ACTIVE_ABILITY_REGISTRY.register("the_necronmicon",
                    () -> new TheNecronmicon(ItemId.THE_NECRONMICON.getId(), ItemId.THE_NECRONMICON.getLevel()));

    public static final RegistryObject<ActiveAbility> UNICORN_STUMP =
            ACTIVE_ABILITY_REGISTRY.register("unicorn_stump",
                    () -> new UnicornStump(ItemId.UNICORN_STUMP.getId(), ItemId.UNICORN_STUMP.getLevel()));

    public static final RegistryObject<ActiveAbility> WOODEN_NICKEL =
            ACTIVE_ABILITY_REGISTRY.register("wooden_nickel",
                    () -> new WoodenNickel(ItemId.WOODEN_NICKEL.getId(), ItemId.WOODEN_NICKEL.getLevel()));

    public static final RegistryObject<ActiveAbility> YUM_HEART =
            ACTIVE_ABILITY_REGISTRY.register("yum_heart",
                    () -> new YumHeart(ItemId.YUM_HEART.getId(), ItemId.YUM_HEART.getLevel()));

    public static final RegistryObject<ActiveAbility> MR_BOOM =
            ACTIVE_ABILITY_REGISTRY.register("mr_boom",
                    () -> new MrBoom(ItemId.MR_BOOM.getId(), ItemId.MR_BOOM.getLevel()));

    public static final RegistryObject<ActiveAbility> TAMMYS_HEAD =
            ACTIVE_ABILITY_REGISTRY.register("tammys_head",
                    () -> new TammysHead(ItemId.TAMMYS_HEAD.getId(), ItemId.TAMMYS_HEAD.getLevel()));

    public static final RegistryObject<ActiveAbility> THE_HOURGLASS =
            ACTIVE_ABILITY_REGISTRY.register("the_hourglass",
                    () -> new TheHourglass(ItemId.THE_HOURGLASS.getId(), ItemId.THE_HOURGLASS.getLevel()));

    public static final RegistryObject<ActiveAbility> IV_BAG =
            ACTIVE_ABILITY_REGISTRY.register("iv_bag",
                    () -> new IvBag(ItemId.IV_BAG.getId(), ItemId.IV_BAG.getLevel()));

    public static final RegistryObject<ActiveAbility> PRAYER_CARD =
            ACTIVE_ABILITY_REGISTRY.register("prayer_card",
                    () -> new PrayerCard(ItemId.PRAYER_CARD.getId(), ItemId.PRAYER_CARD.getLevel()));
}





