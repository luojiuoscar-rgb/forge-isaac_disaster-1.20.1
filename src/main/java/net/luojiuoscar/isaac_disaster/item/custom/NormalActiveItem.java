package net.luojiuoscar.isaac_disaster.item.custom;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.manager.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemC2SPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NormalActiveItem extends IsaacItems {
    private int damagePerUse;
    private int maxItemDamage;



    public NormalActiveItem(Properties properties, int itemLevel, int itemId, int damagePerUse, int maxItemDamage) {
        this(properties, itemLevel, itemId, damagePerUse, maxItemDamage, false);
    }

    public NormalActiveItem(Properties properties, int itemLevel, int itemId, int damagePerUse, int maxItemDamage, boolean useOriginalColor) {
        // 在父类构造前设置最大耐久
        super(properties.stacksTo(1).rarity(Rarity.EPIC).durability(maxItemDamage),
                itemLevel, itemId, useOriginalColor);
        this.maxItemDamage = maxItemDamage;
        this.damagePerUse = damagePerUse;
    }

    @Override
    public void addDescription(List<Component> tooltipComponents){
        tooltipComponents.addAll(
                ActiveItemManager.getInstance().getItemFromId(getItemId()).getDescription()
        );
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents){
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(
                Component.translatable("item.isaac_disaster.special.recharge_require", (maxItemDamage / 20))
                        .withStyle(style -> style.withColor(ColorManager.TRANSPARENT_GRAY)
                                .withItalic(true))
        );
    }



    /**
     * 右键使用效果
     */
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 如果当前物品的耐久度不足；则无法使用物品
        if (stack.getMaxDamage() - stack.getDamageValue() < getDamagePerUse(player)){
            return InteractionResultHolder.fail(stack);
        }
        player.sendSystemMessage(Component.literal("判定通过"));

        // 发送使用请求
        if (level.isClientSide()){
            player.sendSystemMessage(Component.literal("包裹发送"));
            ModMessages.sendToServer(new UseActiveItemC2SPacket(getItemId()));
        }

        return InteractionResultHolder.pass(stack);
    }



    /**
     * 获取每次使用时消耗的耐久值
     * 需要玩家实体参与获取道具信息
     */
    public int getDamagePerUse(Player player){
        AtomicInteger theBatteryCount = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    theBatteryCount.set(playerPassiveItem.getItemCount(ItemId.CAR_BATTERY.getId()));
                });
        if (theBatteryCount.get() != 0){
            return this.damagePerUse / 2;
        }
        return this.damagePerUse;
    }
}