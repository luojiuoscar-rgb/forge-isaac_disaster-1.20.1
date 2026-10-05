package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagicMushroom extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "magic_mushroom", "damage", Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public MagicMushroom(int id, int level) {
        super(id, level);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.MAGIC_MUSHROOM_OBTAIN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        player.setHealth(player.getMaxHealth());
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.SCALE.apply(player, 2.5);
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.DAMAGE.apply(player, 0.25);
        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.RANGE.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);
        StatManager.ENTITY_REACH.apply(player, 1);
        StatManager.modifySetWithId(player, ModSetAbilities.FUN_GUY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.SCALE.apply(player, -2.5);
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.DAMAGE.apply(player, -0.25);
        StatManager.removeMultiplier(player, Multipliers.DAMAGE.id(), 1);
        StatManager.RANGE.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);
        StatManager.ENTITY_REACH.apply(player, -1);
        StatManager.modifySetWithId(player, ModSetAbilities.FUN_GUY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("attribute.isaac_disaster.scale_up"),
                StatManager.MAX_HEALTH.description(1),
                Component.translatable("item.isaac_disaster.action.full_health"),
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.DAMAGE.description(0.25),
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "+50"),
                StatManager.RANGE.description(1),
                StatManager.BLOCK_REACH.description(1),
                StatManager.ENTITY_REACH.description(1)
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
