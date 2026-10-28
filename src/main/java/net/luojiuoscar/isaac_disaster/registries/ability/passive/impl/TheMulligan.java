package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.TheMulligan.chance;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.TheMulligan.GENERATION_LIMIT;

public final class TheMulligan extends PassiveAbility {
    public TheMulligan(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.THE_MULLIGAN.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.BEELZEBUB.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.THE_MULLIGAN.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.BEELZEBUB.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.the_mulligan.lore.1", DescriptionHelper.dynamicNumber(chance() * 100)),
                Component.translatable("item.isaac_disaster.the_mulligan.lore.2", GENERATION_LIMIT));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.BEELZEBUB.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.BEELZEBUB.get().getExtraDesc();
    }
}
