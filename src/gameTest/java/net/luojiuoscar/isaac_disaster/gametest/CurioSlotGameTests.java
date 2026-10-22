package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.event.custom.misc.PassiveItemObtainEvent;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.UUID;
import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CurioSlotGameTests {
    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "slot_test"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        DefaultAttributeManager.apply(player);
        for (var attribute : new net.minecraft.world.entity.ai.attributes.Attribute[]{ModAttributes.TRINKET_SLOTS.get(), ModAttributes.PASSIVE_ITEM_SLOTS.get()}) {
            var instance = player.getAttribute(attribute);
            for (var modifier : new ArrayList<>(instance.getModifiers())) StatManager.removeModifier(player, instance, modifier.getId());
            instance.setBaseValue(attribute == ModAttributes.TRINKET_SLOTS.get() ? 1 : 5);
        }
        CurioSlotManager.tick(player);
        return player;
    }

    private static ICurioStacksHandler slots(ServerPlayer player, String type) {
        return CuriosApi.getCuriosInventory(player).resolve().orElseThrow().getStacksHandler(type).orElseThrow();
    }

    private static PlayerIsaacItems data(ServerPlayer player) {
        return player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
    }

    private static void tick(ServerPlayer player) {
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    private static void equip(ServerPlayer player, String type, int index, Item item) {
        slots(player, type).getStacks().setStackInSlot(index, new ItemStack(item));
        tick(player);
    }

    private static int inventoryCount(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static int rewards(GameTestHelper helper, ServerPlayer player) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(player.position(), player.position()).inflate(2))
                .stream().filter(item -> item.getItem().getItem() instanceof Trinket)
                .mapToInt(item -> item.getItem().getCount()).sum();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void realPurseEquipStacksAndGrantsEachFirstRewardOnce(GameTestHelper helper) {
        var player = player(helper);
        int before = rewards(helper, player);
        equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_PURSE.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 1, ModPassiveItems.MOMS_PURSE.get());
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 3, "Two physical purses add exactly two slots");
        helper.assertTrue(rewards(helper, player) - before == 2, "Two physical purses spawn two first-obtain rewards");
        CurioSlotManager.requestReconcile(player);
        tick(player);
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 3, "Repeated callbacks and reconciliation do not stack");
        helper.assertTrue(rewards(helper, player) - before == 2, "Reconciliation does not repeat first-obtain rewards");
        slots(player, CurioSlotManager.PASSIVE_ITEM).getStacks().setStackInSlot(1, ItemStack.EMPTY);
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 2, "Unequip removes exactly one slot");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void fiveToOneReturnsOverflowAndRemovesModulesAndSets(GameTestHelper helper) {
        var player = player(helper);
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double range = player.getAttributeValue(ModAttributes.BULLET_RANGE.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.DARK_MATTER.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 1, ModPassiveItems.MOMS_CONTACTS.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 2, ModPassiveItems.CONE_HEAD.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 3, ModPassiveItems.SCORPIO.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 4, ModPassiveItems.MOMS_PURSE.get());
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.ALLOW_CURIO_UNEQUIP, false);
            player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(1);
            tick(player);
            tick(player);
            helper.assertTrue(slots(player, CurioSlotManager.PASSIVE_ITEM).getSlots() == 1, "Shrinks despite manual unequip prohibition");
            helper.assertTrue(slots(player, CurioSlotManager.PASSIVE_ITEM).getStacks().getStackInSlot(0).is(ModPassiveItems.DARK_MATTER.get()), "Lowest index stays equipped");
            for (Item item : new Item[]{ModPassiveItems.MOMS_CONTACTS.get(), ModPassiveItems.CONE_HEAD.get(), ModPassiveItems.SCORPIO.get(), ModPassiveItems.MOMS_PURSE.get()}) {
                helper.assertTrue(inventoryCount(player, item) == 1, "Overflow returns exactly once: " + item);
            }
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage - StatManager.DAMAGE.getBonus()) < 1e-6, "Retained damage bonus stays active");
            helper.assertTrue(Math.abs(player.getAttributeValue(ModAttributes.BULLET_RANGE.get()) - range) < 1e-6, "Removed range bonus is gone");
            var modules = player.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow().getTriggerModules();
            helper.assertTrue(modules.contains(ModTriggerModules.DARK_MATTER.getId()) && !modules.contains(ModTriggerModules.MOMS_CONTACTS.getId())
                    && !modules.contains(ModTriggerModules.CONE_HEAD.getId()) && !modules.contains(ModTriggerModules.SCORPIO.getId()), "Only retained modules remain");
            helper.assertTrue(data(player).getSetCountFromId(ModSetAbilities.MOM.getId()) == 0, "Removed Mom set contributions are gone");
            helper.assertTrue(data(player).getActiveCurioSlots().size() == 1, "No overflow equipment records remain");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void zeroCapacityDropsOverflowWhenInventoryIsFullAndRecovers(GameTestHelper helper) {
        var player = player(helper);
        equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.DARK_MATTER.get());
        equip(player, CurioSlotManager.TRINKET, 0, ModTrinkets.PAPER_CLIP.get());
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(0);
        player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(0);
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 0 && slots(player, CurioSlotManager.PASSIVE_ITEM).getSlots() == 0, "Both zero capacities applied");
        for (Item item : new Item[]{ModPassiveItems.DARK_MATTER.get(), ModTrinkets.PAPER_CLIP.get()}) {
            int count = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(player.position(), player.position()).inflate(3))
                    .stream().filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
            helper.assertTrue(count == 1, "Full inventory drops exactly one overflow item: " + item);
        }
        helper.assertTrue(data(player).getActiveCurioSlots().isEmpty(), "No active records remain at zero");
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(1);
        player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 1 && slots(player, CurioSlotManager.PASSIVE_ITEM).getSlots() == 5, "Empty capacities can grow again");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void replacingOneSlotAndReconcilingDoesNotRepeatBonuses(GameTestHelper helper) {
        var player = player(helper);
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double range = player.getAttributeValue(ModAttributes.BULLET_RANGE.get());
        equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.DARK_MATTER.get());
        var stacks = slots(player, CurioSlotManager.PASSIVE_ITEM).getStacks();
        stacks.setStackInSlot(0, stacks.getStackInSlot(0).copy());
        tick(player);
        CurioSlotManager.requestReconcile(player);
        tick(player);
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage - StatManager.DAMAGE.getBonus()) < 1e-6, "Equivalent replacement is idempotent");
        equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_CONTACTS.get());
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage) < 1e-6, "Previous item bonus removed once");
        helper.assertTrue(Math.abs(player.getAttributeValue(ModAttributes.BULLET_RANGE.get()) - range - StatManager.RANGE.getBonus()) < 1e-6, "Replacement bonus applied once");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void removingPurseByShrinkDefersItsSecondShrink(GameTestHelper helper) {
        var player = player(helper);
        equip(player, CurioSlotManager.PASSIVE_ITEM, 4, ModPassiveItems.MOMS_PURSE.get());
        equip(player, CurioSlotManager.TRINKET, 0, ModTrinkets.PAPER_CLIP.get());
        equip(player, CurioSlotManager.TRINKET, 1, ModTrinkets.LUCKY_TOE.get());
        player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(0);
        CurioSlotManager.tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 2, "Callback does not resize another slot recursively");
        CurioSlotManager.tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 1, "Next tick applies the cascade");
        helper.assertTrue(inventoryCount(player, ModPassiveItems.MOMS_PURSE.get()) == 1 && inventoryCount(player, ModTrinkets.LUCKY_TOE.get()) == 1, "Cascade returns each overflow once");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void playerSaveLoadRebuildsTransientCapacityWithoutReobtain(GameTestHelper helper) {
        var original = player(helper);
        equip(original, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_PURSE.get());
        int before = rewards(helper, original);
        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        var restored = player(helper);
        restored.load(saved);
        DefaultAttributeManager.apply(restored);
        CurioSlotManager.requestReconcile(restored);
        CurioSlotManager.tick(restored);
        tick(restored);
        tick(restored);
        helper.assertTrue(slots(restored, CurioSlotManager.TRINKET).getSlots() == GameplayConfig.DEFAULT_TRINKET_SLOTS.get() + 1, "Loaded item bonus and rebuilt configured capacity agree");
        helper.assertTrue(slots(restored, CurioSlotManager.PASSIVE_ITEM).getStacks().getStackInSlot(0).is(ModPassiveItems.MOMS_PURSE.get()), "First Curios tick does not eject loaded equipment");
        helper.assertTrue(data(restored).getActiveCurioSlots().size() == 1, "Current version equipment records load once");
        helper.assertTrue(rewards(helper, original) == before, "Load does not repeat the first-obtain reward");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void swallowedPhysicalTrinketKeepsEffectsAndCannotBeDuplicated(GameTestHelper helper) {
        var player = player(helper);
        double luck = player.getAttributeValue(Attributes.LUCK);
        equip(player, CurioSlotManager.TRINKET, 0, ModTrinkets.LUCKY_TOE.get());
        double equippedLuck = player.getAttributeValue(Attributes.LUCK);
        helper.assertTrue(equippedLuck > luck, "Physical trinket effect is present");
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(player) == 1, "One physical trinket transferred");
        tick(player);
        CurioSlotManager.requestReconcile(player);
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getStacks().getStackInSlot(0).isEmpty(), "Physical slot cleared");
        helper.assertTrue(PlayerHelper.getTrinketCount(ModTrinkets.LUCKY_TOE.getId(), player) == 1, "Swallowed and equipped caches contain only one copy");
        helper.assertTrue(player.getAttributeValue(Attributes.LUCK) == equippedLuck, "Effects are neither removed nor reapplied");
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(player) == 0, "An empty slot cannot be swallowed again");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void commandAndMultiplierValuesOverrideCuriosModifiers(GameTestHelper helper) {
        var player = player(helper);
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),
                "attribute @s isaac_disaster:trinket_slots base set 4.8");
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 4, "Slash command final value floors to four");
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).addTransientModifier(new AttributeModifier(UUID.randomUUID(), "test_multiplier", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL));
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 6, "External player attribute multiplier affects capacity");
        slots(player, CurioSlotManager.TRINKET).addTransientModifier(new AttributeModifier(UUID.randomUUID(), "foreign_slot", 20, AttributeModifier.Operation.ADDITION));
        CurioSlotManager.tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 6, "Curios modifiers cannot become a second capacity source");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void changedDefaultsPreservePurseBonusAndSignedRemoval(GameTestHelper helper) {
        var player = player(helper);
        try (var settings = GameTestDatapackSettings.override(helper)) {
            equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_PURSE.get());
            settings.set(GameplayConfig.DEFAULT_TRINKET_SLOTS, 2);
            settings.set(GameplayConfig.DEFAULT_PASSIVE_ITEM_SLOTS, 7);
            DefaultAttributeManager.apply(player);
            CurioSlotManager.requestReconcile(player);
            tick(player);
            helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 3 && slots(player, CurioSlotManager.PASSIVE_ITEM).getSlots() == 7, "Datapack changes defaults while preserving item bonus");
            StatManager.TRINKET_SLOTS.apply(player, -10);
            tick(player);
            helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 0, "Negative total clamps only at the final attribute");
            StatManager.TRINKET_SLOTS.apply(player, 10);
            tick(player);
            helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 3, "Signed modifier debt is preserved and reverses exactly");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void nestedEquipCallbacksDeferRatherThanReobtain(GameTestHelper helper) {
        var player = player(helper);
        int[] obtains = {0};
        Consumer<PassiveItemObtainEvent> listener = event -> {
            if (event.getPlayer() != player) return;
            obtains[0]++;
            CurioSlotManager.onEquip(new top.theillusivec4.curios.api.SlotContext(CurioSlotManager.PASSIVE_ITEM, player, 0, false, true),
                    ItemStack.EMPTY, event.getStack());
            CurioSlotManager.tick(player);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_PURSE.get());
            tick(player);
            helper.assertTrue(obtains[0] == 1 && slots(player, CurioSlotManager.TRINKET).getSlots() == 2, "Nested callbacks apply only one obtain and one slot bonus");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void deathCloneRetainsOnlyEffectsForRetainedCurios(GameTestHelper helper) {
        boolean keep = helper.getLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        try {
            for (boolean retain : new boolean[]{true, false}) {
                helper.getLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(retain, helper.getLevel().getServer());
                var original = player(helper);
                equip(original, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_PURSE.get());
                var drops = new ArrayList<ItemEntity>();
                new CuriosEventHandler().playerDrops(new LivingDropsEvent(original, original.damageSources().generic(), drops, 0, false));
                helper.assertTrue(drops.size() == (retain ? 0 : 1), "Native death drop stage respects keepInventory: " + retain);
                var clone = player(helper);
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(clone, original, true));
                ForgeEvents.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(clone, false));
                tick(clone);
                tick(clone);
                helper.assertTrue(slots(clone, CurioSlotManager.TRINKET).getSlots() == GameplayConfig.DEFAULT_TRINKET_SLOTS.get() + (retain ? 1 : 0), "Death capacity follows configured defaults and retained Curios: " + retain);
                helper.assertTrue(data(clone).getActiveCurioSlots().size() == (retain ? 1 : 0), "Only retained effects remain after clone: " + retain);
            }
        } finally {
            helper.getLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(keep, helper.getLevel().getServer());
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "curio_slots")
    public static void dimensionReconciliationAndSyncTagsKeepZeroAndRecovery(GameTestHelper helper) {
        var player = player(helper);
        equip(player, CurioSlotManager.PASSIVE_ITEM, 0, ModPassiveItems.MOMS_PURSE.get());
        ForgeEvents.onCurioPlayerChangedDimension(new PlayerEvent.PlayerChangedDimensionEvent(player,
                net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER));
        tick(player);
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getSlots() == 2, "Dimension event does not duplicate bonuses");
        var mirror = new top.theillusivec4.curios.common.inventory.CurioStacksHandler(null, CurioSlotManager.TRINKET, 0,
                true, false, true, top.theillusivec4.curios.api.type.capability.ICurio.DropRule.DEFAULT);
        mirror.applySyncTag(slots(player, CurioSlotManager.TRINKET).getSyncTag());
        helper.assertTrue(mirror.getSlots() == 2, "Curios client sync tag establishes capacity");
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(0);
        StatManager.TRINKET_SLOTS.apply(player, -1);
        tick(player);
        mirror.applySyncTag(slots(player, CurioSlotManager.TRINKET).getSyncTag());
        helper.assertTrue(mirror.getSlots() == 0, "Zero capacity sync resizes the client mirror");
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(1);
        tick(player);
        mirror.applySyncTag(slots(player, CurioSlotManager.TRINKET).getSyncTag());
        helper.assertTrue(mirror.getSlots() == 1, "Capacity recovers on the client mirror");
        helper.succeed();
    }
}
