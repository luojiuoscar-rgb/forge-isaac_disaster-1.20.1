package net.luojiuoscar.isaac_disaster.item_ability.set.sets;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

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
    public void onFirstObtain(Player player){
        LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), LootTableManager.RANDOM_PILLS);
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.DAMAGE.apply(player, 2);
        StatManager.MOVEMENT_SPEED.apply(player, 0.75);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.DAMAGE.apply(player, -2);
        StatManager.MOVEMENT_SPEED.apply(player, -0.75);
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

    @Override
    public List<Component> getExplain() {
        return List.of(
                Component.translatable("set.isaac_disaster.spun").append(": ")
                        .append(StatManager.DAMAGE.description(2)),
                StatManager.ATTACK_SPEED.description(0.75)
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        return List.of(
                Component.translatable("set.isaac_disaster.special.header")
                        .append(Component.translatable("set.isaac_disaster.spun"))
                        .append(Component.literal("("+
                                Math.min(getRequireCount(),
                                        ClientDataManager.getInstance().getSetCountFromId(SetId.SPUN.getId())) + "/" +
                                getRequireCount()+")"
                        )).withStyle(
                                style -> style.withColor(ColorManager.SYNERGY)
                        )
        );
    }
}
