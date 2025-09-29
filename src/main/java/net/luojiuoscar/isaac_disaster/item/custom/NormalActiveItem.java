package net.luojiuoscar.isaac_disaster.item.custom;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemC2SPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class NormalActiveItem extends IsaacItems {
    private int maxItemUseCount;
    private int itemCD;

    public NormalActiveItem(Properties properties, int itemLevel, int itemId, int itemCD) {
        this(properties, itemLevel, itemId, itemCD, 1, false);
    }

    public NormalActiveItem(Properties properties, int itemLevel, int itemId, int itemCD, int maxItemUseCount) {
        this(properties, itemLevel, itemId, itemCD, maxItemUseCount, false);
    }

    public NormalActiveItem(Properties properties, int itemLevel, int itemId, int itemCD, int maxItemUseCount, boolean useOriginalColor) {
        super(properties, itemLevel, itemId, useOriginalColor);
        this.maxItemUseCount = maxItemUseCount;
        this.itemCD = itemCD;
    }



    /**
     * 右键使用效果
     */
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getMainHandItem();

        int currentUseCount = getCurrentItemUseCount(stack);
        int maxUseCount = getMaxItemUseCount(stack);
        int currentCD = getCurrentItemCD(stack);
        player.sendSystemMessage(Component.literal("测试信息0: " + currentUseCount +"  "+ maxUseCount +"  "+ currentCD));


        if (currentUseCount < maxUseCount) {
            ModMessages.sendToServer(new UseActiveItemC2SPacket(this.getItemId()));
        }

        return InteractionResultHolder.pass(stack);
    }


    /**
     * 获取Item的当前CD
     */
    public int getCurrentItemCD(ItemStack stack){
        return stack.getOrCreateTag().getInt("active_item_current_cd"); // 默认返回0
    }
    /**
     * 修改NBT中的当前itemCD
     */
    public void modifyCurrentItemCD(ItemStack stack, int Amount) {
        int newValue = Math.max(0, getCurrentItemCD(stack) + Amount); // 最小为0
        stack.getOrCreateTag().putInt("active_item_current_cd", newValue);
    }


    /**
     * 获取当前Item的CD
     */
    public int getItemCD(ItemStack stack){
        // 如果NBT中没有值则自动初始化为itemCD并写入NBT
        if (!stack.hasTag() || !stack.getTag().contains("active_item_cd")) {
            stack.getOrCreateTag().putInt("active_item_cd", itemCD);
        }
        return stack.getTag().getInt("active_item_cd");
    }
    /**
     * 修改NBT中的itemCD
     */
    public void setItemCD(ItemStack stack, int Amount) {
        int newValue = Math.max(0, Amount); // 最小为0
        stack.getOrCreateTag().putInt("active_item_cd", newValue);
    }
    /**
     * 重新开始新的倒计时
     */
    public void resetCD(ItemStack stack) {
        NormalActiveItem item = (NormalActiveItem) stack.getItem();
        stack.getOrCreateTag().putInt("active_item_current_cd", item.getItemCD(stack));
    }




    /**
     * 获取最大使用次数
     */
    public int getMaxItemUseCount(ItemStack stack){
        // 如果NBT中没有值则自动初始化为maxItemUseCount并写入NBT
        if (!stack.hasTag() || !stack.getTag().contains("active_item_max_use_count")) {
            stack.getOrCreateTag().putInt("active_item_max_use_count", maxItemUseCount);
        }
        return stack.getTag().getInt("active_item_max_use_count");
    }
    /**
     * 修改NBT中的最大使用次数
     */
    public void modifyMaxItemUseCount(ItemStack stack, int modifyAmount) {
        int current = getMaxItemUseCount(stack);
        current += modifyAmount;
        current = Math.max(1, current); // 最小为1
        stack.getOrCreateTag().putInt("active_item_max_use_count", current);
    }

    /**
     * 从NBT获取当前使用次数
     */
    public int getCurrentItemUseCount(ItemStack stack) {
        // 如果NBT中没有值则自动初始化为maxItemUseCount并写入NBT
        if (!stack.hasTag() || !stack.getTag().contains("active_item_current_use_count")) {
            stack.getOrCreateTag().putInt("active_item_current_use_count", 0);
        }
        return stack.getTag().getInt("active_item_current_use_count");
    }

    /**
     * 修改NBT中的当前使用次数
     * @return 是否达到最大使用次数
     */
    public boolean modifyCurrentItemUseCount(ItemStack stack, int modifyAmount) {
        int current = getCurrentItemUseCount(stack);
        current += modifyAmount;
        current = Math.max(0, current); // 最小为0
        stack.getOrCreateTag().putInt("active_item_current_use_count", current);

        // 检查是否达到最大使用次数
        NormalActiveItem item = (NormalActiveItem) stack.getItem();
        if (current >= item.getMaxItemUseCount(stack)){
            return true;
        }
        return false;
    }

    /**
     * 清空当前使用次数并设置为0
     */
    public void clearCurrentItemUseCount(ItemStack stack) {
        stack.getOrCreateTag().putInt("active_item_current_use_count", 0);
    }
}
