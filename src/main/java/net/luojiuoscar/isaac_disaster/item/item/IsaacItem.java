package net.luojiuoscar.isaac_disaster.item.item;


import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 添加Lore的基类：
 * 仅在需要时将lore文本列表自动实现appendHoverText方法
 */
public abstract class IsaacItem extends Item {
    private final int itemId;
    private final int itemLevel;
    private final boolean useOriginalColor;
    private final boolean hasSpecialEffects;

    /**
     * @param properties 物品属性
     * @param itemLevel 物品等级0-4
     * @param itemId 物品id（在ItemIdManager中获取）
     * @param useOriginalColor 默认为false，是否不自动替换为物品等级对应的颜色
     */
    public IsaacItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffects, boolean useOriginalColor) {
        super(properties);
        this.itemId = itemId;
        if (itemLevel < 0 || itemLevel > 4) {
            throw new IllegalArgumentException("itemLevel must be between 0 and 4");
        }
        this.itemLevel = itemLevel;
        this.useOriginalColor = useOriginalColor;
        this.hasSpecialEffects = hasSpecialEffects;
    }

    /**
     * 重写hover文本方法
     * 在ItemManager中获取文本
     */
    @Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (hasSpecialEffects && Screen.hasShiftDown()){
            // 添加解释性文本组件
            addExplainInfo(tooltipComponents);
        }else{
            // 添加描述性文本组件
            addDescription(tooltipComponents, stack);
            addSynergyDescription(tooltipComponents);
            // 添加额外信息
            addAdditionalInfo(tooltipComponents, stack);
            // 添加shift提示
            if (hasSpecialEffects){
                tooltipComponents.add(Component.translatable("item.isaac_disaster.special.require_shift"));
            }
            // 添加稀有度文本组件
            addRarityComponent(tooltipComponents);
        }
    }
    /** 添加描述性文本组件 */
    public abstract void addDescription(List<Component> tooltipComponents, ItemStack stack);

    public abstract void addSynergyDescription(List<Component> tooltipComponents);

    /** 额外信息 */
    public abstract void addAdditionalInfo(List<Component> tooltipComponents, @Nullable ItemStack stack);
    
    /** 解释性信息 用于解释专有名词 */
    public abstract void addExplainInfo(List<Component> tooltipComponents);

    /** 获取Rarity */
    public static Rarity getRarity(int itemLevel){
        return switch (itemLevel) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    /** 添加稀有度文本组件 */
    public void addRarityComponent(List<Component> tooltipComponents){

        String itemIdMarker = "-" + itemId;
        switch (itemLevel){
            case 1: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.uncommon").append(itemIdMarker)
                    .withStyle(style -> style.withColor(ColorManager.UNCOMMON_GREEN).withBold(true))); break;
            case 2: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.rare").append(itemIdMarker)
                    .withStyle(style -> style.withColor(ColorManager.RARE_BLUE).withBold(true))); break;
            case 3: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.epic").append(itemIdMarker)
                    .withStyle(style -> style.withColor(ColorManager.EPIC_PURPLE).withBold(true))); break;
            case 4: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.legendary").append(itemIdMarker)
                    .withStyle(style -> style.withColor(ColorManager.LEGEND_RED).withBold(true))); break;
            default: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.common").append(itemIdMarker)
                    .withStyle(style -> style.withColor(ColorManager.COMMON_WHITE).withBold(true))); break;
        }
    }




    /**
     * 重写该方法使所有自定义物品名称的显示颜色根据等级变化
     * 此Item的名称将被ItemStack.getHoverName()调用
     */
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        // 如果需要使用原始文本颜色
        if(this.useOriginalColor){
            return Component.translatable(this.getDescriptionId(stack));
        }

        // 根据等级设置对应颜色（与上面的方法保持一致）
        int color = switch (this.itemLevel) {
            case 1 -> ColorManager.UNCOMMON_GREEN;
            case 2 -> ColorManager.RARE_BLUE;
            case 3 -> ColorManager.EPIC_PURPLE;
            case 4 -> ColorManager.LEGEND_RED;
            default -> ColorManager.COMMON_WHITE;
        };
        return Component.translatable(this.getDescriptionId(stack)).withStyle(
                style -> style.withColor(color));}

    public int getItemId(){
        return this.itemId;
    }

    public int getItemLevel() {return this.itemLevel; }

}
