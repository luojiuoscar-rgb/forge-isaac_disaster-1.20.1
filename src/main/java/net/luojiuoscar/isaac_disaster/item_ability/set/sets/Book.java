package net.luojiuoscar.isaac_disaster.item_ability.set.sets;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Book implements ISet {
    @Override
    public int getSetId() {
        return SetId.BOOK.getId();
    }

    @Override
    public int getRequireCount(){
        return 3;
    }

    @Override
    public void onFirstObtain(Player player){
    }

    @Override
    public void onObtainEffect(Player player) {
    }

    @Override
    public void onRemoveEffect(Player player) {
    }

    @Override
    public Component onObtainDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.obtain")
                .append(Component.translatable("set.isaac_disaster.book"));
    }

    @Override
    public Component onRemoveDescription(Player player) {
        return Component.translatable("set.isaac_disaster.action.remove")
                .append(Component.translatable("set.isaac_disaster.book"));
    }

    @Override
    public List<Component> getExplain() {
        return List.of(
                Component.translatable("set.isaac_disaster.book").append(": ")
                        .append(Component.translatable("set.isaac_disaster.book.lore.1"))
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("set.isaac_disaster.special.header")
                .append(Component.translatable("set.isaac_disaster.book"))
                .append(Component.literal("("+
                        Math.min(getRequireCount(),
                                ClientDataManager.getInstance().getSetCountFromId(SetId.BOOK.getId())) + "/" +
                        getRequireCount()+")"
                )).withStyle(style -> style.withColor(ColorManager.SYNERGY)));

        return description;
    }
}
