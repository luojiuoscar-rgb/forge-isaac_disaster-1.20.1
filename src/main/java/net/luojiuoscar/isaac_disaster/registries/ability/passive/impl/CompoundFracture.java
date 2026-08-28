package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/** Passive ability for Compound Fracture. */
public final class CompoundFracture extends PassiveAbility {
    public CompoundFracture(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1.0);
        StatManager.addTriggerModule(player, ModTriggerModules.COMPOUND_FRACTURE.getId(), 1);
        StatManager.addBulletVisual(player, ModBulletVisuals.COMPOUND_FRACTURE_FETUS_SKELETON.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -1.0);
        StatManager.addTriggerModule(player, ModTriggerModules.COMPOUND_FRACTURE.getId(), -1);
        StatManager.addBulletVisual(player, ModBulletVisuals.COMPOUND_FRACTURE_FETUS_SKELETON.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.RANGE.description(1.0),
                Component.translatable("item.isaac_disaster.compound_fracture.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player) {
        List<Component> desc = new ArrayList<>();
        if (ClientDataManager.getInstance().getCountFromId(ItemId.CRICKETS_BODY.getId()) > 0) {
            desc.add(DescriptionHelper.getSynergyDesc(
                    Component.translatable("item.isaac_disaster.crickets_body"),
                    Component.translatable("item.isaac_disaster.compound_fracture.synergy.crickets_body.1")
            ));
        }
        return desc;
    }
}
