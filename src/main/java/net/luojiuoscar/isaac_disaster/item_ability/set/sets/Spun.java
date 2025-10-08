package net.luojiuoscar.isaac_disaster.item_ability.set.sets;

import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Spun implements ISet {
    @Override
    public int getSetId() {
        return SetId.SPUN.getId();
    }

    @Override
    public int getRequireCount(){
        return 3;
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.modifyDamageAdder(player, 2);
        StatManager.modifyMovementSpeedAdder(player, 0.75);

    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.modifyDamageAdder(player, -2);
        StatManager.modifyMovementSpeedAdder(player, -0.75);
    }

    @Override
    public Component onObtainDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.obtain")
                .append(Component.translatable("set.isaac_disaster.spun"));
    }

    @Override
    public Component onRemoveDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.remove")
                .append(Component.translatable("set.isaac_disaster.spun"));
    }
}
