package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.event.custom.misc.ActiveItemUseEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ActiveItem extends IsaacItem {
    public static final int DAMAGE_PER_CHARGE_RATE = 100;

    private final int damage_per_use;
    private final int max_item_damage;

    private static final String OVERCHARGED_TAG = "OverCharged";


    public ActiveItem(Properties properties, int chargePerUse, int maxCharge, RegistryObject<ActiveAbility> ability) {
        super(properties.stacksTo(1).durability(maxCharge * DAMAGE_PER_CHARGE_RATE),
                ability);

        this.max_item_damage = maxCharge * DAMAGE_PER_CHARGE_RATE;
        this.damage_per_use = chargePerUse * DAMAGE_PER_CHARGE_RATE;
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents, @Nullable ItemStack stack){
        if (max_item_damage > 0 || !Config.ACTIVE_ITEM_AUTO_RESTORE.get()){
            tooltipComponents.add(
                    Component.translatable("item.isaac_disaster.special.recharge_require", (max_item_damage / 20))
                            .withStyle(style -> style.withColor(ColorManager.TRANSPARENT_GRAY)
                                    .withItalic(true))
            );
        }
    }

    /**
     * 关闭物品耐久变动的动画
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
        return stack.getOrCreateTag().getBoolean(OVERCHARGED_TAG);
    }

    /**
     * 将物品设置为overcharged状态
     */
    public static void setOverCharged(ItemStack stack, boolean state) {
        stack.getOrCreateTag().putBoolean(OVERCHARGED_TAG, state);
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
        if (!(player.getItemInHand(hand).getItem() instanceof IIgnoreRecord)
                && !level.isClientSide && player instanceof ServerPlayer serverPlayer
                && getAbility() instanceof ActiveAbility activeAbility) {

            // 修改服务器端的物品耐久度
            stack = player.getItemInHand(hand);

            // 基于物品的过载情况计算剩余充能
            int damage = stack.getDamageValue();
            if (ActiveItem.getOverCharged(stack)){
                damage += item.getDamagePerUse(player) - stack.getMaxDamage();
                ActiveItem.setOverCharged(stack, false);
            }else{
                damage += item.getDamagePerUse(player);
            }
            damage = Math.max(0, damage);

            // 如果有9伏特，恢复20%的耐久
            if (PlayerHelper.hasItem(ItemId.VOLT_9.getId(), (ServerPlayer) player)){
                damage -= (int) (item.getOriginalDamagePerUse() * 0.2);
            }

            // 如果不是创造模式则消耗耐久
            if (!player.isCreative()) stack.setDamageValue(damage);

            // 设置0.25秒的冷却
            player.getCooldowns().addCooldown(item, 5);

            // 使用效果->首次使用？->音效
            activeAbility.onUse(serverPlayer, hand);
            if (!ActiveItem.hasBeenUsed(stack)){
                activeAbility.onFirstUse(serverPlayer, stack, hand);
                ActiveItem.setHasBeenUsed(stack, true);
            }
            activeAbility.triggerSFX(serverPlayer);

            // 先触发后记录
            serverPlayer.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                    playerItemUseRecord -> playerItemUseRecord.addActiveRecord(getId()));
        }

        return InteractionResultHolder.success(stack);
    }


}