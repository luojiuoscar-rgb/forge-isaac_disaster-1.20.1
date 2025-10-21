package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class Trinket extends Item implements ICurioItem {
    private final int trinketId;
    private static final String ENCHANTED = "enchanted";
    private final boolean hasSpecialEffects;
    private boolean canEquip;
    private boolean canUnequip;


    public Trinket(Properties pProperties, int trinketId, Rarity rarity){
        this(pProperties, trinketId, rarity, false);
    }
    public Trinket(Properties pProperties, int trinketId, Rarity rarity, boolean hasSpecialEffect) {
        super(pProperties.stacksTo(1).rarity(rarity));
        this.trinketId = trinketId;
        this.hasSpecialEffects = hasSpecialEffect;
        this.canEquip = true;
        this.canUnequip = true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        TrinketManager.getInstance().getTrinketFromId(getTrinketId()).onEquipped(slotContext.entity());
    }
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        TrinketManager.getInstance().getTrinketFromId(getTrinketId()).onUnequipped(slotContext.entity());
    }
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        TrinketManager.getInstance().getTrinketFromId(getTrinketId()).onTick(slotContext.entity());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return this.canEquip;
    }
    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return this.canUnequip;
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
}
