package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

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
        setMomKnifeCount(player, 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        setMomKnifeCount(player, 0);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.mom").append(": ")
                        .append(Component.translatable("set.isaac_disaster.mom.lore.1"))
        );
    }

    /**
     * Updates the familiar requirement owned by the Mom set.
     */
    private void setMomKnifeCount(Player player, int count) {
        ResourceLocation momKnifeId = ForgeRegistries.ENTITY_TYPES.getKey(ModEntities.MOM_KNIFE.get());
        if (momKnifeId == null) return;

        player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> data.setCount(momKnifeId, count));
    }
}
