package net.luojiuoscar.isaac_disaster.client.item;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Client-only tooltip assembly for trinkets. */
public final class TrinketClientHooks {
    private TrinketClientHooks() {
    }

    public static void appendHoverText(Trinket item, ItemStack stack, List<Component> tooltipComponents) {
        Player player = Minecraft.getInstance().player;
        var ability = item.getAbility();
        List<Component> extraDesc = ability.getExtraDesc(stack, player);

        if (!extraDesc.isEmpty() && Screen.hasShiftDown()) {
            tooltipComponents.addAll(extraDesc);
            return;
        }

        tooltipComponents.addAll(ability.getDesc(stack, player));
        tooltipComponents.addAll(ability.getSynergyDesc(stack, player));
        if (Trinket.isConsumed(stack)) {
            tooltipComponents.add(Component.translatable("item.isaac_disaster.action.consumed")
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        if (!extraDesc.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.isaac_disaster.special.require_shift"));
        }
    }
}
