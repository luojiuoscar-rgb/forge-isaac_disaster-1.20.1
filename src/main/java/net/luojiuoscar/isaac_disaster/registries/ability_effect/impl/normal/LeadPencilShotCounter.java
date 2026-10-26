package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class LeadPencilShotCounter implements IAbilityEffect {
    public static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "lead_pencil_shot_count");
    private static final int SHOT_INTERVAL = 15;

    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ATTACK_FIRED;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackFiredEvent event)) return false;
        if (event.getOwner() != player) return false;
        if (event.getSourceId().equals(ModExecutableEffects.LEAD_PENCIL_BURST.getId())) return true;

        return player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).map(data -> {
            Double saved = data.getDouble(DATA_KEY);
            int progress = saved == null || !Double.isFinite(saved) ? 0
                    : Math.max(0, Math.min(SHOT_INTERVAL - 1, saved.intValue()));
            if (progress + 1 < SHOT_INTERVAL) {
                data.setDouble(DATA_KEY, progress + 1);
                return true;
            }

            // Reset before dispatching derived work so it cannot recursively advance this round.
            data.setDouble(DATA_KEY, 0);
            return ModExecutableEffects.LEAD_PENCIL_BURST.get() instanceof IAbilityEffect burst
                    && burst.applyEffect(context);
        }).orElse(false);
    }

    /** Clears the shot progress when the player loses the last Lead Pencil. */
    public static void clearProgress(ServerPlayer player) {
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .ifPresent(data -> data.removeDouble(DATA_KEY));
    }
}
