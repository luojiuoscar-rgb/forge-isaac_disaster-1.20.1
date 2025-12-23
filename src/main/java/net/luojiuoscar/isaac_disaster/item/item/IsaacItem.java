package net.luojiuoscar.isaac_disaster.item.item;


import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 添加Lore的基类：
 * 仅在需要时将lore文本列表自动实现appendHoverText方法
 */
public abstract class IsaacItem extends Item {
    protected final RegistryObject<? extends IsaacItemAbility> ability;
    private static final String HAS_BEEN_USED = "has_been_used";

    protected static final String ITEM_POOL = "pool";

    public IsaacItem(Properties properties, RegistryObject<? extends IsaacItemAbility> ability) {
        super(properties);
        this.ability = ability;
    }

    /**
     * 重写hover文本方法
     * 在ItemManager中获取文本
     */
    @Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        List<Component> extraDesc = getAbility().getExtraDesc(stack);

        if (!extraDesc.isEmpty() && Screen.hasShiftDown()){
            // 添加解释性文本组件
            tooltipComponents.addAll(extraDesc);
        }else{
            // 添加描述性文本组件
            tooltipComponents.addAll(getAbility().getDesc(stack));
            tooltipComponents.addAll(getAbility().getSynergyDesc(stack));

            // 空行
            tooltipComponents.add(Component.literal(""));

            // 添加额外信息 (hook)
            addAdditionalInfo(tooltipComponents, stack);
            // 添加shift提示
            if (!extraDesc.isEmpty()){
                tooltipComponents.add(Component.translatable("item.isaac_disaster.special.require_shift"));
            }
            if (hasBeenUsed(stack)){
                tooltipComponents.add(Component.translatable("item.isaac_disaster.action.consumed")
                        .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
            }
            // 添加稀有度文本组件
            addRarityComponent(tooltipComponents);
        }
    }
    /** 额外信息 */
    public abstract void addAdditionalInfo(List<Component> tooltipComponents, @Nullable ItemStack stack);

    public IsaacItemAbility getAbility() {
        return ability.get();
    }

    public int getId(){
        return getAbility().getId();
    }

    public int getLevel(){
        return getAbility().getLevel();
    }

    /** 获取Rarity */
    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack){
        return switch (getLevel()) {
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            case 3, 4 -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
    }

    /** 添加稀有度文本组件 */
    public void addRarityComponent(List<Component> tooltipComponents){

        String itemIdMarker = "-" + getId();
        switch (getLevel()){
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
        int color = switch (getLevel()) {
            case 1 -> ColorManager.UNCOMMON_GREEN;
            case 2 -> ColorManager.RARE_BLUE;
            case 3 -> ColorManager.EPIC_PURPLE;
            case 4 -> ColorManager.LEGEND_RED;
            default -> ColorManager.COMMON_WHITE;
        };
        return Component.translatable(this.getDescriptionId(stack)).withStyle(
                style -> style.withColor(color));}

    public static @NotNull String getPool(ItemStack stack) {
        return stack.getOrCreateTag().getString(ITEM_POOL);
    }
    public static void setPool(ItemStack stack, String pool) {
        stack.getOrCreateTag().putString(ITEM_POOL, pool);
    }

    public static boolean hasBeenUsed(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(HAS_BEEN_USED);
    }

    public static void setHasBeenUsed(ItemStack stack, boolean state) {
        stack.getOrCreateTag().putBoolean(HAS_BEEN_USED, state);
    }



}
