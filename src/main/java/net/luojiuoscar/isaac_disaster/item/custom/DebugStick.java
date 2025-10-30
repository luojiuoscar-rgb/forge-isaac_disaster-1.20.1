package net.luojiuoscar.isaac_disaster.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DebugStick extends Item {
    public DebugStick(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.EPIC).setNoRepair().fireResistant());
    }

    public static boolean hasStoredPos(ItemStack stack) {
        if (!stack.hasTag()) return false;
        return stack.getTag().contains("StoredX")
                && stack.getTag().contains("StoredY")
                && stack.getTag().contains("StoredZ");
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isShiftKeyDown()) {
            clearTag(stack);
            player.displayClientMessage(Component.translatable("message.isaac_disaster.debug_stick.clear"), true);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return stack.getTag() != null;
    }


    public static void clearTag(ItemStack stack) {
        stack.setTag(null);
    }

    // ===== 坐标存储逻辑 =====
    public static void saveBlockPos(ItemStack stack, BlockPos pos) {
        stack.getOrCreateTag().putInt("StoredX", pos.getX());
        stack.getOrCreateTag().putInt("StoredY", pos.getY());
        stack.getOrCreateTag().putInt("StoredZ", pos.getZ());
    }

    public static BlockPos loadBlockPos(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null) {
            int x = stack.getTag().getInt("StoredX");
            int y = stack.getTag().getInt("StoredY");
            int z = stack.getTag().getInt("StoredZ");
            return new BlockPos(x, y, z);
        }
        return null;
    }

}
