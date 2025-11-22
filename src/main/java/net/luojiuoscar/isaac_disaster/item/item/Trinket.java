package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;



public class Trinket extends Item implements IIsaacCuriosItem {
    private final int trinketId;
    private static final String ENCHANTED = "enchanted";
    private static final String SWALLOWING = "swallowing";
    private static final String CONSUMED = "consumed";
    private final boolean hasSpecialEffects;
    private boolean canEquip;
    private boolean canUnequip;


    public Trinket(Properties pProperties, int trinketId){
        this(pProperties, trinketId, false);
    }
    public Trinket(Properties pProperties, int trinketId, boolean hasSpecialEffect) {
        super(pProperties.stacksTo(1).rarity(Rarity.RARE));
        this.trinketId = trinketId;
        this.hasSpecialEffects = hasSpecialEffect;
        this.canEquip = true;
        this.canUnequip = true;
    }

    @Override
    public void tryEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        TrinketManager.getInstance().getTrinketFromId(getTrinketId()).onEquipped(slotContext.entity(), isEnchanted(stack));
        if (!isConsumed(stack)){
            TrinketManager.getInstance().getTrinketFromId(getTrinketId()).onFirstEquipped(slotContext.entity(), isEnchanted(stack));
        }
        setConsumed(stack, true);
    }
    @Override
    public void tryUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (isSwallowing(prevStack) || isSwallowing(stack)) return; // 不知道为什么 但是这样可以
        TrinketManager.getInstance().getTrinketFromId(getTrinketId()).onUnequipped(slotContext.entity(), isEnchanted(stack));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return this.canEquip;
    }
    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return this.canUnequip;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (isEnchanted(stack)){
            return Component.translatable(this.getDescriptionId(stack)).withStyle(
                    style -> style.withColor(ChatFormatting.LIGHT_PURPLE));
        }
        return Component.translatable(this.getDescriptionId(stack));
    }


    public int getTrinketId(){
        return this.trinketId;
    }
    public void setCanEquip(boolean val){
        this.canEquip = val;
    }
    public void setCanUnequip(boolean val){
        this.canUnequip = val;
    }


    @Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        ITrinket trinketAbility = TrinketManager.getInstance().getTrinketFromId(getTrinketId());

        if (hasSpecialEffects && Screen.hasShiftDown()){
            // 添加解释性文本组件
            tooltipComponents.addAll(trinketAbility.getExplain());
        }else{
            tooltipComponents.addAll(trinketAbility.getDescription());
            if (isEnchanted(stack)){
                // 附魔版本的描述信息
                tooltipComponents.addAll(trinketAbility.getEnchantedDescription());
            }
            if (isConsumed(stack)){
                // 已消耗
                tooltipComponents.add(Component.translatable("item.isaac_disaster.action.consumed")
                        .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
            }
            // 解释性信息提示
            if (hasSpecialEffects){
                tooltipComponents.add(Component.translatable("item.isaac_disaster.special.require_shift"));
            }
        }
    }


    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isEnchanted(stack);
    }

    public static boolean isEnchanted(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(ENCHANTED);
    }
    public static void setEnchanted(ItemStack stack, boolean enchanted) {
        stack.getOrCreateTag().putBoolean(ENCHANTED, enchanted);
    }
    public static boolean isSwallowing(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(SWALLOWING);
    }
    public static void setSwallowing(ItemStack stack, boolean b) {
        stack.getOrCreateTag().putBoolean(SWALLOWING, b);
    }
    public static boolean isConsumed(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(CONSUMED);
    }
    public static void setConsumed(ItemStack stack, boolean b) {
        stack.getOrCreateTag().putBoolean(CONSUMED, b);
    }


}
