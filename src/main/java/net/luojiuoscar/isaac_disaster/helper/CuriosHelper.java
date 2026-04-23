package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.item.item.IIsaacCuriosItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



public class CuriosHelper {
    public static final String TRINKET = "isaac_trinket";
    public static final String PASSIVE_ITEM = "isaac_passive_item";

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
    }

    public static void addPermanentSlotModifier(LivingEntity entity, String slotId, UUID uuid, String name, double amount) {
        if (!(entity instanceof Player player)) return;

        CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(playerAbility -> {
                int current = playerAbility.getExtraTrinketSlotCounts();
                int newAmount = current + (int) amount;

                if (newAmount < 0 && Config.AUTO_ADAPT_CURIO_SLOT.get()) {
                    newAmount = 0;
                }

                // 在减少槽位前清理溢出的装备
                if (amount < 0) {
                    ICurioStacksHandler handler = inv.getCurios().get(slotId);
                    if (handler != null) {
                        int totalSlots = handler.getSlots();

                        ItemStack stack = handler.getStacks().getStackInSlot(totalSlots - 1);
                        if (stack.getItem() instanceof IIsaacCuriosItem) {
                            IIsaacCuriosItem.setOnCurios(stack, false);
                        }
                    }
                }

                inv.removeSlotModifier(slotId, uuid);
                inv.addPermanentSlotModifier(slotId, uuid, name, newAmount, AttributeModifier.Operation.ADDITION);
                playerAbility.setExtraTrinketSlotCounts(newAmount);
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
    }
}