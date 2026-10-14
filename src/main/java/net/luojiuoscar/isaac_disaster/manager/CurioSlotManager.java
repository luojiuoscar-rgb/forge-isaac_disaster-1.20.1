package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.CurioSlotKey;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.item.item.IIsaacCuriosItem;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/** Coordinates Curios equipment effects independently of attribute-driven capacity changes. */
public final class CurioSlotManager {
    public static final String TRINKET = "isaac_trinket";
    public static final String PASSIVE_ITEM = "isaac_passive_item";
    private static final List<String> SLOT_TYPES = List.of(TRINKET, PASSIVE_ITEM);
    private static final Map<ServerPlayer, State> STATES = new WeakHashMap<>();

    private CurioSlotManager() { }

    private static final class State {
        private final Set<CurioSlotKey> processing = new HashSet<>();
        private boolean updating;
        private boolean reconcileRequested = true;
    }

    /**
     * 获取玩家在指定 Curios 槽位中装备的所有物品
     *
     * @param player   玩家
     * @param slotType 槽位类型（如 "ring"、"charm"、"isaac_trinket" 等）
     * @return 非空 ItemStack 列表
     */
    public static List<ItemStack> getEquippedItemsInSlot(Player player, String slotType) {
        List<ItemStack> equipped = new ArrayList<>();
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isEmpty()) return equipped;

        ICuriosItemHandler handler = optional.get();

