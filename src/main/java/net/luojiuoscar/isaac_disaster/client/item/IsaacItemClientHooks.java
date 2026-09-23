package net.luojiuoscar.isaac_disaster.client.item;

import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Client-only tooltip assembly for the common Isaac item base class. */
public final class IsaacItemClientHooks {
    private IsaacItemClientHooks() {
    }

    public static void appendHoverText(IsaacItem item, ItemStack stack, List<Component> tooltipComponents) {
        Player player = Minecraft.getInstance().player;
        List<Component> extraDesc = item.getAbility().getExtraDesc(stack, player);

        if (!extraDesc.isEmpty() && Screen.hasShiftDown()) {
            tooltipComponents.addAll(extraDesc);
            return;
        }

        tooltipComponents.addAll(item.getAbility().getDesc(stack, player));
        tooltipComponents.addAll(item.getAbility().getSynergyDesc(stack, player));
        tooltipComponents.add(Component.literal(""));
        item.addAdditionalInfo(tooltipComponents, stack);
        if (!extraDesc.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.isaac_disaster.special.require_shift"));
        }
        if (IsaacItem.hasBeenUsed(stack)) {
            tooltipComponents.add(Component.translatable("item.isaac_disaster.action.consumed")
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        item.addRarityComponent(tooltipComponents);
    }
}
