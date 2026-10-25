package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class AddFamiliars implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        List<ResourceLocation> types = context.get(ContextKeys.RESOURCE_LOCATIONS);
        if (types == null || types.isEmpty() || types.stream().anyMatch(type -> !FamiliarHelper.isRegisteredFamiliarType(type))) {
            return false;
        }
        var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve();
        if (data.isEmpty()) return false;
        int count = Math.max(0, context.getOrDefault(ContextKeys.COUNT, 1));
        for (ResourceLocation type : types) data.get().addCount(type, count);
        return true;
    }
}
