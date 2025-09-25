package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.manager.ItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 简化Lore添加的基类：
 * 只需在构造时传入lore文本列表，自动实现appendHoverText逻辑
 */
public class ModPassiveItems extends Item {

    // 存储Lore文本列表的成员变量
    private final int itemId;

    /**
     * 构造方法：直接接收lore文本列表
     * @param properties 物品属性
     */
    public ModPassiveItems(Properties properties, int itemId) {
        super(properties);
        this.itemId = itemId;
    }

    /**
     * 重写hover文本方法
     * 从ItemManager中获取文本
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        // 从 ItemManager 中获取该道具的描述文本并添加到tooltip
        List<Component> Description = ItemManager.getInstance().getItemFromId(itemId).getDescription();
        if (Description != null) {
            tooltipComponents.addAll(Description);
        }
    }

    /**
     * 重写右键使用方法
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        // 获取玩家手中的物品栈
        ItemStack stack = player.getItemInHand(hand);

        // 删除物品
        stack.shrink(1);

        // 触发效果
        ItemManager.getInstance().getItemFromId(itemId).onObtain(player);

        // 返回成功结果
        return InteractionResultHolder.pass(stack);
    }

    public int getItemId(){
        return this.itemId;
    }

    
}
