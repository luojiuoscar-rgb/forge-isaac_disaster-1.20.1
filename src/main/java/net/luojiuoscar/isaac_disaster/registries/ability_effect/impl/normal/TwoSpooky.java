package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class TwoSpooky implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (PlayerHelper.hasItem(ModPassiveItems.VENUS.getId(), player)) return true;
        context.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.POTIONS.get());
        context.set(ContextKeys.BOOLEAN, List.of(true));
        context.set(ContextKeys.DOUBLE, List.of(0.5));
        context.set(ContextKeys.AMPLIFIER, 1.0);
        context.set(ContextKeys.POTIONS, List.of(new PotionProfile(ModEffects.PANIC.get(), 20, 0, 0, 0, true)));
        ModExecutableEffects.APPLY_EFFECT_TO_NEARBY.get().apply(context);
        return true;
    }
}
