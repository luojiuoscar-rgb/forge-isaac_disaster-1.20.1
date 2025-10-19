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

public class BlueCap implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.BLUE_CAP.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyMaxHealth(player, 1);
        StatManager.healHealth(player, 1);
        StatManager.modifyTearsAdder(player, 1);
        StatManager.modifyAttackSpeedAdder(player, -0.1);
        StatManager.modifyBlockBreakingSpeedAdder(player, 1);
        StatManager.modifyBulletSpeedAdder(player, -0.8);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyMaxHealth(player, -1);
        StatManager.healHealth(player, -1);
        StatManager.modifyTearsAdder(player, -1);
        StatManager.modifyAttackSpeedAdder(player, 0.1);
        StatManager.modifyBlockBreakingSpeedAdder(player, -1);
        StatManager.modifyBulletSpeedAdder(player, 0.8);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.BLUE_CAP.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.action.heal_health", StatManager.getHealthBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.tears", StatManager.getTearsBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_breaking_speed", StatManager.getBlockBreakingSpeed()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.attack_speed_remove",0.1*StatManager.getAttackSpeedBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed_remove", StatManager.getBulletSpeedBonus())


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
