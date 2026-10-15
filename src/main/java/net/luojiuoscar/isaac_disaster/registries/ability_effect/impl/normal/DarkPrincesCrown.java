package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class DarkPrincesCrown implements IAbilityEffect {
    public static final UUID FIRE_RATE = UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":dark_princes_crown/fire_rate").getBytes(StandardCharsets.UTF_8));
    public static final UUID RANGE = UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":dark_princes_crown/range").getBytes(StandardCharsets.UTF_8));
    public static final UUID BULLET_SPEED = UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":dark_princes_crown/bullet_speed").getBytes(StandardCharsets.UTF_8));

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        double amplifier = Math.abs(player.getHealth() - StatManager.MAX_HEALTH.getBonus()) < 1.0e-4
                ? context.getOrDefault(ContextKeys.AMPLIFIER, 1.0) : 0;
        StatManager.setModifier(player, FIRE_RATE, StatManager.FIRE_RATE.getAttribute(),
                2 * StatManager.FIRE_RATE.getBonus() * amplifier, null, null, 0);
        StatManager.setModifier(player, RANGE, StatManager.RANGE.getAttribute(),
                StatManager.RANGE.getBonus() * amplifier, null, null, 0);
        StatManager.setModifier(player, BULLET_SPEED, StatManager.BULLET_SPEED.getAttribute(),
                StatManager.BULLET_SPEED.getBonus() * amplifier, null, null, 0);
        return true;
    }
}
