package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.ChestLootTrinket;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.PennyTrinket;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DaemonsTail;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.SwallowedPenny;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("isaac_disaster_trinket_descriptions")
@PrefixGameTestTemplate(false)
public final class TrinketDescriptionGameTests {
    private static final Set<String> STACKABLE_CHANCES = Set.of("lucky_rock", "black_lipstick",
            "bible_tract", "cartridge", "cricket_leg");

    @GameTest(template = "trajectory_empty")
    public static void everyTooltipResolvesAndOnlyVerifiedProbabilitiesAreMarked(GameTestHelper helper) {
        int checked = 0;
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!(item instanceof Trinket trinket)) continue;
            String path = ForgeRegistries.ITEMS.getKey(item).getPath();
            for (boolean gold : new boolean[]{false, true}) {
                ItemStack stack = stack(item, gold);
                var ability = trinket.getAbility();
                var lines = ability.getDesc(stack, null);
                helper.assertTrue(!lines.isEmpty(), "Every trinket retains its effect description: " + path);
                for (Component line : lines) {
                    String text = line.getString();
                    helper.assertTrue(!text.contains("%s") && !text.contains("%%") && !text.contains("item.isaac_disaster."),
                            "Minecraft resolves placeholders and translations: " + path + " " + text);
                }
                String last = lines.get(lines.size() - 1).getString();
                helper.assertTrue(last.equals(Component.translatable("item.isaac_disaster.special.stackable").getString())
                                == STACKABLE_CHANCES.contains(path),
                        "The final stacking line agrees with the actual probability logic: " + path);
                helper.assertTrue(ability.getSynergyDesc(stack, null).isEmpty(),
                        "Golden values are inline and no redundant no-effect line remains: " + path);
            }
            checked++;
        }
        helper.assertTrue(checked >= 40, "The audit must cover the complete registered trinket set");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void correctedWormStatsMatchTheConfiguredAppliedValues(GameTestHelper helper) {
        for (boolean gold : new boolean[]{false, true}) {
            var lazy = ((Trinket) ModTrinkets.LAZY_WORM.get()).getAbility().getDesc(stack(ModTrinkets.LAZY_WORM.get(), gold), null);
            helper.assertTrue(lazy.get(0).getString().equals(StatManager.BULLET_SPEED.description(gold ? -4 : -2).getString()),
                    "Lazy Worm displays the negative shot-speed modifier");
            for (var item : List.of(ModTrinkets.HOOK_WORM.get(), ModTrinkets.OUROBOROS_WORM.get())) {
                var lore = ((Trinket) item).getAbility().getDesc(stack(item, gold), null);
                helper.assertTrue(lore.stream().filter(c -> c.getString().equals(StatManager.TEARS.description(gold ? 1 : 0.5).getString())).count() == 1,
                        "Both worms display exactly one configured tears bonus");
                helper.assertTrue(lore.stream().filter(c -> c.getString().equals(StatManager.RANGE.description(gold ? 1 : 0.5).getString())).count() == 1,
                        "Both worms display exactly one configured range bonus");
            }
        }
        Component stat = StatManager.BULLET_SPEED.dynamicDescription(-2);
        helper.assertTrue(stat.getSiblings().size() == 1 && stat.getSiblings().get(0).getStyle().getColor().getValue() == ColorManager.SYNERGY
                        && stat.getStyle().getColor().getValue() != ColorManager.SYNERGY,
                "Only the changing number is yellow; the attribute label keeps its normal color");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void probabilityLoreUsesTheCurrentGoldValueAndLuck(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (var expected : List.of(new String[]{"swallowed_penny", "35.00", "70.00"},
                    new String[]{"lucky_rock", "5.00", "10.00"}, new String[]{"black_lipstick", "10.00", "20.00"},
                    new String[]{"bible_tract", "3.33", "6.67"}, new String[]{"daemons_tail", "70.00", "35.00"},
                    new String[]{"lucky_toe", "33.00", "66.00"})) {
                Item item = ForgeRegistries.ITEMS.getValue(ModTrinkets.CRICKET_LEG.getId().withPath(expected[0]));
                for (int i = 0; i < 2; i++) {
                    var lore = ((Trinket) item).getAbility().getDesc(stack(item, i == 1), f.player);
                    String percentage = expected[i + 1];
                    helper.assertTrue(lore.stream().anyMatch(c -> hasYellowValue(c, percentage)),
                            "Inline percentage agrees with the actual effect: " + expected[0]);
                }
            }
            f.player.getAttribute(Attributes.LUCK).setBaseValue(20);
            var cartridge = ((Trinket) ModTrinkets.CARTRIDGE.get()).getAbility();
            helper.assertTrue(hasYellowValue(cartridge.getDesc(stack(ModTrinkets.CARTRIDGE.get(), false), f.player).get(0), "10.00")
                            && hasYellowValue(cartridge.getDesc(stack(ModTrinkets.CARTRIDGE.get(), true), f.player).get(0), "20.00"),
                    "Cartridge uses live player luck for ordinary and gold previews");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void goldQueriesDistinguishVanillaEnchantmentsAndSwallowedGold(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); Fixture reloaded = new Fixture(helper)) {
            var id = ModTrinkets.MATCH_STICK.getId();
            helper.assertTrue(PlayerHelper.getValueFromTrinket(0.33, 0.66, id, f.player) == 0, "An absent trinket gives no probability");
            ItemStack vanilla = stack(ModTrinkets.MATCH_STICK.get(), false);
            vanilla.enchant(Enchantments.UNBREAKING, 1);
            f.equip(vanilla);
            helper.assertTrue(PlayerHelper.getValueFromTrinket(0.33, 0.66, id, f.player) == 0.33
                            && !PlayerHelper.hasTrinket(id, f.player, true),
                    "Vanilla enchantments do not make a golden trinket");
            f.equip(stack(ModTrinkets.MATCH_STICK.get(), true));
            helper.assertTrue(PlayerHelper.getValueFromTrinket(0.33, 0.66, id, f.player) == 0.66,
                    "Custom golden metadata selects the enhanced value");
            helper.assertTrue(CurioSlotManager.swallowTrinkets(f.player) == 1, "One real equipped trinket is swallowed");
            f.tick();
            f.equip(stack(ModTrinkets.MATCH_STICK.get(), false));
            helper.assertTrue(PlayerHelper.getValueFromTrinket(0.33, 0.66, id, f.player) == 0.66,
                    "Swallowed gold wins over an equipped ordinary copy without adding another probability");
            CompoundTag saved = new CompoundTag();
            f.items().saveNBTData(saved);
            reloaded.items().loadNBTData(saved);
            helper.assertTrue(PlayerHelper.hasTrinket(id, reloaded.player, true)
                            && PlayerHelper.getValueFromTrinket(0.33, 0.66, id, reloaded.player) == 0.66,
                    "Swallowed gold remains detectable after NBT reload");
            f.items().removeAt(f.player, 0);
            helper.assertTrue(PlayerHelper.getValueFromTrinket(0.33, 0.66, id, f.player) == 0.33,
                    "Removing swallowed gold restores the remaining ordinary value");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void chestBonusUsesOneGoldAwareRollWithoutCopyStacking(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.equip(stack(ModTrinkets.LUCKY_TOE.get(), false));
            long seed = 0;
            while (true) {
                double roll = RandomSource.create(seed).nextDouble();
                if (roll > 0.33 && roll < 0.66) break;
                seed += 1_000_003;
            }
            helper.assertTrue(!chestAddsLoot(helper, f, seed), "The ordinary 33% chance misses this controlled roll");
            f.items().addToList(stack(ModTrinkets.LUCKY_TOE.get(), false));
            helper.assertTrue(!chestAddsLoot(helper, f, seed), "A second ordinary copy does not increase the chest probability");
            f.items().addToList(stack(ModTrinkets.LUCKY_TOE.get(), true));
            helper.assertTrue(chestAddsLoot(helper, f, seed), "Swallowed gold's 66% chance succeeds on the same roll");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void daemonsTailRemovesTheApprovedFractionAndPreservesOtherLoot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (boolean gold : new boolean[]{false, true}) {
                f.equip(stack(ModTrinkets.DAEMONS_TAIL.get(), gold));
                var event = loot(helper, f.player, new ItemStack(ModItems.RED_HEART.get(), 32));
                event.getLootContext().getRandom().setSeed(1248);
                RandomSource reference = RandomSource.create(1248);
                int kept = 0;
                for (int i = 0; i < 32; i++) if (reference.nextDouble() >= (gold ? 0.35 : 0.7)) kept++;
                var context = new ExecutableEffectContext(f.player);
                context.set(ContextKeys.EVENT, event);
                helper.assertTrue(new DaemonsTail().applyEffect(context), "Valid heart loot is processed");
                helper.assertTrue(event.getObjectArrayList().stream().filter(s -> s.is(ModItems.BLACK_HEART.get())).mapToInt(ItemStack::getCount).sum() == kept,
                        "Only hearts surviving the removal roll become black hearts");
                helper.assertTrue(event.getObjectArrayList().stream().anyMatch(s -> s.is(Items.APPLE)), "Unrelated loot is retained");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void resourcePennyCopiesDoNotChangeLootRolls(GameTestHelper helper) {
        for (var item : List.of(ModTrinkets.BLOODY_PENNY.get(), ModTrinkets.BURNT_PENNY.get(), ModTrinkets.FLAT_PENNY.get(),
                ModTrinkets.BLESSED_PENNY.get(), ModTrinkets.CHARGED_PENNY.get(), ModTrinkets.COUNTERFEIT_PENNY.get())) {
            try (Fixture f = new Fixture(helper)) {
                f.equip(stack(item, false));
                var baseline = new ArrayList<Map<Item, Integer>>();
                for (int seed = 0; seed < 16; seed++) baseline.add(pennyLoot(helper, f, seed * 1_000_003L));
                for (boolean gold : new boolean[]{false, true}) {
                    helper.assertTrue(CurioSlotManager.swallowTrinkets(f.player) == 1, "A real equipped copy is swallowed");
                    f.tick();
                    f.equip(stack(item, gold));
                    for (int seed = 0; seed < 16; seed++) {
                        helper.assertTrue(baseline.get(seed).equals(pennyLoot(helper, f, seed * 1_000_003L)),
                                "Extra ordinary or gold copies cannot increase penny loot: " + ForgeRegistries.ITEMS.getKey(item));
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void swallowedPennyUsesOwnershipRatherThanAggregateStrength(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            long seed = 0;
            while (true) {
                double roll = RandomSource.create(seed).nextDouble();
                if (roll > 0.35 && roll < 0.7) break;
                seed += 1_000_003;
            }
            helper.assertTrue(!swallowedPennyDrops(helper, f, seed), "Missing trinkets do not proc even with a supplied amplifier");
            f.equip(stack(ModTrinkets.SWALLOWED_PENNY.get(), false));
            helper.assertTrue(!swallowedPennyDrops(helper, f, seed), "One ordinary copy uses 35% and misses this roll");
            CurioSlotManager.swallowTrinkets(f.player);
            f.tick();
            f.equip(stack(ModTrinkets.SWALLOWED_PENNY.get(), false));
            helper.assertTrue(!swallowedPennyDrops(helper, f, seed), "Two ordinary copies still use 35%");
            f.equip(stack(ModTrinkets.SWALLOWED_PENNY.get(), true));
            helper.assertTrue(swallowedPennyDrops(helper, f, seed), "A mixed ordinary/gold set uses gold's 70%");
            CurioSlotManager.swallowTrinkets(f.player);
            f.tick();
            f.equip(stack(ModTrinkets.SWALLOWED_PENNY.get(), false));
            helper.assertTrue(swallowedPennyDrops(helper, f, seed), "Swallowed gold preserves the upgrade without combining probabilities");
            f.items().removeAt(f.player, 1);
            helper.assertTrue(!swallowedPennyDrops(helper, f, seed), "Removing gold restores 35% while ordinary copies remain");
        }
        helper.succeed();
    }

    private static Map<Item, Integer> pennyLoot(GameTestHelper helper, Fixture fixture, long seed) {
        Item coin = PlayerHelper.getItemFromConfig(GameplayConfig.COIN_TIER_1_ID.get());
        helper.assertTrue(coin != null, "The configured tier-one coin exists");
        var event = loot(helper, fixture.player, new ItemStack(coin, 4));
        fixture.player.getRandom().setSeed(seed);
        var context = new ExecutableEffectContext(fixture.player);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.AMPLIFIER, 100.0);
        helper.assertTrue(new PennyTrinket().applyEffect(context), "The shared penny loot effect executes");
        Map<Item, Integer> result = new HashMap<>();
        for (ItemStack stack : event.getObjectArrayList()) {
            if (!stack.isEmpty()) result.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return result;
    }

    private static boolean swallowedPennyDrops(GameTestHelper helper, Fixture fixture, long seed) {
        fixture.player.getRandom().setSeed(seed);
        var context = new ExecutableEffectContext(fixture.player);
        context.set(ContextKeys.AMPLIFIER, 100.0);
        helper.assertTrue(new SwallowedPenny().applyEffect(context), "The server-side hurt payoff executes");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, fixture.player.getBoundingBox().inflate(3));
        boolean produced = !drops.isEmpty();
        drops.forEach(ItemEntity::discard);
        return produced;
    }

    private static boolean chestAddsLoot(GameTestHelper helper, Fixture fixture, long seed) {
        var event = loot(helper, fixture.player, ItemStack.EMPTY);
        fixture.player.getRandom().setSeed(seed);
        var context = new ExecutableEffectContext(fixture.player);
        context.set(ContextKeys.EVENT, event);
        helper.assertTrue(new ChestLootTrinket().applyEffect(context), "A real chest loot event executes");
        return event.getObjectArrayList().size() > 1;
    }

    private static GeneralLootModifyEvent loot(GameTestHelper helper, ServerPlayer player, ItemStack extra) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.CHEST);
        var params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absolutePos(pos).getCenter())
                .withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.EMPTY);
        var entries = new ObjectArrayList<ItemStack>();
        entries.add(new ItemStack(Items.APPLE));
        if (!extra.isEmpty()) entries.add(extra);
        return new GeneralLootModifyEvent(player, new LootContext.Builder(params).create(null), entries);
    }

    private static boolean hasYellowValue(Component component, String expected) {
        return component.visit((style, text) -> text.equals(expected) && style.getColor() != null
                && style.getColor().getValue() == ColorManager.SYNERGY ? Optional.of(true) : Optional.<Boolean>empty(), Style.EMPTY).orElse(false);
    }

    private static ItemStack stack(Item item, boolean gold) {
        ItemStack stack = new ItemStack(item);
        Trinket.setEnchanted(stack, gold);
        return stack;
    }

    private static final class Fixture implements AutoCloseable {
        private final ServerPlayer player;

        private Fixture(GameTestHelper helper) {
            player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "trinket-lore"));
            player.setPos(helper.absolutePos(BlockPos.ZERO).getCenter());
            DefaultAttributeManager.apply(player);
            player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(1);
            CurioSlotManager.tick(player);
        }

        private PlayerIsaacItems items() {
            return player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
        }

        private void equip(ItemStack stack) {
            CuriosApi.getCuriosInventory(player).resolve().orElseThrow().getStacksHandler(CurioSlotManager.TRINKET)
                    .orElseThrow().getStacks().setStackInSlot(0, stack);
            tick();
        }

        private void tick() {
            new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
            CurioSlotManager.tick(player);
        }

        @Override
        public void close() {
            equip(ItemStack.EMPTY);
            items().clear(player);
            player.discard();
        }
    }
}
