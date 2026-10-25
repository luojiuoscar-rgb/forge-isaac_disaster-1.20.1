package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.SpawnFamiliars;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class SpiderBaby extends PassiveAbility {
    public SpiderBaby(int id, int level) { super(id, level); }

    public static double chance(double luck) {
        if (!Double.isFinite(luck)) luck = 0;
        return Math.max(0, Math.min(0.25, 0.1 + luck * 0.02));
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.SPIDER_BABY.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.SPIDER_BABY.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        var generation = (SpawnFamiliars) ModExecutableEffects.SPIDER_BABY.get();
        double luck = player == null ? 0 : player.getAttributeValue(Attributes.LUCK);
        return List.of(Component.translatable("item.isaac_disaster.spider_baby.lore.1",
                        DescriptionHelper.dynamicNumber(chance(luck) * 100)),
                Component.translatable("item.isaac_disaster.spider_baby.lore.2", generation.minimumCount(), generation.maximumCount()));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPIDER_BABY.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPIDER_BABY.get().getExtraDesc();
    }
}
