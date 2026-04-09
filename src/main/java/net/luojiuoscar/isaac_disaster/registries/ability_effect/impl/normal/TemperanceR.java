package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class TemperanceR implements IAbilityEffect {
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "temperance_r");

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        amplifier = Mth.clamp(amplifier, 1, 4);

        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(), SCHEDULE_TYPE
                , 20, 20, 5 * amplifier, false, () -> {
                    PillEffectManager.getInstance().triggerRandomEffect(player, false);
                });

        return true;
    }
}
