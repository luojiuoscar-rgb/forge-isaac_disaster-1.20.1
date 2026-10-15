package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModules;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Adrenaline.ratio;

public final class Adrenaline extends PassiveAbility {
    public Adrenaline(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addRecursiveModule(player, ModRecursiveModules.ADRENALINE.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPUN.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addRecursiveModule(player, ModRecursiveModules.ADRENALINE.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPUN.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.adrenaline.lore.1", StatManager.MAX_HEALTH.getBonus()),
                Component.translatable("item.isaac_disaster.adrenaline.lore.2"),
                Component.translatable("item.isaac_disaster.adrenaline.lore.3",
                        DescriptionHelper.dynamicNumber(player == null ? 0 : ratio(player) * StatManager.DAMAGE.getBonus()))
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPUN.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPUN.get().getExtraDesc();
    }
}
