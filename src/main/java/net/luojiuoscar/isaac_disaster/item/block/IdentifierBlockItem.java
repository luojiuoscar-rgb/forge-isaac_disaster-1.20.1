package net.luojiuoscar.isaac_disaster.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class IdentifierBlockItem extends BlockItem {
    public IdentifierBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties.rarity(Rarity.RARE));
    }

    @Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".lore.1"));
        tooltipComponents.add(Component.translatable("block.isaac_disaster.identifier.lore.1"));
        tooltipComponents.add(Component.translatable("block.isaac_disaster.identifier.lore.2"));
    }
}
