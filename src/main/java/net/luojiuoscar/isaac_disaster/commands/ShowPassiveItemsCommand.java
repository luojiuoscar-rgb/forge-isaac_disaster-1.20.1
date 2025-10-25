package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public class ShowPassiveItemsCommand {
    public ShowPassiveItemsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("passiveitems").then(Commands.literal("get")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    Player player = context.getSource().getPlayerOrException();

                    // 从Capability获取被动物品列表
                    player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passiveItems -> {
                        List<ItemStack> items = passiveItems.getPassiveItems();

                        // 构建消息组件
                        MutableComponent message = Component.empty();

                        if (items.isEmpty()) {
                            player.sendSystemMessage(Component.literal("你当前没有任何被动物品"));
                            return;
                        }

                        for (int i = 0; i < items.size(); i++) {
                            int itemId = ((PassiveItem) items.get(i).getItem()).getItemId();
                            IPassiveItem item = PassiveItemManager.getInstance().getItemFromId(itemId);

                            // 创建带悬浮文本的显示名称组件
                            Component itemComponent = Component.literal("").append(item.getDisplayName()).withStyle(
                                    style -> style.withHoverEvent(
                                            new HoverEvent(
                                                    HoverEvent.Action.SHOW_ITEM,
                                                    new HoverEvent.ItemStackInfo(item.getItemStack())
                                            )));

                            // 添加到消息中，元素间用空格分隔
                            message = message.append(itemComponent);
                            if (i < items.size() - 1) {
                                message = message.append(Component.literal(" "));
                            }}

                        // 发送系统消息给玩家
                        player.sendSystemMessage(Component.literal("[测试信息]你获取的道具如下："));
                        player.sendSystemMessage(message);
                    });

                    return 1;
                }))));
    }



}