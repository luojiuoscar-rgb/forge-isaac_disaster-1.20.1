package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class JumperCablesKillCounter implements IAbilityEffect {
    public static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "jumper_cables_kills");
    private static final int KILL_INTERVAL = 15;

    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.KILL_ENTITY;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof LivingDeathEvent event)) return false;
        if (event.isCanceled() || event.getSource().getEntity() != player) return true;
        return player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).map(data -> {
            Double saved = data.getDouble(DATA_KEY);
            int progress = saved == null || !Double.isFinite(saved) ? 0
                    : Math.max(0, Math.min(KILL_INTERVAL - 1, saved.intValue()));
            if (progress + 1 < KILL_INTERVAL) {
                data.setDouble(DATA_KEY, progress + 1);
                return true;
            }
            data.setDouble(DATA_KEY, 0);
            return ModExecutableEffects.JUMPER_CABLES.get() instanceof IAbilityEffect charge
                    && charge.applyEffect(context);
        }).orElse(false);
    }

    /** Clears progress when the player loses the last Jumper Cables. */
    public static void clearProgress(ServerPlayer player) {
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .ifPresent(data -> data.removeDouble(DATA_KEY));
    }
}
