package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CuriosHelper {
    public static final String TRINKET = "isaac_trinket";


    /**
     * 获取玩家在指定 Curios 槽位中装备的所有物品
     * @param player 玩家
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
}
