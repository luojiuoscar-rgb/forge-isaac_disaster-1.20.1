package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsWig.chance;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsWig.generationLimit;

public final class MomsWig extends PassiveAbility {
    public MomsWig(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 1);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.MOMS_WIG.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.MOMS_WIG.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        double luck = player == null ? 0 : player.getAttributeValue(Attributes.LUCK);
        return List.of(StatManager.healHealthDescription(1),
                Component.translatable("item.isaac_disaster.moms_wig.lore.1", DescriptionHelper.dynamicNumber(chance(luck) * 100)),
                Component.translatable("item.isaac_disaster.moms_wig.lore.2", generationLimit(false)));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        List<Component> result = new ArrayList<>(ModSetAbilities.MOM.get().getSynergyDesc());
        boolean hiveMind = player instanceof ServerPlayer serverPlayer
                ? PlayerHelper.hasItem(ModPassiveItems.HIVE_MIND.getId(), serverPlayer)
                : player != null && ClientDataManager.getInstance().getItemCount(ModPassiveItems.HIVE_MIND.getId()) > 0;
        if (hiveMind) result.add(DescriptionHelper.getSynergyDesc(
                Component.translatable("item.isaac_disaster.hive_mind"),
                Component.translatable("item.isaac_disaster.moms_wig.lore.2", generationLimit(true))));
        return result;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.MOM.get().getExtraDesc();
    }
}
