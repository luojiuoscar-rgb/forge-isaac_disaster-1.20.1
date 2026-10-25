package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.List;

public final class Cat extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.DEAL_DAMAGE, ModExecutableEffects.CAT, Cat::shouldGenerate)));

    public Cat() { super(TRIGGER); }

    public static double chance() { return 2.0 / 3.0; }

    private static boolean shouldGenerate(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof LivingDamageEvent event)) return false;
        return !event.isCanceled() && Float.isFinite(event.getAmount()) && event.getAmount() > 0
                && event.getSource().getEntity() == player
                && !(event.getSource().getDirectEntity() instanceof AbstractIsaacFamiliarEntity)
                && event.getEntity().level() == player.level()
                && !EntityHelper.isFriendly(player, event.getEntity())
                && player.getRandom().nextDouble() < chance();
    }
}
