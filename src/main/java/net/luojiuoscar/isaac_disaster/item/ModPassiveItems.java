package net.luojiuoscar.isaac_disaster.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 简化Lore添加的基类：
 * 只需在构造时传入lore文本列表，自动实现appendHoverText逻辑
 */
public class ModPassiveItems extends Item {

    // 存储Lore文本列表的成员变量
    private final List<Component> loreList;
    private final int itemId;

    /**
     * 构造方法：直接接收lore文本列表
     * @param properties 物品属性
     * @param loreList 要显示的lore文本
     */
    public ModPassiveItems(Properties properties, List<Component> loreList, int itemId) {
        super(properties);
        this.loreList = loreList;
        this.itemId = itemId;
    }

    /**
     * 重写hover文本方法，自动添加lore
     */
    @Override
    public void appendHoverText(ItemStack stack,
                                @Nullable Level world,
                                List<Component> tooltipComponents,
                                TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltipComponents, flag);
        // 将存储的lore文本添加到物品提示中
        tooltipComponents.addAll(loreList);
    }

    public int getItemId(){
        return this.itemId;
    }

    
}
