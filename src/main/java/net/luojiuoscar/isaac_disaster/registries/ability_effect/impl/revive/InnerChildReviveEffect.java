package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class InnerChildReviveEffect implements IAbilityEffect {
    private static final float HEAL_AMOUNT = 1.0F;
    private static final float EXPLOSION_POWER = 4.0F;
    private static final float EXPLOSION_DAMAGE = 20.0F;

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) {
            return false;
        }

        ExecutableEffectContext potionContext = context.copy(null);
        potionContext.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(ModEffects.INNER_CHILD.get(),
                        PotionProfile.PERMANENT_DURATION, 0, 0, 1, false)
        ));
        potionContext.set(ContextKeys.BOOLEAN, List.of(false, true));
        ModExecutableEffects.STACK_POTION.get().apply(potionContext);

        StatManager.healHealth(player, HEAL_AMOUNT);
        LevelHelper.explodeCustom(player, player.position(), EXPLOSION_POWER, EXPLOSION_DAMAGE, true, false);
        return true;
    }
}
