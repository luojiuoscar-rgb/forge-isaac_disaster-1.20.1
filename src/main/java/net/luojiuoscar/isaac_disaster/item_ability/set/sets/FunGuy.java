package net.luojiuoscar.isaac_disaster.item_ability.set.sets;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class FunGuy implements ISet {
    @Override
    public int getSetId() {
        return SetId.FUN_GUY.getId();
    }

    @Override
    public int getRequireCount(){
        return 3;
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.modifyMaxHealth(player, 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.modifyMaxHealth(player, -1);
    }

    @Override
    public Component onObtainDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.obtain")
                .append(Component.translatable("set.isaac_disaster.fun_guy"));
    }

    @Override
    public Component onRemoveDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.remove")
                .append(Component.translatable("set.isaac_disaster.fun_guy"));
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("set.isaac_disaster.fun_guy").append(": ")
                        .append(TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus()))
        );
    }
}
