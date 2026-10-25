package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InnerChildReviveEffect extends ReviveExecutableEffect {
    private static final float HEAL_AMOUNT = 1.0F;
    private static final float EXPLOSION_POWER = 4.0F;
    private static final float EXPLOSION_DAMAGE = 20.0F;

    @Override
    protected void applyReviveEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) {
            return;
        }

        ExecutableEffectContext potionContext = context.copy(null);
        potionContext.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(ModEffects.INNER_CHILD.get(),
                        PotionProfile.PERMANENT_DURATION, 0, 0, 1, false)
        ));
        potionContext.set(ContextKeys.BOOLEAN, List.of(false, true));
        ModExecutableEffects.STACK_POTION.get().apply(potionContext);

        StatManager.healHealth(player, HEAL_AMOUNT);
        if (player.level() instanceof ServerLevel level) {
            LevelHelper.explodeCustom(level, player.position(), level.damageSources().explosion(player, player),
                    ExplosionParameters.standard(EXPLOSION_POWER, EXPLOSION_DAMAGE)
                            .withIgnoreFriendly(true).withCausesFire(false));
        }
    }

    @Override
    protected ItemStack getDisplayItem() {
        return new ItemStack(ModPassiveItems.INNER_CHILD.get());
    }
}
