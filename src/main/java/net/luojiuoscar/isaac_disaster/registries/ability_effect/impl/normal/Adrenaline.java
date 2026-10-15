package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Adrenaline implements IAbilityEffect {
    public static final UUID DAMAGE = UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":adrenaline/damage")
            .getBytes(StandardCharsets.UTF_8));

    public static double ratio(LivingEntity entity) {
        double missing = Math.max(0, entity.getMaxHealth() - entity.getHealth()) / StatManager.MAX_HEALTH.getBonus();
        return Math.pow(2, 1.6) * 0.1 * missing;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        StatManager.setModifier(player, DAMAGE, StatManager.DAMAGE.getAttribute(),
                ratio(player) * StatManager.DAMAGE.getBonus(), null, null, 0);
        return true;
    }
}
