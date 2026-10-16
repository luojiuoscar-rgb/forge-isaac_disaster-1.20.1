package net.luojiuoscar.isaac_disaster.datapack;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.luojiuoscar.isaac_disaster.event.ServerTickEvent;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_right_click")
@PrefixGameTestTemplate(false)
public final class RightClickGameTests {
    @GameTest(template = "trajectory_empty")
    public static void trinketUsesFirstEmptySlotAndNeverReplaces(GameTestHelper helper) {
        var player = player(helper);
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(2);
        CurioSlotManager.tick(player);
        var slots = slots(player, CurioSlotManager.TRINKET);
        slots.setStackInSlot(0, new ItemStack(ModTrinkets.PAPER_CLIP.get()));
        tick(player);
        var held = new ItemStack(ModTrinkets.LUCKY_TOE.get());
        Trinket.setEnchanted(held, true);
        held.getOrCreateTag().putString("test_marker", "preserved");
        double luck = player.getAttributeValue(Attributes.LUCK);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        use(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Survival right click moves the held trinket");
        helper.assertTrue(slots.getStackInSlot(0).is(ModTrinkets.PAPER_CLIP.get())
                && slots.getStackInSlot(1).is(ModTrinkets.LUCKY_TOE.get()), "First occupied slot stays; next empty slot receives the trinket");
        tick(player);
        double equippedLuck = player.getAttributeValue(Attributes.LUCK);
        helper.assertTrue(equippedLuck > luck, "Native right-click equipment applies its effect");
        tick(player);
        helper.assertTrue(player.getAttributeValue(Attributes.LUCK) == equippedLuck, "Repeated ticks do not duplicate effects");
        helper.assertTrue(Trinket.isEnchanted(slots.getStackInSlot(1))
                && "preserved".equals(slots.getStackInSlot(1).getOrCreateTag().getString("test_marker")), "Native transfer preserves stack data");
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModTrinkets.PAPER_CLIP.get()));
        use(player, InteractionHand.OFF_HAND);
        helper.assertTrue(!player.getOffhandItem().isEmpty() && slots.getStackInSlot(1).is(ModTrinkets.LUCKY_TOE.get()), "Full slots neither consume nor replace");
        var restored = player(helper);
        restored.load(player.saveWithoutId(new CompoundTag()));
        tick(restored);
        helper.assertTrue(Trinket.isEnchanted(slots(restored, CurioSlotManager.TRINKET).getStackInSlot(1)), "Enhanced equipment survives reload");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void passiveRoutesByBackpackSetting(GameTestHelper helper) {
        withBackpack(helper, false, () -> {
            var player = player(helper);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModPassiveItems.DARK_MATTER.get()));
            double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            use(player, InteractionHand.OFF_HAND);
            helper.assertTrue(player.getOffhandItem().isEmpty(), "Backpack disabled: native use moves passive out of the offhand");
            helper.assertTrue(slots(player, CurioSlotManager.PASSIVE_ITEM).getStackInSlot(0).is(ModPassiveItems.DARK_MATTER.get()), "Passive goes into its own Curios slot");
            tick(player);
            double equippedDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            helper.assertTrue(equippedDamage > damage, "Curios applies passive effects");
            tick(player);
            helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == equippedDamage, "Passive effect applies once");
            helper.assertTrue(player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().getPassiveItems().isEmpty(), "Curios equipment is not also stored in the extra backpack");
            slots(player, CurioSlotManager.PASSIVE_ITEM).setStackInSlot(0, ItemStack.EMPTY);
            tick(player);
            helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == damage, "Unequip reverses removable passive effects");
        });
        withBackpack(helper, true, () -> {
            var player = player(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModPassiveItems.DARK_MATTER.get()));
            use(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Backpack enabled: held passive is consumed into storage");
            helper.assertTrue(slots(player, CurioSlotManager.PASSIVE_ITEM).getStackInSlot(0).isEmpty(), "Curios must not intercept enabled backpack use");
            helper.assertTrue(player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().getPassiveItems().size() == 1, "Enabled backpack receives exactly one passive");
        });
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void zeroAndFullPassiveSlotsPreserveHand(GameTestHelper helper) {
        withBackpack(helper, false, () -> {
            var player = player(helper);
            player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(1);
            CurioSlotManager.tick(player);
            slots(player, CurioSlotManager.PASSIVE_ITEM).setStackInSlot(0, new ItemStack(ModPassiveItems.DARK_MATTER.get()));
            tick(player);
            var held = new ItemStack(ModPassiveItems.MOMS_CONTACTS.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            use(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem() == held && held.getCount() == 1, "Full passive slot preserves the held stack");
            helper.assertTrue(slots(player, CurioSlotManager.PASSIVE_ITEM).getStackInSlot(0).is(ModPassiveItems.DARK_MATTER.get()), "Full passive slot is not replaced");
            player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(0);
            player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(0);
            CurioSlotManager.tick(player);
            use(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(held.getCount() == 1, "Zero passive slots preserve the held stack");
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModTrinkets.LUCKY_TOE.get()));
            use(player, InteractionHand.OFF_HAND);
            helper.assertTrue(player.getOffhandItem().getCount() == 1, "Zero trinket slots preserve the held trinket");
        });
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mainHandWinsAndEquipRestrictionsApply(GameTestHelper helper) {
        var player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModTrinkets.PAPER_CLIP.get()));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModTrinkets.LUCKY_TOE.get()));
        for (var hand : InteractionHand.values()) {
            if (use(player, hand).consumesAction()) break;
        }
        helper.assertTrue(slots(player, CurioSlotManager.TRINKET).getStackInSlot(0).is(ModTrinkets.PAPER_CLIP.get())
                && player.getOffhandItem().is(ModTrinkets.LUCKY_TOE.get()), "Native main-hand-first interaction equips only the main hand");
        var denied = player(helper);
        var held = new ItemStack(ModTrinkets.PAPER_CLIP.get());
        denied.setItemInHand(InteractionHand.MAIN_HAND, held);
        Consumer<top.theillusivec4.curios.api.event.CurioEquipEvent> listener = event -> {
            if (event.getSlotContext().entity() == denied) event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            use(denied, InteractionHand.MAIN_HAND);
            helper.assertTrue(held.getCount() == 1 && slots(denied, CurioSlotManager.TRINKET).getStackInSlot(0).isEmpty(), "Curios equipment restrictions are respected");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void isaacHeadOnlyFiresAndChargesInMainHand(GameTestHelper helper) {
        var player = player(helper);
        var ability = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
        var head = new ItemStack(ModItems.ISAAC_HEAD.get());
        player.setItemInHand(InteractionHand.OFF_HAND, head);
        helper.assertTrue(!PlayerHelper.isHoldingIsaacHead(player), "Offhand head does not enable attacks");
        helper.assertTrue(head.use(player.level(), player, InteractionHand.OFF_HAND).getResult() == InteractionResult.PASS, "Offhand head does not consume vanilla item use");
        int[] attempts = {0};
        Consumer<BeforePerformAttackEvent> listener = event -> {
            if (event.getEntity() == player) { attempts[0]++; event.setCanceled(true); }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            ability.setHoldRightClick(true);
            ServerTickEvent.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
            helper.assertTrue(attempts[0] == 0, "Offhand does not reach the primary attack pipeline");
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, head);
            ServerTickEvent.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
            helper.assertTrue(attempts[0] == 1, "Main-hand primary attack still runs");
            var attack = ModAttackTypes.BRIMSTONE.get();
            var charge = (IChargeableAttack) attack;
            attack.handleChargeInput(player, true);
            attack.tickAttack(player);
            helper.assertTrue(ability.getChargeAmount(charge.getChargeBarId()) > 0, "Main-hand head allows charge");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
            player.setItemInHand(InteractionHand.OFF_HAND, head);
            attack.tickAttack(player);
            helper.assertTrue(ability.getChargeAmount(charge.getChargeBarId()) == 0, "Switching head to offhand clears charge");
            attack.handleChargeInput(player, false);
            helper.assertTrue(ability.getChargeAmount(charge.getChargeBarId()) == 0, "Release cannot fire an offhand charge");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        var interact = new PlayerInteractEvent.EntityInteract(player, InteractionHand.OFF_HAND, player);
        ForgeEvents.onRightClickEntity(interact);
        helper.assertTrue(!interact.isCanceled(), "Offhand head does not block entity interaction");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "right-click-test"));
        player.setPos(helper.absolutePos(BlockPos.ZERO).getCenter());
        player.setGameMode(GameType.SURVIVAL);
        DefaultAttributeManager.apply(player);
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(1);
        player.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
        CurioSlotManager.tick(player);
        return player;
    }

    private static IDynamicStackHandler slots(ServerPlayer player, String type) {
        return CuriosApi.getCuriosInventory(player).resolve().orElseThrow().getStacksHandler(type).orElseThrow().getStacks();
    }

    private static void tick(ServerPlayer player) {
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    private static InteractionResult use(ServerPlayer player, InteractionHand hand) {
        var event = new PlayerInteractEvent.RightClickItem(player, hand);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() ? event.getCancellationResult() : player.getItemInHand(hand).use(player.level(), player, hand).getResult();
    }

    private static void withBackpack(GameTestHelper helper, boolean enabled, Runnable action) {
        var server = helper.getLevel().getServer();
        var original = IsaacDatapackData.forServer(server);
        var json = new JsonObject();
        json.addProperty("extra_passive_item_backpack", enabled);
        var values = new HashMap<>(original.values());
        values.put(IsaacDatapackTypes.GAMEPLAY, GameplaySettings.parse(json));
        IsaacDatapackData.install(server, new IsaacDatapackTypes.Snapshot(values));
        try { action.run(); }
        finally { IsaacDatapackData.install(server, original); }
    }
}
