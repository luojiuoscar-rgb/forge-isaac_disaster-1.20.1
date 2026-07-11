package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Mom extends SetAbility {
    public Mom(int id) {
        super(Component.translatable("set.isaac_disaster.mom"), id, 3);
    }

    @Override
    public void onFirstObtain(Player player) {

    }

    @Override
    public void onObtainEffect(Player player) {
        changeMomKnifeCount(player, 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        changeMomKnifeCount(player, -1);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.mom").append(": ")
                        .append(Component.translatable("set.isaac_disaster.mom.lore.1"))
        );
    }

    /**
     * Adds or removes the familiar requirement contributed by the Mom set.
     */
    private void changeMomKnifeCount(Player player, int delta) {
        ResourceLocation momKnifeId = ModFamiliarEntities.MOM_KNIFE.getId();

        player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> data.addCount(momKnifeId, delta));
    }
}
