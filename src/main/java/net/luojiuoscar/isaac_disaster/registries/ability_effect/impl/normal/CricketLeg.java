package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.Locust;
import net.luojiuoscar.isaac_disaster.entity.familiar.DisposableFamiliar;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class CricketLeg implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.KILL_ENTITY;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof LivingDeathEvent event)) return false;
        if (event.isCanceled() || event.getSource().getEntity() != player || event.getEntity().level() != player.level()) return true;
        if (!player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).isPresent()) return false;
        double chance = getChance(context.getOrDefault(ContextKeys.AMPLIFIER, 1.0));
        if (chance <= 0 || player.getRandom().nextDouble() >= chance) return true;

        var candidates = FamiliarHelper.getRegisteredFamiliarTypeIds()
                .filter(id -> isEligibleType(FamiliarHelper.getFamiliarType(id))).toList();
        if (candidates.isEmpty()) return true;
        var selected = candidates.get(player.getRandom().nextInt(candidates.size()));
        FamiliarHelper.getFamiliarType(selected).spawnEntry(player, selected, event.getEntity().position(), player.getRandom());
        return true;
    }

    public static double getChance(double amplifier) {
        return Double.isFinite(amplifier) ? Math.max(0, Math.min(amplifier, 2)) / 6.0 : 0;
    }

    public static boolean isEligibleType(FamiliarEntityType type) {
        return type != null && type.hasEntityTag(Locust.class) && type.hasEntityTag(DisposableFamiliar.class);
    }
}
