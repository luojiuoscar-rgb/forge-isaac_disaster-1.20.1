package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public final class DeadTooth implements IAbilityEffect {
    public static final double RADIUS = 2.5;
    private static final DustParticleOptions MIST = new DustParticleOptions(new Vector3f(0.2f, 0.75f, 0.15f), 1.5f);
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!player.isAlive()) return true;
        spawnMist(player);
        int amplifier = DeadlyPoisonEffect.amplifierForDamage(player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        for (var target : LevelHelper.selectBySphere(player.level(), player.position(), RADIUS)) {
            if (!target.isAlive() || target.isInvulnerable() || EntityHelper.isFriendly(player, target)) continue;
            var existing = target.getEffect(ModEffects.DEADLY_POISON.get());
            int duration = existing == null ? 40 : Math.min(120, existing.getDuration() + 40);
            var potion = new ExecutableEffectContext(target);
            potion.set(ContextKeys.POTIONS, List.of(new PotionProfile(ModEffects.DEADLY_POISON.get(), duration, amplifier)));
            ModExecutableEffects.POTIONS.get().apply(potion);
        }
        return true;
    }

    private static void spawnMist(ServerPlayer player) {
        var random = player.getRandom();
        for (int i = 0; i < 12; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double vertical = random.nextDouble();
            double radius = Math.cbrt(random.nextDouble()) * RADIUS;
            double horizontal = radius * Math.sqrt(1 - vertical * vertical);
            player.serverLevel().sendParticles(MIST,
                    player.getX() + Math.cos(angle) * horizontal,
                    player.getY() + vertical * radius,
                    player.getZ() + Math.sin(angle) * horizontal,
                    1, 0, 0, 0, 0);
        }
    }
}
