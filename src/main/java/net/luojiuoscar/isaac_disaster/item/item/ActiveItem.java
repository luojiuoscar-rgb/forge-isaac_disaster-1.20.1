package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ActiveItem extends IsaacItem {
    private final int damage_per_use;
    private final int max_item_damage;
    private static final String OVERCHARGED_TAG = "OverCharged";
    private static final String HAS_BEEN_USED = "has_been_used";


    public ActiveItem(Properties properties, int itemLevel, int itemId, int damagePerUse, int maxItemDamage) {
        this(properties, itemLevel, itemId, damagePerUse, maxItemDamage, false, false);
    }

    public ActiveItem(Properties properties, int itemLevel, int itemId, int damagePerUse, int maxItemDamage, boolean hasSpecialEffect) {
        this(properties, itemLevel, itemId, damagePerUse, maxItemDamage, hasSpecialEffect, false);
    }

    public ActiveItem(Properties properties, int itemLevel, int itemId, int damagePerUse, int maxItemDamage, boolean hasSpecialEffect, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(IsaacItem.getRarity(itemLevel)).durability(maxItemDamage),
                itemLevel, itemId, hasSpecialEffect, useOriginalColor);
        this.max_item_damage = maxItemDamage;
        this.damage_per_use = damagePerUse;
    }

    @Override
    public void addDescription(List<Component> tooltipComponents){
        tooltipComponents.addAll(
                ActiveItemManager.getInstance().getItemFromId(getItemId()).getDescription()
        );
        // 添加协同效果
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        tooltipComponents.addAll(
                ActiveItemManager.getInstance().getItemFromId(getItemId()).getSynergyDescription()
        );
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents){
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(
                Component.translatable("item.isaac_disaster.special.recharge_require", (max_item_damage / 20))
                        .withStyle(style -> style.withColor(ColorManager.TRANSPARENT_GRAY)
                                .withItalic(true))
        );
    }

    @Override
    public void addExplainInfo(List<Component> tooltipComponents) {
        tooltipComponents.addAll(
                ActiveItemManager.getInstance().getItemFromId(getItemId()).getExplain()
        );
    }


    /**
     * 关闭物品耐久变动的动画
     * @param oldStack    The old stack that was equipped
     * @param newStack    The new stack
     * @param slotChanged If the current equipped slot was changed, Vanilla does not
     *                    play the animation if you switch between two slots that
     *                    hold the exact same item.
     * @return boolean
     */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // 当唯一变化是耐久（Damage）时，不触发动画
        if (ItemStack.isSameItemSameTags(oldStack, newStack)) {
            return false;
        }
        return slotChanged;  // 保留原版逻辑
    }


    /**
     * 获取每次使用时消耗的耐久值
     * 需要玩家实体参与获取道具信息
     * 以便于减少消耗的道具参与
     */
    public int getDamagePerUse(Player player){
        int[] count = {0};
        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                playerSwallowedTrinkets -> {
                    List<ItemStack> stackList =
                            playerSwallowedTrinkets.getAllTrinketListFromId(player, TrinketId.AAA_BATTERY.getId());
                    for (ItemStack stack : stackList){
                        if (Trinket.isEnchanted(stack)){
                            count[0] += 2;
                        }else{
                            count[0] += 1;
                        }}});

        return Math.max(this.damage_per_use - count[0] * 40, 0);
    }
    public int getOriginalDamagePerUse(){
        return this.damage_per_use;
    }

    /**
     * 附魔外观控制
     */
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return getOverCharged(stack);
    }

    /**
     * 检查物品是否处于overcharged状态
     */
    public static boolean getOverCharged(ItemStack stack) {
        // 从物品的NBT中获取状态，如果标签不存在则返回false
        return stack.getOrCreateTag().getBoolean(OVERCHARGED_TAG);
    }

    /**
     * 将物品设置为overcharged状态
     */
    public static void setOverCharged(ItemStack stack, boolean state) {
        // 将状态写入物品的NBT
        stack.getOrCreateTag().putBoolean(OVERCHARGED_TAG, state);
    }


    public static boolean hasBeenUsed(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(HAS_BEEN_USED);
    }


    public static void setHasBeenUsed(ItemStack stack, boolean state) {
        stack.getOrCreateTag().putBoolean(HAS_BEEN_USED, state);
    }


    public static void fullCharge(ItemStack stack, boolean hasTheBattery){
        stack.setDamageValue(0);
        if(hasTheBattery){
            setOverCharged(stack, true);
        }
    }

    /**
     * 给物品添加多少正充能（恢复多少耐久度）
     */
    public static void modifyCharge(ItemStack stack, int amount, boolean hasTheBattery){
        boolean OverCharged = ActiveItem.getOverCharged(stack);
        // 检查物品是否耐久不满 或可以过载
        if ((stack.getDamageValue() > 0)
                || (hasTheBattery || !OverCharged)) {
            // 计算新的耐久值
            int newDamage = Math.max(stack.getDamageValue() - amount, 0);

            // 未过载且有蓄电池时过载
            if (newDamage == 0 && !OverCharged && hasTheBattery){
                newDamage = stack.getMaxDamage() - 1;
                ActiveItem.setOverCharged(stack, true);
            }

            stack.setDamageValue(newDamage);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ActiveItem item)) return InteractionResultHolder.fail(stack);
        // 如果当前物品的耐久度不足且没有过载则无法使用物品
        if (!ActiveItem.getOverCharged(stack) &&
                stack.getMaxDamage() - stack.getDamageValue() < item.getDamagePerUse(player)){
            return InteractionResultHolder.fail(stack);
        }

        // 返回成功动画
        return InteractionResultHolder.success(stack);
    }
}