package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GlassEye implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.GLASS_EYE.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, 0.75);
            StatManager.modifyLuckAdder(player, 1);
        }
    }

    @Override
    public void onRemove(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, -0.75);
            StatManager.modifyLuckAdder(player, -1);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.GLASS_EYE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                //移动速度以百分比显示
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage", 0.75*StatManager.getDamageBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.luck", StatManager.getLuckBonus())
        );
    }
}
