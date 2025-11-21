package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectories;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Ipecac implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.IPECAC.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addBulletColor(player, ModBulletColors.IPECAC.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModule.IPECAC.getId(), 1);
        StatManager.addTrajectory(player, ModAttackTrajectories.GRAVITY.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addBulletColor(player, ModBulletColors.IPECAC.getId(), -1);
        StatManager.addTriggerModule(player, ModTriggerModule.IPECAC.getId(), -1);
        StatManager.addTrajectory(player, ModAttackTrajectories.GRAVITY.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.ipecac.lore.1"),
                Component.translatable("item.isaac_disaster.ipecac.lore.2"),
                Component.translatable("item.isaac_disaster.ipecac.lore.3"),
                Component.translatable("item.isaac_disaster.ipecac.lore.4"),
                Component.translatable("item.isaac_disaster.ipecac.lore.5"),
                Component.translatable("item.isaac_disaster.ipecac.lore.6")

        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(EffectManager.POISON.getExplainDesc());

        return description;
    }

}
