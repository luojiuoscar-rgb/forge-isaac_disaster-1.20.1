package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.event.custom.ActiveItemUseEvent;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ActiveItem extends IsaacItem {
    public static final int DAMAGE_PER_CHARGE = 100;

    private final int damage_per_use;
    private final int max_item_damage;
    private static final String OVERCHARGED_TAG = "OverCharged";
    private static final String HAS_BEEN_USED = "has_been_used";


    public ActiveItem(Properties properties, int itemLevel, int itemId, int chargePerUse, int maxCharge, boolean hasSpecialEffect, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(IsaacItem.getRarity(itemLevel))
                        .durability(maxCharge * DAMAGE_PER_CHARGE),
                itemLevel, itemId, hasSpecialEffect, useOriginalColor);
        this.max_item_damage = maxCharge * DAMAGE_PER_CHARGE;
        this.damage_per_use = chargePerUse * DAMAGE_PER_CHARGE;
    }


    @Override
    public void addDescription(List<Component> tooltipComponents, ItemStack stack){
        tooltipComponents.addAll(
                ActiveItemManager.getInstance().getItemFromId(getItemId()).getDescription()
        );
    }

    @Override
    public void addSynergyDescription(List<Component> tooltipComponents){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        tooltipComponents.addAll(
                ActiveItemManager.getInstance().getItemFromId(getItemId()).getSynergyDescription()
        );
    };

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents, @Nullable ItemStack stack){
        tooltipComponents.add(Component.literal(""));
        if (stack != null && hasBeenUsed(stack)){
            tooltipComponents.add(Component.translatable("item.isaac_disaster.action.consumed")
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        if (max_item_damage > 0 || !Config.ACTIVE_ITEM_AUTO_RESTORE.get()){
            tooltipComponents.add(
                    Component.translatable("item.isaac_disaster.special.recharge_require", (max_item_damage / 20))
                            .withStyle(style -> style.withColor(ColorManager.TRANSPARENT_GRAY)
                                    .withItalic(true))
            );
        }
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

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ActiveItem item)) return InteractionResultHolder.fail(stack);

        // 触发事件
        ActiveItemUseEvent event = new ActiveItemUseEvent(player, item, stack);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return InteractionResultHolder.pass(stack);

        // 耐久检查
        if (!ActiveItem.getOverCharged(stack) &&
                stack.getMaxDamage() - stack.getDamageValue() < item.getDamagePerUse(player)) {
            return InteractionResultHolder.fail(stack);
        }

        // effect
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // record
            serverPlayer.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                    playerItemUseRecord -> playerItemUseRecord.addActiveRecord(getItemId()));

            ActiveItemManager.getInstance().getItemFromId(item.getItemId()).onUse(player, hand);
            ModMessages.sentToPlayer(new UseActiveItemS2CPacket(item.getItemId()), serverPlayer);
        }

        return InteractionResultHolder.success(stack);
    }


}