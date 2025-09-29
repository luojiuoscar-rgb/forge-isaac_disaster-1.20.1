package net.luojiuoscar.isaac_disaster.item.custom;

import net.luojiuoscar.isaac_disaster.manager.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.PassiveItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 添加Lore的基类：
 * 仅在需要时将lore文本列表自动实现appendHoverText方法
 */
public abstract class IsaacItems extends Item {
    private final int itemId;
    private final int itemLevel;
    private final boolean useOriginalColor;


    /**
     * @param properties 物品属性
     * @param itemLevel 物品等级0-4
     * @param itemId 物品id（在ItemIdManager中获取）
     */
    public IsaacItems(Properties properties, int itemLevel, int itemId) {
        this(properties, itemLevel, itemId, false);

    }


    /**
     * @param properties 物品属性
     * @param itemLevel 物品等级0-4
     * @param itemId 物品id（在ItemIdManager中获取）
     * @param useOriginalColor 默认为false，是否不自动替换为物品等级对应的颜色
     */
    public IsaacItems(Properties properties, int itemLevel, int itemId, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
        this.itemId = itemId;
        if (itemLevel < 0 || itemLevel > 4) {
            throw new IllegalArgumentException("itemLevel must be between 0 and 4");
        }
        this.itemLevel = itemLevel;
        this.useOriginalColor = useOriginalColor;
    }

    /**
     * 重写hover文本方法
     * 在ItemManager中获取文本
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        // 从ItemManager中获取到的物品描述文本添加到tooltip
        List<Component> description = null;
        if (this instanceof NormalPassiveItem){
            description = PassiveItemManager.getInstance().getItemFromId(itemId).getDescription();
        }else if (this instanceof NormalActiveItem){
            description = ActiveItemManager.getInstance().getItemFromId(itemId).getDescription();
        }
        tooltipComponents.addAll(description);

        // 空行分隔
        tooltipComponents.add(Component.literal(""));
        switch (itemLevel){
            case 1: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.uncommon")
                    .withStyle(style -> style.withColor(0x55FF55).withBold(true))); break;
            case 2: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.rare")
                    .withStyle(style -> style.withColor(0x00FFFF).withBold(true))); break;
            case 3: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.epic")
                    .withStyle(style -> style.withColor(0xFC54FC).withBold(true))); break;
            case 4: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.legendary")
                    .withStyle(style -> style.withColor(0xFF3333).withBold(true))); break;
            default: tooltipComponents.add(Component.translatable("rarity.isaac_disaster.common")
                    .withStyle(style -> style.withColor(0xFFFFFF).withBold(true))); break;
        }
    }

    /**
     * 重写该方法使所有自定义物品名称的显示颜色根据等级变化
     * 此Item的名称将被ItemStack.getHoverName()调用
     */
    @Override
    public Component getName(ItemStack pStack) {
        // 如果需要使用原始文本颜色
        if(this.useOriginalColor){
            return Component.translatable(this.getDescriptionId(pStack));
        }

        // 根据等级设置对应颜色（与上面的方法保持一致）
        int color = switch (this.itemLevel) {
            case 1 -> 0x55FF55;   // 绿色
            case 2 -> 0x00FFFF;   // 青色
            case 3 -> 0xFC54FC;   // 粉色
            case 4 -> 0xFF3333;   // 红色
            default -> 0xFFFFFF;  // 白色
        };
        return Component.translatable(this.getDescriptionId(pStack)).withStyle(
                style -> style.withColor(color));
    }

    public int getItemId(){
        return this.itemId;
    }
}
