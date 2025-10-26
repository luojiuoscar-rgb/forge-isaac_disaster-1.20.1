package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class PassiveItem extends IsaacItem implements IIsaacCuriosItem {
    private static final String CONSUMED = "consumed";
    private boolean canEquip;
    private boolean canUnequip;

    public PassiveItem(Properties properties, int itemLevel, int itemId ) {
        this(properties, itemLevel, itemId, false, false);
    }
    public PassiveItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffect) {
        this(properties, itemLevel, itemId, hasSpecialEffect, false);
    }

    public PassiveItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffect, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(IsaacItem.getRarity(itemLevel)), itemLevel, itemId, hasSpecialEffect, useOriginalColor);
        this.canEquip = true;
        this.canUnequip = true;
    }

    @Override
    public void addDescription(List<Component> tooltipComponents){
        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getDescription()
        );
        // 添加协同效果（套装效果也属于协同）
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getSynergyDescription()
        );
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents, @Nullable ItemStack stack){
        tooltipComponents.add(Component.literal(""));
        if (stack != null && isConsumed(stack)){
            tooltipComponents.add(Component.translatable("item.isaac_disaster.action.consumed")
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

    }

    @Override
    public void addExplainInfo(List<Component> tooltipComponents) {
        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getExplain()
        );
    }

    public static boolean isConsumed(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(CONSUMED);
    }
    public static void setConsumed(ItemStack stack, boolean b) {
        stack.getOrCreateTag().putBoolean(CONSUMED, b);
    }
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return this.canEquip;
    }
    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return this.canUnequip;
    }
    public void setCanEquip(boolean val){
        this.canEquip = val;
    }
    public void setCanUnequip(boolean val){
        this.canUnequip = val;
    }

    @Override
    public void tryEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)) return;
        PassiveItemManager.getInstance().getItemFromId(getItemId()).onObtain(player);
        if (!isConsumed(stack)) PassiveItemManager.getInstance().getItemFromId(getItemId()).onFirstObtain(player);
        setConsumed(stack, true);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)) return;
        PassiveItemManager.getInstance().getItemFromId(getItemId()).onRemove(player);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)) return;
        if (!(PassiveItemManager.getInstance().getItemFromId(getItemId()) instanceof IRecursivePassiveItem item)) return;
        if (player.tickCount % item.getTickInterval() != 0) return; // tick interval

        item.recursiveEffect(player); // recursive effect
    }
}