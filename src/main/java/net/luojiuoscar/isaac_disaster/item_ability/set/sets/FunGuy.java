package net.luojiuoscar.isaac_disaster.item_ability.set.sets;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
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
        StatManager.modifyMaxHealth(player, 1, true);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.modifyMaxHealth(player, -1, true);
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
    public List<Component> getExplain() {
        return List.of(
                Component.translatable("set.isaac_disaster.fun_guy").append(": ")
                        .append(TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus()))
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        return List.of(
                Component.translatable("set.isaac_disaster.special.header").append(Component.translatable("set.isaac_disaster.fun_guy"))
                        .append(Component.literal("("+
                                Math.min(getRequireCount(),
                                        ClientDataManager.getInstance().getSetCountFromId(SetId.FUN_GUY.getId())) + "/" +
                                getRequireCount()+")"
                        )).withStyle(
                                style -> style.withColor(ColorManager.SYNERGY)
                        ));
    }
}
