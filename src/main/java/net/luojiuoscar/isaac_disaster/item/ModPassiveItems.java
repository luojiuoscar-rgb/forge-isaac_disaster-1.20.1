package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.manager.ItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 简化Lore添加的基类：
 * 只需在构造时传入lore文本列表，自动实现appendHoverText逻辑
 */
public class ModPassiveItems extends Item {
    // 统一存储所有被动道具
    public static final List<RegistryObject<Item>> PASSIVE_ITEM_LIST = new ArrayList<>();


    // 存储Lore文本列表的成员变量
    private final int itemId;
    private final int itemLevel;
    private final boolean useOriginalColor;


    /**
     * @param properties properties
     * @param itemLevel 物品等级0-4
     * @param itemId 物品id 从ItemIdManager中获取
     */
    public ModPassiveItems (Properties properties, int itemLevel, int itemId) {
        this(properties, itemLevel, itemId, false);

    }


    /**
     * @param properties properties
     * @param itemLevel 物品等级0-4
     * @param itemId 物品id 从ItemIdManager中获取
     * @param useOriginalColor 默认为false 会自动将名称替换为物品等级对应的名称
     */
    public ModPassiveItems(Properties properties, int itemLevel, int itemId, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
        this.itemId = itemId;
        this.itemLevel = itemLevel;
        this.useOriginalColor = useOriginalColor;
    }

    /**
     * 重写hover文本方法
     * 从ItemManager中获取文本
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        // 从 ItemManager 中获取该道具的描述文本并添加到tooltip
        List<Component> description = ItemManager.getInstance().getItemFromId(itemId).getDescription();
        tooltipComponents.addAll(description);

        // empty line
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
     * 重写此方法以全局控制物品名称的显示（包括所有场景）
     * 这是Item类的核心方法，会被ItemStack.getHoverName()调用
     */
    @Override
    public Component getName(ItemStack pStack) {
        // 根据等级计算颜色（和之前的逻辑保持一致）
        int color = switch (this.itemLevel) {
            case 1 -> 0x55FF55;   // 绿色
            case 2 -> 0x00FFFF;   // 青色
            case 3 -> 0xFC54FC;   // 亮紫色
            case 4 -> 0xFF3333;   // 亮红色
            default -> 0xFFFFFF;  // 白色
        };
        return Component.translatable(this.getDescriptionId(pStack)).withStyle(
                style -> style.withColor(color));
    }



    /**
     * 右键使用方法
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        // 获取玩家手中的物品栈
        ItemStack stack = player.getItemInHand(hand);

        // 触发效果
        ItemManager.getInstance().getItemFromId(itemId).onObtain(player);

        // 返回成功结果
        return InteractionResultHolder.pass(stack);
    }

    public int getItemId(){
        return this.itemId;
    }

    public int getItemLevel(){
        return this.itemLevel;
    }

    public boolean isUseOriginalColor(){
        return this.useOriginalColor;
    }

    
}
