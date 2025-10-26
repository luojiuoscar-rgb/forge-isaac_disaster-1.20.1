package net.luojiuoscar.isaac_disaster.item_ability.set.sets;

import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Adult implements ISet {
    @Override
    public int getSetId() {
        return SetId.ADULT.getId();
    }

    @Override
    public int getRequireCount(){
        return 3;
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.MAX_HEALTH.apply(player, 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.MAX_HEALTH.apply(player, -1);
    }

    @Override
    public Component onObtainDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.obtain")
                .append(Component.translatable("set.isaac_disaster.adult"));
    }

    @Override
    public Component onRemoveDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.remove")
                .append(Component.translatable("set.isaac_disaster.adult"));
    }

    @Override
    public List<Component> getExplain() {
        return List.of(
                Component.translatable("set.isaac_disaster.adult").append(": ")
                        .append(StatManager.MAX_HEALTH.description(1)),
                Component.translatable("set.isaac_disaster.adult.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return new ArrayList<>();
    }
}
