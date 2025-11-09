package net.luojiuoscar.isaac_disaster.item.custom;

import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ChestPlaceholder extends IsaacChestBlockItem {
    public ChestPlaceholder(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".lore.1"));
    }
}
