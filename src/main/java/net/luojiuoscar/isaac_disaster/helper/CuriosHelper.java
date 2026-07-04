package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.CurioSlotKey;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.item.item.IIsaacCuriosItem;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CuriosHelper {
    public static final String TRINKET = "isaac_trinket";
    public static final String PASSIVE_ITEM = "isaac_passive_item";
    private static final String LEGACY_ON_CURIOS = "on_curios";

    public static final UUID ISAAC_TRINKET_SLOT_MODIFIER_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":" + TRINKET).getBytes());
    public static final UUID ISAAC_PASSIVE_ITEM_SLOT_MODIFIER_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":" + PASSIVE_ITEM).getBytes());

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

    public static void handleIsaacCurioEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;
        if (stack.isEmpty() || !(stack.getItem() instanceof IIsaacCuriosItem item)) return;

        CurioSlotKey key = CurioSlotKey.from(slotContext);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                playerIsaacItems -> equipSlot(playerIsaacItems, key, slotContext, prevStack, stack, item)
        );
    }

    public static void handleIsaacCurioUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;

        CurioSlotKey key = CurioSlotKey.from(slotContext);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(playerIsaacItems -> {
            Optional<ItemStack> record = playerIsaacItems.getActiveCurioStack(key);
            if (record.isEmpty()) return;

            if (!newStack.isEmpty() && isSameCurioItem(record.get(), newStack)) {
                clearLegacyOnCurios(newStack);
                playerIsaacItems.setActiveCurioStack(key, newStack);
                return;
            }

            ItemStack recordedStack = playerIsaacItems.removeActiveCurioStack(key).orElse(record.get());
            ItemStack stackForUnequip = prepareUnequipStack(recordedStack, stack);
            if (stackForUnequip.getItem() instanceof IIsaacCuriosItem item) {
                item.tryUnequip(slotContext, newStack, stackForUnequip);
            }
        });
    }

    public static void forgetIsaacCurioSlot(ServerPlayer player, CurioSlotKey key) {
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                playerIsaacItems -> playerIsaacItems.removeActiveCurioStack(key)
        );
    }

    public static void syncIsaacCurioSlot(ServerPlayer player, CurioSlotKey key) {
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(playerIsaacItems -> {
            ItemStack currentStack = getStackInSlot(player, key);
            Optional<ItemStack> record = playerIsaacItems.getActiveCurioStack(key);

            if (currentStack.isEmpty()) {
                record.ifPresent(stack -> {
                    playerIsaacItems.removeActiveCurioStack(key);
                    if (stack.getItem() instanceof IIsaacCuriosItem item) {
                        item.tryUnequip(createSlotContext(player, key), ItemStack.EMPTY, stack);
                    }
                });
                return;
            }

            if (!(currentStack.getItem() instanceof IIsaacCuriosItem currentItem)) {
                record.ifPresent(stack -> {
                    playerIsaacItems.removeActiveCurioStack(key);
                    if (stack.getItem() instanceof IIsaacCuriosItem item) {
                        item.tryUnequip(createSlotContext(player, key), currentStack, stack);
                    }
                });
                return;
            }

            SlotContext slotContext = createSlotContext(player, key);
            if (record.isEmpty()) {
                if (migrateLegacyCurioRecord(playerIsaacItems, key, currentStack)) {
                    return;
                }

                currentItem.tryEquip(slotContext, ItemStack.EMPTY, currentStack);
                playerIsaacItems.setActiveCurioStack(key, currentStack);
                return;
            }

            ItemStack recordedStack = record.get();
            if (isSameCurioItem(recordedStack, currentStack)) {
                clearLegacyOnCurios(currentStack);
                playerIsaacItems.setActiveCurioStack(key, currentStack);
                return;
            }

            playerIsaacItems.removeActiveCurioStack(key);
            if (recordedStack.getItem() instanceof IIsaacCuriosItem oldItem) {
                oldItem.tryUnequip(slotContext, currentStack, recordedStack);
            }

            currentItem.tryEquip(slotContext, recordedStack, currentStack);
            playerIsaacItems.setActiveCurioStack(key, currentStack);
        });
    }

    public static void syncAllIsaacCurios(ServerPlayer player) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isEmpty()) return;

        ICuriosItemHandler inv = optional.get();
        for (String slotType : List.of(TRINKET, PASSIVE_ITEM)) {
            ICurioStacksHandler handler = inv.getCurios().get(slotType);
            if (handler == null) continue;

            for (int i = 0; i < handler.getSlots(); i++) {
                syncIsaacCurioSlot(player, new CurioSlotKey(slotType, i));
            }
        }

        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(playerIsaacItems -> {
            for (Map.Entry<CurioSlotKey, ItemStack> entry : playerIsaacItems.getActiveCurioSlots().entrySet()) {
                CurioSlotKey key = entry.getKey();
                if ((TRINKET.equals(key.slotType()) || PASSIVE_ITEM.equals(key.slotType()))
                        && hasSlot(player, key)) continue;

                playerIsaacItems.removeActiveCurioStack(key).ifPresent(stack -> {
                    if (stack.getItem() instanceof IIsaacCuriosItem item) {
                        item.tryUnequip(createSlotContext(player, key), ItemStack.EMPTY, stack);
                    }
                });
            }
        });
    }

    private static void equipSlot(PlayerIsaacItems playerIsaacItems, CurioSlotKey key, SlotContext slotContext,
                                  ItemStack prevStack, ItemStack stack, IIsaacCuriosItem item) {
        Optional<ItemStack> record = playerIsaacItems.getActiveCurioStack(key);

        if (record.isEmpty()) {
            if (migrateLegacyCurioRecord(playerIsaacItems, key, stack)) {
                return;
            }

            item.tryEquip(slotContext, prevStack, stack);
            playerIsaacItems.setActiveCurioStack(key, stack);
            return;
        }

        ItemStack recordedStack = record.get();
        if (isSameCurioItem(recordedStack, stack)) {
            clearLegacyOnCurios(stack);
            playerIsaacItems.setActiveCurioStack(key, stack);
            return;
        }

        playerIsaacItems.removeActiveCurioStack(key);
        if (recordedStack.getItem() instanceof IIsaacCuriosItem oldItem) {
            oldItem.tryUnequip(slotContext, stack, recordedStack);
        }

        item.tryEquip(slotContext, recordedStack, stack);
        playerIsaacItems.setActiveCurioStack(key, stack);
    }

    private static ItemStack prepareUnequipStack(ItemStack recordedStack, ItemStack eventStack) {
        ItemStack stack;
        if (!eventStack.isEmpty() && recordedStack.getItem() == eventStack.getItem()) {
            stack = eventStack;
            stack.setTag(recordedStack.getTag() == null ? null : recordedStack.getTag().copy());
        } else {
            stack = recordedStack.copy();
        }

        if (isTrinketSwallowing(eventStack) && stack.getItem() instanceof Trinket) {
            Trinket.setSwallowing(stack, true);
        }

        clearLegacyOnCurios(stack);
        return stack;
    }

    private static boolean migrateLegacyCurioRecord(PlayerIsaacItems playerIsaacItems, CurioSlotKey key, ItemStack stack) {
        if (!hasLegacyOnCurios(stack)) return false;

        clearLegacyOnCurios(stack);
        playerIsaacItems.setActiveCurioStack(key, stack);
        return true;
    }

    private static boolean hasLegacyOnCurios(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().getBoolean(LEGACY_ON_CURIOS);
    }

    private static void clearLegacyOnCurios(ItemStack stack) {
        if (!stack.hasTag()) return;
        stack.getOrCreateTag().remove(LEGACY_ON_CURIOS);
    }

    private static boolean isTrinketSwallowing(ItemStack stack) {
        return stack.getItem() instanceof Trinket && Trinket.isSwallowing(stack);
    }

    private static boolean isSameCurioItem(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) return false;

        ItemStack leftCopy = left.copy();
        ItemStack rightCopy = right.copy();
        clearLegacyOnCurios(leftCopy);
        clearLegacyOnCurios(rightCopy);

        return ItemStack.matches(leftCopy, rightCopy);
    }

    private static ItemStack getStackInSlot(Player player, CurioSlotKey key) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isEmpty()) return ItemStack.EMPTY;

        Optional<ICurioStacksHandler> stacksHandler = optional.get().getStacksHandler(key.slotType());
        if (stacksHandler.isEmpty()) return ItemStack.EMPTY;

        ICurioStacksHandler handler = stacksHandler.get();
        if (key.index() < 0 || key.index() >= handler.getSlots()) return ItemStack.EMPTY;

        return handler.getStacks().getStackInSlot(key.index());
    }

    private static boolean hasSlot(Player player, CurioSlotKey key) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isEmpty()) return false;

        Optional<ICurioStacksHandler> stacksHandler = optional.get().getStacksHandler(key.slotType());
        return stacksHandler
                .map(handler -> key.index() >= 0 && key.index() < handler.getSlots())
                .orElse(false);
    }

    private static SlotContext createSlotContext(ServerPlayer player, CurioSlotKey key) {
        return new SlotContext(key.slotType(), player, key.index(), false, true);
    }

    /**
     * 动态添加临时槽位修饰符（重登后消失）
     *
     * @param entity 玩家或生物
     * @param slotId 槽位类型
     * @param uuid   唯一标识符
     * @param name   修饰符名称，仅用于识别
     * @param amount 增加的槽位数量
     */
    public static void addTransientSlotModifier(LivingEntity entity, String slotId, UUID uuid, String name, double amount) {
        CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
            inv.addTransientSlotModifier(slotId, uuid, name, amount, AttributeModifier.Operation.ADDITION);
        });
    }

    /**
     * 动态添加永久槽位修饰符（会写入玩家存档）
     *
     * @param entity 玩家或生物
     * @param slotId 槽位类型
     * @param uuid   唯一标识符，用于后续移除
     * @param name   修饰符名称，仅用于识别
     * @param amount 增加的槽位数量
     */
    public static void setPermanentSlotModifier(LivingEntity entity, String slotId, UUID uuid, String name, double amount) {
        CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
            inv.removeSlotModifier(slotId, uuid);
            inv.addPermanentSlotModifier(slotId, uuid, name, amount, AttributeModifier.Operation.ADDITION);
        });
        if (!(entity instanceof Player player)) return;
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setExtraTrinketSlotCounts((int) amount)
        );
        if (player instanceof ServerPlayer serverPlayer) {
            syncAllIsaacCurios(serverPlayer);
        }
    }

    public static void addPermanentSlotModifier(LivingEntity entity, String slotId, UUID uuid, String name, double amount) {
        if (!(entity instanceof ServerPlayer player)) return;

        CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(playerAbility -> {
                int current = playerAbility.getExtraTrinketSlotCounts();
                int newAmount = current + (int) amount;

                if (newAmount < 0 && Config.AUTO_ADAPT_CURIO_SLOT.get()) {
                    newAmount = 0;
                }

                inv.removeSlotModifier(slotId, uuid);
                inv.addPermanentSlotModifier(slotId, uuid, name, newAmount, AttributeModifier.Operation.ADDITION);
                playerAbility.setExtraTrinketSlotCounts(newAmount);
                syncAllIsaacCurios(player);
            });
        });
    }

    /**
     * 移除指定槽位的修饰符
     *
     * @param entity 玩家或生物
     * @param slotId 槽位类型
     * @param uuid   UUID
     */
    public static void removeSlotModifier(LivingEntity entity, String slotId, UUID uuid) {
        CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
            inv.removeSlotModifier(slotId, uuid);
        });
        if (!(entity instanceof Player player)) return;
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setExtraTrinketSlotCounts(0)
        );
        if (player instanceof ServerPlayer serverPlayer) {
            syncAllIsaacCurios(serverPlayer);
        }
    }
}
