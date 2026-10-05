package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ThinOddMushroom extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "thin_odd_mushroom", "damage", Attributes.ATTACK_DAMAGE, -0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public ThinOddMushroom(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.SCALE.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.TEARS.apply(player, 1.75);
        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.modifySetWithId(player, ModSetAbilities.FUN_GUY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.SCALE.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.TEARS.apply(player, -1.5);
        StatManager.removeMultiplier(player, Multipliers.DAMAGE.id(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.FUN_GUY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("attribute.isaac_disaster.scale_down"),
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.TEARS.description(1.75),
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "-10")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player){
        return ModSetAbilities.FUN_GUY.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player){
        return ModSetAbilities.FUN_GUY.get().getExtraDesc();
    }

}