        // 获取指定槽类型的 handler
        handler.getStacksHandler(slotType).ifPresent(stacksHandler -> {
            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    equipped.add(stack);
                }
            }
        });

        return equipped;
    }

    public static Map<CurioSlotKey, ItemStack> getEquippedItemsBySlot(Player player, String slotType) {
        Map<CurioSlotKey, ItemStack> equipped = new HashMap<>();
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isEmpty()) return equipped;

        ICuriosItemHandler handler = optional.get();

        handler.getStacksHandler(slotType).ifPresent(stacksHandler -> {
            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    equipped.put(new CurioSlotKey(slotType, i), stack);
                }
            }
        });

        return equipped;
    }


    public static int capacity(double value) {
        return (int) Math.max(0, Math.min(1024, Math.floor(value)));
    }

    public static void requestReconcile(ServerPlayer player) {
        STATES.computeIfAbsent(player, ignored -> new State()).reconcileRequested = true;
    }

    public static void onEquip(SlotContext context, ItemStack previous, ItemStack stack) {
        if (!(context.entity() instanceof ServerPlayer player) || !SLOT_TYPES.contains(context.identifier())) return;
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(items -> {
            CurioSlotKey key = CurioSlotKey.from(context);
            State state = STATES.computeIfAbsent(player, ignored -> new State());
            if (!state.processing.add(key)) {
                state.reconcileRequested = true;
                return;
            }
            try {
                ItemStack recorded = items.getActiveCurioStack(key).orElse(ItemStack.EMPTY);
                if (!recorded.isEmpty() && ItemStack.isSameItemSameTags(recorded, stack)) {
                    items.setActiveCurioStack(key, stack);
                    return;
                }
                items.removeActiveCurioStack(key);
                if (recorded.getItem() instanceof IIsaacCuriosItem oldItem) {
                    oldItem.tryUnequip(context, stack, recorded);
                }
                if (!stack.isEmpty() && stack.getItem() instanceof IIsaacCuriosItem item) {
                    item.tryEquip(context, previous, stack);
                    items.setActiveCurioStack(key, stack);
                }
                state.reconcileRequested = true;
                syncCount(player, items, recorded);
                syncCount(player, items, stack);
            } finally {
                state.processing.remove(key);
            }
        });
    }

    public static void onUnequip(SlotContext context, ItemStack replacement, ItemStack stack) {
        if (!(context.entity() instanceof ServerPlayer player) || !SLOT_TYPES.contains(context.identifier())) return;
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(items -> {
            CurioSlotKey key = CurioSlotKey.from(context);
            State state = STATES.computeIfAbsent(player, ignored -> new State());
            if (!state.processing.add(key)) {
                state.reconcileRequested = true;
                return;
            }
            try {
                ItemStack recorded = items.getActiveCurioStack(key).orElse(ItemStack.EMPTY);
                if (recorded.isEmpty()) return;
                if (!replacement.isEmpty() && ItemStack.isSameItemSameTags(recorded, replacement)) {
                    items.setActiveCurioStack(key, replacement);
                    return;
                }
                items.removeActiveCurioStack(key);
                // Use the physical stack when available, so swallowing and final item state stay intact.
                ItemStack removed = !stack.isEmpty() && stack.getItem() == recorded.getItem() ? stack : recorded;
                if (removed.getItem() instanceof IIsaacCuriosItem item) {
                    item.tryUnequip(context, replacement, removed);
                }
                state.reconcileRequested = true;
                syncCount(player, items, recorded);
            } finally {
                state.processing.remove(key);
            }
        });
    }

    /** Called once at the end of a server player tick; cascades are deferred to the next tick. */
    public static void tick(ServerPlayer player) {
        State state = STATES.computeIfAbsent(player, ignored -> new State());
        if (state.updating || !state.processing.isEmpty()) {
            state.reconcileRequested = true;
            return;
        }
        var inventory = CuriosApi.getCuriosInventory(player).resolve();
        var items = player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve();
        if (inventory.isEmpty() || items.isEmpty()) return;
        int[] capacities = {
                capacity(player.getAttributeValue(ModAttributes.TRINKET_SLOTS.get())),
                capacity(player.getAttributeValue(ModAttributes.PASSIVE_ITEM_SLOTS.get()))
        };
        state.updating = true;
        boolean reconcile = state.reconcileRequested || player.tickCount % 200 == 0;
        state.reconcileRequested = false;
        try {
            Set<ICurioStacksHandler> changed = new HashSet<>();
            // Finish replacing all modifiers before any read can trigger a resize.
            for (int i = 0; i < SLOT_TYPES.size(); i++) {
                var handler = inventory.get().getCurios().get(SLOT_TYPES.get(i));
                if (handler != null && installCapacity(handler, capacities[i])) changed.add(handler);
            }
            for (var handler : changed) handler.update();
            if (reconcile || !changed.isEmpty()) {
                reconcileEquipment(player, items.get());
                items.get().refreshItemCountCache();
            }
            inventory.get().handleInvalidStacks();
            if (!changed.isEmpty()) {
                // Curios' normal tick sends modifiers inside its slot loop, which is skipped at zero capacity.
                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new SPacketSyncModifiers(player.getId(), changed));
                inventory.get().getUpdatingInventories().removeAll(changed);
            }
        } finally {
            state.updating = false;
        }
    }

    private static boolean installCapacity(ICurioStacksHandler handler, int count) {
        UUID uuid = UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":capacity/" + handler.getIdentifier())
                .getBytes(StandardCharsets.UTF_8));
        AttributeModifier existing = handler.getModifiers().get(uuid);
        // Deserialized transient modifiers are cached by Curios. They must not be cleared on its first tick.
        if (handler.getModifiers().size() == 1 && existing != null && existing.getAmount() == count
                && existing.getOperation() == AttributeModifier.Operation.ADDITION
                && handler.getPermanentModifiers().isEmpty()) {
            handler.getCachedModifiers().remove(existing);
            return false;
        }
        handler.clearCachedModifiers();
        handler.clearModifiers();
        handler.addTransientModifier(new AttributeModifier(uuid, "isaac_capacity", count,
                AttributeModifier.Operation.ADDITION));
        return true;
    }

    private static void reconcileEquipment(ServerPlayer player, PlayerIsaacItems items) {
        Set<CurioSlotKey> present = new HashSet<>();
        for (String type : SLOT_TYPES) {
            for (var entry : getEquippedItemsBySlot(player, type).entrySet()) {
                CurioSlotKey key = entry.getKey();
                present.add(key);
                onEquip(context(player, key), items.getActiveCurioStack(key).orElse(ItemStack.EMPTY), entry.getValue());
            }
        }
        for (var entry : items.getActiveCurioSlots().entrySet()) {
            if (SLOT_TYPES.contains(entry.getKey().slotType()) && !present.contains(entry.getKey())) {
                onUnequip(context(player, entry.getKey()), ItemStack.EMPTY, entry.getValue());
            }
        }
    }

    /** Transfers the actual Curios stacks into swallowed storage without removing or reapplying their effects. */
    public static int swallowTrinkets(ServerPlayer player) {
        var inventory = CuriosApi.getCuriosInventory(player).resolve();
        var items = player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve();
        if (inventory.isEmpty() || items.isEmpty()) return 0;
        var handler = inventory.get().getCurios().get(TRINKET);
        if (handler == null) return 0;
        State state = STATES.computeIfAbsent(player, ignored -> new State());
        if (state.updating || !state.processing.isEmpty()) {
            state.reconcileRequested = true;
            return 0;
        }
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStacks().getStackInSlot(i);
            if (!(stack.getItem() instanceof Trinket)) continue;
            CurioSlotKey key = new CurioSlotKey(TRINKET, i);
            onEquip(context(player, key), ItemStack.EMPTY, stack);
            ItemStack swallowed = stack.copy();
            items.get().removeActiveCurioStack(key);
            items.get().swallow(stack);
            handler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
            syncCount(player, items.get(), swallowed);
            count++;
        }
        state.reconcileRequested = true;
        return count;
    }

    private static SlotContext context(ServerPlayer player, CurioSlotKey key) {
        return new SlotContext(key.slotType(), player, key.index(), false, true);
    }

    private static void syncCount(ServerPlayer player, PlayerIsaacItems items, ItemStack stack) {
        if (!stack.isEmpty()) items.syncCountToClient(player, stack);
    }
}
