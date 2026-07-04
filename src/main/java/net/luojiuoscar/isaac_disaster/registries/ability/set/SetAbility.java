package net.luojiuoscar.isaac_disaster.registries.ability.set;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class SetAbility {
    private final int id;
    private final int requirementCount;
    private final Component name;

    public SetAbility(Component name, int id, int requirementCount){
        this.name = name;
        this.id = id;
        this.requirementCount = requirementCount;
    }

    public int getId() {
        return id;
    }

    public int getRequirementCount() {
        return requirementCount;
    }

    public Component getName() {
        return name;
    }

    public void onObtain(Player player){
        onObtainEffect(player);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                playerPassiveItem -> {
                    if(!playerPassiveItem.isObtainedSet(getId())){
                        onFirstObtain(player);
                        playerPassiveItem.setObtainedSet(getId()); // 标记为已触发
                    }
                }
        );
        player.sendSystemMessage(onObtainDesc());
    }

    public void onRemove(Player player){
        onRemoveEffect(player);
        player.sendSystemMessage(onRemoveDesc());
    }

    public abstract void onFirstObtain(Player player);
    public abstract void onObtainEffect(Player player);
    public abstract void onRemoveEffect(Player player);

    public Component onObtainDesc() {
        return Component.translatable("set.isaac_disaster.action.obtain")
                .append(getName());
    }

    public Component onRemoveDesc() {
        return Component.translatable("set.isaac_disaster.action.remove")
                .append(getName());
    }

    public abstract List<Component> getExtraDesc();

    public List<Component> getSynergyDesc(){
        return List.of(
                DescriptionHelper.getSynergyDesc(
                        Component.translatable("set.isaac_disaster.special.header"),
                        Component.empty().append(getName()).append(Component.literal("("+
                                Math.min(getRequirementCount(),
                                        ClientDataManager.getInstance().getSetCountFromId(getId())) + "/" +
                                getRequirementCount()+")"))
                )
        );
    }
}
