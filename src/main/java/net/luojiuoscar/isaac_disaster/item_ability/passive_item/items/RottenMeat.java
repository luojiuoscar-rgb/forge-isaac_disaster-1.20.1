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

public class RottenMeat implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.ROTTEN_MEAT.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        //属性修改需要在服务端权威修改
        if(!player.level().isClientSide()){
            StatManager.modifyMaxHealth(player, 1);
            StatManager.healHealth(player, 1.0f);
        }
    }

    @Override
    public void onRemove(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMaxHealth(player, -1);
        }
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.ROTTEN_MEAT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus())
        );
    }
}
