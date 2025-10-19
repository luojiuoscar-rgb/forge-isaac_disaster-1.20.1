package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MiniMush implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.MINI_MUSH.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyScaleAdder(player, -2);
        StatManager.modifyMovementSpeedAdder(player, 1.5);
        StatManager.modifyRangeAdder(player, 1);
        StatManager.modifyBlockReachAdder(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyScaleAdder(player, 2);
        StatManager.modifyMovementSpeedAdder(player, -1.5);
        StatManager.modifyRangeAdder(player, -1);
        StatManager.modifyBlockReachAdder(player, -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.MINI_MUSH.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.scale_down"),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed",1500*StatManager.getMovementSpeedBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_range", StatManager.getRangeBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_reach", StatManager.getBlockReachBonus())


        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return List.of(
                Component.translatable("set.isaac_disaster.special.header")
                        .append(Component.translatable("set.isaac_disaster.fun_guy"))
                        .append(Component.literal("("+
                                Math.min(3, ClientDataManager.getInstance().getSetCountFromId(SetId.FUN_GUY.getId())) + "/" +
                                SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getRequireCount()+")"
                        )).withStyle(
                                style -> style.withColor(ColorManager.SYNERGY)
                        )
        );
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getDescription();
    }
}
