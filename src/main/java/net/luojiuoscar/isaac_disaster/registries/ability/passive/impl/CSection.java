package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CSection extends PassiveAbility {
    public CSection(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackType.C_SECTION.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModule.C_SECTION.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackType.C_SECTION.getId(), -1);
        StatManager.addTriggerModule(player, ModTriggerModule.C_SECTION.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.c_section.lore.1"),
                Component.translatable("item.isaac_disaster.c_section.lore.2"),
                Component.translatable("item.isaac_disaster.c_section.lore.3")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        List<Component> desc = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.TECHNOLOGY.getId()) > 0){
            desc.add(Component.translatable("item.isaac_disaster.c_section.synergy.technology.1"));
        }

        return desc;
    }

}
