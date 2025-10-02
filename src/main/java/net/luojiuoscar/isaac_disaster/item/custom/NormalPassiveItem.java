package net.luojiuoscar.isaac_disaster.item.custom;

import net.luojiuoscar.isaac_disaster.manager.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ObtainPassiveItemC2SPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class NormalPassiveItem extends IsaacItems {

    public NormalPassiveItem(Properties properties, int itemLevel, int itemId ) {
        this(properties, itemLevel, itemId, false);
    }

    public NormalPassiveItem(Properties properties, int itemLevel, int itemId, boolean useOriginalColor) {
        super(properties.stacksTo(1).rarity(IsaacItems.getRarity(itemLevel)), itemLevel, itemId, useOriginalColor);
    }

    @Override
    public void addDescription(List<Component> tooltipComponents){
        tooltipComponents.addAll(
                PassiveItemManager.getInstance().getItemFromId(getItemId()).getDescription()
        );
    }

    @Override
    public void addAdditionalInfo(List<Component> tooltipComponents){
        tooltipComponents.add(Component.literal(""));
    }

    /**
     * 右键使用方法
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        // 获取玩家手中的物品栈
        ItemStack stack = player.getItemInHand(hand);

        // 发包请求服务端回应
        ModMessages.sendToServer(new ObtainPassiveItemC2SPacket(this.getItemId()));

        // 返回成功结果
        return InteractionResultHolder.pass(stack);
    }
}
